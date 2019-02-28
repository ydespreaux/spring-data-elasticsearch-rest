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
 *
 */

package com.github.ydespreaux.spring.data.elasticsearch.core;

import com.github.ydespreaux.spring.data.elasticsearch.annotations.IndexedDocument;
import com.github.ydespreaux.spring.data.elasticsearch.annotations.ProjectionDocument;
import com.github.ydespreaux.spring.data.elasticsearch.annotations.RolloverDocument;
import com.github.ydespreaux.spring.data.elasticsearch.core.converter.ElasticsearchConverter;
import com.github.ydespreaux.spring.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.*;
import com.github.ydespreaux.spring.data.elasticsearch.core.request.RequestsBuilder;
import com.github.ydespreaux.spring.data.elasticsearch.core.triggers.TriggerManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.join.query.HasChildQueryBuilder;
import org.elasticsearch.join.query.HasParentQueryBuilder;
import org.elasticsearch.join.query.JoinQueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.elasticsearch.index.query.QueryBuilders.wrapperQuery;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * @author Yoann Despréaux
 * @since 1.0.0
 */
@Slf4j
public abstract class ElasticsearchTemplateSupport implements ApplicationContextAware, InitializingBean {

    /**
     * {@link ElasticsearchConverter} property.
     */
    @Getter
    private final ElasticsearchConverter elasticsearchConverter;

    /**
     *
     */
    @Getter
    private final ResultsMapper resultsMapper;

    @Getter
    private final TriggerManager triggerManager;

    private RequestsBuilder requestsBuilder;

    /**
     * The used {@link ApplicationContext}
     */
    private ApplicationContext applicationContext;

    /**
     * Construct an instance with the given client and elasticsearchConverter parameters.
     *
     * @param elasticsearchConverter the given elasticsearchConverter.
     * @param resultsMapper          the given result mapper
     */
    public ElasticsearchTemplateSupport(final ElasticsearchConverter elasticsearchConverter,
                                        final ResultsMapper resultsMapper,
                                        final TriggerManager triggerManager) {
        this.elasticsearchConverter = elasticsearchConverter;
        this.resultsMapper = resultsMapper;
        this.triggerManager = triggerManager;
    }

    /**
     * @param values the list to transfor to array
     * @return
     */
    private static String[] toArray(List<String> values) {
        String[] valuesAsArray = new String[values.size()];
        return values.toArray(valuesAsArray);
    }

    /**
     * @return
     */
    protected RequestsBuilder requestsBuilder() {
        return this.requestsBuilder;
    }

    /**
     * @see ApplicationContextAware#setApplicationContext(ApplicationContext)  method
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.requestsBuilder = new RequestsBuilder(this.applicationContext);
        if (elasticsearchConverter instanceof ApplicationContextAware) {
            ((ApplicationContextAware) elasticsearchConverter).setApplicationContext(applicationContext);
        }
    }

    @Override
    public void afterPropertiesSet() {
    }


    /**
     * @param clazz the entity class
     * @param <T>   generic type
     * @return the {@link ElasticsearchPersistentEntity}
     * @see ElasticsearchOperations#getPersistentEntityFor(Class)  method
     */
    public <T> ElasticsearchPersistentEntity<T> getPersistentEntityFor(Class<T> clazz) {
        Assert.isTrue(clazz.isAnnotationPresent(IndexedDocument.class)
                || clazz.isAnnotationPresent(RolloverDocument.class)
                || clazz.isAnnotationPresent(ProjectionDocument.class), "Unable to identify document. " + clazz.getSimpleName()
                + " is not a elasticsearch document. Make sure the document class is annotated with @Document or @DocumentRollover or @Projection");
        return getElasticsearchConverter().getRequiredPersistentEntity(clazz);
    }

    /**
     * @param locationPath the resource path
     * @return the {@link Resource}
     */
    protected Resource getResource(String locationPath) {
        return this.applicationContext.getResource(locationPath);
    }

    /**
     * @param response
     */
    protected void checkForBulkUpdateFailure(BulkResponse response) {
        if (response.hasFailures()) {
            Map<String, String> failedDocuments = new HashMap<>();
            for (BulkItemResponse item : response.getItems()) {
                if (item.isFailed())
                    failedDocuments.put(item.getId(), item.getFailureMessage());
            }
            throw new ElasticsearchException(
                    "Bulk indexing has failures. Use ElasticsearchException.getFailedDocuments() for detailed messages ["
                            + failedDocuments + "]",
                    failedDocuments);
        }
    }


