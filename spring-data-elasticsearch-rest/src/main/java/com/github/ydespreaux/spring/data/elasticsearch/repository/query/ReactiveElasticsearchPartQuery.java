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

import com.github.ydespreaux.spring.data.elasticsearch.core.ReactiveElasticsearchOperations;
import com.github.ydespreaux.spring.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.CriteriaQuery;
import com.github.ydespreaux.spring.data.elasticsearch.repository.query.parser.ElasticsearchQueryCreator;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.lang.Nullable;

/**
 * ElasticsearchPartQuery
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public class ReactiveElasticsearchPartQuery extends AbstractReactiveElasticsearchRepositoryQuery {

    private final PartTree tree;
    private final MappingContext<?, ElasticsearchPersistentProperty> mappingContext;

    public ReactiveElasticsearchPartQuery(ReactiveElasticsearchQueryMethod method, ReactiveElasticsearchOperations elasticsearchOperations) {
        super(method, elasticsearchOperations);
        this.tree = new PartTree(method.getName(), method.getEntityInformation().getJavaType());
        this.mappingContext = elasticsearchOperations.getElasticsearchConverter().getMappingContext();
    }

    @Nullable
    @Override
    public Object execute(Object[] parameters) {
        ParametersParameterAccessor accessor = new ParametersParameterAccessor(queryMethod.getParameters(), parameters);
        CriteriaQuery query = createQuery(accessor);
        if (tree.isDelete()) {
            return reactiveElasticsearchOperations.delete(query, queryMethod.getEntityInformation().getJavaType());
        } else if (tree.isCountProjection()) {
            return reactiveElasticsearchOperations.count(query, queryMethod.getEntityInformation().getJavaType());
        } else if (tree.isExistsProjection()) {
            return reactiveElasticsearchOperations.existsByQuery(query, queryMethod.getEntityInformation().getJavaType());
        } else if (queryMethod.isStreamQuery()) {
            return reactiveElasticsearchOperations.search(query, queryMethod.getEntityInformation().getJavaType());
        }
        return reactiveElasticsearchOperations.findOne(query, queryMethod.getEntityInformation().getJavaType());
    }

    public CriteriaQuery createQuery(ParametersParameterAccessor accessor) {
        return new ElasticsearchQueryCreator(tree, accessor, mappingContext).createQuery();
    }
}
