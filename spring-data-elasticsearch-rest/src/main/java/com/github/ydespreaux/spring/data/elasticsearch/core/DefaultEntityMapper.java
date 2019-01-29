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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ydespreaux.spring.data.elasticsearch.annotations.IndexName;
import com.github.ydespreaux.spring.data.elasticsearch.annotations.Score;
import com.github.ydespreaux.spring.data.elasticsearch.core.converter.adapter.TimeTypeAdapterRegistry;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.data.annotation.Version;

/**
 * EntityMapper based on a Jackson {@link ObjectMapper}.
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public class DefaultEntityMapper implements EntityMapper {

    private final Gson mapper;

    /**
     * @param jacksonProperties the jackson properties
     */
    public DefaultEntityMapper(final JacksonProperties jacksonProperties) {
        GsonBuilder builder = new GsonBuilder();
        TimeTypeAdapterRegistry.registerAll(builder);
        if (jacksonProperties.getDateFormat() != null) {
            builder.setDateFormat(jacksonProperties.getDateFormat());
        } else {
            builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        }
        TimeTypeAdapterRegistry.registerAll(builder);
        builder.addSerializationExclusionStrategy(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                return fieldAttributes.getAnnotation(Version.class) != null
                        || fieldAttributes.getAnnotation(IndexName.class) != null
                        || fieldAttributes.getAnnotation(Score.class) != null;
            }

            @Override
            public boolean shouldSkipClass(Class<?> aClass) {
                return false;
            }
        });
        mapper = builder.create();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.EntityMapper#mapToString(java.lang.Object)
     */
    @Override
    public String mapToString(Object object) {
        return mapper.toJson(object);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.EntityMapper#mapToObject(java.lang.String, java.lang.Class)
     */
    @Override
    public <T> T mapToObject(String source, Class<T> clazz) {
        return mapper.fromJson(source, clazz);
    }

}
