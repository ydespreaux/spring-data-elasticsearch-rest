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

package com.github.ydespreaux.spring.data.elasticsearch.client.reactive;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsRequest;
import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsResponse;
import org.elasticsearch.action.admin.cluster.settings.ClusterUpdateSettingsRequest;
import org.elasticsearch.action.admin.cluster.settings.ClusterUpdateSettingsResponse;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.admin.indices.rollover.RolloverRequest;
import org.elasticsearch.action.admin.indices.rollover.RolloverResponse;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsResponse;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesRequest;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesResponse;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateResponse;
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
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.script.mustache.MultiSearchTemplateRequest;
import org.elasticsearch.script.mustache.MultiSearchTemplateResponse;
import org.elasticsearch.script.mustache.SearchTemplateRequest;
import org.elasticsearch.script.mustache.SearchTemplateResponse;
import reactor.core.publisher.Mono;

public interface ReactiveRestElasticsearchClient {

    RequestOptions getDefaultRequestOptions();

    RestHighLevelClient getRestHighLevelClient();

    default Mono<ClusterHealthResponse> clusterHealth(ClusterHealthRequest request) {
        return clusterHealth(request, getDefaultRequestOptions());
    }

    default Mono<ClusterUpdateSettingsResponse> clusterPutSettings(ClusterUpdateSettingsRequest request) {
        return clusterPutSettings(request, getDefaultRequestOptions());
    }

    default Mono<ClusterGetSettingsResponse> clusterPutSettings(ClusterGetSettingsRequest request) {
        return clusterPutSettings(request, getDefaultRequestOptions());
    }

    default Mono<GetSettingsResponse> getSettings(GetSettingsRequest request) {
        return getSettings(request, getDefaultRequestOptions());
    }

    default Mono<GetAliasesResponse> existsAlias(GetAliasesRequest request) {
        return existsAlias(request, getDefaultRequestOptions());
    }

    default Mono<GetIndexResponse> getIndex(GetIndexRequest request) {
        return getIndex(request, getDefaultRequestOptions());
    }

    default Mono<Boolean> indicesExist(GetIndexRequest request) {
        return indicesExist(request, getDefaultRequestOptions());
    }

    default Mono<UpdateSettingsResponse> indexPutSettings(UpdateSettingsRequest request) {
        return indexPutSettings(request, getDefaultRequestOptions());
    }

    default Mono<PutIndexTemplateResponse> putTemplate(PutIndexTemplateRequest request) {
        return putTemplate(request, getDefaultRequestOptions());
    }

    default Mono<GetIndexTemplatesResponse> getTemplates(GetIndexTemplatesRequest request) {
        return getTemplates(request, getDefaultRequestOptions());
    }

    default Mono<CreateIndexResponse> createIndex(CreateIndexRequest request) {
        return createIndex(request, getDefaultRequestOptions());
    }

    default Mono<DeleteIndexResponse> deleteIndex(DeleteIndexRequest request) {
        return deleteIndex(request, getDefaultRequestOptions());
    }

    default Mono<IndicesAliasesResponse> updateAliases(IndicesAliasesRequest request) {
        return updateAliases(request, getDefaultRequestOptions());
    }

    default Mono<GetMappingsResponse> getMappings(GetMappingsRequest request) {
        return getMappings(request, getDefaultRequestOptions());
    }

    default Mono<GetFieldMappingsResponse> getFieldMapping(GetFieldMappingsRequest request) {
        return getFieldMapping(request, getDefaultRequestOptions());
    }

    default Mono<PutMappingResponse> putMapping(PutMappingRequest request) {
        return putMapping(request, getDefaultRequestOptions());
    }

    default Mono<RefreshResponse> refresh(RefreshRequest request) {
        return refresh(request, getDefaultRequestOptions());
    }

    default Mono<SearchTemplateResponse> searchTemplate(SearchTemplateRequest request) {
        return searchTemplate(request, getDefaultRequestOptions());
    }

    default Mono<MultiSearchTemplateResponse> multiSearchTemplate(MultiSearchTemplateRequest request) {
        return multiSearchTemplate(request, getDefaultRequestOptions());
    }

    default Mono<RolloverResponse> rollover(RolloverRequest request) {
        return rollover(request, getDefaultRequestOptions());
    }

    default Mono<GetAliasesResponse> getAlias(GetAliasesRequest request) {
        return getAlias(request, getDefaultRequestOptions());
    }

    default Mono<Boolean> exists(GetRequest request) {
        return exists(request, getDefaultRequestOptions());
    }