    /**
     * @param query
     * @param clazz
     * @param <T>
     * @return
     */
    protected <T> SearchRequest prepareSearch(Query query, Class<T> clazz) {
        setPersistentEntityIndexAndTypeAndSourceFilter(query, clazz);
        return prepareSearch(query, Optional.empty());
    }

    /**
     * @param query
     * @param clazz
     * @param <T>
     * @return
     */
    protected <T> SearchRequest prepareSearch(SearchQuery query, Class<T> clazz) {
        setPersistentEntityIndexAndTypeAndSourceFilter(query, clazz);
        return prepareSearch(query, Optional.ofNullable(query.getQuery()));
    }

    /**
     * @param query
     * @param builder
     * @return
     */
    protected SearchRequest prepareSearch(Query query, Optional<QueryBuilder> builder) {
        assertNotNullIndices(query);
        assertNotNullTypes(query);

        int startRecord = 0;
        SearchRequest request = new SearchRequest(toArray(query.getIndices()));
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        request.types(toArray(query.getTypes()));
        sourceBuilder.version(true);
        sourceBuilder.trackScores(query.getTrackScores());

        if (builder.isPresent()) {
            sourceBuilder.query(builder.get());
        }

        if (query.getSourceFilter() != null) {
            SourceFilter sourceFilter = query.getSourceFilter();
            sourceBuilder.fetchSource(sourceFilter.getIncludes(), sourceFilter.getExcludes());
        }

        if (query.getPageable().isPaged()) {
            startRecord = query.getPageable().getPageNumber() * query.getPageable().getPageSize();
            sourceBuilder.size(query.getPageable().getPageSize());
        }
        sourceBuilder.from(startRecord);

        if (!query.getFields().isEmpty()) {
            sourceBuilder.fetchSource(toArray(query.getFields()), null);
        }

        if (query.getIndicesOptions() != null) {
            request.indicesOptions(query.getIndicesOptions());
        }
        doSort(query.getSort(), sourceBuilder);
        if (query.getMinScore() > 0) {
            sourceBuilder.minScore(query.getMinScore());
        }
        request.source(sourceBuilder);
        return request;
    }

    /**
     * @param searchRequest
     * @param searchQuery
     * @return
     */
    protected SearchRequest doSearch(SearchRequest searchRequest, SearchQuery searchQuery) {
        if (searchQuery.getFilter() != null) {
            searchRequest.source().postFilter(searchQuery.getFilter());
        }

        if (searchQuery.getElasticsearchSorts() != null) {
            searchQuery.getElasticsearchSorts().forEach(searchRequest.source()::sort);
        }
        if (searchQuery.hasHighlight()) {
            searchRequest.source().highlighter(searchQuery.buildHighlight());
        }
        if (searchQuery.getIndicesBoost() != null) {
            searchQuery.getIndicesBoost().forEach(indexBoost -> searchRequest.source().indexBoost(indexBoost.getIndexName(), indexBoost.getBoost()));
        }
        if (searchQuery.getAggregations() != null) {
            searchQuery.getAggregations().forEach(searchRequest.source()::aggregation);
        }
        return searchRequest;
    }

    /**
     * @param request
     * @param criteriaQuery
     * @return
     */
    protected SearchRequest doSearch(SearchRequest request, CriteriaQuery criteriaQuery) {
        QueryBuilder query = new CriteriaQueryProcessor().createQueryFromCriteria(criteriaQuery.getCriteria());
        QueryBuilder filter = new CriteriaFilterProcessor()
                .createFilterFromCriteria(criteriaQuery.getCriteria());
        if (query != null) {
            request.source().query(query);
        } else {
            request.source().query(QueryBuilders.matchAllQuery());
        }
        if (criteriaQuery.getSort() != null) {
            criteriaQuery.getSort().forEach(order -> request.source().sort(order.getProperty(), order.getDirection() == Sort.Direction.ASC ? SortOrder.ASC : SortOrder.DESC));
        }
        if (criteriaQuery.getMinScore() > 0) {
            request.source().minScore(criteriaQuery.getMinScore());
        }

        if (filter != null)
            request.source().postFilter(filter);
        return request;
    }

