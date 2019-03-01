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

@Getter
@Setter
public abstract class JoinDescriptor<D extends JoinDescriptor, T> {

    private String name;
    private String type;
    private Class<T> javaType;

    public D name(String name) {
        this.name = name;
        return (D) this;
    }

    public D type(String type) {
        this.type = type;
        return (D) this;
    }

    public D javaType(Class<T> javaType) {
        this.javaType = javaType;
        return (D) this;
    }
}
