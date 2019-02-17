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
package com.github.ydespreaux.spring.data.autoconfigure.elasticsearch;

import com.github.ydespreaux.spring.data.elasticsearch.core.DefaultEntityMapper;
import com.github.ydespreaux.spring.data.elasticsearch.core.DefaultResultsMapper;
import com.github.ydespreaux.spring.data.elasticsearch.core.EntityMapper;
import com.github.ydespreaux.spring.data.elasticsearch.core.ResultsMapper;
import com.github.ydespreaux.spring.data.elasticsearch.core.converter.ElasticsearchConverter;
import com.github.ydespreaux.spring.data.elasticsearch.core.converter.MappingElasticsearchConverter;
import com.github.ydespreaux.spring.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import com.github.ydespreaux.spring.data.elasticsearch.core.triggers.TriggerManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * @author Yoann Despréaux
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableScheduling
@AutoConfigureAfter(RestClientAutoConfiguration.class)
@EnableConfigurationProperties({JacksonProperties.class})
public class ElasticsearchConfigurationSupport {

    @Bean
    ElasticsearchConverter elasticsearchConverter() {
        return new MappingElasticsearchConverter(elasticsearchMappingContext());
    }

    @Bean
    @ConditionalOnMissingBean
    EntityMapper entityMapper(JacksonProperties jacksonPropetrties) {
        return new DefaultEntityMapper(jacksonPropetrties);
    }

    @Bean
    @ConditionalOnMissingBean
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
    @SneakyThrows
    SimpleElasticsearchMappingContext elasticsearchMappingContext() {
        return new SimpleElasticsearchMappingContext();
    }

    @Bean
    @ConditionalOnMissingBean
    TaskScheduler taskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    @Bean
    @ConditionalOnMissingBean
    TriggerManager triggerManagement(TaskScheduler taskScheduler) {
        return new TriggerManager(taskScheduler);
    }
}
