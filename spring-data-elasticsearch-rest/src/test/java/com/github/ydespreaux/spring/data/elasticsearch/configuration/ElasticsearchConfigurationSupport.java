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

package com.github.ydespreaux.spring.data.elasticsearch.configuration;

import com.github.ydespreaux.spring.data.elasticsearch.client.DefaultRestElasticsearchClient;
import com.github.ydespreaux.spring.data.elasticsearch.client.RestElasticsearchClient;
import com.github.ydespreaux.spring.data.elasticsearch.config.IngestTemplate;
import com.github.ydespreaux.spring.data.elasticsearch.config.TemplateProperties;
import com.github.ydespreaux.spring.data.elasticsearch.core.*;
import com.github.ydespreaux.spring.data.elasticsearch.core.converter.ElasticsearchConverter;
import com.github.ydespreaux.spring.data.elasticsearch.core.converter.MappingElasticsearchConverter;
import com.github.ydespreaux.spring.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import com.github.ydespreaux.spring.data.elasticsearch.core.triggers.TriggerManager;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
@EnableConfigurationProperties({JacksonProperties.class, TemplateProperties.class})
public class ElasticsearchConfigurationSupport {

    @Autowired
    private TemplateProperties templateProperties;

    @Bean
    RestElasticsearchClient restElasticsearchClient(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") final RestHighLevelClient client) {
        return new DefaultRestElasticsearchClient(client);
    }

    @Bean
    ElasticsearchOperations restElasticsearchTemplate(
            final RestElasticsearchClient client,
            final ElasticsearchConverter converter,
            final ResultsMapper resultsMapper,
            final TriggerManager triggerManager) {
        ElasticsearchTemplate template = new ElasticsearchTemplate(client, converter, resultsMapper, triggerManager);
        template.setIngestTemplate(new IngestTemplate(templateProperties, template));
        return template;
    }

    @Bean
    ElasticsearchConverter elasticsearchConverter(EntityMapper mapper) {
        return new MappingElasticsearchConverter(elasticsearchMappingContext(), mapper);
    }

    @Bean
    EntityMapper entityMapper(JacksonProperties jacksonPropetrties) {
        return new DefaultEntityMapper(jacksonPropetrties);
    }

    @Bean
    ResultsMapper resultsMapper(EntityMapper mapper, ElasticsearchConverter converter) {
        return new DefaultResultsMapper(mapper, converter);
    }

    /**
     * Creates a {@link SimpleElasticsearchMappingContext} equipped with entity classes scanned from the mapping base
     * package.
     *
     * @return never {@literal null}.
     */
    @Bean
    SimpleElasticsearchMappingContext elasticsearchMappingContext() {
        return new SimpleElasticsearchMappingContext();
    }

    @Bean
    TaskScheduler taskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    @Bean
    TriggerManager triggerManagement(TaskScheduler taskScheduler) {
        return new TriggerManager(taskScheduler);
    }
}
