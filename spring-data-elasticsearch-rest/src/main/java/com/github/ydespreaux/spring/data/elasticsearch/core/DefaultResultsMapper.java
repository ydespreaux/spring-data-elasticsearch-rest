/*
 * Copyright (C) 2018 Yoann Despréaux
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; see the file COPYING . If not, write to the
 * Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * Please send bugreports with examples or suggestions to yoann.despreaux@believeit.fr
 */

package com.github.ydespreaux.spring.data.elasticsearch.core;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.github.ydespreaux.spring.data.elasticsearch.core.converter.ElasticsearchConverter;
import com.github.ydespreaux.spring.data.elasticsearch.core.scroll.ScrolledPageResult;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.data.domain.Page;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public class DefaultResultsMapper implements ResultsMapper {

    private final EntityMapper entityMapper;
    private final ElasticsearchConverter converter;

    public DefaultResultsMapper(final EntityMapper entityMapper, final ElasticsearchConverter converter) {
        Assert.notNull(entityMapper, "EntityMapper must not be null!");
        this.entityMapper = entityMapper;
        this.converter = converter;
    }

    /**
     * @return the entity mapper
     */
    @Override
    public EntityMapper getEntityMapper() {
        return this.entityMapper;
    }

    /**
     * @param values the document fields
     * @param clazz  the entity class
     * @param <T>    generic method
     * @return the entity
     */
    @Nullable
    @Override
    public <S extends T, T> S mapEntity(Collection<DocumentField> values, Class<T> clazz) {
        return mapEntity(buildJSONFromFields(values), clazz);
    }

    /**
     * Map a single {@link GetResult} to an instance of the given type.
     *
     * @param response must not be {@literal null}.
     * @param clazz    must not be {@literal null}.
     * @return can be {@literal null} if the {@link GetResult#isSourceEmpty() is empty}.
     */
    @Override
    public <S extends T, T> S mapResult(GetResponse response, Class<T> clazz) {
        S result = mapEntity(response.getSourceAsString(), clazz);
        if (result != null) {
            setPersistentEntity(result, response, (Class<S>) result.getClass());
        }
        return result;
    }

    /**
     * Map a single {@link SearchHit} to an instance of the given type.
     *
     * @param hit must not be {@literal null}.
     * @param type      must not be {@literal null}.
     * @return can be {@literal null} if the {@link SearchHit} does not have {@link SearchHit#hasSource() a source}.
     */
    @Override
    public <S extends T, T> S mapEntity(SearchHit hit, Class<T> type) {
        S result = null;
        if (!StringUtils.isEmpty(hit.getSourceAsString())) {
            result = mapEntity(hit.getSourceAsString(), type);
        } else {
            result = mapEntity(hit.getFields().values(), type);
        }
        if (result != null) {
            setPersistentEntity(result, hit, (Class<S>) result.getClass());
        }
        return result;
    }

    @Override
    public <S extends T, T> List<S> mapEntity(SearchHits searchHits, Class<T> type) {
        List<S> results = new ArrayList<>();
        searchHits.forEach(hit -> results.add(mapEntity(hit, type)));
        return results.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }


    /**
     * @param response the response
     * @param clazz    the entity class
     * @param <T>      the generic type
     * @return the new {@link Page}
     */
    @Override
    public <S extends T, T> Page<S> mapResults(SearchResponse response, Class<T> clazz) {
        long totalHits = response.getHits().getTotalHits();
        List<S> results = new ArrayList<>();
        for (SearchHit hit : response.getHits()) {
            if (hit != null) {
                S result = null;
                if (!StringUtils.isEmpty(hit.getSourceAsString())) {
                    result = this.mapEntity(hit.getSourceAsString(), clazz);
                } else {
                    result = this.mapEntity(hit.getFields().values(), clazz);
                }
                setPersistentEntity(result, hit, (Class<S>) result.getClass());
                results.add(result);
            }
        }
        return ScrolledPageResult.of(results, totalHits, response.getScrollId());
    }

    /**
     * @param responses the response
     * @param clazz     the entity class
     * @param <T>       the generic type
     * @return the map of results
     */
    @Override
    public <T> List<T> mapResults(MultiGetResponse responses, Class<T> clazz) {
        LinkedList<T> list = new LinkedList<>();
        for (MultiGetItemResponse response : responses.getResponses()) {
            if (!response.isFailed() && response.getResponse().isExists()) {
                list.add(mapResult(response.getResponse(), clazz));
            }
        }
        return list;
    }

    /**
     * @param values
     * @return
     */
    private String buildJSONFromFields(Collection<DocumentField> values) {
        JsonFactory nodeFactory = new JsonFactory();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try (JsonGenerator generator = nodeFactory.createGenerator(stream, JsonEncoding.UTF8)) {
            generator.writeStartObject();
            for (DocumentField value : values) {
                if (value.getValues().size() > 1) {
                    generator.writeArrayFieldStart(value.getName());
                    for (Object val : value.getValues()) {
                        generator.writeObject(val);
                    }
                    generator.writeEndArray();
                } else {
                    generator.writeObjectField(value.getName(), value.getValue());
                }
            }
            generator.writeEndObject();
            generator.flush();
            return new String(stream.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * @param result
     * @param response
     * @param clazz
     * @param <T>
     */
    private <S extends T, T> void setPersistentEntity(S result, GetResponse response, Class<S> clazz) {
        this.converter.getRequiredPersistentEntity(clazz).setPersistentEntity(result, response);
    }

    /**
     * @param result
     * @param hit
     * @param clazz
     * @param <T>
     */
    private <S extends T, T> void setPersistentEntity(S result, SearchHit hit, Class<S> clazz) {
        this.converter.getRequiredPersistentEntity(clazz).setPersistentEntity(result, hit);
    }

}
