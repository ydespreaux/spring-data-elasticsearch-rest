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
import com.github.ydespreaux.spring.data.elasticsearch.core.completion.Completion;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.lang.Nullable;

import java.util.*;

/**
 * Elasticsearch specific {@link org.springframework.data.mapping.PersistentProperty} implementation processing
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public class SimpleElasticsearchPersistentProperty extends
        AnnotationBasedPersistentProperty<ElasticsearchPersistentProperty> implements ElasticsearchPersistentProperty {

    private static final List<String> SUPPORTED_ID_PROPERTY_NAMES = Arrays.asList("id", "document");

    private final boolean isScore;
    private final boolean isParent;
    private final boolean isId;
    private final boolean isIndexName;
    private final boolean isCompletion;
    private final boolean isScript;

    public SimpleElasticsearchPersistentProperty(Property property,
                                                 PersistentEntity<?, ElasticsearchPersistentProperty> owner,
                                                 SimpleTypeHolder simpleTypeHolder) {

        super(property, owner, simpleTypeHolder);

        this.isId = super.isIdProperty() || SUPPORTED_ID_PROPERTY_NAMES.contains(getFieldName());
        this.isScore = isAnnotationPresent(Score.class);
        this.isParent = isAnnotationPresent(ParentId.class);
        this.isIndexName = isAnnotationPresent(IndexName.class);
        this.isCompletion = isAnnotationPresent(CompletionField.class);
        this.isScript = isAnnotationPresent(ScriptedField.class);

        if (isIdProperty() && !Arrays.asList(UUID.class, String.class).contains(getType())) {
            throw new MappingException(String.format("Id property %s must be of type String or UUID. Check your mapping configuration!", property.getName()));
        }
        if (isVersionProperty() && getType() != Long.class) {
            throw new MappingException(String.format("Version property %s must be of type Long!", property.getName()));
        }

        if (isScore && !Arrays.asList(Float.TYPE, Float.class).contains(getType())) {
            throw new MappingException(
                    String.format("Score property %s must be either of type float or Float!", property.getName()));
        }

        if (isParent && !Arrays.asList(UUID.class, String.class).contains(getType())) {
            throw new MappingException(String.format("Parent property %s must be of type String or UUID. Check your mapping configuration!", property.getName()));
        }

        if (isIndexName && getType() != String.class) {
            throw new MappingException(String.format("IndexName property %s must be of type String!", property.getName()));
        }

        if (isCompletion && getType() != Completion.class
                && Collection.class.isAssignableFrom(getType()) && (getType().getTypeParameters()[0]).getGenericDeclaration() != Completion.class) {
            throw new MappingException(String.format("Completion property %s must be of type Completion or Collection<Completion> !", property.getName()));
        }
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty#getName()
     */
    @Override
    public String getFieldName() {
        return getProperty().getName();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.model.AnnotationBasedPersistentProperty#isIdProperty()
     */
    @Override
    public boolean isIdProperty() {
        return isId;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.model.AbstractPersistentProperty#createAssociation()
     */
    @Nullable
    @Override
    protected Association<ElasticsearchPersistentProperty> createAssociation() {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty#isScoreProperty()
     */
    @Override
    public boolean isScoreProperty() {
        return isScore;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mapping.model.AbstractPersistentProperty#isImmutable()
     */
    @Override
    public boolean isImmutable() {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty#isParentProperty()
     */
    @Override
    public boolean isParentProperty() {
        return isParent;
    }

    /**
     * @return
     */
    @Override
    public boolean isIndexNameProperty() {
        return this.isIndexName;
    }

    /**
     * @return
     */
    @Override
    public boolean isCompletionProperty() {
        return this.isCompletion;
    }

    /**
     * @return
     */
    @Override
    public boolean isScriptProperty() {
        return this.isScript;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleElasticsearchPersistentProperty)) return false;
        if (!super.equals(o)) return false;
        SimpleElasticsearchPersistentProperty that = (SimpleElasticsearchPersistentProperty) o;
        return isScore == that.isScore &&
                isParent == that.isParent &&
                isId == that.isId &&
                isCompletion == that.isCompletion &&
                isIndexName == that.isIndexName &&
                isScript == that.isScript;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isScore, isParent, isId, isCompletion, isIndexName, isScript);
    }
}
