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
import com.github.ydespreaux.spring.data.elasticsearch.core.request.config.RolloverConfig;
import com.github.ydespreaux.spring.data.elasticsearch.repository.support.ElasticsearchEntityInformation;
import org.elasticsearch.ElasticsearchException;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.util.StringUtils;

public class RolloverTrigger implements Trigger {

    private static final String DEFAULT_CRON_EXPRESSION = "0 */1 * * * *";

    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchEntityInformation<?, ?> entityInformation;
    private final CronTrigger cronTrigger;

    public RolloverTrigger(ElasticsearchOperations elasticsearchOperations, ElasticsearchEntityInformation<?, ?> entityInformation, String cronExpression) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.entityInformation = entityInformation;
        this.cronTrigger = StringUtils.isEmpty(cronExpression) ? new CronTrigger(DEFAULT_CRON_EXPRESSION) : new CronTrigger(cronExpression);
    }

    @Override
    public CronTrigger getCronTrigger() {
        return this.cronTrigger;
    }

    @Override
    public ElasticsearchEntityInformation<?, ?> getEntityInformation() {
        return this.entityInformation;
    }

    @Override
    public Runnable processor() {
        return () -> {
            RolloverConfig config = entityInformation.getRolloverConfig();
            try {
                elasticsearchOperations.rolloverIndex(config.getAlias().getName(), null, entityInformation.getIndexPath(), config.getConditions());
            } catch (ElasticsearchException e) {
            }
        };
    }
}