    /**
     * @param request
     * @param stringQuery
     * @return
     */
    protected SearchRequest doSearch(SearchRequest request, StringQuery stringQuery) {
        request.source().query((wrapperQuery(stringQuery.getSource())));
        return request;
    }

    /**
     * @param query
     * @param scrollTime
     * @param clazz
     * @param <T>
     * @return
     */
    protected <T> SearchRequest prepareScroll(Query query, Duration scrollTime, Class<T> clazz) {
        setPersistentEntityIndexAndTypeAndSourceFilter(query, clazz);
        return prepareScroll(query, scrollTime);
    }

    /**
     * @param query
     * @param scrollTime
     * @return
     */
    protected SearchRequest prepareScroll(Query query, Duration scrollTime) {
        assertNotNullIndices(query);
        assertNotNullTypes(query);
        assertNotNullPageable(query);

        SearchRequest request = new SearchRequest(toArray(query.getIndices()));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        request.types(toArray(query.getTypes()));
        request.scroll(TimeValue.timeValueMillis(scrollTime.toMillis()));

        if (query.getPageable().isPaged()) {
            searchSourceBuilder.size(query.getPageable().getPageSize());
        }
        doSort(query.getSort(), searchSourceBuilder);

        if (!isEmpty(query.getFields())) {
            searchSourceBuilder.fetchSource(toArray(query.getFields()), null);
        }
        request.source(searchSourceBuilder);
        return request;
    }

    /**
     * @param request
     * @param query
     * @return
     */
    protected SearchRequest doScroll(SearchRequest request, SearchQuery query) {
        assertNotNullIndices(query);
        assertNotNullTypes(query);
        assertNotNullPageable(query);

        if (query.getQuery() != null) {
            request.source().query(query.getQuery());
        }
        if (query.getFilter() != null) {
            request.source().postFilter(query.getFilter());
        }
        request.source().version(true);
        return request;
    }

    /**
     * @param request
     * @param query
     * @return
     */
    protected SearchRequest doScroll(SearchRequest request, CriteriaQuery query) {
        assertNotNullIndices(query);
        assertNotNullTypes(query);
        assertNotNullPageable(query);

        QueryBuilder elasticsearchQuery = new CriteriaQueryProcessor().createQueryFromCriteria(query.getCriteria());
        QueryBuilder elasticsearchFilter = new CriteriaFilterProcessor().createFilterFromCriteria(query.getCriteria());

        if (elasticsearchQuery != null) {
            request.source().query(elasticsearchQuery);
        } else {
            request.source().query(QueryBuilders.matchAllQuery());
        }

        if (elasticsearchFilter != null) {
            request.source().postFilter(elasticsearchFilter);
        }
        request.source().version(true);
        return request;
    }

    /**
     * @param request
     * @param stringQuery
     * @return
     */
    protected SearchRequest doScroll(SearchRequest request, StringQuery stringQuery) {
        request.source().query(wrapperQuery(stringQuery.getSource()));
        return request;
    }

    /**
     * @param query
     * @param clazz
     * @param <T>
     * @return
     */
    protected <T> SearchRequest prepareCount(Query query, Class<T> clazz) {
        setPersistentEntityIndexAndTypeAndSourceFilter(query, clazz);
        return prepareCount(query);
    }

    /**
     * @param query
     * @return
     */
    protected SearchRequest prepareCount(Query query) {
        assertNotNullIndices(query);
        SearchRequest countRequestBuilder = new SearchRequest(toArray(query.getIndices()));
        if (!isEmpty(query.getTypes())) {
            countRequestBuilder.types(toArray(query.getTypes()));
        }
        return countRequestBuilder;
    }

    /**
     * @param searchRequest
     * @param query
     * @return
     */
    protected SearchRequest doCount(SearchRequest searchRequest, SearchQuery query) {
        return doCount(searchRequest, query.getQuery(), query.getFilter());
    }

    /**
     * @param searchRequest
     * @param criteriaQuery
     * @return
     */
    protected SearchRequest doCount(SearchRequest searchRequest, CriteriaQuery criteriaQuery) {
        QueryBuilder query = new CriteriaQueryProcessor().createQueryFromCriteria(criteriaQuery.getCriteria());
        QueryBuilder filter = new CriteriaFilterProcessor()
                .createFilterFromCriteria(criteriaQuery.getCriteria());
        return doCount(searchRequest, query, filter);
    }

