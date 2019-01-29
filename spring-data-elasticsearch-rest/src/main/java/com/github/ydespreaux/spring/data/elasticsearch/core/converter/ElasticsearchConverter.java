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

package com.github.ydespreaux.spring.data.elasticsearch.core.converter;

import com.github.ydespreaux.spring.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import com.github.ydespreaux.spring.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import org.springframework.data.mapping.context.MappingContext;

/**
 * ElasticsearchConverter
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public interface ElasticsearchConverter {

    /**
     * Give the required {@link ElasticsearchPersistentEntity} for the given clazz parameter.
     *
     * @param clazz the given clazz.
     * @param <T>   genereic type
     * @return an {@link ElasticsearchPersistentEntity} of T.
     */
    <T> ElasticsearchPersistentEntity<T> getRequiredPersistentEntity(Class<T> clazz);

    /**
     * Returns the underlying {@link org.springframework.data.mapping.context.MappingContext} used by the converter.
     *
     * @return never {@literal null}
     */
    MappingContext<? extends ElasticsearchPersistentEntity, ElasticsearchPersistentProperty> getMappingContext();
}
