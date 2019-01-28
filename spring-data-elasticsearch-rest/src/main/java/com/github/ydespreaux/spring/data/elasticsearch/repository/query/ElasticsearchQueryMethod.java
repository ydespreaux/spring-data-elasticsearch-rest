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
package com.github.ydespreaux.spring.data.elasticsearch.repository.query;

import com.github.ydespreaux.spring.data.elasticsearch.annotations.Query;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;

import java.lang.reflect.Method;

import static org.springframework.util.StringUtils.isEmpty;

/**
 * ElasticsearchQueryMethod
 *
 * @author Yoann Despréaux
 * @since 0.1.0
 */
public class ElasticsearchQueryMethod extends QueryMethod {

    private final Query queryAnnotation;

    public ElasticsearchQueryMethod(Method method, RepositoryMetadata metadata, ProjectionFactory factory) {
        super(method, metadata, factory);
        this.queryAnnotation = method.getAnnotation(Query.class);
    }

    public boolean hasAnnotatedQuery() {
        return this.queryAnnotation != null;
    }

    public boolean hasAnnotatedNamedQuery() {
        return this.queryAnnotation != null && !isEmpty(this.queryAnnotation.name());
    }

    public String getAnnotatedQuery() {
        return (String) AnnotationUtils.getValue(queryAnnotation, "value");
    }

    @Override
    public String getNamedQueryName() {
        if (hasAnnotatedNamedQuery()) {
            return getAnnotatedNamedQuery();
        }
        return super.getNamedQueryName();
    }

    public String getAnnotatedNamedQuery() {
        return (String) AnnotationUtils.getValue(queryAnnotation, "name");
    }

}
