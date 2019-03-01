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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.ydespreaux.spring.data.elasticsearch.annotations.IndexName;
import com.github.ydespreaux.spring.data.elasticsearch.annotations.ParentId;
import com.github.ydespreaux.spring.data.elasticsearch.annotations.Score;
import com.github.ydespreaux.spring.data.elasticsearch.core.converter.ElasticsearchTypeModule;
import com.github.ydespreaux.spring.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import org.elasticsearch.ElasticsearchException;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * EntityMapper based on a Jackson {@link ObjectMapper}.
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public class DefaultEntityMapper implements EntityMapper {

    private final ObjectMapper mapper;

    private final EntitySerializerRegistry registry;

    /**
     * @param jacksonProperties the jackson properties
     */
    public DefaultEntityMapper(final JacksonProperties jacksonProperties) {
        this.mapper = new ObjectMapper();
        this.registry = new EntitySerializerRegistry(this.mapper);
        configure(jacksonProperties);
    }

    protected void configure(final JacksonProperties jacksonProperties) {
        if (jacksonProperties.getDateFormat() != null) {
            mapper.setDateFormat(new SimpleDateFormat(jacksonProperties.getDateFormat()));
        } else {
            mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
        }
        mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {

            @Override
            public boolean hasIgnoreMarker(AnnotatedMember m) {
                return m.hasAnnotation(Id.class)
                        || m.hasAnnotation(Version.class)
                        || m.hasAnnotation(IndexName.class)
                        || m.hasAnnotation(Score.class)
                        || m.hasAnnotation(ParentId.class)
                        || super.hasIgnoreMarker(m);
            }
        });
        mapper.registerModules(new JavaTimeModule(), new ElasticsearchTypeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.EntityMapper#mapToString(java.lang.Object)
     */
    @Override
    public <T> String mapToString(T object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ElasticsearchException("Json processing failed : ", e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.EntityMapper#mapToObject(java.lang.String, java.lang.Class)
     */
    @Override
    public <S extends T, T> S mapToObject(String source, Class<T> clazz) {
        try {
            return (S) mapper.readValue(source, this.registry.getEntityClassFromJson(clazz, source));
        } catch (IOException e) {
            throw new ElasticsearchException("Json processing failed : ", e);
        }
    }

    /**
     * @param persistentEntity
     */
    @Override
    public <T> void register(ElasticsearchPersistentEntity<T> persistentEntity) {
        this.registry.register(persistentEntity);
    }


}
