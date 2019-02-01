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

package com.github.ydespreaux.spring.data.elasticsearch;

import com.github.ydespreaux.spring.data.elasticsearch.client.ClientLoggerAspect;
import com.github.ydespreaux.spring.data.elasticsearch.configuration.ElasticsearchConfigurationSupport;
import com.github.ydespreaux.spring.data.elasticsearch.core.ElasticsearchOperations;
import com.github.ydespreaux.spring.data.elasticsearch.core.IndexTimeBasedParameter;
import com.github.ydespreaux.spring.data.elasticsearch.entities.ArticleTimeBasedSupport;
import com.github.ydespreaux.spring.data.elasticsearch.entities.City;
import com.github.ydespreaux.spring.data.elasticsearch.entities.CityTimeBasedSupport;
import com.github.ydespreaux.spring.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import com.github.ydespreaux.spring.data.elasticsearch.utils.AdminClientUtils;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.metadata.IndexTemplateMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Clock;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        RestClientAutoConfiguration.class,
        ITElasticsearchTemplateMappingTest.ElasticsearchConfiguration.class})
@Profile("test-no-template")
public class ITElasticsearchTemplateMappingTest {

    private static final String INDEX_BOOK_NAME = "books";

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;
    @Autowired
    private RestHighLevelClient client;

    @Test
    public void articleIndexSettignsAndMapping() throws Exception {

        IndexTemplateMetaData template = AdminClientUtils.getTemplate(client, "article");
        Assert.assertThat(template, Matchers.is(notNullValue()));
        Assert.assertThat(template.getName(), Matchers.is(equalTo("article")));
        Assert.assertThat(template.getPatterns().size(), Matchers.is(equalTo(1)));
        Assert.assertThat(template.getPatterns().get(0), Matchers.is(equalTo("article-*")));
        Settings settings = template.getSettings();
        Assert.assertThat(settings, Matchers.is(notNullValue()));
        Assert.assertThat(settings.get("index.refresh_interval"), Matchers.is(equalTo("1s")));
        Assert.assertThat(settings.get("index.number_of_shards"), Matchers.is(equalTo("1")));
        Assert.assertThat(settings.get("index.number_of_replicas"), Matchers.is(equalTo("1")));
        Assert.assertThat(settings.get("index.store.type"), Matchers.is(equalTo("fs")));
        Assert.assertThat(template.getAliases().containsKey("articles"), Matchers.is(true));
        Assert.assertThat(template.getMappings().containsKey("article"), Matchers.is(true));
        //
        ArticleTimeBasedSupport timeBased = new ArticleTimeBasedSupport();
        String indexName = timeBased.buildIndex(IndexTimeBasedParameter.of("'article-%s-'yyyy", LocalDate.now(Clock.systemUTC())));
        Assert.assertThat(this.elasticsearchOperations.indexExists(indexName), Is.is(true));
    }

    @Test
    public void cityIndexSettignsAndMapping() throws Exception {

        IndexTemplateMetaData template = AdminClientUtils.getTemplate(client, "city");
        Assert.assertThat(template, Matchers.is(notNullValue()));
        Assert.assertThat(template.getName(), Matchers.is(equalTo("city")));
        Assert.assertThat(template.getPatterns().size(), Matchers.is(equalTo(2)));
        Assert.assertThat(template.getPatterns().get(0), Matchers.is(equalTo("ville-*")));
        Assert.assertThat(template.getPatterns().get(1), Matchers.is(equalTo("metropole-*")));
        Settings settings = template.getSettings();
        Assert.assertThat(settings, Matchers.is(notNullValue()));
        Assert.assertThat(settings.get("index.refresh_interval"), Matchers.is(equalTo("1s")));
        Assert.assertThat(settings.get("index.number_of_shards"), Matchers.is(equalTo("1")));
        Assert.assertThat(settings.get("index.number_of_replicas"), Matchers.is(equalTo("1")));
        Assert.assertThat(settings.get("index.store.type"), Matchers.is(equalTo("fs")));
        Assert.assertThat(template.getAliases().containsKey("cities"), Matchers.is(true));
        Assert.assertThat(template.getMappings().containsKey("city"), Matchers.is(true));
        //
        CityTimeBasedSupport timeBased = new CityTimeBasedSupport();
        String indexName = timeBased.buildIndex(IndexTimeBasedParameter.of("%s-%s", (City) null));
        Assert.assertThat(this.elasticsearchOperations.indexExists(indexName), Is.is(true));
    }

    @Test
    public void bookIndexSettignsAndMapping() throws Exception {
        Assert.assertThat(this.elasticsearchOperations.indexExists(INDEX_BOOK_NAME), Matchers.is(true));

        GetMappingsRequest mappingsRequest = new GetMappingsRequest()
                .indices(INDEX_BOOK_NAME);
        GetMappingsResponse response = this.client.indices().getMapping(mappingsRequest, RequestOptions.DEFAULT);
        Assert.assertThat(response.getMappings().containsKey(INDEX_BOOK_NAME), is(true));
        Assert.assertThat(response.getMappings().get(INDEX_BOOK_NAME).containsKey("book"), is(true));

        GetSettingsRequest settingsRequest = new GetSettingsRequest()
                .indices(INDEX_BOOK_NAME);
        GetSettingsResponse settingsResponse = this.client.indices().getSettings(settingsRequest, RequestOptions.DEFAULT);

        ImmutableOpenMap<String, Settings> indexSettings = settingsResponse.getIndexToSettings();
        Assert.assertThat(indexSettings.containsKey(INDEX_BOOK_NAME), is(true));

        Settings settings = indexSettings.get(INDEX_BOOK_NAME);
        Assert.assertThat(settings.get("index.refresh_interval"), is(equalTo("1s")));
        Assert.assertThat(settings.get("index.number_of_shards"), is(equalTo("1")));
        Assert.assertThat(settings.get("index.number_of_replicas"), is(equalTo("1")));
        Assert.assertThat(settings.get("index.store.type"), is(equalTo("fs")));
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
