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

import com.github.ydespreaux.spring.data.elasticsearch.annotations.Child;
import com.github.ydespreaux.spring.data.elasticsearch.annotations.Parent;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.mapping.MappingException;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import static com.github.ydespreaux.spring.data.elasticsearch.core.utils.ContextUtils.getEnvironmentValue;
import static java.lang.String.format;

/**
 * JoinDescriptorBuilder
 *
 * @author Yoann Despréaux
 * @since 1.0.2
 */

public class JoinDescriptorBuilder<T> {


    @Nullable
    private final Environment environment;
    private final Class<T> entityClass;

    public JoinDescriptorBuilder(@Nullable ApplicationContext context, Class<T> entityClass) {
        this.environment = context == null ? null : context.getEnvironment();
        this.entityClass = entityClass;
    }

    @Nullable
    private static <T> Parent findRootParentAnnotation(Class<T> entityClass) {
        Class<? super T> parentClass = entityClass.getSuperclass();
        while (parentClass != null) {
            if (parentClass.isAnnotationPresent(Parent.class)) {
                return parentClass.getAnnotation(Parent.class);
            }
            parentClass = parentClass.getSuperclass();
        }
        return null;
    }

    @Nullable
    private static <T> Class<? super T> findParentClass(Class<T> entityClass) {
        Class<? super T> parentClass = entityClass.getSuperclass();
        while (parentClass != null) {
            if (parentClass.isAnnotationPresent(Parent.class)
                    || (parentClass.isAnnotationPresent(Child.class) && parentClass.getAnnotation(Child.class).isParent())) {
                return parentClass;
            }
            parentClass = parentClass.getSuperclass();
        }
        return null;
    }

    private static <T> String findParentType(Class<? super T> parentClass) {
        if (parentClass.isAnnotationPresent(Parent.class)) {
            return parentClass.getAnnotation(Parent.class).type();
        } else if (parentClass.isAnnotationPresent(Child.class)) {
            return parentClass.getAnnotation(Child.class).type();
        }
        throw new MappingException(format("Parent class (%s) must be annotated by Parent or Child", parentClass.getSimpleName()));
    }

    /**
     * @return
     */
    public JoinDescriptor<T> build() {
        if (this.entityClass.isAnnotationPresent(Parent.class)) {
            return buildParentDescriptor();
        } else if (this.entityClass.isAnnotationPresent(Child.class)) {
            return buildChildDescriptor();
        }
        throw new MappingException(format("Entity (%s) must be annotated by Parent or Child", this.entityClass.getSimpleName()));
    }

    /**
     * @return
     */
    private JoinDescriptor<T> buildParentDescriptor() {
        Parent parentAnnotation = this.entityClass.getAnnotation(Parent.class);
        if (StringUtils.isEmpty(parentAnnotation.name())) {
            throw new MappingException("name attribute is mandatory. Check your mapping configuration!");
        }
        if (StringUtils.isEmpty(parentAnnotation.type())) {
            throw new MappingException("type attribute is mandatory. Check your mapping configuration!");
        }
        return new JoinDescriptor<T>()
                .parentDocument(true)
                .name(getEnvironmentValue(this.environment, parentAnnotation.name()))
                .type(getEnvironmentValue(this.environment, parentAnnotation.type()))
                .javaType(this.entityClass);
    }

    /**
     * @return
     */
    private JoinDescriptor<T> buildChildDescriptor() {
        Parent rootParentAnnotation = findRootParentAnnotation(this.entityClass);
        if (rootParentAnnotation == null) {
            throw new MappingException("Child class no extends to a parentDocument class");
        }
        Class<? super T> parentClass = findParentClass(this.entityClass);
        if (parentClass == null) {
            throw new MappingException(format("No parent document found for class '%s'. Check your mapping configuration!", this.entityClass.getSimpleName()));
        }
        Child childAnnotation = this.entityClass.getAnnotation(Child.class);
        if (StringUtils.isEmpty(childAnnotation.routing())) {
            throw new MappingException("routing attribute is mandatory. Check your mapping configuration!");
        }
        if (StringUtils.isEmpty(childAnnotation.type())) {
            throw new MappingException("type attribute is mandatory. Check your mapping configuration!");
        }
        String joinName = getEnvironmentValue(this.environment, rootParentAnnotation.name());
        String parentType = getEnvironmentValue(this.environment, findParentType(parentClass));
        if (StringUtils.isEmpty(joinName)) {
            throw new MappingException("name attribute is mandatory. Check your mapping configuration!");
        }
        return new JoinDescriptor<T>()
                .childDocument(true)
                .parentDocument(childAnnotation.isParent())
                .name(joinName)
                .type(getEnvironmentValue(this.environment, childAnnotation.type()))
                .routing(getEnvironmentValue(this.environment, childAnnotation.routing()))
                .javaType(this.entityClass)
                .parent(new JoinDescriptor<>()
                        .parentDocument(true)
                        .name(joinName)
                        .type(parentType)
                        .javaType((Class<Object>) parentClass)
                );
    }

}
