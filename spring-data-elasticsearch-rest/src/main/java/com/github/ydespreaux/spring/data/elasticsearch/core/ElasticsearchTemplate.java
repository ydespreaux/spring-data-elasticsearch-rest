/* * Copyright (C) 2018 Yoann Despréaux * * This program is free software; you can redistribute it and/or modify * it under the terms of the GNU General Public License as published by * the Free Software Foundation; either version 2 of the License, or * (at your option) any later version. * * This program is distributed in the hope that it will be useful, * but WITHOUT ANY WARRANTY; without even the implied warranty of * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the * GNU General Public License for more details. * * You should have received a copy of the GNU General Public License * along with this program; see the file COPYING . If not, write to the * Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA. * * Please send bugreports with examples or suggestions to yoann.despreaux@believeit.fr */package com.github.ydespreaux.spring.data.elasticsearch.core;import com.github.ydespreaux.spring.data.elasticsearch.client.RestElasticsearchClient;import com.github.ydespreaux.spring.data.elasticsearch.core.converter.ElasticsearchConverter;import com.github.ydespreaux.spring.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;import com.github.ydespreaux.spring.data.elasticsearch.core.query.*;import com.github.ydespreaux.spring.data.elasticsearch.core.request.config.RolloverConfig;import com.github.ydespreaux.spring.data.elasticsearch.core.scroll.ScrolledPage;import com.github.ydespreaux.spring.data.elasticsearch.core.scroll.ScrolledPageResult;import com.github.ydespreaux.spring.data.elasticsearch.core.triggers.TriggerManager;import lombok.extern.slf4j.Slf4j;import org.elasticsearch.ElasticsearchException;import org.elasticsearch.ElasticsearchStatusException;import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;import org.elasticsearch.action.admin.indices.get.GetIndexRequest;import org.elasticsearch.action.admin.indices.rollover.RolloverRequest;import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesRequest;import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesResponse;import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;import org.elasticsearch.action.bulk.BulkItemResponse;import org.elasticsearch.action.bulk.BulkRequest;import org.elasticsearch.action.bulk.BulkResponse;import org.elasticsearch.action.get.GetRequest;import org.elasticsearch.action.get.GetResponse;import org.elasticsearch.action.index.IndexRequest;import org.elasticsearch.action.index.IndexResponse;import org.elasticsearch.action.search.*;import org.elasticsearch.client.GetAliasesResponse;import org.elasticsearch.client.Request;import org.elasticsearch.client.RequestOptions;import org.elasticsearch.client.Response;import org.elasticsearch.cluster.metadata.AliasMetaData;import org.elasticsearch.common.Nullable;import org.elasticsearch.common.unit.TimeValue;import org.elasticsearch.index.query.QueryBuilder;import org.elasticsearch.index.query.QueryBuilders;import org.elasticsearch.rest.RestStatus;import org.elasticsearch.search.SearchHits;import org.elasticsearch.search.suggest.SuggestBuilder;import org.springframework.core.io.Resource;import org.springframework.data.domain.PageRequest;import org.springframework.util.Assert;import java.io.IOException;import java.time.Duration;import java.util.*;import static org.elasticsearch.client.Requests.refreshRequest;/** * Class define the Elastic Search operations. * * @author Yoann Despréaux * @since 1.0.0 */@Slf4jpublic class ElasticsearchTemplate extends ElasticsearchTemplateSupport implements ElasticsearchOperations {    /**     * RestHighLevelClient client.     */    private final RestElasticsearchClient client;    /**     * Construct an instance with the given client and elasticsearchConverter parameters.     *     * @param client                 the given client.     * @param elasticsearchConverter the given elasticsearchConverter.     * @param resultsMapper          the given result mapper     */    public ElasticsearchTemplate(final RestElasticsearchClient client,                                 final ElasticsearchConverter elasticsearchConverter,                                 final ResultsMapper resultsMapper,                                 final TriggerManager triggerManager) {        super(elasticsearchConverter, resultsMapper, triggerManager);        this.client = client;    }    /**     * @param request the request parameter     * @return the reponse of the result request     */    @Override    public Response performRequest(Request request) throws IOException {        return client.getRestHighLevelClient().getLowLevelClient().performRequest(request);    }    /**     * @see ElasticsearchOperations#createTemplate(String, String, Boolean)  method     */    @Override    public void createTemplate(String templateName, String location, Boolean createOnly) {        Resource resource = getResource(location);        if (!resource.exists()) {            throw new ElasticsearchException("File {} not found", location);        }        createTemplate(templateName, Collections.singletonList(resource), createOnly);    }    /**     * @see ElasticsearchOperations#createTemplate(String, String, Boolean)  method     */    @Override    public void createTemplate(String templateName, List<Resource> locations, Boolean createOnly) {        if (createOnly && templateExists(templateName)) {            return;        }        PutIndexTemplateRequest templateRequest = this.requestsBuilder().createPutIndexTemplateRequest(templateName, locations);        try {            this.client.putTemplate(templateRequest);            if (log.isDebugEnabled()) {                log.debug("New template {} added : {}", templateName, templateRequest.toString());            }        } catch (IOException e) {            throw new ElasticsearchException("Error for request: " + templateRequest.toString(), e);        }    }    /**     * @see ElasticsearchOperations#templateExists(String)  method     */    @Override    public boolean templateExists(String templateName) {        GetIndexTemplatesRequest request = new GetIndexTemplatesRequest(templateName);        try {            GetIndexTemplatesResponse response = this.client.getTemplates(request);            return !response.getIndexTemplates().isEmpty();        } catch (ElasticsearchStatusException e) {            if (e.status() == RestStatus.NOT_FOUND) {                return false;            }            throw new ElasticsearchException("Error for Get template request: " + request.toString(), e);        } catch (IOException e) {            throw new ElasticsearchException("Error for Get template request: " + request.toString(), e);        }    }    /**     * @see ElasticsearchOperations#deleteTemplate(String)  method     */    @Override    public void deleteTemplate(String templateName) {        if (templateExists(templateName)) {            Request request = this.requestsBuilder().deleteTemplateRequest(templateName);            try {                this.performRequest(request);                if (log.isDebugEnabled()) {                    log.debug("Deleted template {}", templateName);                }            } catch (IOException e) {                throw new ElasticsearchException("Error for delete template request: " + request.toString(), e);            }        }    }    /**     * @see ElasticsearchOperations#deleteIndexByName(String)   method     */    @Override    public boolean deleteIndexByName(String indexName) {        Objects.requireNonNull(indexName);        DeleteIndexRequest request = this.requestsBuilder().deleteIndexRequest(indexName);        try {            boolean deleted = this.client.deleteIndex(request).isAcknowledged();            if (log.isDebugEnabled() && deleted) {                log.debug("Deleted index {}", indexName);            }            return deleted;        } catch (ElasticsearchStatusException e) {            if (e.status() == RestStatus.NOT_FOUND) {                return false;            }            throw buildDeleteException(e, request);        } catch (IOException e) {            throw buildDeleteException(e, request);        }    }    /**     * @see ElasticsearchOperations#deleteIndexByAlias(String)    method     */    @Override    public void deleteIndexByAlias(String aliasName) {        Objects.requireNonNull(aliasName);        GetAliasesRequest request = new GetAliasesRequest(aliasName);        try {            Map<String, Set<AliasMetaData>> indices = this.client.getAlias(request).getAliases();            indices.keySet().forEach(this::deleteIndexByName);        } catch (IOException e) {            throw new ElasticsearchException("Error for get aliases request: " + request.toString(), e);        }    }    /**     * Check if Index Exist     *     * @param indexName the index name     * @return true if index exist     */    @Override    public boolean indexExists(String indexName) {        Objects.requireNonNull(indexName);        GetIndexRequest request = this.requestsBuilder().getIndexRequest(indexName);        try {            return client.indicesExist(request, RequestOptions.DEFAULT);        } catch (IOException e) {            throw new ElasticsearchException("Error while for indexExists request: " + request.toString(), e);        }    }    @Override    public boolean createIndex(String indexName) {        Assert.notNull(indexName, "No index defined for Query");        try {            return client.createIndex(this.requestsBuilder().createIndexRequest(indexName)).isAcknowledged();        } catch (Exception e) {            throw new ElasticsearchException("Failed to create index " + indexName, e);        }    }    /**     * @param rolloverConfig     * @param indexName     * @return     */    @Override    public boolean createRolloverIndex(RolloverConfig rolloverConfig, String indexName) {        CreateIndexRequest request = this.requestsBuilder().createRolloverIndex(rolloverConfig.getDefaultAlias(), generateRolloverIndexName(indexName));        try {            return this.client.createIndex(request).isAcknowledged();        } catch (IOException e) {            throw new ElasticsearchException("Failed to create index request: " + request.toString(), e);        }    }    /**     * @param indexName the index name     * @param indexPath the json index path     * @return true if the index was created     */    @Override    public boolean createIndexWithSettingsAndMapping(String indexName, String indexPath) {        CreateIndexRequest request = this.requestsBuilder().createIndexRequest(indexName, indexPath);        try {            return this.client.createIndex(request).isAcknowledged();        } catch (IOException e) {            throw new ElasticsearchException("Failed to create index request: " + request.toString(), e);        }    }    /**     * @param rolloverConfig     * @param indexName     * @param indexPath     * @return     */    @Override    public boolean createRolloverIndexWithSettingsAndMapping(RolloverConfig rolloverConfig, String indexName, String indexPath) {        CreateIndexRequest request = this.requestsBuilder().createRolloverIndex(rolloverConfig.getDefaultAlias(), generateRolloverIndexName(indexName), indexPath);        try {            return this.client.createIndex(request).isAcknowledged();        } catch (IOException e) {            throw new ElasticsearchException("Failed to create index request: " + request.toString(), e);        }    }    /**     * @param aliasName     * @param newIndexName     * @param indexPath     * @param conditions     * @return     */    @Override    public boolean rolloverIndex(String aliasName, String newIndexName, String indexPath, RolloverConfig.RolloverConditions conditions) {        RolloverRequest request = this.requestsBuilder().rolloverRequest(aliasName, newIndexName, indexPath, conditions);        try {            return this.client.rollover(request).isAcknowledged();        } catch (IOException e) {            throw new ElasticsearchException("Failed to rollover index request: " + request.toString(), e);        }    }    /**     * @see ElasticsearchOperations#index(Object, Class)     method     */    @Override    public <T> T index(T entity, Class<T> clazz) {        ElasticsearchPersistentEntity<T> persistentEntity = getPersistentEntityFor(clazz);        IndexRequest request = this.requestsBuilder().indexRequest(entity, persistentEntity, this.getResultsMapper());        try {            IndexResponse response = client.index(request);            persistentEntity.setPersistentEntity(entity, response);            return entity;        } catch (IOException e) {            throw new ElasticsearchException("Error while index for request: " + request.toString(), e);        }    }    /**     * @see ElasticsearchOperations#bulkIndex(List, Class) method     */    @Override    public <T> List<T> bulkIndex(List<T> entities, Class<T> clazz) {        if (entities.isEmpty())            return entities;        ElasticsearchPersistentEntity<T> persistentEntity = getPersistentEntityFor(clazz);        BulkRequest bulkRequest = new BulkRequest();        entities.forEach(entity -> bulkRequest.add(createIndexRequest(entity, clazz)));        try {            BulkResponse response = client.bulk(bulkRequest);            checkForBulkUpdateFailure(response);            BulkItemResponse[] items = response.getItems();            for (int i = 0; i < entities.size(); i++) {                persistentEntity.setPersistentEntity(entities.get(i), items[i]);            }            return entities;        } catch (IOException e) {            throw new ElasticsearchException("Error while bulk for request: " + bulkRequest.toString(), e);        }    }    @Override    public List bulkIndex(List<?> entities) {        if (entities.isEmpty())            return entities;        BulkRequest bulkRequest = new BulkRequest();        entities.forEach(entity -> bulkRequest.add(createIndexRequest(entity, entity.getClass())));        try {            BulkResponse response = client.bulk(bulkRequest);            checkForBulkUpdateFailure(response);            BulkItemResponse[] items = response.getItems();            for (int i = 0; i < entities.size(); i++) {                Object entity = entities.get(i);                ElasticsearchPersistentEntity persistentEntity = getPersistentEntityFor(entity.getClass());                persistentEntity.setPersistentEntity(entities.get(i), items[i]);            }            return entities;        } catch (IOException e) {            throw new ElasticsearchException("Error while bulk for request: " + bulkRequest.toString(), e);        }    }    /**     * @see ElasticsearchOperations#findById(Class, String)  method     */    @Override    public <T> T findById(Class<T> clazz, String documentId) {        Objects.requireNonNull(documentId, "documentId parameter canno't be null !");        ElasticsearchPersistentEntity<T> persistentEntity = getPersistentEntityFor(clazz);        if (persistentEntity.isIndexTimeBased() || persistentEntity.isRolloverIndex()) {            SearchQuery query = new NativeSearchQuery.NativeSearchQueryBuilder()                    .withQuery(QueryBuilders.termQuery("_id", documentId))                    .withPageable(PageRequest.of(0, 1))                    .build();            SearchResponse response = executeSearch(doSearch(prepareSearch(query, clazz), query));            if (response != null && response.getHits().totalHits > 0) {                return this.getResultsMapper().mapEntity(response.getHits().getAt(0), clazz);            }            return null;        } else {            GetRequest request = this.requestsBuilder().getRequest(persistentEntity.getAliasOrIndexReader(), persistentEntity.getTypeName(), documentId);            try {                GetResponse response = client.get(request);                return this.getResultsMapper().mapResult(response, clazz);            } catch (ElasticsearchStatusException e) {                if (e.status() == RestStatus.NOT_FOUND) {                    return null;                }                throw new ElasticsearchException("Error for delete index request: " + request.toString(), e);            } catch (IOException e) {                throw new ElasticsearchException("Error while getting for request: " + request.toString(), e);            }        }    }    @Override    public <T> T findOne(CriteriaQuery query, Class<T> clazz) {        return getResultOne(this.search(query, clazz));    }    @Override    public <T> T findOne(SearchQuery query, Class<T> clazz) {        return getResultOne(this.search(query, clazz));    }    @Override    public <T> T findOne(StringQuery query, Class<T> clazz) {        return getResultOne(this.search(query, clazz));    }    private <T> T getResultOne(List<T> items) {        if (items.isEmpty()) {            return null;        }        return items.get(0);    }    /**     * @param query     * @param clazz     * @return     */    @Override    public <T> long count(SearchQuery query, Class<T> clazz) {        return executeCount(doCount(prepareCount(query, clazz), query));    }    /**     * @param criteriaQuery     * @param clazz     * @return     */    @Override    public <T> long count(CriteriaQuery criteriaQuery, Class<T> clazz) {        return executeCount(doCount(prepareCount(criteriaQuery, clazz), criteriaQuery));    }    /**     * @param searchRequest     * @return     */    private long executeCount(SearchRequest searchRequest) {        try {            return client.search(searchRequest).getHits().getTotalHits();        } catch (IOException e) {            throw buildSearchException(e, searchRequest);        }    }    @Override    public <T> Boolean existsById(Class<T> clazz, String documentId) {        Objects.requireNonNull(documentId, "documentId parameter canno't be null !");        FetchSourceFilter.FetchSourceFilterBuilder sourceBuilder = new FetchSourceFilter.FetchSourceFilterBuilder();        sourceBuilder.withIncludes("id");        SearchQuery query = new NativeSearchQuery.NativeSearchQueryBuilder()                .withQuery(QueryBuilders.termQuery("_id", documentId))                .withSourceFilter(sourceBuilder.build())                .withPageable(PageRequest.of(0, 1))                .build();        SearchResponse response = executeSearch(doSearch(prepareSearch(query, clazz), query));        return response != null && response.getHits().totalHits > 0;    }    /**     * @param query     * @param javaType     * @return     */    @Override    public <T> Boolean existsByQuery(CriteriaQuery query, Class<T> javaType) {        SearchResponse response = executeSearch(doSearch(prepareSearch(query, javaType), query));        return response != null && response.getHits().totalHits > 0;    }    @Override    public <T> Boolean existsByQuery(SearchQuery query, Class<T> javaType) {        SearchResponse response = executeSearch(doSearch(prepareSearch(query, javaType), query));        return response != null && response.getHits().totalHits > 0;    }    @Override    public <T> Boolean existsByQuery(StringQuery query, Class<T> javaType) {        SearchResponse response = executeSearch(doSearch(prepareSearch(query, javaType), query));        return response != null && response.getHits().totalHits > 0;    }    @Override    public <T> List<T> search(SearchQuery query, Class<T> clazz) {        SearchResponse response = executeSearch(doSearch(prepareSearch(query, clazz), query));        if (response == null) {            return Collections.emptyList();        }        return this.getResultsMapper().mapEntity(response.getHits(), clazz);    }    /**     * @param criteriaQuery     * @param clazz     * @return     */    @Override    public <T> List<T> search(CriteriaQuery criteriaQuery, Class<T> clazz) {        SearchResponse response = executeSearch(doSearch(prepareSearch(criteriaQuery, clazz), criteriaQuery));        if (response == null) {            return Collections.emptyList();        }        return this.getResultsMapper().mapEntity(response.getHits(), clazz);    }    /**     * @param stringQuery     * @param clazz     * @return     */    @Override    public <T> List<T> search(StringQuery stringQuery, Class<T> clazz) {        SearchResponse response = executeSearch(doSearch(prepareSearch(stringQuery, clazz), stringQuery));        if (response == null) {            return Collections.emptyList();        }        return this.getResultsMapper().mapEntity(response.getHits(), clazz);    }    /**     * Start the {@link ScrolledPage}, with the given scrollTime, size, builder and clazz.     *     * @param searchQuery the given query.     * @param clazz       the given {@link Class} clazz.     * @return a {@link ScrolledPage} of T instances.     */    @Override    public <T> ScrolledPage<T> startScroll(SearchQuery searchQuery, Class<T> clazz) {        ElasticsearchPersistentEntity<T> persistentEntity = this.getPersistentEntityFor(clazz);        return this.startScroll(persistentEntity.getScrollTime(), searchQuery, clazz);    }    /**     * @param scrollTime  the scroll time.     * @param searchQuery the given query.     * @param clazz       the given {@link Class} clazz.     * @param <T>         the items type     * @return the scrolled page for the current continueScroll     */    @Override    public <T> ScrolledPage<T> startScroll(Duration scrollTime, SearchQuery searchQuery, Class<T> clazz) {        return this.startScroll(scrollTime, searchQuery, clazz, this.getResultsMapper());    }    /**     * Start the {@link ScrolledPage}, with the given scrollTime, size, builder and clazz.     *     * @param criteriaQuery the given query.     * @param clazz         the given {@link Class} clazz.     * @return a {@link ScrolledPage} of T instances.     */    @Override    public <T> ScrolledPage<T> startScroll(CriteriaQuery criteriaQuery, Class<T> clazz) {        ElasticsearchPersistentEntity<T> persistentEntity = this.getPersistentEntityFor(clazz);        return this.startScroll(persistentEntity.getScrollTime(), criteriaQuery, clazz);    }    /**     * @param scrollTime     * @param criteriaQuery     * @param clazz     * @return     */    @Override    public <T> ScrolledPage<T> startScroll(Duration scrollTime, CriteriaQuery criteriaQuery, Class<T> clazz) {        return this.startScroll(scrollTime, criteriaQuery, clazz, this.getResultsMapper());    }    /**     * Start the {@link ScrolledPage}, with the given scrollTime, size, builder and clazz.     *     * @param stringQuery the given query.     * @param clazz       the given {@link Class} clazz.     * @return a {@link ScrolledPage} of T instances.     */    @Override    public <T> ScrolledPage<T> startScroll(StringQuery stringQuery, Class<T> clazz) {        ElasticsearchPersistentEntity<T> persistentEntity = this.getPersistentEntityFor(clazz);        return this.startScroll(persistentEntity.getScrollTime(), stringQuery, clazz);    }    /**     * Start the {@link ScrolledPage}, with the given scrollTime, size, builder and clazz.     *     * @param scrollTime  the scroll time.     * @param stringQuery the given query.     * @param clazz       the given {@link Class} clazz.     * @return a {@link ScrolledPage} of T instances.     */    @Override    public <T> ScrolledPage<T> startScroll(Duration scrollTime, StringQuery stringQuery, Class<T> clazz) {        return this.startScroll(scrollTime, stringQuery, clazz, this.getResultsMapper());    }    /**     * @param scrollTime     * @param searchQuery     * @param entityType     * @return     */    public <T> SearchResponse startScrollResponse(Duration scrollTime, SearchQuery searchQuery, Class<T> entityType) {        return executeSearch(doScroll(prepareScroll(searchQuery, scrollTime, entityType), searchQuery));    }    /**     * @param scrollTime     * @param searchQuery     * @return     */    public SearchResponse startScrollResponse(Duration scrollTime, SearchQuery searchQuery) {        return executeSearch(doScroll(prepareScroll(searchQuery, scrollTime), searchQuery));    }    /**     * @param scrollTime  the scroll time     * @param searchQuery the query     * @param clazz       the domain type     * @param mapper      the mapper     * @param <T>         the generic type     * @return the scrolled page for the current continueScroll     */    @Override    public <T> ScrolledPage<T> startScroll(Duration scrollTime, SearchQuery searchQuery, Class<T> clazz, SearchResultMapper mapper) {        SearchResponse response = startScrollResponse(scrollTime, searchQuery, clazz);        return mapper.mapResults(response, clazz);    }    /**     * @param scrollTime     * @param criteriaQuery     * @param entityType     * @param <T>     * @return     */    public <T> SearchResponse startScrollResponse(Duration scrollTime, CriteriaQuery criteriaQuery, Class<T> entityType) {        return executeSearch(doScroll(prepareScroll(criteriaQuery, scrollTime, entityType), criteriaQuery));    }    /**     * @param scrollTime    the given scrollId.     * @param criteriaQuery the given query.     * @param clazz         the item domain type     * @param mapper        the mapper to transform results     * @return a {@link ScrolledPage} of T instancess.     */    @Override    public <T> ScrolledPage<T> startScroll(Duration scrollTime, CriteriaQuery criteriaQuery, Class<T> clazz, SearchResultMapper mapper) {        SearchResponse response = startScrollResponse(scrollTime, criteriaQuery, clazz);        return mapper.mapResults(response, clazz);    }    /**     * @param scrollTime     * @param stringQuery     * @param entityType     * @param <T>     * @return     */    public <T> SearchResponse startScrollResponse(Duration scrollTime, StringQuery stringQuery, Class<T> entityType) {        return executeSearch(doScroll(prepareScroll(stringQuery, scrollTime, entityType), stringQuery));    }    /**     * @param scrollTime  the given scrollId.     * @param stringQuery the given query.     * @param clazz       the item domain type     * @param mapper      the mapper to transform results     * @return a {@link ScrolledPage} of T instancess.     */    @Override    public <T> ScrolledPage<T> startScroll(Duration scrollTime, StringQuery stringQuery, Class<T> clazz, SearchResultMapper mapper) {        SearchResponse response = startScrollResponse(scrollTime, stringQuery, clazz);        return mapper.mapResults(response, clazz);    }    /**     * @param request     * @return     */    private SearchResponse executeSearch(SearchRequest request) {        try {            return client.search(request);        } catch (ElasticsearchStatusException e) {            if (e.status() == RestStatus.NOT_FOUND) {                return new SearchResponse(                        new SearchResponseSections(SearchHits.empty(), null, null, false, true, null, 0),                        null, 1, 1, 0, 1000, null, null);            }            throw buildSearchException(e, request);        } catch (IOException e) {            throw buildSearchException(e, request);        }    }    /**     * @see ElasticsearchOperations#continueScroll(String, Duration, Class)   method     */    @Override    public <T> ScrolledPage<T> continueScroll(@Nullable String scrollId, Duration scrollTime, Class<T> clazz) {        return continueScroll(scrollId, scrollTime, clazz, this.getResultsMapper());    }    public <T> SearchResponse continueScrollResponse(@Nullable String scrollId, Duration scrollTime) {        SearchScrollRequest request = this.requestsBuilder().searchScrollRequest(scrollId, scrollTime).scroll(TimeValue.timeValueMillis(scrollTime.toMillis()));        try {            return client.searchScroll(request);        } catch (ElasticsearchStatusException e) {            if (e.status() == RestStatus.NOT_FOUND || e.status() == RestStatus.INTERNAL_SERVER_ERROR) {                return null;            }            throw new ElasticsearchException("Error for continue scroll request: " + request.toString(), e);        } catch (IOException e) {            throw new ElasticsearchException("Error for continue scroll request: " + request.toString(), e);        }    }    /**     * @param scrollId   the scroll id     * @param scrollTime the scroll time     * @param clazz      the entity class     * @param mapper     the mapper     * @param <T>        generic type     * @return a new {@link ScrolledPage}     */    @Override    public <T> ScrolledPage<T> continueScroll(@Nullable String scrollId, Duration scrollTime, Class<T> clazz, SearchResultMapper mapper) {        SearchResponse response = continueScrollResponse(scrollId, scrollTime);        if (response == null) {            return ScrolledPageResult.of(Collections.emptyList(), 0L, scrollId);        }        return mapper.mapResults(response, clazz);    }    /**     * @see ElasticsearchOperations#clearScroll(String)  method     */    @Override    public void clearScroll(String scrollId) {        ClearScrollRequest request = this.requestsBuilder().clearScrollRequest(scrollId);        try {            client.clearScroll(request);        } catch (ElasticsearchStatusException e) {            if (e.status() == RestStatus.NOT_FOUND) {                return;            }            throw new ElasticsearchException("Error for continueScroll request with scroll: " + request.toString(), e);        } catch (IOException e) {            throw new ElasticsearchException("Error for continueScroll request with scroll: " + request.toString(), e);        }    }    /**     * @see ElasticsearchOperations#deleteAll(Class)  method     */    @Override    public <T> void deleteAll(Class<T> clazz) {        ElasticsearchPersistentEntity<T> persistentEntity = getPersistentEntityFor(clazz);        if (persistentEntity.isRolloverIndex()) {            RolloverConfig rollover = persistentEntity.getRolloverConfig();            this.deleteIndexByAlias(persistentEntity.getAliasOrIndexWriter());            this.rolloverIndex(                    persistentEntity.getAliasOrIndexWriter(),                    persistentEntity.getIndexName(),                    persistentEntity.getIndexPath(),                    rollover.getConditions());        } else if (persistentEntity.isIndexTimeBased()) {            GetAliasesRequest request = this.requestsBuilder().getAliasesRequest(persistentEntity.getAliasOrIndexReader());            try {                GetAliasesResponse response = this.client.getAlias(request);                response.getAliases().keySet().forEach(this::deleteIndexByName);            } catch (IOException e) {                throw new ElasticsearchException("Error for get aliases request: " + request.toString(), e);            }        } else {            deleteByQuery(persistentEntity.getAliasOrIndexReader(), persistentEntity.getTypeName(), QueryBuilders.matchAllQuery(), null);        }    }    /**     * @see ElasticsearchOperations#deleteAll(List, Class)  method     */    @Override    public <T> void deleteAll(List<T> entities, Class<T> clazz) {        ElasticsearchPersistentEntity<T> persistentEntity = getPersistentEntityFor(clazz);        Set<String> ids = new HashSet<>();        entities.forEach(entity -> {            String id = persistentEntity.getPersistentEntityId(entity);            if (id != null) {                ids.add(id);            }        });        if (!ids.isEmpty()) {            if (persistentEntity.isRolloverIndex()) {                deleteByQuery(persistentEntity.getAliasOrIndexWriter(), persistentEntity.getTypeName(), QueryBuilders.termsQuery("_id", ids), null);            } else {                deleteByQuery(persistentEntity.getAliasOrIndexReader(), persistentEntity.getTypeName(), QueryBuilders.termsQuery("_id", ids), null);            }        }    }    /**     * @see ElasticsearchOperations#delete(Object, Class)   method     */    @Override    public <T> void delete(T entity, Class<T> clazz) {        ElasticsearchPersistentEntity<T> persistentEntity = getPersistentEntityFor(clazz);        String id = persistentEntity.getPersistentEntityId(entity);        if (id != null) {            this.deleteById(id, clazz);        }    }    /**     * delete the document for the given entity, and clazz     *     * @param query the given query.     * @param clazz the given clazz.     */    @Override    public <T> void delete(CriteriaQuery query, Class<T> clazz) {        ElasticsearchPersistentEntity<T> persistentEntity = getPersistentEntityFor(clazz);        QueryBuilder queryBuilder = new CriteriaQueryProcessor().createQueryFromCriteria(query.getCriteria());        QueryBuilder filterBuilder = new CriteriaFilterProcessor().createFilterFromCriteria(query.getCriteria());        if (persistentEntity.isRolloverIndex()) {            deleteByQuery(persistentEntity.getAliasOrIndexWriter(), persistentEntity.getTypeName(), queryBuilder, filterBuilder);        } else {            deleteByQuery(persistentEntity.getAliasOrIndexReader(), persistentEntity.getTypeName(), queryBuilder, filterBuilder);        }    }    /**     * @see ElasticsearchOperations#deleteById(String, Class)   method     */    @Override    public <T> void deleteById(String documentId, Class<T> clazz) {        ElasticsearchPersistentEntity<T> persistentEntity = getPersistentEntityFor(clazz);        Assert.notNull(documentId, "documentId must not be null!");        QueryBuilder queryBuilder = QueryBuilders.termQuery("_id", documentId);        if (persistentEntity.isRolloverIndex()) {            deleteByQuery(persistentEntity.getAliasOrIndexWriter(), persistentEntity.getTypeName(), queryBuilder, null);        } else {            deleteByQuery(persistentEntity.getAliasOrIndexReader(), persistentEntity.getTypeName(), queryBuilder, null);        }    }    /**     * @param query     */    private <T> void deleteByQuery(String indexName, String typeName, QueryBuilder query, QueryBuilder filter) {        Integer pageSize = 1000;        Duration scrollTime = Duration.ofSeconds(5);        SearchQuery searchQuery = new NativeSearchQuery.NativeSearchQueryBuilder()                .withQuery(query)                .withFilter(filter)                .withIndices(indexName)                .withTypes(typeName)                .withPageable(PageRequest.of(0, pageSize))                .build();        SearchResponse response = startScrollResponse(scrollTime, searchQuery);        BulkRequest request = new BulkRequest();        List<String[]> documentsInfo = new ArrayList<>();        do {            response.getHits().forEach(searchHit -> documentsInfo.add(new String[]{searchHit.getIndex(), searchHit.getId()}));            if (response.getHits().getHits().length != 0 && response.getScrollId() != null) {                response = continueScrollResponse(response.getScrollId(), scrollTime);            }        } while (response != null && response.getHits().getHits().length > 0);        for (String[] documentInfo : documentsInfo) {            request.add(this.requestsBuilder().deleteRequest(documentInfo[0], typeName, documentInfo[1]));        }        if (request.numberOfActions() > 0) {            BulkResponse bulkResponse;            try {                bulkResponse = client.bulk(request);                checkForBulkUpdateFailure(bulkResponse);            } catch (IOException e) {                throw new ElasticsearchException("Error while deleting bulk: " + request.toString(), e);            }        }        if (response.getScrollId() != null) {            clearScroll(response.getScrollId());        }    }    /**     * @see ElasticsearchOperations#refresh(Class)  method     */    @Override    public <T> void refresh(Class<T> clazz) {        ElasticsearchPersistentEntity<T> persistentEntity = getPersistentEntityFor(clazz);        if (persistentEntity.isRolloverIndex()) {            this.refresh(persistentEntity.getAliasOrIndexWriter());        } else {            this.refresh(persistentEntity.getAliasOrIndexReader());        }    }    /**     * @param indexName the index name     */    @Override    public void refresh(String indexName) {        Assert.notNull(indexName, "No index defined for refresh()");        try {            client.refresh(refreshRequest(indexName));        } catch (ElasticsearchStatusException e) {            if (e.status() == RestStatus.NOT_FOUND) {                return;            }            throw new ElasticsearchException("failed to refresh index: " + indexName, e);        } catch (IOException e) {            throw new ElasticsearchException("failed to refresh index: " + indexName, e);        }    }    @Override    public <T> T search(SearchQuery query, ResultsExtractor<T> resultsExtractor) {        SearchResponse response = executeSearch(doSearch(prepareSearch(query, Optional.of(query.getQuery())), query));        return resultsExtractor.extract(response);    }    /**     * @param suggestion     * @param indices     * @return     */    @Override    public SearchResponse suggest(SuggestBuilder suggestion, String... indices) {        SearchRequest searchRequest = prepareSuggestSearch(suggestion, indices);        try {            return client.search(searchRequest);        } catch (IOException e) {            throw new ElasticsearchException("Could not execute search request : " + searchRequest.toString(), e);        }    }    /**     * @param suggestion     * @param indices     * @return     */    @Override    public <T> T suggest(SuggestBuilder suggestion, String[] indices, ResultsExtractor<T> extractor) {        return extractor.extract(suggest(suggestion, indices));    }    /**     * @param suggestion     * @param clazz     * @param extractor     * @return     */    @Override    public <R, T> R suggest(SuggestBuilder suggestion, Class<T> clazz, ResultsExtractor<R> extractor) {        ElasticsearchPersistentEntity<T> persistentEntity = getPersistentEntityFor(clazz);        return suggest(suggestion, new String[]{persistentEntity.getAliasOrIndexReader()}, extractor);    }}