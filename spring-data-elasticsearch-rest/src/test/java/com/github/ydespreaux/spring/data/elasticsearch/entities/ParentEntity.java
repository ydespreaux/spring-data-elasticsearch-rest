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
package com.github.ydespreaux.spring.data.elasticsearch.entities;

import com.github.ydespreaux.spring.data.elasticsearch.annotations.Index;
import com.github.ydespreaux.spring.data.elasticsearch.annotations.IndexedDocument;
import com.github.ydespreaux.spring.data.elasticsearch.annotations.Parent;
import org.springframework.core.style.ToStringCreator;
import org.springframework.data.annotation.Id;

/**
 * ParentEntity
 *
 * @author Yoann Despréaux
 */
@Parent(name = "relation", type = "question")
@IndexedDocument(index = @Index(name = ParentEntity.INDEX, type = ParentEntity.TYPE, settingsAndMappingPath = "classpath:indices/parent-child.index"))
public class ParentEntity {

    public static final String INDEX = "parent-child";
    public static final String TYPE = "parent-child";

    @Id
    private String id;
    private String name;

    public ParentEntity() {
    }

    public ParentEntity(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return new ToStringCreator(this).append("id", id).append("name", name).toString();
    }

    @IndexedDocument(index = @Index(name = ParentEntity.INDEX, type = TYPE, createIndex = false))
    public static class ChildEntity {

        @Id
        private String id;

        @Parent(name = "relation", type = "answer", routing = "1")
        private String parentId;

        private String name;

        public ChildEntity() {
        }

        public ChildEntity(String id, String parentId, String name) {
            this.id = id;
            this.parentId = parentId;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getParentId() {
            return this.parentId;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return new ToStringCreator(this).append("id", id).append("parentId", getParentId()).append("name", name).toString();
        }
    }
}
