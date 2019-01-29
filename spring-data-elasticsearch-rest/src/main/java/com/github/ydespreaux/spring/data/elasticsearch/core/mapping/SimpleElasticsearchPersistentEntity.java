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

import com.github.ydespreaux.spring.data.elasticsearch.annotations.*;
import com.github.ydespreaux.spring.data.elasticsearch.core.IndexTimeBasedParameter;
import com.github.ydespreaux.spring.data.elasticsearch.core.IndexTimeBasedSupport;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.FetchSourceFilter;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.SourceFilter;
import com.github.ydespreaux.spring.data.elasticsearch.core.request.config.RolloverConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @param <T> generic type
 * @author Yoann Despréaux
 * @since 1.0.0
 */
@Slf4j
@Getter
public class SimpleElasticsearchPersistentEntity<T> extends BasicPersistentEntity<T, ElasticsearchPersistentProperty> implements ElasticsearchPersistentEntity<T>, ApplicationContextAware {

    private static final Pattern pattern = Pattern.compile("\\Q${\\E(.+?)\\Q}\\E");

    private ApplicationContext context;
    private Class<T> entityClass;
    private String aliasName;
    private String indexName;
    private String indexPattern;
    private String typeName;
    private IndexTimeBasedSupport<T> indexSupport;
    private Boolean createIndex;
    private Boolean indexTimeBased;
    private String indexPath;
    private ElasticsearchPersistentProperty parentIdProperty;
    private ElasticsearchPersistentProperty scoreProperty;
    private ElasticsearchPersistentProperty indexNameProperty;
    private SourceFilter sourceFilter;
    private Duration scrollTime;
    private RolloverConfig rollover;

    /**
     * @param typeInformation a {@link TypeInformation} parameter
     */
    public SimpleElasticsearchPersistentEntity(TypeInformation<T> typeInformation) {
        super(typeInformation);
    }

    /**
     *
     */
    private void afterPropertiesSet() {
        this.entityClass = this.getTypeInformation().getType();
        if (this.entityClass.isAnnotationPresent(ProjectionDocument.class)) {
            afterProjectionDocumentPropertySet(this.entityClass.getAnnotation(ProjectionDocument.class));
        } else if (this.entityClass.isAnnotationPresent(IndexedDocument.class)) {
            afterIndexedDocumentPropertySet(this.entityClass.getAnnotation(IndexedDocument.class));
        } else if (this.entityClass.isAnnotationPresent(RolloverDocument.class)) {
            afterRolloverDocumentPropertySet(this.entityClass.getAnnotation(RolloverDocument.class));
        }
    }

    private void afterProjectionDocumentPropertySet(ProjectionDocument document) {
        this.sourceFilter = new FetchSourceFilter.FetchSourceFilterBuilder().withIncludes(document.fields()).build();
        Class<?> targetClass = document.target();
        Assert.isTrue(targetClass.isAnnotationPresent(IndexedDocument.class) || targetClass.isAnnotationPresent(RolloverDocument.class),
                "Invalid document projection. " + targetClass.getSimpleName()
                        + "TargetClass is not a elasticsearch document. Make sure the target document class is annotated with @Document or @DocumentRollover");
        if (targetClass.isAnnotationPresent(IndexedDocument.class)) {
            afterIndexedDocumentPropertySet(targetClass.getAnnotation(IndexedDocument.class));
        } else if (targetClass.isAnnotationPresent(RolloverDocument.class)) {
            afterRolloverDocumentPropertySet(targetClass.getAnnotation(RolloverDocument.class));
        }
    }

    private void afterIndexedDocumentPropertySet(IndexedDocument document) {
        this.typeName = document.type();
        this.createIndex = document.createIndex();
        this.indexPath = document.settingsAndMappingPath();

        Environment env = context.getEnvironment();
        this.aliasName = getEnvironmentValue(env, document.aliasName());
        this.indexName = getEnvironmentValue(env, document.indexName());
        this.indexPattern = getEnvironmentValue(env, document.indexPattern());
        this.indexTimeBased = StringUtils.hasText(this.indexPattern);
        try {
            this.indexSupport = document.indexTimeBasedSupport().newInstance();
        } catch (Exception e) {
            throw new ElasticsearchException(e);
        }
        this.scrollTime = Duration.ofSeconds(document.scrollTimeSeconds());
    }

