package com.github.ydespreaux.spring.data.elasticsearch.repository.support;

import com.github.ydespreaux.spring.data.elasticsearch.core.CriteriaQueryProcessor;
import com.github.ydespreaux.spring.data.elasticsearch.core.ReactiveElasticsearchOperations;
import com.github.ydespreaux.spring.data.elasticsearch.core.completion.reactive.ReactiveEntitySuggestExtractor;
import com.github.ydespreaux.spring.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.*;
import com.github.ydespreaux.spring.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

public abstract class AbstractReactiveElasticsearchRepository<T, K> extends AbstractElasticsearchRepositorySupport<T, K> implements ReactiveElasticsearchRepository<T, K> {

    protected final ReactiveElasticsearchOperations reactiveElasticsearchOperations;

    public AbstractReactiveElasticsearchRepository(ReactiveElasticsearchOperations reactiveElasticsearchOperations) {
        this.reactiveElasticsearchOperations = reactiveElasticsearchOperations;
    }

    /**
     * @param metadata                        the entity metadata
     * @param reactiveElasticsearchOperations a {@link ReactiveElasticsearchOperations}
     */
    public AbstractReactiveElasticsearchRepository(ElasticsearchEntityInformation<T, K> metadata, ReactiveElasticsearchOperations reactiveElasticsearchOperations) {
        super(metadata);
        //
        this.reactiveElasticsearchOperations = reactiveElasticsearchOperations;
        this.reactiveElasticsearchOperations.createIndex(getEntityClass()).subscribe();
    }

    /**
     * Retrieves an entity by its id.
     *
     * @param id must not be {@literal null}.
     * @return the entity with the given id or {@literal Optional#empty()} if none found
     * @throws IllegalArgumentException if {@code id} is {@literal null}.
     */
    @Override
    public Mono<T> findById(K id) {
        assertNotNullId(id);
        return reactiveElasticsearchOperations.findById(stringIdRepresentation(id), getEntityClass());
    }

    /**
     * @param id the identifier
     * @return true if the document exists
     */
    @Override
    public Mono<Boolean> existsById(K id) {
        assertNotNullId(id);
        return reactiveElasticsearchOperations.existsById(getEntityClass(), stringIdRepresentation(id));

    }

    /**
     * @return
     */
    @Override
    public Mono<Long> count() {
        return count(QueryBuilders.matchAllQuery());
    }

    /**
     * @param query
     * @return
     */
    @Override
    public Mono<Long> count(QueryBuilder query) {
        NativeSearchQuery.NativeSearchQueryBuilder builder = new NativeSearchQuery.NativeSearchQueryBuilder()
                .withQuery(query);
        return this.reactiveElasticsearchOperations.count(builder.build(), this.getEntityClass());
    }

    /**
     * @param criteria
     * @return
     */
    @Override
    public Mono<Long> count(Criteria criteria) {
        return this.reactiveElasticsearchOperations.count(new CriteriaQuery(criteria), this.getEntityClass());
    }

    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     *
     * @param entity must not be {@literal null}.
     * @return the saved entity will never be {@literal null}.
     */
    @Override
    public Mono<T> save(T entity) {
        Assert.notNull(entity, "Cannot save 'null' entity.");
        return this.reactiveElasticsearchOperations.index(entity, getEntityClass());
    }

    /**
     * Saves all given entities.
     *
     * @param entities must not be {@literal null}.
     * @return the saved entities will never be {@literal null}.
     * @throws IllegalArgumentException in case the given entity is {@literal null}.
     */
    @Override
    public Flux<T> save(List<T> entities) {
        Assert.notNull(entities, "entities cannot be null");
        return save(Flux.fromIterable(entities));
    }

    /**
     * @param entities
     * @return
     */
    @Override
    public Flux<T> save(Flux<T> entities) {
        Assert.notNull(entities, "entities cannot be null");
        return this.reactiveElasticsearchOperations.bulkIndex(entities, getEntityClass());
    }

