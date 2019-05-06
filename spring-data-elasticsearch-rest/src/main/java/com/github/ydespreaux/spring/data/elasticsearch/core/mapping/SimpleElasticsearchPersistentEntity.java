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
import com.github.ydespreaux.spring.data.elasticsearch.core.JoinDescriptor;
import com.github.ydespreaux.spring.data.elasticsearch.core.JoinDescriptorBuilder;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.FetchSourceFilter;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.SourceFilter;
import com.github.ydespreaux.spring.data.elasticsearch.core.request.config.RolloverConfig;
import com.github.ydespreaux.spring.data.elasticsearch.core.utils.ContextUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * @param <T> generic type
 * @author Yoann Despréaux
 * @since 1.0.0
 */
@Slf4j
@Getter
public class SimpleElasticsearchPersistentEntity<T> extends BasicPersistentEntity<T, ElasticsearchPersistentProperty> implements ElasticsearchPersistentEntity<T>, ApplicationContextAware {

    @Nullable
    private ApplicationContext context;
    private Class<T> entityClass;
    private org.elasticsearch.action.admin.indices.alias.Alias alias;
    private String indexName;
    private String indexPattern;
    private IndexTimeBasedSupport<T> indexSupport;
    private Boolean createIndex;
    private Boolean indexTimeBased;
    private String indexPath;
    private ElasticsearchPersistentProperty parentIdProperty;
    private ElasticsearchPersistentProperty scoreProperty;
    private ElasticsearchPersistentProperty indexNameProperty;
    private ElasticsearchPersistentProperty completionProperty;
    private Set<ScriptFieldProperty> scriptProperties = new HashSet<>();
    private SourceFilter sourceFilter;
    private Duration scrollTime;
    private RolloverConfig rollover;

    @Nullable
    private JoinDescriptor<T> joinDescriptor;

    /**
     * @param typeInformation a {@link TypeInformation} parameter
     */
    public SimpleElasticsearchPersistentEntity(TypeInformation<T> typeInformation) {
        super(typeInformation);
        this.entityClass = this.getTypeInformation().getType();
    }