    private void afterRolloverDocumentPropertySet(RolloverDocument document) {
        this.typeName = document.type();
        this.createIndex = document.createIndex();
        this.indexPath = document.settingsAndMappingPath();

        Environment env = context.getEnvironment();
        this.aliasName = getEnvironmentValue(env, document.aliasName());
        this.indexName = getEnvironmentValue(env, document.indexName());
        this.indexPattern = getEnvironmentValue(env, document.indexPattern());
        this.indexTimeBased = StringUtils.hasText(this.indexPattern);
        this.indexSupport = new IndexTimeBasedSupport();
        this.scrollTime = Duration.ofSeconds(document.scrollTimeSeconds());

        Rollover rolloverAnnotation = document.rollover();
        Alias aliasAnnotation = rolloverAnnotation.alias();
        Trigger triggerAnnotation = rolloverAnnotation.trigger();

        this.rollover = RolloverConfig.builder()
                .alias(RolloverConfig.RolloverAlias.builder()
                        .name(getEnvironmentValue(env, aliasAnnotation.name()))
                        .indexRouting(getEnvironmentValue(env, aliasAnnotation.indexRouting()))
                        .filter(getEnvironmentValue(env, aliasAnnotation.filter()))
                        .searchRouting(getEnvironmentValue(env, aliasAnnotation.searchRouting()))
                        .build())
                .conditions(RolloverConfig.RolloverConditions.builder()
                        .maxAge(rolloverAnnotation.maxAge())
                        .maxDocs(rolloverAnnotation.maxDoc())
                        .maxSize(rolloverAnnotation.maxSize())
                        .build())
                .trigger(RolloverConfig.TriggerConfig.builder()
                        .enabled(triggerAnnotation.enabled())
                        .cronExpression(getEnvironmentValue(env, triggerAnnotation.cronExpression()))
                        .build())
                .build();
        if (!this.rollover.hasConditions()) {
            throw new IllegalArgumentException("No condition defined");
        }
    }


    @Override
    public void addPersistentProperty(ElasticsearchPersistentProperty property) {
        super.addPersistentProperty(property);
        if (property.isParentProperty()) {
            if (this.parentIdProperty != null) {
                throw new MappingException(
                        String.format("Attempt to add parent property %s but already have property %s registered "
                                + "as parent property. Check your mapping configuration!", property.getField(), parentIdProperty.getField()));
            }
            this.parentIdProperty = property;
        } else if (property.isScoreProperty()) {
            if (this.scoreProperty != null) {
                throw new MappingException(
                        String.format("Attempt to add score property %s but already have property %s registered "
                                + "as score property. Check your mapping configuration!", property.getField(), scoreProperty.getField()));
            }

            this.scoreProperty = property;
        } else if (property.isIndexNameProperty()) {
            if (this.indexNameProperty != null) {
                throw new MappingException(
                        String.format("Attempt to add indexName property %s but already have property %s registered "
                                + "as index name property. Check your mapping configuration!", property.getField(), indexNameProperty.getField()));
            }
            this.indexNameProperty = property;
        }
    }

    @Override
    public String getIndexName(T source) {
        if (isIndexTimeBased()) {
            return this.indexSupport.buildIndex(IndexTimeBasedParameter.of(indexPattern, LocalDate.now(Clock.systemUTC()), source));
        } else {
            return this.indexName;
        }
    }

    @Override
    public String getAliasOrIndexReader() {
        return StringUtils.isEmpty(this.aliasName) ? this.indexName : this.aliasName;
    }

    @Override
    public String getAliasOrIndexWriter(T source) {
        if (this.isRolloverIndex()) {
            return this.rollover.getAlias().getName();
        }
        if (isIndexTimeBased()) {
            return this.indexSupport.buildIndex(IndexTimeBasedParameter.of(indexPattern, LocalDate.now(Clock.systemUTC()), source));
        }
        return this.indexName;
    }

    @Override
    public void setPersistentEntityId(T entity, String id) {
        ElasticsearchPersistentProperty idProperty = getIdProperty();
        if (idProperty == null) {
            if (log.isWarnEnabled()) {
                log.warn("No propertyId defined for entity class {}", entityClass);
            }
            return;
        }
        getPropertyAccessor(entity).setProperty(idProperty, id);
    }

    /**
     * @param entity  the entity
     * @param version the document version
     */
    @Override
    public void setPersistentEntityVersion(T entity, Long version) {
        ElasticsearchPersistentProperty versionProperty = getVersionProperty();
        if (versionProperty != null) {
            getPropertyAccessor(entity).setProperty(versionProperty, version);
        }
    }

