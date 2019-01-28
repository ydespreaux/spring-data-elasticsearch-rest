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

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;

/**
 * ResultsMapper
 *
 * @author Yoann Despréaux
 * @since 0.0.1
 */
public interface ResultsMapper extends SearchResultMapper, GetResultMapper, MultiGetResultMapper {

    /**
     * @return the entity mapper
     */
    EntityMapper getEntityMapper();

    /**
     * @param values the document fields
     * @param clazz  the entity class
     * @param <T>    generic method
     * @return the entity
     */
    <T> T mapEntity(Collection<DocumentField> values, Class<T> clazz);

    /**
     * @param source the json source
     * @param clazz  the entity class
     * @param <T>    generic type
     * @return the entity
     */
    @Nullable
    default <T> T mapEntity(String source, Class<T> clazz) {

        if (StringUtils.isEmpty(source)) {
            return null;
        }
        return getEntityMapper().mapToObject(source, clazz);
    }

    /**
     * Map a single {@link SearchHit} to an instance of the given type.
     *
     * @param searchHit must not be {@literal null}.
     * @param type      must not be {@literal null}.
     * @param <T>       generic method
     * @return can be {@literal null} if the {@link SearchHit} does not have {@link SearchHit#hasSource() a source}.
     */
    <T> T mapEntity(SearchHit searchHit, Class<T> type);

    /**
     * @param searchHits the hits
     * @param type       the entity class
     * @param <T>        method generic
     * @return the list of entities
     */
    <T> List<T> mapEntity(SearchHits searchHits, Class<T> type);

    /**
     * @param response the response
     * @param clazz    the entity class
     * @param <T>      method generic
     * @return the entity
     */
    <T> T mapResult(GetResponse response, Class<T> clazz);
}