    /**
     *
     */
    private void afterPropertiesSet() {
        if (this.entityClass.isAnnotationPresent(ProjectionDocument.class)) {
            afterProjectionDocumentPropertySet(this.entityClass.getAnnotation(ProjectionDocument.class));
        } else if (this.entityClass.isAnnotationPresent(IndexedDocument.class)) {
            afterIndexedDocumentPropertySet(this.entityClass.getAnnotation(IndexedDocument.class));
        } else if (this.entityClass.isAnnotationPresent(RolloverDocument.class)) {
            afterRolloverDocumentPropertySet(this.entityClass.getAnnotation(RolloverDocument.class));
        }
        if (this.entityClass.isAnnotationPresent(Child.class) || this.entityClass.isAnnotationPresent(Parent.class)) {
            this.joinDescriptor = new JoinDescriptorBuilder<>(this.context, this.entityClass).build();
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
        afterPropertySet(document.index());
        afterPropertySet(document.alias());
        this.scrollTime = Duration.ofSeconds(document.scrollTimeSeconds());
    }

    private void afterRolloverDocumentPropertySet(RolloverDocument document) {
        afterPropertySet(document.index());
        afterPropertySet(document.alias());
        afterPropertySet(document.rollover());
        this.scrollTime = Duration.ofSeconds(document.scrollTimeSeconds());
    }

    private void afterPropertySet(Alias aliasAnnotation) {
        if (StringUtils.isEmpty(aliasAnnotation.name())) {
            return;
        }
        this.alias = new org.elasticsearch.action.admin.indices.alias.Alias(getEnvironmentValue(aliasAnnotation.name()));
        if (StringUtils.hasText(aliasAnnotation.searchRouting())) {
            this.alias.searchRouting(getEnvironmentValue(aliasAnnotation.searchRouting()));
        }
        if (StringUtils.hasText(aliasAnnotation.indexRouting())) {
            this.alias.searchRouting(getEnvironmentValue(aliasAnnotation.indexRouting()));
        }
        if (StringUtils.hasText(aliasAnnotation.filter())) {
            this.alias.searchRouting(getEnvironmentValue(aliasAnnotation.filter()));
        }
    }

    private void afterPropertySet(Index indexAnnotation) {
        if (StringUtils.isEmpty(indexAnnotation.name()) && StringUtils.isEmpty(indexAnnotation.indexPattern())) {
            throw new IllegalArgumentException("Index name or index pattern no defined");
        }
        this.createIndex = indexAnnotation.createIndex();
        this.indexName = getEnvironmentValue(indexAnnotation.name());
        this.indexPattern = getEnvironmentValue(indexAnnotation.indexPattern());
        this.indexPath = getEnvironmentValue(indexAnnotation.settingsAndMappingPath());
        this.indexTimeBased = StringUtils.hasText(this.indexPattern);
        if (this.indexTimeBased) {
            try {
                this.indexSupport = indexAnnotation.indexTimeBasedSupport().getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new ElasticsearchException(e);
            }
        }
    }

    private void afterPropertySet(Rollover rolloverAnnotation) {
        Alias aliasAnnotation = rolloverAnnotation.alias();
        Trigger triggerAnnotation = rolloverAnnotation.trigger();
        this.rollover = RolloverConfig.builder()
                .alias(RolloverConfig.RolloverAlias.builder()
                        .name(getEnvironmentValue(aliasAnnotation.name()))
                        .indexRouting(getEnvironmentValue(aliasAnnotation.indexRouting()))
                        .filter(getEnvironmentValue(aliasAnnotation.filter()))
                        .searchRouting(getEnvironmentValue(aliasAnnotation.searchRouting()))
                        .build())
                .conditions(RolloverConfig.RolloverConditions.builder()
                        .maxAge(rolloverAnnotation.maxAge())
                        .maxDocs(rolloverAnnotation.maxDoc())
                        .maxSize(rolloverAnnotation.maxSize())
                        .build())
                .trigger(RolloverConfig.TriggerConfig.builder()
                        .enabled(isTriggerEnabled(triggerAnnotation))
                        .cronExpression(getEnvironmentValue(triggerAnnotation.cronExpression()))
                        .build())
                .build();
        if (!this.rollover.hasConditions()) {
            throw new IllegalArgumentException("No condition defined");
        }
    }

    private boolean isTriggerEnabled(Trigger triggerAnnotation) {
        if (triggerAnnotation.value()) {
            return true;
        }
        if (StringUtils.hasText(triggerAnnotation.enabled())) {
            return Boolean.valueOf(getEnvironmentValue(triggerAnnotation.enabled()));
        }
        return false;
    }

    @Override
    public void addPersistentProperty(ElasticsearchPersistentProperty property) {
        super.addPersistentProperty(property);
        if (property.isParentProperty()) {
            addPersistentParentProperty(property);
        } else if (property.isScoreProperty()) {
            addPersistentScoreProperty(property);
        } else if (property.isIndexNameProperty()) {
            addPersistentIndexNameProperty(property);
        } else if (property.isCompletionProperty()) {
            addPersistentCompletionProperty(property);
        } else if (property.isScriptProperty()) {
            addPersistentScriptProperty(property);
        }
    }


    @Override
    public String getIndexName() {
        if (isIndexTimeBased()) {
            return this.indexSupport.buildIndex(IndexTimeBasedParameter.of(indexPattern, LocalDate.now(Clock.systemUTC()), null));
        } else {
            return this.indexName;
        }
    }

    @Override
    public String getAliasOrIndexReader() {
        return this.alias != null ? this.alias.name() : this.indexName;
    }

    @Override
    public String getAliasOrIndexWriter(@Nullable T source) {
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
        ElasticsearchPersistentProperty property = getIndexNameProperty();
        if (property != null) {
            getPropertyAccessor(entity).setProperty(property, indexName);
        }
    }

    /**
     * @param source the source
     * @return the document id
     */
    @Nullable
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
    @Nullable
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
    @Nullable
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
    public boolean hasCompletionProperty() {
        return this.completionProperty != null;
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
     * Retourne le type de document de l'entité courante.
     *
     * @return the type name
     */
    @Override
    public String getTypeName() {
        return Index.TYPE;
    }

    @Override
    public org.elasticsearch.action.admin.indices.alias.Alias getAlias() {
        return this.alias;
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
    public String getIndexSettingAndMappingPath() {
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
     * Returns the parentDocument Id. Can be {@literal null}.
     *
     * @return can be {@literal null}.
     */
    @Nullable
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
     * @param entity
     * @param id
     */
    @Override
    public void setParentId(T entity, Object id) {
        if (!this.isChildDocument()) {
            return;
        }
        getPropertyAccessor(entity).setProperty(getParentIdProperty(), id);
    }

    @Override
    public boolean isParentDocument() {
        return this.joinDescriptor != null && this.joinDescriptor.isParentDocument();
    }

    @Override
    public boolean isChildDocument() {
        return this.joinDescriptor != null && this.joinDescriptor.isChildDocument();
    }


    /**
     * @param expression  the SPel expression
     * @return evaluate the expression
     */
    private String getEnvironmentValue(String expression) {
        return ContextUtils.getEnvironmentValue(this.context, expression);
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

    private void addPersistentParentProperty(ElasticsearchPersistentProperty property) {
        if (this.parentIdProperty != null) {
            throw new MappingException(
                    String.format("Attempt to add parentDocument property %s but already have property %s registered "
                            + "as parentDocument property. Check your mapping configuration!", property.getField(), parentIdProperty.getField()));
        }
        this.parentIdProperty = property;
    }

    private void addPersistentScoreProperty(ElasticsearchPersistentProperty property) {
        if (this.scoreProperty != null) {
            throw new MappingException(
                    String.format("Attempt to add score property %s but already have property %s registered "
                            + "as score property. Check your mapping configuration!", property.getField(), scoreProperty.getField()));
        }
        this.scoreProperty = property;
    }

    private void addPersistentIndexNameProperty(ElasticsearchPersistentProperty property) {
        if (this.indexNameProperty != null) {
            throw new MappingException(
                    String.format("Attempt to add indexName property %s but already have property %s registered "
                            + "as index name property. Check your mapping configuration!", property.getField(), indexNameProperty.getField()));
        }
        this.indexNameProperty = property;
    }

    private void addPersistentCompletionProperty(ElasticsearchPersistentProperty property) {
        if (this.completionProperty != null) {
            throw new MappingException(
                    String.format("Attempt to add completion property %s but already have property %s registered "
                            + "as completion property. Check your mapping configuration!", property.getField(), completionProperty.getField()));
        }
        this.completionProperty = property;
    }

    private void addPersistentScriptProperty(ElasticsearchPersistentProperty property) {
        this.scriptProperties.add(new ScriptFieldPropertyImpl(property));
    }

    /**
     *
     */
    public class ScriptFieldPropertyImpl implements ScriptFieldProperty<T> {

        private final String fieldName;
        private final ElasticsearchPersistentProperty property;

        public ScriptFieldPropertyImpl(ElasticsearchPersistentProperty property) {
            this.property = property;
            String name = property.findAnnotation(ScriptedField.class).name();
            this.fieldName = StringUtils.isEmpty(name) ? property.getFieldName() : name;
        }

        @Override
        public void setScriptValue(T entity, Object value) {
            getPropertyAccessor(entity).setProperty(property, value);
        }


        public String getFieldName() {
            return this.fieldName;
        }
    }
}