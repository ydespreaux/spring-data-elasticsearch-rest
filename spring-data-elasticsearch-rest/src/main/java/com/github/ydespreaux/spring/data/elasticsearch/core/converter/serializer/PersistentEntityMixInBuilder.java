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

import com.github.ydespreaux.spring.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import org.elasticsearch.ElasticsearchException;
import org.springframework.util.StringUtils;

public class PersistentEntityMixInBuilder {

    private static final String DEFAULT_PACKAGE = "com.github.ydespreaux.spring.data.elasticsearch.javassist.generated";

    private final ElasticsearchPersistentEntity<?> persistentEntity;

    public PersistentEntityMixInBuilder(ElasticsearchPersistentEntity<?> persistentEntity) {
        this.persistentEntity = persistentEntity;
    }


    public Class<?> build() {
        String className = DEFAULT_PACKAGE + "." + StringUtils.capitalize(persistentEntity.getJavaType().getSimpleName() + "Mixin");

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
        }

        ClassPool classPool = ClassPool.getDefault();
        CtClass ctClass = classPool.makeClass(DEFAULT_PACKAGE + "." + StringUtils.capitalize(persistentEntity.getJavaType().getSimpleName() + "Mixin"));
//        if (persistentEntity.hasGeoShapeProperty()) {
//            ClassFile classFile = ctClass.getClassFile2();
//            persistentEntity.getGeoShapeProperties().forEach(property -> {
//                try {
//                    CtClass attributClazz = ClassPool.getDefault().get(property.getJavaType().getName());
//                    CtField field = new CtField(attributClazz, property.getFieldName(), ctClass);
//
//                    // create the annotation
//                    AnnotationsAttribute jsonIgnoreAnnotation = new AnnotationsAttribute(classFile.getConstPool(), AnnotationsAttribute.visibleTag);
//                    Annotation annot = new Annotation(JsonIgnore.class.getName(), classFile.getConstPool());
//                    jsonIgnoreAnnotation.addAnnotation(annot);
//                    field.getFieldInfo2().addAttribute(jsonIgnoreAnnotation);
//                    ctClass.addField(field);
//                } catch (NotFoundException | CannotCompileException e) {
//                    e.printStackTrace();
//                    throw new MappingException(e.getMessage());
//                }
//            });
//        }
        try {
            return ctClass.toClass();
        } catch (CannotCompileException e) {
            throw new ElasticsearchException(e);
        }
    }
}
