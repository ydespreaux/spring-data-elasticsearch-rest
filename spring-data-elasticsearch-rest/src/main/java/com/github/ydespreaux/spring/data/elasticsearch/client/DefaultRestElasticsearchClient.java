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

package com.github.ydespreaux.spring.data.elasticsearch.client;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsRequest;
import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsResponse;
import org.elasticsearch.action.admin.cluster.settings.ClusterUpdateSettingsRequest;
import org.elasticsearch.action.admin.cluster.settings.ClusterUpdateSettingsResponse;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.admin.indices.rollover.RolloverRequest;
import org.elasticsearch.action.admin.indices.rollover.RolloverResponse;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesRequest;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesResponse;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.script.mustache.MultiSearchTemplateRequest;
import org.elasticsearch.script.mustache.MultiSearchTemplateResponse;
import org.elasticsearch.script.mustache.SearchTemplateRequest;
import org.elasticsearch.script.mustache.SearchTemplateResponse;

import java.io.IOException;

/**
 * @author Yoann Despréaux
 * @since 1.0.0
 */
@Slf4j
public class DefaultRestElasticsearchClient implements RestElasticsearchClient {

    private final RestHighLevelClient client;

    public DefaultRestElasticsearchClient(RestHighLevelClient client) {
        this.client = client;
    }

    // Admin cluster

    @Override
    public RequestOptions getDefaultRequestOptions() {
        return RequestOptions.DEFAULT;
    }

    @Override
    public RestHighLevelClient getRestHighLevelClient() {
        return this.client;
    }

    @Override
    public ClusterHealthResponse clusterHealth(ClusterHealthRequest request, RequestOptions options) throws IOException {
        return this.client.cluster().health(request, options);
    }

    @Override
    public ClusterUpdateSettingsResponse clusterPutSettings(ClusterUpdateSettingsRequest request, RequestOptions options) throws IOException {
        return this.client.cluster().putSettings(request, options);
    }

    @Override
    public ClusterGetSettingsResponse clusterGetSettings(ClusterGetSettingsRequest request, RequestOptions options) throws IOException {
        return this.client.cluster().getSettings(request, options);
    }

    // Admin Indices
    @Override
    public GetSettingsResponse getSettings(GetSettingsRequest request, RequestOptions options) throws IOException {
        return this.client.indices().getSettings(request, options);
    }

    @Override
    public GetAliasesResponse existsAlias(GetAliasesRequest request, RequestOptions options) throws IOException {
        return this.client.indices().getAlias(request, options);
    }

    @Override
    public GetIndexResponse getIndex(GetIndexRequest request, RequestOptions options) throws IOException {
        return this.client.indices().get(request, options);
    }

    @Override
    public Boolean indicesExist(GetIndexRequest request, RequestOptions options) throws IOException {
        return this.client.indices().exists(request, options);
    }

    @Override
    public AcknowledgedResponse indexPutSettings(UpdateSettingsRequest request, RequestOptions options) throws IOException {
        return this.client.indices().putSettings(request, options);
    }

    @Override
    public AcknowledgedResponse putTemplate(PutIndexTemplateRequest request, RequestOptions options) throws IOException {
        return this.client.indices().putTemplate(request, options);
    }

    @Override
    public GetIndexTemplatesResponse getTemplates(GetIndexTemplatesRequest request, RequestOptions options) throws IOException {
        return this.client.indices().getTemplate(request, options);
    }

    @Override
    public CreateIndexResponse createIndex(CreateIndexRequest request, RequestOptions options) throws IOException {
        return this.client.indices().create(request, options);
    }

    @Override
    public AcknowledgedResponse deleteIndex(DeleteIndexRequest request, RequestOptions options) throws IOException {
        return this.client.indices().delete(request, options);
    }

    @Override
    public AcknowledgedResponse updateAliases(IndicesAliasesRequest request, RequestOptions options) throws IOException {
        return this.client.indices().updateAliases(request, options);
    }

    @Override
    public GetMappingsResponse getMappings(GetMappingsRequest request, RequestOptions options) throws IOException {
        return this.client.indices().getMapping(request, options);
    }

    @Override
    public GetFieldMappingsResponse getFieldMapping(GetFieldMappingsRequest request, RequestOptions options) throws IOException {
        return this.client.indices().getFieldMapping(request, options);
    }

    @Override
    public AcknowledgedResponse putMapping(PutMappingRequest request, RequestOptions options) throws IOException {
        return this.client.indices().putMapping(request, options);
    }

    @Override
    public RefreshResponse refresh(RefreshRequest request, RequestOptions options) throws IOException {
        return this.client.indices().refresh(request, options);
    }

    @Override
    public SearchTemplateResponse searchTemplate(SearchTemplateRequest request, RequestOptions options) throws IOException {
        return this.client.searchTemplate(request, options);
    }

    @Override
    public MultiSearchTemplateResponse multiSearchTemplate(MultiSearchTemplateRequest request, RequestOptions options) throws IOException {
        return this.client.msearchTemplate(request, options);
    }

    @Override
    public RolloverResponse rollover(RolloverRequest request, RequestOptions options) throws IOException {
        return this.client.indices().rollover(request, options);
    }

    @Override
    public GetAliasesResponse getAlias(GetAliasesRequest request, RequestOptions options) throws IOException {
        return this.client.indices().getAlias(request, options);
    }

    // Default operations

    @Override
    public Boolean exists(GetRequest request, RequestOptions options) throws IOException {
        return this.client.exists(request, options);
    }

    @Override
    public IndexResponse index(IndexRequest request, RequestOptions options) throws IOException {
        return this.client.index(request, options);
    }

    @Override
    public BulkResponse bulk(BulkRequest request, RequestOptions options) throws IOException {
        return this.client.bulk(request, options);
    }

    @Override
    public UpdateResponse update(UpdateRequest request, RequestOptions options) throws IOException {
        return this.client.update(request, options);
    }

    @Override
    public DeleteResponse delete(DeleteRequest request, RequestOptions options) throws IOException {
        return this.client.delete(request, options);
    }

    @Override
    public GetResponse get(GetRequest request, RequestOptions options) throws IOException {
        return this.client.get(request, options);
    }

    @Override
    public MultiGetResponse multiGet(MultiGetRequest request, RequestOptions options) throws IOException {
        return this.client.mget(request, options);
    }

    @Override
    public SearchResponse search(SearchRequest request, RequestOptions options) throws IOException {
        return this.client.search(request, options);
    }

    @Override
    public SearchResponse searchScroll(SearchScrollRequest request, RequestOptions options) throws IOException {
        return this.client.scroll(request, options);
    }

    @Override
    public ClearScrollResponse clearScroll(ClearScrollRequest request, RequestOptions options) throws IOException {
        return this.client.clearScroll(request, options);
    }

    @Override
    public MultiSearchResponse multiSearch(MultiSearchRequest request, RequestOptions options) throws IOException {
        return this.client.msearch(request, options);
    }

    @Override
    public BulkByScrollResponse deleteBy(DeleteByQueryRequest request, RequestOptions options) throws IOException {
        return this.client.deleteByQuery(request, options);
    }
}