    /**
     * @param entity
     * @param indexName
     */
    @Override
    public void setPersistentEntityIndexName(T entity, String indexName) {
        ElasticsearchPersistentProperty indexNameProperty = getIndexNameProperty();
        if (indexNameProperty != null) {
            getPropertyAccessor(entity).setProperty(indexNameProperty, indexName);
        }
    }

    /**
     * @param source the source
     * @return the document id
     */
    @Override
    public String getPersistentEntityId(T source) {
        if (getIdProperty() == null) {
            if (log.isWarnEnabled()) {
                log.warn("No propertyId defined for entity class {}", entityClass);
            }
            return null;
        }
        try {
            return (String) getPropertyAccessor(source).getProperty(getIdProperty());
        } catch (Exception e) {
            throw new IllegalStateException("failed to load id field", e);
        }
    }

    /**
     * @param source the document
     * @return the document version
     */
    @Override
    public Long getPersistentEntityVersion(T source) {
        if (getVersionProperty() == null) {
            if (log.isDebugEnabled()) {
                log.debug("No version defined for entity class {}", entityClass);
            }
            return null;
        }
        try {
            return (Long) getPropertyAccessor(source).getProperty(getVersionProperty());
        } catch (Exception e) {
            throw new IllegalStateException("failed to load version field", e);
        }
    }

    /**
     * @param source
     * @return
     */
    @Override
    public String getPersistentEntityIndexName(T source) {
        if (getIndexNameProperty() == null) {
            if (log.isDebugEnabled()) {
                log.debug("No propertyIndexName defined for entity class {}", entityClass);
            }
            return null;
        }
        try {
            return (String) getPropertyAccessor(source).getProperty(getIndexNameProperty());
        } catch (Exception e) {
            throw new IllegalStateException("failed to load id field", e);
        }
    }

    /**
     * @return true if score property is defined
     */
    @Override
    public boolean hasScoreProperty() {
        return scoreProperty != null;
    }

    /**
     * @param result the document
     * @param score  the score
     */
    @Override
    public void setPersistentEntityScore(T result, float score) {
        if (!this.hasScoreProperty()) {
            return;
        }
        getPropertyAccessor(result) //
                .setProperty(getScoreProperty(), score);
    }

    /**
     * @return
     */
    @Override
    public SourceFilter getSourceFilter() {
        return sourceFilter;
    }

    /**
     * @return
     */
    @Override
    public boolean hasSourceFiler() {
        return this.sourceFilter != null;
    }

    /**
     * @return
     */
    @Override
    public Duration getScrollTime() {
        return this.scrollTime;
    }

    /**
     * @return true if the index must be created
     */
    @Override
    public Boolean createIndex() {
        return this.createIndex;
    }

    /**
     * @return true if the current index is a time based index
     */
    @Override
    public Boolean isIndexTimeBased() {
        return this.indexTimeBased;
    }

    /**
     * @return the index path
     */
    @Override
    public String getIndexPath() {
        return this.indexPath;
    }

    @Override
    public boolean isRolloverIndex() {
        return this.rollover != null;
    }

    @Override
    public RolloverConfig getRolloverConfig() {
        return this.rollover;
    }

    /**
     * Returns the parent Id. Can be {@literal null}.
     *
     * @return can be {@literal null}.
     */
    @Override
    public Object getParentId(T source) {
        if (this.parentIdProperty == null) {
            if (log.isDebugEnabled()) {
                log.debug("No parentId defined for entity class {}", entityClass);
            }
            return null;
        }
        try {
            return getPropertyAccessor(source).getProperty(this.parentIdProperty);
        } catch (Exception e) {
            throw new IllegalStateException("failed to load parentId field", e);
        }
    }

    /**
     * @return true if the current document have a parent
     */
    @Override
    public boolean hasParent() {
        return this.parentIdProperty != null;
    }

    /**
     * @param environment the environment
     * @param expression  the SPel expression
     * @return evaluate the expression
     */
    private String getEnvironmentValue(Environment environment, String expression) {
        String value = null;
        // Create the matcher
        Matcher matcher = pattern.matcher(expression);
        // If the matching is there, then add it to the map and return the value
        if (matcher.find()) {
            value = environment.getProperty(matcher.group(1));
        }
        return value == null ? expression : value;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.context = applicationContext;
        afterPropertiesSet();
    }

    @Override
    public String getId(T source) {
        return getPersistentEntityId(source);
    }

    @Override
    public Class<String> getIdType() {
        return String.class;
    }

    @Override
    public Class<T> getJavaType() {
        return this.entityClass;
    }

}