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

import com.github.ydespreaux.spring.data.elasticsearch.client.reactive.ReactiveRestElasticsearchClient;
import com.github.ydespreaux.spring.data.elasticsearch.core.ElasticsearchOperations;
import com.github.ydespreaux.spring.data.elasticsearch.core.ReactiveElasticsearchOperations;
import com.github.ydespreaux.testcontainers.elasticsearch.ElasticsearchContainer;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexTemplatesRequest;
import org.elasticsearch.client.indices.GetIndexTemplatesResponse;
import org.elasticsearch.client.indices.IndexTemplateMetaData;
import org.elasticsearch.common.settings.Settings;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Yoann Despréaux
 * @since 1.0.0
 */
@Tag("integration")
@Testcontainers
public class ElasticsearchStarterTest {

    @Container
    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer(Versions.ELASTICSEARCH_VERSION);

    @Nested
    @SpringBootTest(classes = {
            RestClientAutoConfiguration.class,
            ElasticsearchDataAutoConfiguration.class})
    class ElasticsearchConfigurationTest {

        @Autowired
        private RestHighLevelClient client;

        @Autowired
        private ApplicationContext context;

        @Test
        void context() throws Exception {

            assertThat(context.getBean(ElasticsearchOperations.class), is(notNullValue()));

            IndexTemplateMetaData template = getTemplate(client, "article");
            assertThat(template, is(notNullValue()));
            assertThat(template.name(), is(equalTo("article")));
            assertThat(template.patterns().size(), is(equalTo(1)));
            assertThat(template.patterns().get(0), is(equalTo("article-*")));
            Settings settings = template.settings();
            assertThat(settings, is(notNullValue()));
            assertThat(settings.get("index.refresh_interval"), is(equalTo("1s")));
            assertThat(settings.get("index.number_of_shards"), is(equalTo("1")));
            assertThat(settings.get("index.number_of_replicas"), is(equalTo("1")));
            assertThat(settings.get("index.store.type"), is(equalTo("fs")));
            assertThat(template.aliases().containsKey("articles"), is(true));
            assertThat(template.mappings().type(), is(equalTo("_doc")));
        }

        /**
         * @param templateName
         * @return
         * @throws IOException
         */
        private IndexTemplateMetaData getTemplate(RestHighLevelClient client, String templateName) throws IOException {
            GetIndexTemplatesResponse response = client.indices().getIndexTemplate(new GetIndexTemplatesRequest(templateName), RequestOptions.DEFAULT);
            List<IndexTemplateMetaData> templates = response.getIndexTemplates();
            if (templates.isEmpty()) {
                return null;
            }
            return templates.get(0);
        }

    }

    @Nested
    @SpringBootTest(classes = {
            RestClientAutoConfiguration.class,
            ReactiveElasticsearchDataAutoConfiguration.class})
    public class ReactiveElasticsearchConfigurationTest {

        @Autowired
        private ApplicationContext context;

        @Test
        void context() {
            assertThat(context.getBean(ReactiveRestElasticsearchClient.class), is(notNullValue()));
            assertThat(context.getBean(ReactiveElasticsearchOperations.class), is(notNullValue()));
        }

    }
}
