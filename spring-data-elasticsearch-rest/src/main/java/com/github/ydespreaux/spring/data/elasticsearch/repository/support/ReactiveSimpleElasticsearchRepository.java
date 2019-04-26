package com.github.ydespreaux.spring.data.elasticsearch.repository.support;

import com.github.ydespreaux.spring.data.elasticsearch.core.ReactiveElasticsearchOperations;

public class ReactiveSimpleElasticsearchRepository<T> extends AbstractReactiveElasticsearchRepository<T, String> {

    public ReactiveSimpleElasticsearchRepository(ReactiveElasticsearchOperations elasticsearchOperations) {
        super(elasticsearchOperations);
    }

    /**
     * @param metadata                the entity metadata
     * @param elasticsearchOperations a {@link ReactiveElasticsearchOperations}
     */
    public ReactiveSimpleElasticsearchRepository(ElasticsearchEntityInformation<T, String> metadata, ReactiveElasticsearchOperations elasticsearchOperations) {
        super(metadata, elasticsearchOperations);
    }

    /**
     * @param id the identifier
     * @return the string representation
     */
    @Override
    protected String stringIdRepresentation(String id) {
        return id;
    }
}