    /**
     * Deletes the entity with the given id.
     *
     * @param id must not be {@literal null}.
     * @throws IllegalArgumentException in case the given {@code id} is {@literal null}
     */
    @Override
    public Mono<Void> deleteById(K id) {
        assertNotNullId(id);
        return this.reactiveElasticsearchOperations.deleteById(stringIdRepresentation(id), getEntityClass());
    }

    /**
     * Deletes a given entity.
     *
     * @param entity the entity
     * @throws IllegalArgumentException in case the given entity is {@literal null}.
     */
    @Override
    public Mono<Void> delete(T entity) {
        Assert.notNull(entity, "Cannot delete 'null' entity.");
        return this.reactiveElasticsearchOperations.delete(entity, getEntityClass());
    }

    /**
     * Deletes the given entities.
     *
     * @param entities the entities
     * @throws IllegalArgumentException in case the given {@link Iterable} is {@literal null}.
     */
    @Override
    public Mono<Void> deleteAll(Collection<T> entities) {
        Assert.notNull(entities, "Cannot delete 'null' as a List.");
        Assert.notEmpty(entities, "Cannot delete empty List.");
        return deleteAll(Flux.fromIterable(entities));
    }

    @Override
    public Mono<Void> deleteAll(Flux<T> entities) {
        Assert.notNull(entities, "entities cannot be null.");
        return this.reactiveElasticsearchOperations.deleteAll(entities, getEntityClass());
    }

    /**
     * Deletes all entities managed by the repository.
     */
    @Override
    public Mono<Void> deleteAll() {
        return this.reactiveElasticsearchOperations.deleteAll(getEntityClass());
    }

    /**
     *
     */
    @Override
    public Mono<Void> refresh() {
        return this.reactiveElasticsearchOperations.refresh(getEntityClass());
    }

    /**
     * @return
     */
    @Override
    public Flux<T> findAll() {
        return this.findByQuery(QueryBuilders.matchAllQuery(), Sort.unsorted());
    }

    /**
     * @param query the query
     * @param sort  the sort
     * @return items for the query
     */
    @Override
    public <S extends T> Flux<S> findByQuery(QueryBuilder query, @Nullable Sort sort) {
        return findByQuery(query, sort, this.getEntityClass());
    }

    /**
     * @param criteria the query
     * @param sort     the sort
     * @return items for the query
     */
    @Override
    public <S extends T> Flux<S> findByQuery(Criteria criteria, @Nullable Sort sort) {
        return findByQuery(criteria, sort, this.getEntityClass());
    }

    /**
     * @param query
     * @param sort
     * @param domainClass
     * @return
     */
    @Override
    public <S extends D, D> Flux<S> findByQuery(QueryBuilder query, @Nullable Sort sort, Class<D> domainClass) {
        NativeSearchQuery.NativeSearchQueryBuilder queryBuilder = new NativeSearchQuery.NativeSearchQueryBuilder().withQuery(query);
        if (sort != null) {
            sort.forEach(order -> queryBuilder.withSort(SortBuilders.fieldSort(order.getProperty()).order(order.isAscending() ? SortOrder.ASC : SortOrder.DESC)));
        }
        return this.reactiveElasticsearchOperations.search(queryBuilder.build(), domainClass);
    }

    /**
     * @param criteria
     * @param sort
     * @param domainClass
     * @return
     */
    @Override
    public <S extends D, D> Flux<S> findByQuery(Criteria criteria, @Nullable Sort sort, Class<D> domainClass) {
        CriteriaQuery query = new CriteriaQuery(criteria);
        if (sort != null) {
            query.addSort(sort);
        }
        return this.reactiveElasticsearchOperations.search(query, domainClass);
    }

    /**
     * @param query
     * @return
     */
    @Override
    public Flux<T> suggest(String query) {
        return this.suggest(query, getEntityClass());
    }

