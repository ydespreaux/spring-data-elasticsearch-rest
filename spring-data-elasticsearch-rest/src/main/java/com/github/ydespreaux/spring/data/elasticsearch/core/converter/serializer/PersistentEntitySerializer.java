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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.github.ydespreaux.spring.data.elasticsearch.core.JoinDescriptor;
import com.github.ydespreaux.spring.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

/**
 * PersistentEntitySerializer
 *
 * @author Yoann Despréaux
 * @since 1.0.1
 */
public class PersistentEntitySerializer<T> extends JsonSerializer<T> {

    private final ElasticsearchPersistentEntity<T> persistentEntity;

    public PersistentEntitySerializer(ElasticsearchPersistentEntity<T> persistentEntity) {
        this.persistentEntity = persistentEntity;
    }

    @Override
    public void serialize(T value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        JavaType javaType = serializerProvider.constructType(persistentEntity.getJavaType());
        BeanDescription beanDesc = serializerProvider.getConfig().introspect(javaType);
        JsonSerializer<Object> serializer = BeanSerializerFactory.instance.findBeanSerializer(serializerProvider,
                javaType,
                beanDesc);
        // this is basically your 'writeAllFields()'-method:
        serializer.unwrappingSerializer(null).serialize(value, jsonGenerator, serializerProvider);

        JoinDescriptor<T> descriptor = this.persistentEntity.getJoinDescriptor();
        if (this.persistentEntity.isChildDocument()) {
            jsonGenerator.writeObjectField(descriptor.getName(), JoinType.builder().name(descriptor.getType()).parent(this.persistentEntity.getParentId(value)).build());
        } else if (this.persistentEntity.isParentDocument()) {
            jsonGenerator.writeObjectField(descriptor.getName(), JoinType.builder().name(descriptor.getType()).build());
        }
        jsonGenerator.writeEndObject();
    }

    @Getter
    @Setter
    @Builder
    static class JoinType {

        private String name;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Object parent;
    }
}