    default Mono<IndexResponse> index(IndexRequest request) {
        return index(request, getDefaultRequestOptions());
    }

    default Mono<BulkResponse> bulk(BulkRequest request) {
        return bulk(request, getDefaultRequestOptions());
    }

    default Mono<UpdateResponse> update(UpdateRequest request) {
        return update(request, getDefaultRequestOptions());
    }

    default Mono<DeleteResponse> delete(DeleteRequest request) {
        return delete(request, getDefaultRequestOptions());
    }

    default Mono<GetResponse> get(GetRequest request) {
        return get(request, getDefaultRequestOptions());
    }

    default Mono<MultiGetResponse> multiGet(MultiGetRequest request) {
        return multiGet(request, getDefaultRequestOptions());
    }

    default Mono<SearchResponse> search(SearchRequest request) {
        return search(request, getDefaultRequestOptions());
    }

    default Mono<SearchResponse> searchScroll(SearchScrollRequest request) {
        return searchScroll(request, getDefaultRequestOptions());
    }

    default Mono<ClearScrollResponse> clearScroll(ClearScrollRequest request) {
        return clearScroll(request, getDefaultRequestOptions());
    }

    default Mono<MultiSearchResponse> multiSearch(MultiSearchRequest request) {
        return multiSearch(request, getDefaultRequestOptions());
    }


    Mono<ClusterHealthResponse> clusterHealth(ClusterHealthRequest request, RequestOptions options);

    Mono<ClusterUpdateSettingsResponse> clusterPutSettings(ClusterUpdateSettingsRequest request, RequestOptions options);

    Mono<ClusterGetSettingsResponse> clusterPutSettings(ClusterGetSettingsRequest request, RequestOptions options);

    Mono<GetSettingsResponse> getSettings(GetSettingsRequest request, RequestOptions options);

    Mono<GetAliasesResponse> existsAlias(GetAliasesRequest request, RequestOptions options);

    Mono<GetIndexResponse> getIndex(GetIndexRequest request, RequestOptions options);

    Mono<Boolean> indicesExist(GetIndexRequest request, RequestOptions options);

    Mono<UpdateSettingsResponse> indexPutSettings(UpdateSettingsRequest request, RequestOptions options);

    Mono<PutIndexTemplateResponse> putTemplate(PutIndexTemplateRequest request, RequestOptions options);

    Mono<GetIndexTemplatesResponse> getTemplates(GetIndexTemplatesRequest request, RequestOptions options);

    Mono<CreateIndexResponse> createIndex(CreateIndexRequest request, RequestOptions options);

    Mono<DeleteIndexResponse> deleteIndex(DeleteIndexRequest request, RequestOptions options);

    Mono<IndicesAliasesResponse> updateAliases(IndicesAliasesRequest request, RequestOptions options);

    Mono<GetMappingsResponse> getMappings(GetMappingsRequest request, RequestOptions options);

    Mono<GetFieldMappingsResponse> getFieldMapping(GetFieldMappingsRequest request, RequestOptions options);

    Mono<PutMappingResponse> putMapping(PutMappingRequest request, RequestOptions options);

    Mono<RefreshResponse> refresh(RefreshRequest request, RequestOptions options);

    Mono<SearchTemplateResponse> searchTemplate(SearchTemplateRequest request, RequestOptions options);

    Mono<MultiSearchTemplateResponse> multiSearchTemplate(MultiSearchTemplateRequest request, RequestOptions options);

    Mono<RolloverResponse> rollover(RolloverRequest request, RequestOptions options);

    Mono<GetAliasesResponse> getAlias(GetAliasesRequest request, RequestOptions options);

    // Default operations

    Mono<Boolean> exists(GetRequest request, RequestOptions options);

    Mono<IndexResponse> index(IndexRequest request, RequestOptions options);

    Mono<BulkResponse> bulk(BulkRequest request, RequestOptions options);

    Mono<UpdateResponse> update(UpdateRequest request, RequestOptions options);

    Mono<DeleteResponse> delete(DeleteRequest request, RequestOptions options);

    Mono<GetResponse> get(GetRequest request, RequestOptions options);

    Mono<MultiGetResponse> multiGet(MultiGetRequest request, RequestOptions options);

    Mono<SearchResponse> search(SearchRequest request, RequestOptions options);

    Mono<SearchResponse> searchScroll(SearchScrollRequest request, RequestOptions options);

    Mono<ClearScrollResponse> clearScroll(ClearScrollRequest request, RequestOptions options);

    Mono<MultiSearchResponse> multiSearch(MultiSearchRequest request, RequestOptions options);

}
