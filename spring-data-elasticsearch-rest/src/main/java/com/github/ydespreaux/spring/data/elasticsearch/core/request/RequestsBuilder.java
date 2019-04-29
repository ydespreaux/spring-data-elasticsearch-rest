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

package com.github.ydespreaux.spring.data.elasticsearch.core.request;

import com.github.ydespreaux.spring.data.elasticsearch.core.ResultsMapper;
import com.github.ydespreaux.spring.data.elasticsearch.core.indices.CreateIndexBuilder;
import com.github.ydespreaux.spring.data.elasticsearch.core.indices.TemplateBuilder;
import com.github.ydespreaux.spring.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import com.github.ydespreaux.spring.data.elasticsearch.core.request.config.RolloverConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutIndexTemplateRequest;
import org.elasticsearch.client.indices.rollover.RolloverRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.elasticsearch.index.VersionType.EXTERNAL;

@Slf4j
public class RequestsBuilder {

    private final ApplicationContext applicationContext;

    /**
     * @param applicationContext
     */
    public RequestsBuilder(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * @param indices
     * @return
     */
    public GetIndexRequest getIndexRequest(String... indices) {
        return new GetIndexRequest(indices);
    }

    /**
     * @param indexName
     * @return
     */
    public CreateIndexRequest createIndexRequest(@Nullable Alias alias, String indexName) {
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        if (alias != null) {
            request.alias(alias);
        }
        return request;
    }

    /**
     *
     * @param alias
     * @param indexName
     * @param indexPath
     * @return
     */
    public CreateIndexRequest createIndexRequest(@Nullable Alias alias, String indexName, String indexPath) {

        CreateIndexRequest indexRequest = new CreateIndexBuilder()
                .name(indexName)
                .sources(getResources(indexPath))
                .build();
        if (alias != null && !indexRequest.aliases().contains(alias)) {
            indexRequest.alias(alias);
        }
        return indexRequest;
    }

    /**
     *
     * @param aliasReader
     * @param aliasWriter
     * @param newIndexName
     * @return
     */
    public CreateIndexRequest createRolloverIndex(@Nullable Alias aliasReader, Alias aliasWriter, String newIndexName) {
        Assert.notNull(aliasWriter, "alias no defined");
        CreateIndexRequest indexRequest = new CreateIndexRequest(newIndexName);
        if (aliasReader != null) {
            indexRequest.alias(aliasReader);
        }
        indexRequest.alias(aliasWriter);
        return indexRequest;
    }

    /**
     *
     * @param aliasReader
     * @param aliasWriter
     * @param newIndexName
     * @param indexPath
     * @return
     */
    public CreateIndexRequest createRolloverIndex(@Nullable Alias aliasReader, Alias aliasWriter, String newIndexName, String indexPath) {
        Assert.notNull(aliasWriter, "aliasWriter no defined");
        CreateIndexRequest indexRequest = new CreateIndexBuilder()
                .name(newIndexName)
                .sources(getResources(indexPath))
                .build();
        if (aliasReader != null && !indexRequest.aliases().contains(aliasReader)) {
            indexRequest.alias(aliasReader);
        }
        if (!indexRequest.aliases().contains(aliasWriter)) {
            indexRequest.alias(aliasWriter);
        }
        return indexRequest;
    }


    public PutIndexTemplateRequest createPutIndexTemplateRequest(String templateName, List<Resource> locations) {
        return new TemplateBuilder().name(templateName).sources(locations).build();
    }

    public Request deleteTemplateRequest(String templateName) {
        return new Request("DELETE", "_template/" + templateName);
    }

    public GetAliasesRequest getAliasesRequest(String aliasName) {
        return new GetAliasesRequest(aliasName);
    }

    /**
     * @param indices
     * @return
     */
    public DeleteIndexRequest deleteIndexRequest(String indices) {
        return new DeleteIndexRequest().indices(indices);
    }

    /**
     * @param indexName
     * @param typeName
     * @param documentId
     * @return
     */
    public DeleteRequest deleteRequest(String indexName, String typeName, String documentId) {
        return new DeleteRequest(indexName, typeName, documentId);
    }

    /**
     * @param aliasName
     * @param indexPath
     * @param conditions
     * @return
     */
    public RolloverRequest rolloverRequest(String aliasName, String indexPath, RolloverConfig.RolloverConditions conditions) {
        RolloverRequest request = new RolloverRequest(aliasName, null);
        if (conditions.getMaxAge() != null) {
            request.addMaxIndexAgeCondition(conditions.getMaxAge());
        }
        if (conditions.getMaxSize() != null) {
            request.addMaxIndexSizeCondition(conditions.getMaxSize());
        }
        if (conditions.getMaxDocs() > 0) {
            request.addMaxIndexDocsCondition(conditions.getMaxDocs());
        }
        if (!StringUtils.isEmpty(indexPath)) {
            new CreateIndexBuilder().sources(getResources(indexPath)).build(request.getCreateIndexRequest());
            request.getCreateIndexRequest().aliases().remove(new Alias(aliasName));
        }
        return request;
    }


    /**
     * @param source
     * @param persistentEntity
     * @param mapper
     * @param <T>
     * @return
     */
    public <T> IndexRequest indexRequest(T source, ElasticsearchPersistentEntity<T> persistentEntity, ResultsMapper mapper) {
        Objects.requireNonNull(source);
        String indexName = persistentEntity.getAliasOrIndexWriter(source);
        String type = persistentEntity.getTypeName();
        String id = persistentEntity.getPersistentEntityId(source);
        IndexRequest indexRequest = id != null ? new IndexRequest(indexName, type, id) : new IndexRequest(indexName, type);
        indexRequest.source(mapper.getEntityMapper().mapToString(source), Requests.INDEX_CONTENT_TYPE);
        Long version = persistentEntity.getPersistentEntityVersion(source);
        if (version != null) {
            indexRequest.version(version);
            indexRequest.versionType(EXTERNAL);
        }
        if (persistentEntity.isChildDocument()) {
            indexRequest.routing(persistentEntity.getJoinDescriptor().getRouting());
        }
        return indexRequest;
    }

    public GetRequest getRequest(String indexName, String typeName, String documentId) {
        return new GetRequest(indexName, typeName, documentId);
    }

    public SearchScrollRequest searchScrollRequest(String scrollId, Duration scrollTime) {
        return new SearchScrollRequest(scrollId).scroll(TimeValue.timeValueMillis(scrollTime.toMillis()));
    }

    public ClearScrollRequest clearScrollRequest(String scrollId) {
        ClearScrollRequest request = new ClearScrollRequest();
        request.addScrollId(scrollId);
        return request;
    }

    /**
     *
     * @param indexName
     * @param typeName
     * @param query
     * @return
     */
    public DeleteByQueryRequest deleteBy(String indexName, String typeName, @Nullable QueryBuilder query) {
        return deleteBy(indexName, typeName, 1000, query);
    }

    /**
     *
     * @param indexName
     * @param typeName
     * @param batchSize
     * @param query
     * @return
     */
    public DeleteByQueryRequest deleteBy(String indexName, String typeName, int batchSize, @Nullable QueryBuilder query) {
        return new DeleteByQueryRequest(indexName)
                .setBatchSize(batchSize)
                .setQuery(query == null ? QueryBuilders.matchAllQuery() : query)
                .types(typeName);
    }


    /**
     * @param locationPath the resource path
     * @return list of {@link Resource}
     */
    private List<Resource> getResources(String locationPath) {
        final String[] profiles = this.applicationContext.getEnvironment().getActiveProfiles();
        List<Resource> locations = new ArrayList<>(profiles.length + 1);
        Resource resource = this.applicationContext.getResource(locationPath);
        if (resource.exists()) {
            locations.add(resource);
        } else if (log.isWarnEnabled()) {
            log.warn("Resource {} not found", locationPath);
        }
        String extension = FilenameUtils.getExtension(locationPath);
        boolean hasExtension = StringUtils.hasLength(extension);
        String prefix = locationPath.substring(0, locationPath.length() - (hasExtension ? extension.length() + 1 : 0));
        for (String profile : profiles) {
            String profilPath = prefix + "-" + profile + (hasExtension ? "." + extension : "");
            Resource profilResource = this.applicationContext.getResource(profilPath);
            if (profilResource.exists()) {
                locations.add(profilResource);
            }
        }
        return locations;
    }

}
