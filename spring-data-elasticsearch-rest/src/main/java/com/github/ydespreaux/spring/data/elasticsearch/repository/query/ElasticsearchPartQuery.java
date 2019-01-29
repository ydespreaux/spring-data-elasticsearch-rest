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

import com.github.ydespreaux.spring.data.elasticsearch.core.ElasticsearchOperations;
import com.github.ydespreaux.spring.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.CriteriaQuery;
import com.github.ydespreaux.spring.data.elasticsearch.repository.query.parser.ElasticsearchQueryCreator;
import org.elasticsearch.ElasticsearchException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.util.ClassUtils;

/**
 * ElasticsearchPartQuery
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public class ElasticsearchPartQuery extends AbstractElasticsearchRepositoryQuery {

    private final PartTree tree;
    private final MappingContext<?, ElasticsearchPersistentProperty> mappingContext;

    public ElasticsearchPartQuery(ElasticsearchQueryMethod method, ElasticsearchOperations elasticsearchOperations) {
        super(method, elasticsearchOperations);
        this.tree = new PartTree(method.getName(), method.getEntityInformation().getJavaType());
        this.mappingContext = elasticsearchOperations.getElasticsearchConverter().getMappingContext();
    }

    @Override
    public Object execute(Object[] parameters) {
        ParametersParameterAccessor accessor = new ParametersParameterAccessor(queryMethod.getParameters(), parameters);
        CriteriaQuery query = createQuery(accessor);
        if (tree.isDelete()) {
            Object result = countOrGetDocumentsForDelete(query, accessor);
            elasticsearchOperations.delete(query, queryMethod.getEntityInformation().getJavaType());
            return result;
        } else if (queryMethod.isPageQuery()) {
            query.setPageable(accessor.getPageable());
            return elasticsearchOperations.startScroll(query, queryMethod.getEntityInformation().getJavaType());
        } else if (queryMethod.isStreamQuery()) {
            throw new ElasticsearchException("Stream query method not supported");
        } else if (queryMethod.isCollectionQuery()) {
            if (accessor.getPageable() == null) {
                int itemCount = (int) elasticsearchOperations.count(query, queryMethod.getEntityInformation().getJavaType());
                query.setPageable(PageRequest.of(0, Math.max(1, itemCount)));
            } else {
                query.setPageable(accessor.getPageable());
            }
            return elasticsearchOperations.search(query, queryMethod.getEntityInformation().getJavaType());
        } else if (tree.isCountProjection()) {
            return elasticsearchOperations.count(query, queryMethod.getEntityInformation().getJavaType());
        } else if (tree.isExistsProjection()) {
            if (accessor.getPageable() == null) {
                query.setPageable(PageRequest.of(0, 1));
            }
            return elasticsearchOperations.existsByQuery(query, queryMethod.getEntityInformation().getJavaType());
        }
        return elasticsearchOperations.findOne(query, queryMethod.getEntityInformation().getJavaType());
    }

    private Object countOrGetDocumentsForDelete(CriteriaQuery query, ParametersParameterAccessor accessor) {

        Object result = null;

        if (queryMethod.isCollectionQuery()) {
            if (accessor.getPageable().isUnpaged()) {
                int itemCount = (int) elasticsearchOperations.count(query, queryMethod.getEntityInformation().getJavaType());
                query.setPageable(PageRequest.of(0, Math.max(1, itemCount)));
            } else {
                query.setPageable(accessor.getPageable());
            }
            result = elasticsearchOperations.search(query, queryMethod.getEntityInformation().getJavaType());
        }

        if (ClassUtils.isAssignable(Number.class, queryMethod.getReturnedObjectType())) {
            result = elasticsearchOperations.count(query, queryMethod.getEntityInformation().getJavaType());
        }
        return result;
    }

    public CriteriaQuery createQuery(ParametersParameterAccessor accessor) {
        return new ElasticsearchQueryCreator(tree, accessor, mappingContext).createQuery();
    }
}
