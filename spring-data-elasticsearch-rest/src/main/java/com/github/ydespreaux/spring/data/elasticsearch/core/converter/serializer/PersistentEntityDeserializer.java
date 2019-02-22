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

package com.github.ydespreaux.spring.data.elasticsearch.core.converter.serializer;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.github.ydespreaux.spring.data.elasticsearch.core.ParentDescriptor;
import com.github.ydespreaux.spring.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;

import java.io.IOException;

/**
 * PersistentEntityDeserializer
 *
 * @author Yoann Despréaux
 * @since 1.0.1
 */
public class PersistentEntityDeserializer<T> extends JsonDeserializer<T> implements ResolvableDeserializer {

    private final JsonDeserializer<T> defaultDeserializer;
    private final ElasticsearchPersistentEntity<T> persistentEntity;

    public PersistentEntityDeserializer(JsonDeserializer<T> deserializer, ElasticsearchPersistentEntity<T> persistentEntity) {
        this.defaultDeserializer = deserializer;
        this.persistentEntity = persistentEntity;
    }

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonLocation startLocation = jsonParser.getCurrentLocation();
        T bean = this.defaultDeserializer.deserialize(jsonParser, deserializationContext);
        if (persistentEntity.hasParent()) {
            JsonNode node = new ObjectMapper().readTree(startLocation.getSourceRef().toString());
            ParentDescriptor parentDescriptor = persistentEntity.getParentDescriptor();
            String fieldName = parentDescriptor.getName();
            if (node.has(fieldName)) {
                JsonNode joinNode = node.get(fieldName);
                if (joinNode.has("parent")) {
                    persistentEntity.setParentId(bean, joinNode.get("parent").asText());
                }
            }
        }
        return bean;
    }

    @Override
    public void resolve(DeserializationContext deserializationContext) throws JsonMappingException {
        ((ResolvableDeserializer) defaultDeserializer).resolve(deserializationContext);
    }
}
