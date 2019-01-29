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

package com.github.ydespreaux.spring.data.elasticsearch.repository.support;

import com.github.ydespreaux.spring.data.elasticsearch.core.ElasticsearchOperations;
import com.github.ydespreaux.spring.data.elasticsearch.repository.ElasticsearchRepository;
import com.github.ydespreaux.spring.data.elasticsearch.repository.SimpleElasticsearchRepository;
import com.github.ydespreaux.spring.data.elasticsearch.repository.query.ElasticsearchPartQuery;
import com.github.ydespreaux.spring.data.elasticsearch.repository.query.ElasticsearchQueryMethod;
import com.github.ydespreaux.spring.data.elasticsearch.repository.query.ElasticsearchStringQuery;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Factory to create {@link ElasticsearchRepository}
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public class ElasticsearchRepositoryFactory extends RepositoryFactorySupport {

    private final ElasticsearchOperations elasticsearchOperations;

    public ElasticsearchRepositoryFactory(ElasticsearchOperations elasticsearchOperations) {

        Assert.notNull(elasticsearchOperations, "ElasticsearchOperations must not be null!");

        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public <T, K> ElasticsearchEntityInformation<T, K> getEntityInformation(Class<T> domainClass) {
        return (ElasticsearchEntityInformation<T, K>) this.elasticsearchOperations.getElasticsearchConverter().getRequiredPersistentEntity(domainClass);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Object getTargetRepository(RepositoryInformation metadata) {
        return getTargetRepositoryViaReflection(metadata, getEntityInformation(metadata.getDomainType()),
                elasticsearchOperations);
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        if (metadata.getIdType() == String.class) {
            return SimpleElasticsearchRepository.class;
        } else {
            throw new IllegalArgumentException("Unsupported ID type " + metadata.getIdType());
        }
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(QueryLookupStrategy.Key key,
                                                                   QueryMethodEvaluationContextProvider evaluationContextProvider) {
        return Optional.of(new ElasticsearchQueryLookupStrategy());
    }

    private class ElasticsearchQueryLookupStrategy implements QueryLookupStrategy {

        /*
         * (non-Javadoc)
         * @see org.springframework.data.repository.query.QueryLookupStrategy#resolveQuery(java.lang.reflect.Method, org.springframework.data.repository.core.RepositoryMetadata, org.springframework.data.projection.ProjectionFactory, org.springframework.data.repository.core.NamedQueries)
         */
        @Override
        public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
                                            NamedQueries namedQueries) {

            ElasticsearchQueryMethod queryMethod = new ElasticsearchQueryMethod(method, metadata, factory);
            String namedQueryName = queryMethod.getNamedQueryName();
            if (namedQueries.hasQuery(namedQueryName)) {
                String namedQuery = namedQueries.getQuery(namedQueryName);
                return new ElasticsearchStringQuery(queryMethod, elasticsearchOperations, namedQuery);
            } else if (queryMethod.hasAnnotatedQuery()) {
                return new ElasticsearchStringQuery(queryMethod, elasticsearchOperations, queryMethod.getAnnotatedQuery());
            }
            return new ElasticsearchPartQuery(queryMethod, elasticsearchOperations);
        }
    }

}
