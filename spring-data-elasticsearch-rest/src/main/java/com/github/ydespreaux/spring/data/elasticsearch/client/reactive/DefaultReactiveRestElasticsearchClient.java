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

import com.github.ydespreaux.spring.data.elasticsearch.client.ClientLogger;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
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
import org.elasticsearch.client.*;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.script.mustache.MultiSearchTemplateRequest;
import org.elasticsearch.script.mustache.MultiSearchTemplateResponse;
import org.elasticsearch.script.mustache.SearchTemplateRequest;
import org.elasticsearch.script.mustache.SearchTemplateResponse;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

@Slf4j
public class DefaultReactiveRestElasticsearchClient implements ReactiveRestElasticsearchClient {

    private final RestHighLevelClient client;

    public DefaultReactiveRestElasticsearchClient(RestHighLevelClient client) {
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
    public Mono<ClusterHealthResponse> clusterHealth(ClusterHealthRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.cluster().healthAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<ClusterUpdateSettingsResponse> clusterPutSettings(ClusterUpdateSettingsRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.cluster().putSettingsAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<ClusterGetSettingsResponse> clusterPutSettings(ClusterGetSettingsRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.cluster().getSettingsAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    // Admin Indices
    @Override
    public Mono<GetSettingsResponse> getSettings(GetSettingsRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.indices().getSettingsAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<GetAliasesResponse> existsAlias(GetAliasesRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.indices().getAliasAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<GetIndexResponse> getIndex(GetIndexRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.indices().getAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<Boolean> indicesExist(GetIndexRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.indices().existsAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<AcknowledgedResponse> indexPutSettings(UpdateSettingsRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.indices().putSettingsAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<AcknowledgedResponse> putTemplate(PutIndexTemplateRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.indices().putTemplateAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<GetIndexTemplatesResponse> getTemplates(GetIndexTemplatesRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.indices().getTemplateAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<CreateIndexResponse> createIndex(CreateIndexRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.indices().createAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<AcknowledgedResponse> deleteIndex(DeleteIndexRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.indices().deleteAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<AcknowledgedResponse> updateAliases(IndicesAliasesRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.indices().updateAliasesAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<GetMappingsResponse> getMappings(GetMappingsRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.indices().getMappingAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<GetFieldMappingsResponse> getFieldMapping(GetFieldMappingsRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.indices().getFieldMappingAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<AcknowledgedResponse> putMapping(PutMappingRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.indices().putMappingAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<RefreshResponse> refresh(RefreshRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.indices().refreshAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<SearchTemplateResponse> searchTemplate(SearchTemplateRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.searchTemplateAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<MultiSearchTemplateResponse> multiSearchTemplate(MultiSearchTemplateRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.msearchTemplateAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<RolloverResponse> rollover(RolloverRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.indices().rolloverAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<GetAliasesResponse> getAlias(GetAliasesRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.indices().getAliasAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    // Default operations

    @Override
    public Mono<Boolean> exists(GetRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.existsAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<IndexResponse> index(IndexRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.indexAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<BulkResponse> bulk(BulkRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.bulkAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<UpdateResponse> update(UpdateRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.updateAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<DeleteResponse> delete(DeleteRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.deleteAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<BulkByScrollResponse> deleteBy(DeleteByQueryRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.deleteByQueryAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<GetResponse> get(GetRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.getAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<MultiGetResponse> multiGet(MultiGetRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.mgetAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<SearchResponse> search(SearchRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.searchAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<SearchResponse> searchScroll(SearchScrollRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.scrollAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<ClearScrollResponse> clearScroll(ClearScrollRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.clearScrollAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    @Override
    public Mono<MultiSearchResponse> multiSearch(MultiSearchRequest request, RequestOptions options) {
        return Mono.create(sink -> this.client.msearchAsync(request, options, listenerToSink(logRequest(request), sink)));
    }

    /**
     * @param sink
     * @param <T>
     * @return
     */
    private <T> ActionListener<T> listenerToSink(String logId, MonoSink<T> sink) {
        return new ActionListener<T>() {
            @Override
            public void onResponse(T response) {
                ClientLogger.logResponse(logId, response.toString());
                sink.success(response);
            }

            @Override
            public void onFailure(Exception e) {
                ClientLogger.logFailure(logId, e);
                sink.error(e);
            }
        };
    }

    private <T> ResponseListener responseListenerToSink(String logId, MonoSink<T> sink) {
        return new ResponseListener() {

            @Override
            public void onSuccess(Response response) {
                sink.success((T)response);
            }

            @Override
            public void onFailure(Exception e) {
                ClientLogger.logFailure(logId, e);
                sink.error(e);
            }
        };
    }

    private String logRequest(ActionRequest request){
        String logId = ClientLogger.newLogId();
        ClientLogger.logRequest(logId, request);
        return logId;
    }
}
