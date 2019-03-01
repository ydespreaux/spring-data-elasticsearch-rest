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

/**
 * ChildDescriptor
 *
 * @author yoann Despréaux
 * @since 1.0.2
 */
@Getter
@Setter
public class ChildDescriptor<T> extends JoinDescriptor<ChildDescriptor<T>, T> {

    private String routing;
    private ParentDescriptor parent;


    public ChildDescriptor<T> routing(String routing) {
        this.routing = routing;
        return this;
    }

    public ChildDescriptor<T> parent(ParentDescriptor parent) {
        this.parent = parent;
        return this;
    }

    public Class<? super T> getParentJavaType() {
        return this.parent.getJavaType();
    }
}
