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

package com.github.ydespreaux.spring.data.elasticsearch.core.mapping;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mapping.PersistentProperty;

/**
 * ElasticsearchPersistentProperty
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public interface ElasticsearchPersistentProperty extends PersistentProperty<ElasticsearchPersistentProperty> {

    String getFieldName();

    /**
     * Returns whether the current property is a <em>potential</em> score property of the owning
     * {@link ElasticsearchPersistentEntity}. This method is mainly used by {@link ElasticsearchPersistentEntity}
     * implementation to discover score property candidates on {@link ElasticsearchPersistentEntity} creation you should
     * rather call {@link ElasticsearchPersistentProperty#isScoreProperty()}  to determine whether the
     * current property is the score property of that {@link ElasticsearchPersistentEntity} under consideration.
     *
     * @return whether the current property is a <em>potential</em> score property of the owning
     */
    boolean isScoreProperty();

    /**
     * Returns whether the current property is a <em>potential</em> parent property of the owning
     * {@link ElasticsearchPersistentEntity}. This method is mainly used by {@link ElasticsearchPersistentEntity}
     * implementation to discover parent property candidates on {@link ElasticsearchPersistentEntity} creation you should
     * rather call {@link ElasticsearchPersistentProperty#isParentProperty()} to determine whether the current property is
     * the parent property of that {@link ElasticsearchPersistentEntity} under consideration.
     *
     * @return whether the current property is a <em>potential</em> parent property of the owning
     */
    boolean isParentProperty();

    /**
     * @return
     */
    boolean isIndexNameProperty();

    /**
     * @return
     */
    boolean isCompletionProperty();

    /**
     * @return
     */
    boolean isScriptProperty();

    enum PropertyToFieldNameConverter implements Converter<ElasticsearchPersistentProperty, String> {

        INSTANCE;

        public String convert(ElasticsearchPersistentProperty source) {
            return source.getFieldName();
        }
    }
}