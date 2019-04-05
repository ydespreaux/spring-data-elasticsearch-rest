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
public interface RestElasticsearchClient {

    RequestOptions getDefaultRequestOptions();

    RestHighLevelClient getRestHighLevelClient();

    default ClusterHealthResponse clusterHealth(ClusterHealthRequest request) throws IOException {
        return clusterHealth(request, getDefaultRequestOptions());
    }

    default ClusterUpdateSettingsResponse clusterPutSettings(ClusterUpdateSettingsRequest request) throws IOException {
        return clusterPutSettings(request, getDefaultRequestOptions());
    }

    default ClusterGetSettingsResponse clusterGetSettings(ClusterGetSettingsRequest request) throws IOException {
        return clusterGetSettings(request, getDefaultRequestOptions());
    }

    default GetSettingsResponse getSettings(GetSettingsRequest request) throws IOException {
        return getSettings(request, getDefaultRequestOptions());
    }

    default GetAliasesResponse existsAlias(GetAliasesRequest request) throws IOException {
        return existsAlias(request, getDefaultRequestOptions());
    }

    default GetIndexResponse getIndex(GetIndexRequest request) throws IOException {
        return getIndex(request, getDefaultRequestOptions());
    }

    default Boolean indicesExist(GetIndexRequest request) throws IOException {
        return indicesExist(request, getDefaultRequestOptions());
    }

    default AcknowledgedResponse indexPutSettings(UpdateSettingsRequest request) throws IOException {
        return indexPutSettings(request, getDefaultRequestOptions());
    }

    default AcknowledgedResponse putTemplate(PutIndexTemplateRequest request) throws IOException {
        return putTemplate(request, getDefaultRequestOptions());
    }

    default GetIndexTemplatesResponse getTemplates(GetIndexTemplatesRequest request) throws IOException {
        return getTemplates(request, getDefaultRequestOptions());
    }

    default CreateIndexResponse createIndex(CreateIndexRequest request) throws IOException {
        return createIndex(request, getDefaultRequestOptions());
    }

    default AcknowledgedResponse deleteIndex(DeleteIndexRequest request) throws IOException {
        return deleteIndex(request, getDefaultRequestOptions());
    }

    default AcknowledgedResponse updateAliases(IndicesAliasesRequest request) throws IOException {
        return updateAliases(request, getDefaultRequestOptions());
    }

    default GetMappingsResponse getMappings(GetMappingsRequest request) throws IOException {
        return getMappings(request, getDefaultRequestOptions());
    }

    default GetFieldMappingsResponse getFieldMapping(GetFieldMappingsRequest request) throws IOException {
        return getFieldMapping(request, getDefaultRequestOptions());
    }

    default AcknowledgedResponse putMapping(PutMappingRequest request) throws IOException {
        return putMapping(request, getDefaultRequestOptions());
    }

    default RefreshResponse refresh(RefreshRequest request) throws IOException {
        return refresh(request, getDefaultRequestOptions());
    }

    default SearchTemplateResponse searchTemplate(SearchTemplateRequest request) throws IOException {
        return searchTemplate(request, getDefaultRequestOptions());
    }

    default MultiSearchTemplateResponse multiSearchTemplate(MultiSearchTemplateRequest request) throws IOException {
        return multiSearchTemplate(request, getDefaultRequestOptions());
    }

    default RolloverResponse rollover(RolloverRequest request) throws IOException {
        return rollover(request, getDefaultRequestOptions());
    }

    default GetAliasesResponse getAlias(GetAliasesRequest request) throws IOException {
        return getAlias(request, getDefaultRequestOptions());
    }

    default Boolean exists(GetRequest request) throws IOException {
        return exists(request, getDefaultRequestOptions());
    }

    default IndexResponse index(IndexRequest request) throws IOException {
        return index(request, getDefaultRequestOptions());
    }

    default BulkResponse bulk(BulkRequest request) throws IOException {
        return bulk(request, getDefaultRequestOptions());
    }

    default UpdateResponse update(UpdateRequest request) throws IOException {
        return update(request, getDefaultRequestOptions());
    }

    default DeleteResponse delete(DeleteRequest request) throws IOException {
        return delete(request, getDefaultRequestOptions());
    }

    default GetResponse get(GetRequest request) throws IOException {
        return get(request, getDefaultRequestOptions());
    }

