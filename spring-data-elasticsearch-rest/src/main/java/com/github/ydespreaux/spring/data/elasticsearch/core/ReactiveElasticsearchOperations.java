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
import com.github.ydespreaux.spring.data.elasticsearch.core.query.CriteriaQuery;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.SearchQuery;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.StringQuery;
import com.github.ydespreaux.spring.data.elasticsearch.core.request.config.RolloverConfig;
import com.github.ydespreaux.spring.data.elasticsearch.core.triggers.TriggerManager;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.Nullable;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

/**
 *
 */
public interface ReactiveElasticsearchOperations {

    /**
     * Give the {@link ElasticsearchPersistentEntity} for the given {@link Class}.
     *
     * @param clazz the given {@link Class}.
     * @param <T>   method generic
     * @return ElasticsearchPersistentEntity the persitant entity for the given {@link Class} parameter.
     */
    <T> ElasticsearchPersistentEntity<T> getPersistentEntityFor(Class<T> clazz);

    /**
     * @return the elasticsearch converter
     */
    ElasticsearchConverter getElasticsearchConverter();

    /**
     * @return
     */
    TriggerManager getTriggerManager();

    /**
     * @param callback
     * @param <T>
     * @return
     */
    <T> Publisher<T> execute(ClientCallback<Publisher<T>> callback);

    /**
     * method checking the existance of the given indexName.
     *
     * @param indexName the given indexName.
     * @return true if indexName exist in elastic continueScroll.
     */
    Mono<Boolean> indexExists(String indexName);

    /**
     * @param indexName the index name
     * @return true if the index name was created
     */
    default Mono<Boolean> createIndex(String indexName) { return createIndex(null, indexName); }

    /**
     *
     * @param alias
     * @param indexName
     * @return
     */
    Mono<Boolean> createIndex(Alias alias, String indexName);

    /**
     * @param clazz
     * @param <T>
     * @return
     */
    <T> Mono<Boolean> createIndex(Class<T> clazz);


    /**
     *
     * @param aliasWriter
     * @param indexName
     * @return
     */
    default Mono<Boolean> createRolloverIndex(Alias aliasWriter, String indexName) {
        return this.createRolloverIndex(null, aliasWriter, indexName);
    }

    /**
     *
     * @param aliasReader
     * @param aliasWriter
     * @param indexName
     * @return
     */
    Mono<Boolean> createRolloverIndex(Alias aliasReader, Alias aliasWriter, String indexName);

    /**
     * @param indexName the index name
     * @param indexPath the path of the json index file
     * @return true if the index was created
     */
    default Mono<Boolean> createIndexWithSettingsAndMapping(String indexName, String indexPath) {
        return this.createIndexWithSettingsAndMapping(null, indexName, indexPath);
    }

    Mono<Boolean> createIndexWithSettingsAndMapping(Alias alias, String indexName, String indexPath);

    /**
     *
     * @param aliasWriter
     * @param indexName
     * @param indexPath
     * @return
     */
    default Mono<Boolean> createRolloverIndexWithSettingsAndMapping(Alias aliasWriter, String indexName, String indexPath) {
        return createRolloverIndexWithSettingsAndMapping(null, aliasWriter, indexName, indexPath);
    }

    /**
     *
     * @param aliasReader
     * @param aliasWriter
     * @param indexName
     * @param indexPath
     * @return
     */
    Mono<Boolean> createRolloverIndexWithSettingsAndMapping(Alias aliasReader, Alias aliasWriter, String indexName, String indexPath);

    /**
     * @param aliasName
     * @param newIndexName
     * @param indexPath
     * @param conditions
     * @return
     */
    Mono<Boolean> rolloverIndex(String aliasName, @Nullable String newIndexName, @Nullable String indexPath, RolloverConfig.RolloverConditions conditions);

    /**
     *
     * @param entityClass
     * @param <T>
     * @return
     */
    <T> Mono<Boolean> rolloverIndex(Class<T> entityClass);

    /**
     * Delete index
     *
     * @param indexName index name
     * @return true if the index was deleted
     */
    Mono<Boolean> deleteIndexByName(String indexName);

    /**
     * Delete all indices for a aliasOrIndex
     *
     * @param aliasName he given aliasName.
     */
    Mono<Boolean> deleteIndexByAlias(String aliasName);

    /**
     * refresh the elasticsearch index for the given clazz
     *
     * @param clazz     the given clazz.
     * @param <T>       method generic.
     */
    <T> Mono<Void> refresh(Class<T> clazz);

    /**
     * @param indexName the index name
     */
    Mono<Void> refresh(String indexName);

    //***************************************
    // Index / continueScroll operations
    //***************************************

    /**
     * Index the given T entity, for the geiven clazz.
     *
     * @param entity the given entity.
     * @param clazz  the gievn {@link Class}.
     * @param <T>    generic method
     * @return T the indexed entity.
     */
    <T> Mono<T> index(T entity, Class<T> clazz);

    /**
     * Bulk index operation for the given {@link List} of entities, and gievn {@link Class}.
     *
     * @param entities the given entities {@link List}.
     * @param clazz    the given {@link Class}.
     * @param <T>      the {@link List} of indexed entities.
     * @return documents indexed
     */
    default <T> Flux<T> bulkIndex(List<T> entities, Class<T> clazz) {
        return bulkIndex(Flux.fromIterable(entities), clazz);
    }

