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

import com.github.ydespreaux.spring.data.elasticsearch.client.reactive.ReactiveRestElasticsearchClient;
import com.github.ydespreaux.spring.data.elasticsearch.core.converter.ElasticsearchConverter;
import com.github.ydespreaux.spring.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import com.github.ydespreaux.spring.data.elasticsearch.core.triggers.TriggerManager;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.index.IndexNotFoundException;
import org.reactivestreams.Publisher;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ReactiveElasticsearchTemplate extends ElasticsearchTemplateSupport implements ReactiveElasticsearchOperations {

    private final ReactiveRestElasticsearchClient client;

    private final ElasticsearchExceptionTranslator exceptionTranslator;

    /**
     * Construct an instance with the given client and elasticsearchConverter parameters.
     *
     * @param client                 the given client.
     * @param elasticsearchConverter the given elasticsearchConverter.
     * @param resultsMapper          the given result mapper
     */
    public ReactiveElasticsearchTemplate(
            ReactiveRestElasticsearchClient client,
            ElasticsearchConverter elasticsearchConverter,
            ResultsMapper resultsMapper,
            TriggerManager triggerManager) {
        super(elasticsearchConverter, resultsMapper, triggerManager);
        this.client = client;
        this.exceptionTranslator = new ElasticsearchExceptionTranslator();
    }

    /**
     * Obtain the {@link ReactiveRestElasticsearchClient} to operate upon.
     *
     * @return never {@literal null}.
     */
    protected ReactiveRestElasticsearchClient getClient() {
        return this.client;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations#exctute(ClientCallback)
     */
    @Override
    public <T> Publisher<T> execute(ClientCallback<Publisher<T>> callback) {
        return Flux.defer(() -> callback.doWithClient(client)).onErrorMap(this::translateException);
    }

    private Throwable translateException(Throwable throwable) {
        RuntimeException exception = throwable instanceof RuntimeException ? (RuntimeException) throwable
                : new RuntimeException(throwable.getMessage(), throwable);
        Throwable potentiallyTranslatedException = exceptionTranslator.translateExceptionIfPossible(exception);
        return potentiallyTranslatedException != null ? potentiallyTranslatedException : throwable;
    }


//    /**
//     * method checking the existance of the given indexName.
//     *
//     * @param indexName the given indexName.
//     * @return true if indexName exist in elastic continueScroll.
//     */
//    @Override
//    public Mono<Boolean> indexExists(String indexName) {
//        Objects.requireNonNull(indexName);
//        GetIndexRequest request = this.requestsBuilder().getIndexRequest(indexName);
//        return Mono.<Boolean>create(sink -> getClient().indices().existsAsync(request, RequestOptions.DEFAULT, listenerToSink(sink)))
//                .onErrorResume(error -> Mono.error(new ElasticsearchException("Failed to get index request: " + request.toString(), error)));
//    }
//
//    /**
//     * @param indexName the index name
//     * @return true if the index name was created
//     */
//    @Override
//    public Mono<Boolean> createIndex(String indexName) {
//        Assert.notNull(indexName, "No index defined for Query");
//        CreateIndexRequest request = this.requestsBuilder().createIndexRequest(indexName);
//        return Mono.<CreateIndexResponse>create(sink -> getClient().indices().createAsync(request, RequestOptions.DEFAULT, listenerToSink(sink)))
//                .map(CreateIndexResponse::isAcknowledged)
//                .onErrorResume(error -> Mono.error(new ElasticsearchException("Failed to create index: " + request.toString(), error)));
//    }
//
//    /**
//     * @param indexName the index name
//     * @param settingsAndMappingPath the path of the json index file
//     * @return true if the index was created
//     */
//    @Override
//    public Mono<Boolean> createIndexWithSettingsAndMapping(String indexName, String settingsAndMappingPath) {
//        CreateIndexRequest request = this.requestsBuilder().createIndexRequest(indexName, settingsAndMappingPath);
//        return Mono.<CreateIndexResponse>create(sink -> this.getClient().indices().createAsync(request, RequestOptions.DEFAULT, listenerToSink(sink)))
//                .map(CreateIndexResponse::isAcknowledged)
//                .onErrorResume(error -> Mono.error(new ElasticsearchException("Failed to create index request: " + request.toString(), error)));
//    }
//
//    /**
//     * Delete index
//     *
//     * @param indexName index name
//     * @return true if the index was deleted
//     */
//    @Override
//    public Mono<Boolean> deleteIndexByName(String indexName) {
//        Objects.requireNonNull(indexName);
//        return Mono
//                .<DeleteIndexResponse>create(sink -> this.getClient().indices().deleteAsync(this.requestsBuilder().deleteIndexRequest(indexName), RequestOptions.DEFAULT, listenerToSink(sink)))
//                .map(DeleteIndexResponse::isAcknowledged)
//                .onErrorResume(error -> Mono.just(false));
//    }
//
//    /**
//     * Delete all indices for a aliasOrIndex
//     *
//     * @param aliasName he given aliasName.
//     */
//    @Override
//    public Mono<Boolean> deleteIndexByAlias(String aliasName) {
//        Objects.requireNonNull(aliasName);
//        GetAliasesRequest request = new GetAliasesRequest(aliasName);
//        return Mono
//                .<GetAliasesResponse>create(sink -> this.getClient().indices().getAliasAsync(request, RequestOptions.DEFAULT, listenerToSink(sink)))
//                .map(GetAliasesResponse::getAliases)
//                .map(Map::keySet)
//                .flatMapMany(Flux::fromIterable)
//                .doOnNext(this::deleteIndexByName)
//                .hasElements()
//                .onErrorResume(error -> Mono.just(false));
//    }
//
//    /**
//     * refresh the elasticsearch index for the given clazz
//     *
//     * @param clazz the given clazz.
//     */
//    @Override
//    public <T> Mono<Void> refresh(Class<T> clazz) {
//        ElasticsearchPersistentEntity<T> persistentEntity = getPersistentEntityFor(clazz);
//        return this.refresh(persistentEntity.getAliasOrIndexName());
//    }
//
//    /**
//     * @param indexName the index name
//     */
//    @Override
//    public Mono<Void> refresh(String indexName) {
//        Assert.notNull(indexName, "No index defined for refresh()");
//        return Mono.<RefreshResponse>create(sink -> getClient().indices().refreshAsync(refreshRequest(indexName), RequestOptions.DEFAULT, listenerToSink(sink)))
//                .then()
//                .onErrorResume(error -> Mono.error(new ElasticsearchException("failed to refresh index: " + indexName, error)));
//    }
//

    /**
     * Index the given T entity, for the geiven clazz.
     *
     * @param entity the given entity.
     * @param clazz  the gievn {@link Class}.
     * @return T the indexed entity.
     */
    @Override
    public <T> Mono<T> index(T entity, Class<T> clazz) {
        ElasticsearchPersistentEntity<T> persistentEntity = getPersistentEntityFor(clazz);
        IndexRequest request = createIndexRequest(entity, clazz);
        return doIndex(request)
                .map(response -> {
                    persistentEntity.setPersistentEntity(entity, response);
                    return entity;
                })
                .onErrorResume(error -> Mono.error(new ElasticsearchException("Error while index for request: " + request.toString(), error)));
    }

//    /**
//     * Index the given T entity, for the geiven clazz.
//     *
//     * @param entity the given entity.
//     * @param clazz  the gievn {@link Class}.
//     * @return T the indexed entity.
//     */
//    @Override
//    public <T> Mono<T> index(T entity, Class<T> clazz) {
//        ElasticsearchPersistentEntity<T> persistentEntity = getPersistentEntityFor(clazz);
//        IndexRequest request = createIndexRequest(entity, clazz);
//        return Mono.<IndexResponse>create(sink -> getClient().indexAsync(request, RequestOptions.DEFAULT, listenerToSink(sink)))
//                .map(response -> {
//                    persistentEntity.setPersistentEntityId(entity, response.getId());
//                    persistentEntity.setPersistentEntityVersion(entity, response.getVersion());
//                    return entity;
//                })
//                .onErrorResume(error -> Mono.error(new ElasticsearchException("Error while index for request: " + request.toString(), error)));
//    }
//
//    /**
//     * Bulk index operation for the given {@link List} of entities, and gievn {@link Class}.
//     *
//     * @param entities the given entities {@link List}.
//     * @param clazz    the given {@link Class}.
//     * @return documents indexed
//     */
//    @Override
//    public <T> Flux<T> bulkIndex(List<T> entities, Class<T> clazz) {
//        if (entities.isEmpty())
//            return Flux.empty();
//        ElasticsearchPersistentEntity<T> persistentEntity = getPersistentEntityFor(clazz);
//        BulkRequest bulkRequest = new BulkRequest();
//        entities.forEach(entity -> bulkRequest.add(createIndexRequest(entity, clazz)));
//        return Mono.<BulkResponse>create(sink -> getClient().bulkAsync(bulkRequest, RequestOptions.DEFAULT, listenerToSink(sink)))
//                .flatMapMany(response -> {
//                    checkForBulkUpdateFailure(response);
//                    BulkItemResponse[] items = response.getItems();
//                    for (int i = 0; i < entities.size(); i++) {
//                        persistentEntity.setPersistentEntityId(entities.get(i), items[i].getId());
//                        persistentEntity.setPersistentEntityVersion(entities.get(i), items[i].getVersion());
//                    }
//                    return Flux.fromIterable(entities);
//                });
//    }
//
//    /**
//     * @param entities all entities to index
//     * @return the entities indexed
//     */
//    @Override
//    public Flux<?> bulkIndex(List<?> entities) {
//        if (entities.isEmpty())
//            return Flux.empty();
//        BulkRequest bulkRequest = new BulkRequest();
//        entities.forEach(entity -> bulkRequest.add(createIndexRequest(entity, entity.getClass())));
//        return Mono.<BulkResponse>create(sink -> getClient().bulkAsync(bulkRequest, RequestOptions.DEFAULT, listenerToSink(sink)))
//                .flatMapMany(response -> {
//                    checkForBulkUpdateFailure(response);
//                    BulkItemResponse[] items = response.getItems();
//                    for (int i = 0; i < entities.size(); i++) {
//                        Object entity = entities.get(i);
//                        ElasticsearchPersistentEntity persistentEntity = getPersistentEntityFor(entity.getClass());
//                        persistentEntity.setPersistentEntityId(entity, items[i].getId());
//                        persistentEntity.setPersistentEntityVersion(entity, items[i].getVersion());
//                    }
//                    return Flux.fromIterable(entities);
//                });
//    }
//

    /**
     * Find an elasticsearch document for the given clazz, and documentId.
     *
     * @param id         the given documentId.
     * @param entityType the given clazz.
     * @return the entity for the given documentId or null.
     */
    @Override
    public <T> Mono<T> findById(String id, Class<T> entityType) {
        Assert.notNull(id, "Id must not be null!");
        ElasticsearchPersistentEntity<T> persistentEntity = getPersistentEntityFor(entityType);
        return doFindById(id, persistentEntity.getAliasOrIndexReader(), persistentEntity.getTypeName())
                .filter(GetResponse::isExists)
                .map(it -> this.getResultsMapper().mapResult(it, entityType))
                .onErrorResume(IndexNotFoundException.class, ex -> Mono.empty());
    }


//    @Override
//    public <T> Mono<T> findOne(CriteriaQuery query, Class<T> clazz) {
//        return null;
//    }
//
//    @Override
//    public <T> Mono<T> findOne(SearchQuery query, Class<T> clazz) {
//        return null;
//    }
//
//    @Override
//    public <T> Mono<T> findOne(StringQuery query, Class<T> clazz) {
//        return null;
//    }
//
//    /**
//     * @param query
//     * @param clazz
//     * @return
//     */
//    @Override
//    public <T> Mono<Long> count(SearchQuery query, Class<T> clazz) {
//        return null;
//    }
//
//    /**
//     * @param query
//     * @param clazz
//     * @return
//     */
//    @Override
//    public <T> Mono<Long> count(CriteriaQuery query, Class<T> clazz) {
//        return null;
//    }
//
//    /**
//     * @param clazz      the domain type
//     * @param documentId the document id.
//     * @return true if the document corresponding to the id exists
//     */
//    @Override
//    public <T> Mono<Boolean> existsById(Class<T> clazz, String documentId) {
//        return null;
//    }
//
//    /**
//     * @param query
//     * @param javaType
//     * @return
//     */
//    @Override
//    public <T> Mono<Boolean> existsByQuery(CriteriaQuery query, Class<T> javaType) {
//        return null;
//    }
//
//    @Override
//    public <T> Mono<Boolean> existsByQuery(SearchQuery query, Class<T> javaType) {
//        return null;
//    }
//
//    @Override
//    public <T> Mono<Boolean> existsByQuery(StringQuery query, Class<T> javaType) {
//        return null;
//    }
//
//    /**
//     * Delete all the documents for the given clazz
//     *
//     * @param clazz the given clazz.
//     */
//    @Override
//    public <T> Mono<Void> deleteAll(Class<T> clazz) {
//        return null;
//    }
//
//    /**
//     * Delete all the {@link List} of entities, for the given clazz.
//     *
//     * @param entities the {@link List} of entities.
//     * @param clazz    the given clazz.
//     */
//    @Override
//    public <T> Mono<Void> deleteAll(List<T> entities, Class<T> clazz) {
//        return null;
//    }
//
//    /**
//     * delete the document for the given entity, and clazz
//     *
//     * @param entity the given entity.
//     * @param clazz  the given clazz.
//     */
//    @Override
//    public <T> Mono<Void> delete(T entity, Class<T> clazz) {
//        return null;
//    }
//
//    /**
//     * delete the document for the given entity, and clazz
//     *
//     * @param query the given query.
//     * @param clazz the given clazz.
//     */
//    @Override
//    public <T> Mono<Void> delete(CriteriaQuery query, Class<T> clazz) {
//        return null;
//    }
//
//    /**
//     * delete the document with the given documentId and clazz.
//     *
//     * @param documentId the given documentId.
//     * @param clazz      the given clazz.
//     */
//    @Override
//    public <T> Mono<Void> deleteById(String documentId, Class<T> clazz) {
//        return null;
//    }
//
//    /**
//     * Search with the given {@link SearchRequest} continueScroll, and given {@link Class} clazz.
//     *
//     * @param search the given {@link SearchRequest} instance.
//     * @param clazz  the given clazz.
//     * @return a {@link List} of the method generic type.
//     */
//    @Override
//    public <T> Flux<T> search(SearchQuery search, Class<T> clazz) {
//        return null;
//    }
//
//    /**
//     * @param search
//     * @param clazz
//     * @return
//     */
//    @Override
//    public <T> Flux<T> search(CriteriaQuery search, Class<T> clazz) {
//        return null;
//    }
//
//    /**
//     * @param stringQuery
//     * @param clazz
//     * @return
//     */
//    @Override
//    public <T> Flux<T> search(StringQuery stringQuery, Class<T> clazz) {
//        return null;
//    }
//
//    /**
//     * @param query
//     * @param resultsExtractor
//     * @return
//     */
//    @Override
//    public <T> Flux<T> search(SearchQuery query, ResultsExtractor<T> resultsExtractor) {
//        return null;
//    }
//
//    /**
//     * @param suggestion
//     * @param indices
//     * @return
//     */
//    @Override
//    public Mono<SearchResponse> suggest(SuggestBuilder suggestion, String... indices) {
//        return null;
//    }
//
//    /**
//     * @param suggestion
//     * @param indices
//     * @param extractor
//     * @return
//     */
//    @Override
//    public <R> Flux<R> suggest(SuggestBuilder suggestion, String[] indices, ResultsExtractor<R> extractor) {
//        return null;
//    }
//
//    /**
//     * @param suggestion
//     * @param clazz
//     * @param extractor
//     * @return
//     */
//    @Override
//    public <R, T> Flux<R> suggest(SuggestBuilder suggestion, Class<T> clazz, ResultsExtractor<R> extractor) {
//        return null;
//    }

    /**
     * Customization hook on the actual execution result {@link Publisher}. <br />
     * You know what you're doing here? Well fair enough, go ahead on your own risk.
     *
     * @param request the already prepared {@link IndexRequest} ready to be executed.
     * @return a {@link Mono} emitting the result of the operation.
     */
    protected Mono<IndexResponse> doIndex(IndexRequest request) {
        return Mono.from(execute(client -> client.index(request)));
    }

    private Mono<GetResponse> doFindById(String id, String indexName, String typeName) {
        return Mono.defer(() -> doFindById(new GetRequest(indexName, typeName, id)));
    }

    /**
     * Customization hook on the actual execution result {@link Publisher}. <br />
     *
     * @param request the already prepared {@link GetRequest} ready to be executed.
     * @return a {@link Mono} emitting the result of the operation.
     */
    protected Mono<GetResponse> doFindById(GetRequest request) {
        return Mono.from(execute(client -> client.get(request)));
    }

}