    /**
     * @param query
     * @param domainClass
     * @return
     */
    @Override
    public <D> Flux<D> suggest(String query, Class<D> domainClass) {
        ElasticsearchPersistentEntity<T> persistentEntity = this.reactiveElasticsearchOperations.getPersistentEntityFor(getEntityClass());
        if (!persistentEntity.hasCompletionProperty()) {
            throw new InvalidDataAccessApiUsageException("No completion property field defined");
        }
        SuggestionBuilder completionSuggestionFuzzyBuilder = SuggestBuilders.completionSuggestion(persistentEntity.getCompletionProperty().getFieldName()).prefix(query, Fuzziness.AUTO);
        return reactiveElasticsearchOperations.suggest(
                new SuggestQuery(new SuggestBuilder().addSuggestion("suggest-" + domainClass.getName(), completionSuggestionFuzzyBuilder)),
                domainClass,
                new ReactiveEntitySuggestExtractor<>(domainClass, this.reactiveElasticsearchOperations.getResultsMapper()));
    }

    /**
     * Returns all parents
     *
     * @return
     */
    @Override
    public Flux hasChild() {
        return hasChildByQuery(QueryBuilders.matchAllQuery());
    }

    /**
     * Returns parent documents which associated children have matched with query.
     *
     * @param query
     * @return
     */
    @Override
    public Flux hasChildByQuery(QueryBuilder query) {
        assertChildDocument();
        return this.reactiveElasticsearchOperations.hasChild(
                HasChildQuery.builder()
                        .type(this.entityInformation.getJoinDescriptor().getType())
                        .query(query)
                        .scoreMode(ScoreMode.None)
                        .build(), this.getEntityClass());
    }

    /**
     * Returns parent documents which associated children have matched with query.
     *
     * @param criteria
     * @return
     */
    @Override
    public Flux hasChildByQuery(Criteria criteria) {
        return hasChildByQuery(new CriteriaQueryProcessor().createQueryFromCriteria(criteria).orElseThrow());
    }

    /**
     * Returns all children
     *
     * @return
     */
    @Override
    public <S extends T> Flux<S> hasParent() {
        return hasParentByQuery(QueryBuilders.matchAllQuery());
    }

    /**
     * Returns child documents which associated parents have matched with query.
     *
     * @param query
     * @return
     */
    @Override
    public <S extends T> Flux<S> hasParentByQuery(QueryBuilder query) {
        assertParentDocument();
        return this.reactiveElasticsearchOperations.hasParent(HasParentQuery.builder().type(this.entityInformation.getJoinDescriptor().getType()).query(query).build(), this.getEntityClass());
    }

    /**
     * Returns child documents which associated parents have matched with query.
     *
     * @param criteria
     * @return
     */
    @Override
    public <S extends T> Flux<S> hasParentByQuery(Criteria criteria) {
        return hasParentByQuery(new CriteriaQueryProcessor().createQueryFromCriteria(criteria).orElseThrow());
    }

    /**
     * Return child documents which associated parent id
     *
     * @param parentId
     * @return
     */
    @Override
    public Flux<T> hasParentId(String parentId) {
        return hasParentId(parentId, (QueryBuilder) null);
    }

    /**
     * Return child documents which associated parent id and children have matched with query.
     *
     * @param parentId
     * @param criteria
     * @return
     */
    @Override
    public Flux<T> hasParentId(String parentId, Criteria criteria) {
        return hasParentId(parentId, new CriteriaQueryProcessor().createQueryFromCriteria(criteria).orElseThrow());
    }

    /**
     * Return child documents which associated parent id and children have matched with query.
     *
     * @param parentId
     * @param query
     * @return
     */
    @Override
    public Flux<T> hasParentId(String parentId, QueryBuilder query) {
        assertChildDocument();
        return this.reactiveElasticsearchOperations.hasParentId(
                ParentIdQuery.builder().type(this.entityInformation.getJoinDescriptor().getType()).parentId(parentId).query(query).build(), this.getEntityClass());
    }
}
