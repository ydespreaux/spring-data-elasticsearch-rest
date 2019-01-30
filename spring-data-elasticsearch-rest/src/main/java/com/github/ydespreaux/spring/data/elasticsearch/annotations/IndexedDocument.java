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

package com.github.ydespreaux.spring.data.elasticsearch.annotations;


import com.github.ydespreaux.spring.data.elasticsearch.core.IndexTimeBasedSupport;

import java.lang.annotation.*;

/**
 * Cette annotation permet de définir un document à indexer dans elasticsearch utilisant un index time-based.
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface IndexedDocument {

    /**
     * Création de l'index si ce dernier n'existe pas.
     * La création de l'index se base sur le template correspondant pour la configuration, le mapping etc...
     */
    boolean createIndex() default true;

    /**
     * Nom de l'alias ou de l'index permettant d'effectuer des recherches dans elasticsearch
     *
     * @return the index name
     */
    String aliasName() default "";

    /**
     * Type du document à indexer
     *
     * @return the type name
     */
    String type() default "";

    /**
     * @return
     */
    String indexName() default "";

    /**
     * Pattern définissant l'index en court. Ce pattern défini le nom de l'index time-based.
     *
     * @return the index pattern
     */
    String indexPattern() default "";

    /**
     * La classe IndexTimeBasedSupport permet de générer le nom de l'index pour l'indexation de documents
     * en fonction de la date courante ainsi que le document à indexer.
     *
     * @return the index time based support
     */
    Class<? extends IndexTimeBasedSupport> indexTimeBasedSupport() default IndexTimeBasedSupport.class;

    /**
     * Path du fichier de configuration de l'index
     *
     * @return the index path
     */
    String settingsAndMappingPath() default "";

    /**
     * @return
     */
    long scrollTimeSeconds() default 300;

}

