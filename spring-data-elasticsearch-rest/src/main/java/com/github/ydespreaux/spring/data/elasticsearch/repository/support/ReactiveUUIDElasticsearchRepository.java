package com.github.ydespreaux.spring.data.elasticsearch.repository.support;

import com.github.ydespreaux.spring.data.elasticsearch.core.ReactiveElasticsearchOperations;

import java.util.UUID;

public class ReactiveUUIDElasticsearchRepository<T> extends AbstractReactiveElasticsearchRepository<T, UUID> {

    public ReactiveUUIDElasticsearchRepository(ReactiveElasticsearchOperations elasticsearchOperations) {
        super(elasticsearchOperations);
    }

    /**
     * @param metadata                the entity metadata
     * @param elasticsearchOperations a {@link ReactiveElasticsearchOperations}
     */
    public ReactiveUUIDElasticsearchRepository(ElasticsearchEntityInformation<T, UUID> metadata, ReactiveElasticsearchOperations elasticsearchOperations) {
        super(metadata, elasticsearchOperations);
    }

    /**
     * @param id the identifier
     * @return the string representation
     */
    @Override
    protected String stringIdRepresentation(UUID id) {
        return id == null ? null : id.toString();
    }
}
