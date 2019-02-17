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

package com.github.ydespreaux.spring.data.elasticsearch.core.mapping;

import com.github.ydespreaux.spring.data.elasticsearch.core.query.SourceFilter;
import com.github.ydespreaux.spring.data.elasticsearch.core.request.config.RolloverConfig;
import com.github.ydespreaux.spring.data.elasticsearch.repository.support.ElasticsearchEntityInformation;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.search.SearchHit;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.lang.Nullable;

import java.time.Duration;

/**
 * @param <T> generic type
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public interface ElasticsearchPersistentEntity<T> extends PersistentEntity<T, ElasticsearchPersistentProperty>, ElasticsearchEntityInformation<T, String> {

    /**
     * Retourne le type de document de l'entité courante.
     *
     * @return the type name
     */
    String getTypeName();

    /**
     * @return
     */
    Alias getAlias();

    /**
     * @return
     */
    String getAliasOrIndexReader();

    /**
     * @return
     */
    default String getAliasOrIndexWriter() {
        return getAliasOrIndexWriter(null);
    }

    /**
     * @param source
     * @return
     */
    String getAliasOrIndexWriter(T source);

    /**
     * Get the current index name for writing operations.
     *
     * @return
     */
    String getIndexName();

    /**
     * @return
     */
    boolean isRolloverIndex();

    /**
     * @return
     */
    RolloverConfig getRolloverConfig();

    /**
     * @param entity the source of docuement
     * @param id     the identifier
     */
    void setPersistentEntityId(T entity, String id);

    /**
     * @param entity  the source of document
     * @param version the version
     */
    void setPersistentEntityVersion(T entity, Long version);

    /**
     * @param entity
     * @param indexName
     */
    void setPersistentEntityIndexName(T entity, String indexName);

    /**
     * @param entity the entity
     * @return the document id
     */
    String getPersistentEntityId(T entity);

    /**
     * @param source the entity
     * @return the version
     */
    Long getPersistentEntityVersion(T source);

    /**
     * @param entity
     * @return
     */
    String getPersistentEntityIndexName(T entity);

    /**
     * @return true if the index must be create
     */
    Boolean createIndex();

    /**
     * @return true if the current index is a index timebased
     */
    Boolean isIndexTimeBased();

    /**
     * @return the index path
     */
    String getIndexSettingAndMappingPath();

    /**
     * @return the parent id property
     */
    ElasticsearchPersistentProperty getParentIdProperty();

    /**
     * Returns the parent Id. Can be {@literal null}.
     *
     * @param source the document source
     * @return can be {@literal null}.
     */
    @Nullable
    Object getParentId(T source);

    /**
     * @return true if the current entity has a parent
     */
    boolean hasParent();

    /**
     * @return true if the score property is defined
     */
    boolean hasScoreProperty();

    /**
     * Returns the score property of the {@link ElasticsearchPersistentEntity}. Can be {@literal null} in case no score
     * property is available on the entity.
     *
     * @return the score {@link ElasticsearchPersistentProperty} of the {@link PersistentEntity} or {@literal null} if not
     * defined.
     */
    @Nullable
    ElasticsearchPersistentProperty getScoreProperty();

    /**
     * Set score to entity
     *
     * @param result the entity
     * @param score  the score
     */
    void setPersistentEntityScore(T result, float score);

    /**
     * @return
     */
    boolean hasCompletionProperty();

    /**
     * @return
     */
    @Nullable
    ElasticsearchPersistentProperty getCompletionProperty();

    /**
     * @return
     */
    SourceFilter getSourceFilter();

    /**
     * @return
     */
    boolean hasSourceFiler();

    /**
     * @return
     */
    Duration getScrollTime();

    /**
     * @param result
     * @param response
     */
    default void setPersistentEntity(T result, GetResponse response) {
        setPersistentEntityId(result, response.getId());
        setPersistentEntityVersion(result, response.getVersion());
        setPersistentEntityIndexName(result, response.getIndex());
    }

    /**
     * @param result
     * @param response
     */
    default void setPersistentEntity(T result, BulkItemResponse response) {
        setPersistentEntityId(result, response.getId());
        setPersistentEntityVersion(result, response.getVersion());
        setPersistentEntityIndexName(result, response.getIndex());
    }

    /**
     * @param result
     * @param response
     */
    default void setPersistentEntity(T result, IndexResponse response) {
        setPersistentEntityId(result, response.getId());
        setPersistentEntityVersion(result, response.getVersion());
        setPersistentEntityIndexName(result, response.getIndex());
    }

    /**
     * @param result
     * @param hit
     */
    default void setPersistentEntity(T result, SearchHit hit) {
        setPersistentEntityId(result, hit.getId());
        setPersistentEntityVersion(result, hit.getVersion());
        setPersistentEntityScore(result, hit.getScore());
        setPersistentEntityIndexName(result, hit.getIndex());
    }

}