    /**
     * @param searchRequest
     * @param elasticsearchQuery
     * @param elasticsearchFilter
     * @return
     */
    private SearchRequest doCount(SearchRequest searchRequest, QueryBuilder elasticsearchQuery, QueryBuilder elasticsearchFilter) {
        if (elasticsearchQuery != null) {
            searchRequest.source().query(elasticsearchQuery);
        } else {
            searchRequest.source().query(QueryBuilders.matchAllQuery());
        }
        if (elasticsearchFilter != null) {
            searchRequest.source().postFilter(elasticsearchFilter);
        }
        return searchRequest;
    }

    /**
     *
     * @param query
     * @param clazz
     * @param <T>
     * @return
     */
    protected <T> SearchRequest prepareSuggest(SuggestQuery query, Class<T> clazz) {
        setPersistentEntityIndexAndTypeAndSourceFilter(query, clazz);
        return prepareSuggest(query);
    }

    /**
     * @param query
     * @return
     */
    protected SearchRequest prepareSuggest(SuggestQuery query) {
        assertNotNullIndices(query);
        assertNotNullTypes(query);

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                .suggest(query.getSuggestion());
        if (query.getSourceFilter() != null) {
            SourceFilter sourceFilter = query.getSourceFilter();
            sourceBuilder.fetchSource(sourceFilter.getIncludes(), sourceFilter.getExcludes());
        }
        return new SearchRequest(toArray(query.getIndices()))
                .types(toArray(query.getTypes()))
                .source(sourceBuilder);
    }


    protected <T> SearchQuery prepareHasChildQuery(HasChildQuery query, Class<T> clazz) {
        assertNotNull(query);
        setPersistentEntityJoinType(query, clazz);
        return prepareHasChildQuery(query);
    }

    protected SearchQuery prepareHasChildQuery(HasChildQuery query) {
        assertNotNull(query);
        HasChildQueryBuilder childQueryBuilder = JoinQueryBuilders.hasChildQuery(query.getType(), query.getQuery(), query.getScoreMode())
                .ignoreUnmapped(query.isIgnoreUnmapped())
                .minMaxChildren(query.getMinChildren(), query.getMaxChildren());
        if (query.getInnerHitBuilder() != null) {
            childQueryBuilder.innerHit(query.getInnerHitBuilder());
        }
        return new NativeSearchQuery(childQueryBuilder);
    }

    protected <T> SearchQuery prepareHasParentQuery(HasParentQuery query, Class<T> entityClass) {
        assertNotNull(query);
        setPersistentEntityJoinType(query, entityClass);
        return prepareHasParentQuery(query);
    }

    protected SearchQuery prepareHasParentQuery(HasParentQuery query) {
        assertNotNull(query);
        HasParentQueryBuilder parentQueryBuilder = JoinQueryBuilders.hasParentQuery(query.getType(), query.getQuery(), false)
                .ignoreUnmapped(query.isIgnoreUnmapped());
        if (query.getInnerHitBuilder() != null) {
            parentQueryBuilder.innerHit(query.getInnerHitBuilder());
        }
        return new NativeSearchQuery(parentQueryBuilder);
    }

    protected <T> SearchQuery prepareHasParentId(ParentIdQuery query, Class<T> entityClass) {
        assertNotNull(query);
        setPersistentEntityJoinType(query, entityClass);
        return prepareHasParentId(query);
    }

    protected SearchQuery prepareHasParentId(ParentIdQuery query) {
        assertNotNull(query);
        QueryBuilder completedQuery = null;
        QueryBuilder parentIdQueryBuilder = JoinQueryBuilders.parentId(query.getType(), query.getParentId()).ignoreUnmapped(query.isIgnoreUnmapped());
        QueryBuilder originalQuery = query.getQuery();
        if (originalQuery != null) {
            if (originalQuery instanceof BoolQueryBuilder) {
                completedQuery = ((BoolQueryBuilder) originalQuery).must(parentIdQueryBuilder);
            } else {
                completedQuery = QueryBuilders.boolQuery()
                        .must(originalQuery)
                        .must(parentIdQueryBuilder);
            }
        } else {
            completedQuery = parentIdQueryBuilder;
        }
        return new NativeSearchQuery(completedQuery);
    }