    /**
     * Bulk index operation for the given {@link List} of entities, and gievn {@link Class}.
     *
     * @param publisher the given entities {@link List}.
     * @param clazz    the given {@link Class}.
     * @param <T>      the {@link List} of indexed entities.
     * @return documents indexed
     */
    <T> Flux<T> bulkIndex(Flux<T> publisher, Class<T> clazz);

    /**
     * @param entities  all entities to index
     * @return the entities indexed
     */
    default Flux<?> bulkIndex(List<?> entities) {
        return bulkIndex(Flux.fromIterable(entities));
    }

    /**
     *
     * @param publisher
     * @return
     */
    Flux<?> bulkIndex(Flux<?> publisher);

    /**
     * Find an elasticsearch document for the given clazz, and documentId.
     *
     * @param documentId the given documentId.
     * @param clazz      the given clazz.
     * @param <T>        the document
     * @return the entity for the given documentId or null.
     */
    <T> Mono<T> findById(String documentId, Class<T> clazz);

    <T> Mono<T> findOne(CriteriaQuery query, Class<T> clazz);

    <T> Mono<T> findOne(SearchQuery query, Class<T> clazz);

    <T> Mono<T> findOne(StringQuery query, Class<T> clazz);


    /**
     * @param query
     * @param clazz
     * @param <T>
     * @return
     */
    <T> Mono<Long> count(SearchQuery query, Class<T> clazz);

    /**
     * @param query
     * @param clazz
     * @param <T>
     * @return
     */
    <T> Mono<Long> count(CriteriaQuery query, Class<T> clazz);

    /**
     * @param query
     * @param clazz
     * @param <T>
     * @return
     */
    <T> Mono<Long> count(StringQuery query, Class<T> clazz);

    /**
     * @param clazz         the domain type
     * @param documentId    the document id.
     * @param <T>           method generic.
     * @return true if the document corresponding to the id exists
     */
    <T> Mono<Boolean> existsById(Class<T> clazz, String documentId);

    /**
     * @param query
     * @param javaType
     * @param <T>
     * @return
     */
    <T> Mono<Boolean> existsByQuery(CriteriaQuery query, Class<T> javaType);

    <T> Mono<Boolean> existsByQuery(SearchQuery query, Class<T> javaType);

    <T> Mono<Boolean> existsByQuery(StringQuery query, Class<T> javaType);


    /**
     * Delete all the documents for the given clazz
     *
     * @param clazz the given clazz.
     * @param <T>   method generic.
     */
    <T> Mono<Void> deleteAll(Class<T> clazz);

    /**
     * Delete all the {@link List} of entities, for the given clazz.
     *
     * @param entities the {@link List} of entities.
     * @param clazz    the given clazz.
     * @param <T>      method generic.
     */
    <T> Mono<Void> deleteAll(Collection<T> entities, Class<T> clazz);

    /**
     * delete the document for the given entity, and clazz
     *
     * @param entity the given entity.
     * @param clazz  the given clazz.
     * @param <T>    method generic.
     */
    <T> Mono<Void> delete(T entity, Class<T> clazz);

    /**
     * delete the document for the given entity, and clazz
     *
     * @param query the given query.
     * @param clazz the given clazz.
     * @param <T>   method generic.
     */
    <T> Mono<Void> delete(CriteriaQuery query, Class<T> clazz);

    /**
     * delete the document with the given documentId and clazz.
     *
     * @param documentId the given documentId.
     * @param clazz      the given clazz.
     * @param <T>        method generic.
     */
    <T> Mono<Void> deleteById(String documentId, Class<T> clazz);

    /**
     * Search with the given {@link SearchRequest} continueScroll, and given {@link Class} clazz.
     *
     * @param search the given {@link SearchRequest} instance.
     * @param clazz  the given clazz.
     * @param <T>    generic method.
     * @return a {@link List} of the method generic type.
     */
    <S extends T, T> Flux<S> search(SearchQuery search, Class<T> clazz);

    /**
     * @param search
     * @param clazz
     * @param <T>
     * @return
     */
    <S extends T, T> Flux<S> search(CriteriaQuery search, Class<T> clazz);

    /**
     * @param stringQuery
     * @param clazz
     * @param <T>
     * @return
     */
    <S extends T, T> Flux<S> search(StringQuery stringQuery, Class<T> clazz);

    /**
     * @param query
     * @param resultsExtractor
     * @param <T>
     * @return
     */
    <T> Flux<T> search(SearchQuery query, ResultsExtractor<Flux<T>> resultsExtractor);

//    /**
//     * @param suggestion
//     * @param indices
//     * @return
//     */
//    Mono<SearchResponse> suggest(SuggestBuilder suggestion, String... indices);
//
//    /**
//     * @param suggestion
//     * @param indices
//     * @param extractor
//     * @param <R>
//     * @return
//     */
//    <R> Flux<R> suggest(SuggestBuilder suggestion, String[] indices, ResultsExtractor<R> extractor);
//
//    /**
//     * @param suggestion
//     * @param clazz
//     * @param extractor
//     * @param <R>
//     * @param <T>
//     * @return
//     */
//    <R, T> Flux<R> suggest(SuggestBuilder suggestion, Class<T> clazz, ResultsExtractor<R> extractor);

    /**
     * Callback interface to be used with {@link #execute(ClientCallback)} for operating directly on
     * {@link ReactiveRestElasticsearchClient}.
     *
     * @param <T>
     * @author Christoph Strobl
     * @since 1.0.0
     */
    interface ClientCallback<T extends Publisher<?>> {

        T doWithClient(ReactiveRestElasticsearchClient client);
    }

}