    default MultiGetResponse multiGet(MultiGetRequest request) throws IOException {
        return multiGet(request, getDefaultRequestOptions());
    }

    default SearchResponse search(SearchRequest request) throws IOException {
        return search(request, getDefaultRequestOptions());
    }

    default SearchResponse searchScroll(SearchScrollRequest request) throws IOException {
        return searchScroll(request, getDefaultRequestOptions());
    }

    default ClearScrollResponse clearScroll(ClearScrollRequest request) throws IOException {
        return clearScroll(request, getDefaultRequestOptions());
    }

    default MultiSearchResponse multiSearch(MultiSearchRequest request) throws IOException {
        return multiSearch(request, getDefaultRequestOptions());
    }

    default BulkByScrollResponse deleteBy(DeleteByQueryRequest request) throws IOException {
        return deleteBy(request, getDefaultRequestOptions());
    }

    ClusterHealthResponse clusterHealth(ClusterHealthRequest request, RequestOptions options) throws IOException;

    ClusterUpdateSettingsResponse clusterPutSettings(ClusterUpdateSettingsRequest request, RequestOptions options) throws IOException;

    ClusterGetSettingsResponse clusterGetSettings(ClusterGetSettingsRequest request, RequestOptions options) throws IOException;

    GetSettingsResponse getSettings(GetSettingsRequest request, RequestOptions options) throws IOException;

    GetAliasesResponse existsAlias(GetAliasesRequest request, RequestOptions options) throws IOException;

    GetIndexResponse getIndex(GetIndexRequest request, RequestOptions options) throws IOException;

    Boolean indicesExist(GetIndexRequest request, RequestOptions options) throws IOException;

    AcknowledgedResponse indexPutSettings(UpdateSettingsRequest request, RequestOptions options) throws IOException;

    AcknowledgedResponse putTemplate(PutIndexTemplateRequest request, RequestOptions options) throws IOException;

    GetIndexTemplatesResponse getTemplates(GetIndexTemplatesRequest request, RequestOptions options) throws IOException;

    CreateIndexResponse createIndex(CreateIndexRequest request, RequestOptions options) throws IOException;

    AcknowledgedResponse deleteIndex(DeleteIndexRequest request, RequestOptions options) throws IOException;

    AcknowledgedResponse updateAliases(IndicesAliasesRequest request, RequestOptions options) throws IOException;

    GetMappingsResponse getMappings(GetMappingsRequest request, RequestOptions options) throws IOException;

    GetFieldMappingsResponse getFieldMapping(GetFieldMappingsRequest request, RequestOptions options) throws IOException;

    AcknowledgedResponse putMapping(PutMappingRequest request, RequestOptions options) throws IOException;

    RefreshResponse refresh(RefreshRequest request, RequestOptions options) throws IOException;

    SearchTemplateResponse searchTemplate(SearchTemplateRequest request, RequestOptions options) throws IOException;

    MultiSearchTemplateResponse multiSearchTemplate(MultiSearchTemplateRequest request, RequestOptions options) throws IOException;

    RolloverResponse rollover(RolloverRequest request, RequestOptions options) throws IOException;

    GetAliasesResponse getAlias(GetAliasesRequest request, RequestOptions options) throws IOException;

    // Default operations

    Boolean exists(GetRequest request, RequestOptions options) throws IOException;

    IndexResponse index(IndexRequest request, RequestOptions options) throws IOException;

    BulkResponse bulk(BulkRequest request, RequestOptions options) throws IOException;

    UpdateResponse update(UpdateRequest request, RequestOptions options) throws IOException;

    DeleteResponse delete(DeleteRequest request, RequestOptions options) throws IOException;

    GetResponse get(GetRequest request, RequestOptions options) throws IOException;

    MultiGetResponse multiGet(MultiGetRequest request, RequestOptions options) throws IOException;

    SearchResponse search(SearchRequest request, RequestOptions options) throws IOException;

    SearchResponse searchScroll(SearchScrollRequest request, RequestOptions options) throws IOException;

    ClearScrollResponse clearScroll(ClearScrollRequest request, RequestOptions options) throws IOException;

    MultiSearchResponse multiSearch(MultiSearchRequest request, RequestOptions options) throws IOException;

    BulkByScrollResponse deleteBy(DeleteByQueryRequest request, RequestOptions options) throws IOException;
}
