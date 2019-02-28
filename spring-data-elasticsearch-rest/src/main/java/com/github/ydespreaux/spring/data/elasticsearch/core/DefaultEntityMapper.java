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

package com.github.ydespreaux.spring.data.elasticsearch.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.ydespreaux.spring.data.elasticsearch.annotations.IndexName;
import com.github.ydespreaux.spring.data.elasticsearch.annotations.ParentId;
import com.github.ydespreaux.spring.data.elasticsearch.annotations.Score;
import com.github.ydespreaux.spring.data.elasticsearch.core.converter.ElasticsearchTypeModule;
import com.github.ydespreaux.spring.data.elasticsearch.core.converter.serializer.PersistentEntityDeserializer;
import com.github.ydespreaux.spring.data.elasticsearch.core.converter.serializer.PersistentEntitySerializer;
import com.github.ydespreaux.spring.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import org.elasticsearch.ElasticsearchException;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;

/**
 * EntityMapper based on a Jackson {@link ObjectMapper}.
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public class DefaultEntityMapper implements EntityMapper {

    private static final String JOIN_TYPE_FIELD = "name";

    private final ObjectMapper mapper;

    private final SimpleModule deserializersCustomModule = new SimpleModule("deserializersCustomModule");

    private Map<Class<?>, ElasticsearchPersistentEntity<?>> persistentEntitySerializers = new ConcurrentHashMap<>();
    private Map<Class<?>, ParentDescriptor> parentDescriptors = new ConcurrentHashMap<>();
    private Map<Class<?>, Map<String, Class<?>>> relationships = new ConcurrentHashMap<>();
    private Set<Class<?>> entityClassRegistry = new HashSet<>();


    /**
     * @param jacksonProperties the jackson properties
     */
    public DefaultEntityMapper(final JacksonProperties jacksonProperties) {
        mapper = new ObjectMapper();
        configure(jacksonProperties);
    }

    protected void configure(final JacksonProperties jacksonProperties) {
        if (jacksonProperties.getDateFormat() != null) {
            mapper.setDateFormat(new SimpleDateFormat(jacksonProperties.getDateFormat()));
        } else {
            mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
        }
        mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {

            @Override
            public boolean hasIgnoreMarker(AnnotatedMember m) {
                return m.hasAnnotation(Id.class)
                        || m.hasAnnotation(Version.class)
                        || m.hasAnnotation(IndexName.class)
                        || m.hasAnnotation(Score.class)
                        || m.hasAnnotation(ParentId.class)
                        || super.hasIgnoreMarker(m);
            }
        });
        mapper.registerModules(new JavaTimeModule(), new ElasticsearchTypeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        deserializersCustomModule.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                if (persistentEntitySerializers.containsKey(beanDesc.getBeanClass())) {
                    return new PersistentEntityDeserializer(deserializer, persistentEntitySerializers.get(beanDesc.getBeanClass()));
                }
                return deserializer;
            }
        });
        mapper.registerModule(deserializersCustomModule);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.EntityMapper#mapToString(java.lang.Object)
     */
    @Override
    public <T> String mapToString(T object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ElasticsearchException("Json processing failed : ", e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.EntityMapper#mapToObject(java.lang.String, java.lang.Class)
     */
    @Override
    public <S extends T, T> S mapToObject(String source, Class<T> clazz) {
        try {
            if (parentDescriptors.containsKey(clazz)) {
                Class<S> childClass = getChildClassFromJson(parentDescriptors.get(clazz), source);
                return mapper.readValue(source, childClass);
            }
            return (S) mapper.readValue(source, clazz);
        } catch (IOException e) {
            throw new ElasticsearchException("Json processing failed : ", e);
        }
    }

    /**
     * @param persistentEntity
     */
    @Override
    public <T> void register(ElasticsearchPersistentEntity<T> persistentEntity) {
        Class<T> javaType = persistentEntity.getJavaType();
        if (this.entityClassRegistry.contains(javaType)) {
            return;
        }
        if (persistentEntity.isParentDocument() || persistentEntity.isChildDocument()) {
            addRelationship(persistentEntity);
            deserializersCustomModule.addSerializer(javaType, new PersistentEntitySerializer<>(persistentEntity));
            if (persistentEntity.isChildDocument()) {
                persistentEntitySerializers.put(javaType, persistentEntity);
            }
            // Update registry module
            mapper.registerModule(deserializersCustomModule);
        }
        this.entityClassRegistry.add(javaType);
    }

    private void addRelationship(ElasticsearchPersistentEntity<?> persistentEntity) {
        if (persistentEntity.isParentDocument()) {
            parentDescriptors.put(persistentEntity.getJavaType(), persistentEntity.getParentDescriptor());
            this.addRelationship(persistentEntity.getJavaType(), persistentEntity.getJavaType(), persistentEntity.getParentDescriptor().getType());
        } else if (persistentEntity.isChildDocument()) {
            ChildDescriptor<?> descriptor = persistentEntity.getChildDescriptor();
            this.addRelationship(descriptor.getParentJavaType(), persistentEntity.getJavaType(), descriptor.getType());
        }
    }

    /**
     * @param parentClass
     * @param childClass
     * @param type
     */
    private void addRelationship(Class<?> parentClass, Class<?> childClass, String type) {
        if (!this.relationships.containsKey(parentClass)) {
            this.relationships.put(parentClass, new HashMap<>());
        }
        this.relationships.get(parentClass).put(type, childClass);
    }

    /**
     * @param descriptor
     * @param json
     * @return
     */
    private <S extends T, T> Class<S> getChildClassFromJson(ParentDescriptor<T> descriptor, String json) throws IOException {
        JsonNode rootNode = new ObjectMapper().readTree(json);
        String fieldName = descriptor.getName();
        if (rootNode.has(fieldName)) {
            JsonNode joinNode = rootNode.get(fieldName);
            if (joinNode.has(JOIN_TYPE_FIELD)) {
                String type = joinNode.get(JOIN_TYPE_FIELD).asText();
                return getChildClassByType(descriptor.getJavaType(), type);
            }
        }
        return (Class<S>) descriptor.getJavaType();
    }

    /**
     * @param parentClass
     * @param type
     * @param <T>
     * @return
     */
    private <S extends T, T> Class<S> getChildClassByType(Class<T> parentClass, String type) {
        Class<S> childClass = null;
        if (relationships.containsKey(parentClass)) {
            childClass = (Class<S>) relationships.get(parentClass).get(type);
        }
        if (childClass == null) {
            throw new InvalidDataAccessApiUsageException(format("No child found with type %s for the parent document %s", type, parentClass.getSimpleName()));
        }
        return childClass;
    }
}
