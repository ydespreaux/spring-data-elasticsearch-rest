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

package com.github.ydespreaux.spring.data.elasticsearch.core.triggers;

import com.github.ydespreaux.spring.data.elasticsearch.core.ElasticsearchOperations;
import com.github.ydespreaux.spring.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;

@Slf4j
public class RolloverTrigger<T> extends AbstractRolloverTrigger<T> {

    private final ElasticsearchOperations elasticsearchOperations;

    public RolloverTrigger(ElasticsearchOperations elasticsearchOperations, ElasticsearchPersistentEntity<T> persistentEntity, String cronExpression) {
        super(persistentEntity, cronExpression);
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public Runnable processor() {
        return () -> {
            try {
                elasticsearchOperations.rolloverIndex(getPersistentEntity().getJavaType());
            } catch (ElasticsearchException e) {
                if (log.isWarnEnabled()) {
                    log.warn("Rollover index {} failed : {}", getPersistentEntity().getAliasOrIndexWriter(), e.getLocalizedMessage());
                }
            }
        };
    }
}