    private <T> void setPersistentEntityJoinType(HasChildQuery query, Class<T> clazz) {
        ElasticsearchPersistentEntity<T> persistentEntity = getPersistentEntityFor(clazz);
        assertChildDocument(persistentEntity);
        if (StringUtils.isEmpty(query.getType())) {
            query.setType(persistentEntity.getChildDescriptor().getType());
        }
    }

    private <T> void setPersistentEntityJoinType(HasParentQuery query, Class<T> entityClass) {
        ElasticsearchPersistentEntity<T> persistentEntity = getPersistentEntityFor(entityClass);
        assertParentDocument(persistentEntity);
        if (StringUtils.isEmpty(query.getType())) {
            query.setType(persistentEntity.getParentDescriptor().getType());
        }
    }

    private <T> void setPersistentEntityJoinType(ParentIdQuery query, Class<T> clazz) {
        ElasticsearchPersistentEntity<T> persistentEntity = getPersistentEntityFor(clazz);
        assertChildDocument(persistentEntity);
        if (StringUtils.isEmpty(query.getType())) {
            query.setType(persistentEntity.getChildDescriptor().getType());
        }
    }

    /**
     * @param sort
     * @param searchSourceBuilder
     */
    private void doSort(Sort sort, SearchSourceBuilder searchSourceBuilder) {
        if (sort != null) {
            for (Sort.Order order : sort) {
                FieldSortBuilder fieldSortBuilder = SortBuilders.fieldSort(order.getProperty())
                        .order(order.getDirection().isDescending() ? SortOrder.DESC : SortOrder.ASC);
                if (order.getNullHandling() == Sort.NullHandling.NULLS_FIRST) {
                    fieldSortBuilder.missing("_first");
                } else if (order.getNullHandling() == Sort.NullHandling.NULLS_LAST) {
                    fieldSortBuilder.missing("_last");
                }
                searchSourceBuilder.sort(fieldSortBuilder);
            }
        }
    }


    /**
     * @param query
     * @param clazz
     * @param <T>
     */
    protected <T> void setPersistentEntityIndexAndTypeAndSourceFilter(Query query, Class<T> clazz) {
        ElasticsearchPersistentEntity<T> persistentEntity = getPersistentEntityFor(clazz);
        if (query.getIndices().isEmpty()) {
            query.addIndices(persistentEntity.getAliasOrIndexReader());
        }
        if (query.getTypes().isEmpty()) {
            query.addTypes(persistentEntity.getTypeName());
        }
        if (query.getSourceFilter() == null && persistentEntity.hasSourceFiler()) {
            query.addSourceFilter(persistentEntity.getSourceFilter());
        }
    }

    /**
     * @param indexName
     * @return
     */
    protected String generateRolloverIndexName(String indexName) {
        return MessageFormat.format("{0}-{1,number,000000}", indexName, 1);
    }


    private void assertNotNullIndices(Query query) {
        Assert.notNull(query.getIndices(), "No index defined for Query");
    }

    private void assertNotNullTypes(Query query) {
        Assert.notNull(query.getTypes(), "No type defined for Query");
    }

    private void assertNotNullPageable(Query query) {
        Assert.notNull(query.getPageable(), "Query.pageable is required for scan & scroll");
    }

    private void assertNotNull(Query query) {
        Assert.notNull(query, "Query is required");
    }

    /**
     * @param e
     * @param request
     * @return
     */
    protected ElasticsearchException buildDeleteException(Exception e, DeleteIndexRequest request) {
        return new ElasticsearchException("Error for delete index request: " + request.toString(), e);
    }

    protected ElasticsearchException buildSearchException(Exception e, SearchRequest request) {
        return new ElasticsearchException("Error for continueScroll request: " + request.toString(), e);
    }

    protected ElasticsearchException buildClearScrollException(Exception e, ClearScrollRequest request) {
        return new ElasticsearchException("Error for clear scroll request: " + request.toString(), e);
    }

    protected <T> void assertChildDocument(ElasticsearchPersistentEntity<T> persistentEntity) {
        if (!persistentEntity.isChildDocument()) {
            throw new InvalidDataAccessApiUsageException("The document must be a child document !!!");
        }
    }

    protected <T> void assertParentDocument(ElasticsearchPersistentEntity<T> persistentEntity) {
        if (!persistentEntity.isParentDocument()) {
            throw new InvalidDataAccessApiUsageException("The document must be a parent document !!!");
        }
    }

}
