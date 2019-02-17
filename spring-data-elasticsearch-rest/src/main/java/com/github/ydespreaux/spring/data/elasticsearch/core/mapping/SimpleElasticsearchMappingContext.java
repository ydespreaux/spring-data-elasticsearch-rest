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

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;

/**
 * SimpleElasticsearchMappingContext
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public class SimpleElasticsearchMappingContext extends
        AbstractMappingContext<SimpleElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> implements ApplicationContextAware {

    private ApplicationContext context;

    @Override
    protected <T> SimpleElasticsearchPersistentEntity<?> createPersistentEntity(TypeInformation<T> typeInformation) {
        final SimpleElasticsearchPersistentEntity<T> persistentEntity = new SimpleElasticsearchPersistentEntity<>(
                typeInformation);
        if (context != null) {
            persistentEntity.setApplicationContext(context);
        }
        return persistentEntity;
    }

    @Override
    protected ElasticsearchPersistentProperty createPersistentProperty(Property property,
                                                                       SimpleElasticsearchPersistentEntity<?> owner, SimpleTypeHolder simpleTypeHolder) {
        return new SimpleElasticsearchPersistentProperty(property, owner, simpleTypeHolder);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }
}
