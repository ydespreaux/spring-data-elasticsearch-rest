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

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.Assert;

/**
 * JoinDescriptor
 *
 * @author Yoann Despréaux
 * @since 1.0.2
 */
@Getter
@Setter
public class JoinDescriptor<T> {

    private String name;
    private String type;
    private String routing;
    private JoinDescriptor<? super T> parent;
    private Class<T> javaType;
    private boolean parentDocument;
    private boolean childDocument;

    public JoinDescriptor<T> name(String name) {
        this.name = name;
        return this;
    }

    public JoinDescriptor<T> type(String type) {
        this.type = type;
        return this;
    }

    public JoinDescriptor<T> javaType(Class<T> javaType) {
        this.javaType = javaType;
        return this;
    }

    public JoinDescriptor<T> routing(String routing) {
        this.routing = routing;
        return this;
    }

    public JoinDescriptor<T> parent(JoinDescriptor<? super T> parent) {
        this.parent = parent;
        return this;
    }

    public Class<? super T> getParentJavaType() {
        Assert.notNull(this.parent, "parent must be defined");
        return this.parent.getJavaType();
    }

    public JoinDescriptor<T> parentDocument(boolean parentDocument) {
        this.parentDocument = parentDocument;
        return this;
    }

    public JoinDescriptor<T> childDocument(boolean childDocument) {
        this.childDocument = childDocument;
        return this;
    }
}
