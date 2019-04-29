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

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.ydespreaux.spring.data.elasticsearch.core.converter.serializer.PersistentEntityDeserializer;
import com.github.ydespreaux.spring.data.elasticsearch.core.converter.serializer.PersistentEntitySerializer;
import com.github.ydespreaux.spring.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * SerializerRegistry
 *
 * @author Yoann Despréaux
 * @since 1.0.2
 */
public class EntitySerializerRegistry {

    private static final String JOIN_TYPE_FIELD = "name";


    private final SimpleModule deserializersCustomModule = new SimpleModule("deserializersCustomModule");
    private final ObjectMapper mapper;
    private Map<Class<?>, ElasticsearchPersistentEntity<?>> customSerializers = new ConcurrentHashMap<>();
    private Map<Class<?>, JoinDescriptor<?>> descriptors = new ConcurrentHashMap<>();
    private Map<Class<?>, Map<String, Class<?>>> relationships = new ConcurrentHashMap<>();
    private Map<Class<?>, Boolean> registry = new ConcurrentHashMap<>();

    EntitySerializerRegistry(ObjectMapper mapper) {
        this.mapper = mapper;
        deserializersCustomModule.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                if (customSerializers.containsKey(beanDesc.getBeanClass())) {
                    return new PersistentEntityDeserializer(deserializer, customSerializers.get(beanDesc.getBeanClass()));
                }
                return deserializer;
            }
        });
        mapper.registerModule(deserializersCustomModule);
    }

    private boolean isJoinDocument(Class<?> clazz) {
        Boolean isJoinDocument = this.registry.get(clazz);
        return isJoinDocument != null && isJoinDocument;
    }

    /**
     * @param clazz
     * @param source
     * @param <S>
     * @param <T>
     * @return
     * @throws IOException
     */
    public <S extends T, T> Class<S> getEntityClassFromJson(Class<T> clazz, String source) throws IOException {
        if (isJoinDocument(clazz)) {
            return getEntityClassFromJson((JoinDescriptor<T>) descriptors.get(clazz), source);
        }
        return (Class<S>) clazz;
    }

    /**
     * @param persistentEntity
     * @param <T>
     */
    public <T> void register(ElasticsearchPersistentEntity<T> persistentEntity) {
        Class<T> javaType = persistentEntity.getJavaType();
        if (this.registry.containsKey(javaType)) {
            return;
        }
        if (isCustomSerializer(persistentEntity)) {
            this.customSerializers.put(javaType, persistentEntity);
        }
        if (isCustomDeserializer(persistentEntity)) {
            deserializersCustomModule.addSerializer(javaType, new PersistentEntitySerializer<>(persistentEntity));
            // Update registry module
            mapper.registerModule(deserializersCustomModule);
        }
        if (persistentEntity.isParentDocument() || persistentEntity.isChildDocument()) {
            addRelationship(persistentEntity);
        }
        this.registry.put(javaType, persistentEntity.isParentDocument() || persistentEntity.isChildDocument());
    }

    private <T> boolean isCustomSerializer(ElasticsearchPersistentEntity<T> persistentEntity) {
        return persistentEntity.isChildDocument();
    }

    private <T> boolean isCustomDeserializer(ElasticsearchPersistentEntity<T> persistentEntity) {
        return persistentEntity.isParentDocument() || persistentEntity.isChildDocument();
    }

    /**
     * @param descriptor
     * @param json
     * @return
     */
    private <S extends T, T> Class<S> getEntityClassFromJson(JoinDescriptor<T> descriptor, String json) throws IOException {
        JsonNode rootNode = new ObjectMapper().readTree(json);
        String fieldName = descriptor.getName();
        if (rootNode.has(fieldName)) {
            JsonNode joinNode = rootNode.get(fieldName);
            if (joinNode.has(JOIN_TYPE_FIELD)) {
                String type = joinNode.get(JOIN_TYPE_FIELD).asText();
                return getEntityClassByType(descriptor.getJavaType(), type);
            }
        }
        return (Class<S>) descriptor.getJavaType();
    }

    /**
     * @param entityClass
     * @param type
     * @param <T>
     * @return
     */
    private <S extends T, T> Class<S> getEntityClassByType(Class<T> entityClass, String type) {
        Class<S> childClass = (Class<S>) getChildEntityClassByTypeImpl(entityClass, type, new HashSet<>());
        if (childClass == null) {
            throw new InvalidDataAccessApiUsageException(format("No entity found with TYPE %s for the parent document %s", type, entityClass.getSimpleName()));
        }
        return childClass;
    }

    @Nullable
    private Class<?> getChildEntityClassByTypeImpl(Class<?> entityClass, String type, Set<Class<?>> discovryClasses) {
        if (relationships.containsKey(entityClass)) {
            Map<String, Class<?>> relationsByEntity = relationships.get(entityClass);
            if (relationsByEntity.containsKey(type)) {
                return relationsByEntity.get(type);
            } else {
                discovryClasses.add(entityClass);
                List<Class<?>> childrenClass = relationsByEntity.values().stream().filter(subClass -> !discovryClasses.contains(subClass)).collect(Collectors.toList());
                for (Class<?> subChildClass : childrenClass) {
                    Class<?> childClass = getChildEntityClassByTypeImpl(subChildClass, type, discovryClasses);
                    if (childClass != null) {
                        return childClass;
                    }
                }
            }
        }
        return null;
    }

    private void addRelationship(ElasticsearchPersistentEntity<?> persistentEntity) {
        JoinDescriptor<?> descriptor = persistentEntity.getJoinDescriptor();
        if (descriptor == null) {
            return;
        }
        descriptors.put(persistentEntity.getJavaType(), descriptor);
        this.addRelationship(persistentEntity.getJavaType(), persistentEntity.getJavaType(), descriptor.getType());
        if (descriptor.isChildDocument()) {
            this.addRelationship(descriptor.getParentJavaType(), persistentEntity.getJavaType(), descriptor.getType());
            this.addRelationship(descriptor.getJavaType(), descriptor.getParentJavaType(), descriptor.getParent().getType());
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
}
