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

import com.github.ydespreaux.spring.data.elasticsearch.Versions;
import com.github.ydespreaux.spring.data.elasticsearch.client.ClientLoggerAspect;
import com.github.ydespreaux.spring.data.elasticsearch.configuration.ElasticsearchConfigurationSupport;
import com.github.ydespreaux.spring.data.elasticsearch.entities.ArticleTimeBasedSupport;
import com.github.ydespreaux.spring.data.elasticsearch.entities.City;
import com.github.ydespreaux.spring.data.elasticsearch.entities.CityTimeBasedSupport;
import com.github.ydespreaux.spring.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import com.github.ydespreaux.spring.data.elasticsearch.utils.AdminClientUtils;
import com.github.ydespreaux.testcontainers.elasticsearch.ElasticsearchContainer;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.IndexTemplateMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Tag("integration")
@SpringBootTest(classes = {
        RestClientAutoConfiguration.class,
        ElasticsearchTemplateMappingTest.ElasticsearchConfiguration.class})
@Profile("test-no-template")
@Testcontainers
public class ElasticsearchTemplateMappingTest {

    @Container
    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer(Versions.ELASTICSEARCH_VERSION)
            .withConfigDirectory("elastic-config");

    private static final String INDEX_BOOK_NAME = "books";

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;
    @Autowired
    private RestHighLevelClient client;

    @Test
    void articleIndexSettignsAndMapping() throws Exception {

        IndexTemplateMetaData template = AdminClientUtils.getTemplate(client, "article");
        assertThat(template, Matchers.is(notNullValue()));
        assertThat(template.name(), Matchers.is(equalTo("article")));
        assertThat(template.patterns().size(), Matchers.is(equalTo(1)));
        assertThat(template.patterns().get(0), Matchers.is(equalTo("article-*")));
        Settings settings = template.settings();
        assertThat(settings, Matchers.is(notNullValue()));
        assertThat(settings.get("index.refresh_interval"), Matchers.is(equalTo("1s")));
        assertThat(settings.get("index.number_of_shards"), Matchers.is(equalTo("1")));
        assertThat(settings.get("index.number_of_replicas"), Matchers.is(equalTo("1")));
        assertThat(settings.get("index.store.TYPE"), Matchers.is(equalTo("fs")));
        assertThat(template.aliases().containsKey("articles"), Matchers.is(true));
        assertThat(template.mappings().type(), Matchers.is(equalTo("_doc")));
        //
        ArticleTimeBasedSupport timeBased = new ArticleTimeBasedSupport();
        String indexName = timeBased.buildIndex(IndexTimeBasedParameter.of("'article-%s-'yyyy", LocalDate.now(Clock.systemUTC())));
        assertThat(this.elasticsearchOperations.indexExists(indexName), Is.is(true));
    }

    @Test
    void cityIndexSettignsAndMapping() throws Exception {

        IndexTemplateMetaData template = AdminClientUtils.getTemplate(client, "city");
        assertThat(template, Matchers.is(notNullValue()));
        assertThat(template.name(), Matchers.is(equalTo("city")));
        assertThat(template.patterns().size(), Matchers.is(equalTo(2)));
        assertThat(template.patterns().get(0), Matchers.is(equalTo("ville-*")));
        assertThat(template.patterns().get(1), Matchers.is(equalTo("metropole-*")));
        Settings settings = template.settings();
        assertThat(settings, Matchers.is(notNullValue()));
        assertThat(settings.get("index.refresh_interval"), Matchers.is(equalTo("1s")));
        assertThat(settings.get("index.number_of_shards"), Matchers.is(equalTo("1")));
        assertThat(settings.get("index.number_of_replicas"), Matchers.is(equalTo("1")));
        assertThat(settings.get("index.store.TYPE"), Matchers.is(equalTo("fs")));
        assertThat(template.aliases().containsKey("cities"), Matchers.is(true));
        assertThat(template.mappings().type(), Matchers.is(equalTo("_doc")));
        //
        CityTimeBasedSupport timeBased = new CityTimeBasedSupport();
        String indexName = timeBased.buildIndex(IndexTimeBasedParameter.of("%s-%s", (City) null));
        assertThat(this.elasticsearchOperations.indexExists(indexName), Is.is(true));
    }

    @Test
    void bookIndexSettignsAndMapping() throws Exception {
        assertThat(this.elasticsearchOperations.indexExists(INDEX_BOOK_NAME), Matchers.is(true));

        GetMappingsRequest mappingsRequest = new GetMappingsRequest()
                .indices(INDEX_BOOK_NAME);
        GetMappingsResponse response = this.client.indices().getMapping(mappingsRequest, RequestOptions.DEFAULT);
        assertThat(response.getMappings().containsKey(INDEX_BOOK_NAME), is(true));
        assertThat(response.getMappings().get(INDEX_BOOK_NAME).containsKey("_doc"), is(true));

        GetSettingsRequest settingsRequest = new GetSettingsRequest()
                .indices(INDEX_BOOK_NAME);
        GetSettingsResponse settingsResponse = this.client.indices().getSettings(settingsRequest, RequestOptions.DEFAULT);

        ImmutableOpenMap<String, Settings> indexSettings = settingsResponse.getIndexToSettings();
        assertThat(indexSettings.containsKey(INDEX_BOOK_NAME), is(true));

        Settings settings = indexSettings.get(INDEX_BOOK_NAME);
        assertThat(settings.get("index.refresh_interval"), is(equalTo("1s")));
        assertThat(settings.get("index.number_of_shards"), is(equalTo("1")));
        assertThat(settings.get("index.number_of_replicas"), is(equalTo("1")));
        assertThat(settings.get("index.store.TYPE"), is(equalTo("fs")));
    }

    @Configuration
    @EnableAspectJAutoProxy
    @EnableAutoConfiguration
    @EnableElasticsearchRepositories(
            basePackages = {
                    "com.github.ydespreaux.spring.data.elasticsearch.repositories.template",
                    "com.github.ydespreaux.spring.data.elasticsearch.repositories.synonyms",
            },
            namedQueriesLocation = "classpath:named-queries/*-named-queries.properties")
    static class ElasticsearchConfiguration extends ElasticsearchConfigurationSupport {

        @Bean
        ClientLoggerAspect clientLoggerAspect() {
            return new ClientLoggerAspect();
        }
    }
}
