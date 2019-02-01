/* * Copyright (C) 2018 Yoann Despréaux * * This program is free software; you can redistribute it and/or modify * it under the terms of the GNU General Public License as published by * the Free Software Foundation; either version 2 of the License, or * (at your option) any later version. * * This program is distributed in the hope that it will be useful, * but WITHOUT ANY WARRANTY; without even the implied warranty of * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the * GNU General Public License for more details. * * You should have received a copy of the GNU General Public License * along with this program; see the file COPYING . If not, write to the * Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA. * * Please send bugreports with examples or suggestions to yoann.despreaux@believeit.fr */package com.github.ydespreaux.spring.data.elasticsearch;import com.github.ydespreaux.spring.data.elasticsearch.client.ClientLoggerAspect;import com.github.ydespreaux.spring.data.elasticsearch.configuration.ElasticsearchConfigurationSupport;import com.github.ydespreaux.spring.data.elasticsearch.core.ElasticsearchOperations;import com.github.ydespreaux.spring.data.elasticsearch.core.ResultsExtractor;import com.github.ydespreaux.spring.data.elasticsearch.core.query.NativeSearchQuery;import com.github.ydespreaux.spring.data.elasticsearch.core.query.SearchQuery;import com.github.ydespreaux.spring.data.elasticsearch.core.scroll.ScrolledPage;import com.github.ydespreaux.spring.data.elasticsearch.entities.Article;import com.github.ydespreaux.spring.data.elasticsearch.entities.Book;import com.github.ydespreaux.spring.data.elasticsearch.repository.config.EnableElasticsearchRepositories;import com.github.ydespreaux.spring.data.elasticsearch.utils.AdminClientUtils;import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;import org.elasticsearch.action.search.SearchResponse;import org.elasticsearch.action.search.SearchType;import org.elasticsearch.client.RequestOptions;import org.elasticsearch.client.RestHighLevelClient;import org.elasticsearch.cluster.metadata.IndexTemplateMetaData;import org.elasticsearch.cluster.metadata.MappingMetaData;import org.elasticsearch.common.collect.ImmutableOpenMap;import org.elasticsearch.common.settings.Settings;import org.elasticsearch.index.query.QueryBuilders;import org.elasticsearch.search.aggregations.Aggregations;import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;import org.elasticsearch.search.aggregations.bucket.terms.Terms;import org.junit.Before;import org.junit.Test;import org.junit.runner.RunWith;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.autoconfigure.EnableAutoConfiguration;import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;import org.springframework.boot.test.context.SpringBootTest;import org.springframework.context.annotation.Bean;import org.springframework.context.annotation.Configuration;import org.springframework.context.annotation.EnableAspectJAutoProxy;import org.springframework.test.annotation.DirtiesContext;import org.springframework.test.context.junit4.SpringRunner;import java.io.IOException;import java.time.Duration;import java.util.ArrayList;import java.util.List;import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;import static org.hamcrest.Matchers.*;import static org.junit.Assert.*;/** * @author Yoann Despréaux * @since 1.0.0 */@DirtiesContext@RunWith(SpringRunner.class)@SpringBootTest(classes = {        RestClientAutoConfiguration.class,        ITElasticsearchTemplateTest.ElasticsearchConfiguration.class})public class ITElasticsearchTemplateTest {    @Configuration    @EnableAspectJAutoProxy    @EnableAutoConfiguration    @EnableElasticsearchRepositories(            basePackages = {                    "com.github.ydespreaux.spring.data.elasticsearch.repositories.query",                    "com.github.ydespreaux.spring.data.elasticsearch.repositories.template",                    "com.github.ydespreaux.spring.data.elasticsearch.repositories.synonyms"            },            namedQueriesLocation = "classpath:named-queries/*-named-queries.properties")    static class ElasticsearchConfiguration extends ElasticsearchConfigurationSupport {        @Bean        ClientLoggerAspect clientLoggerAspect() {            return new ClientLoggerAspect();        }    }    private static final String DEFAULT_TEMPLATE_NAME = "junit-template";    private static final String DEFAULT_TEMPLATE_URL = "classpath:templates/junit.template";    private static final String UPDATE_TEMPLATE_URL = "classpath:templates/junit-update.template";    private static final String INDEX1_NAME = "index1";    private static final String INDEX_BOOK_PATH = "classpath:indices/book.index";    private static final String INDEX_BOOK_NAME = "books";    @Autowired    private ElasticsearchOperations operations;    @Autowired    private RestHighLevelClient client;    @Before    public void initialize() {        this.operations.deleteTemplate(DEFAULT_TEMPLATE_NAME);        this.operations.deleteIndexByName(INDEX1_NAME);        this.operations.deleteIndexByName(INDEX_BOOK_NAME);    }    @Test    public void createTemplate() throws IOException {        this.operations.createTemplate(DEFAULT_TEMPLATE_NAME, DEFAULT_TEMPLATE_URL, false);        IndexTemplateMetaData template = AdminClientUtils.getTemplate(client, DEFAULT_TEMPLATE_NAME);        assertThat(template, is(notNullValue()));        assertThat(template.getName(), is(equalTo("junit-template")));        assertThat(template.getPatterns().size(), is(equalTo(1)));        assertThat(template.getPatterns().get(0), is(equalTo("junit-*")));        Settings settings = template.getSettings();        assertThat(settings, is(notNullValue()));        assertThat(settings.get("index.refresh_interval"), is(equalTo("1s")));        assertThat(settings.get("index.number_of_shards"), is(equalTo("1")));        assertThat(settings.get("index.number_of_replicas"), is(equalTo("1")));        assertThat(settings.get("index.store.type"), is(equalTo("fs")));        assertThat(template.getAliases().containsKey("junit-alias"), is(true));        assertThat(template.getMappings().containsKey("tweet"), is(true));        assertThat(template.getOrder(), is(equalTo(0)));    }    @Test    public void updateTemplate_whenTemplateExists_withCreateOnly() throws IOException {        // Create template        this.operations.createTemplate(DEFAULT_TEMPLATE_NAME, DEFAULT_TEMPLATE_URL, false);        IndexTemplateMetaData templateInserted = AdminClientUtils.getTemplate(client, DEFAULT_TEMPLATE_NAME);        assertThat(templateInserted, is(notNullValue()));        assertThat(templateInserted.getName(), is(equalTo("junit-template")));        assertThat(templateInserted.getPatterns().size(), is(equalTo(1)));        assertThat(templateInserted.getPatterns().get(0), is(equalTo("junit-*")));        Settings settings = templateInserted.getSettings();        assertThat(settings, is(notNullValue()));        assertThat(settings.get("index.refresh_interval"), is(equalTo("1s")));        assertThat(settings.get("index.number_of_shards"), is(equalTo("1")));        assertThat(settings.get("index.number_of_replicas"), is(equalTo("1")));        assertThat(settings.get("index.store.type"), is(equalTo("fs")));        assertThat(templateInserted.getAliases().containsKey("junit-alias"), is(true));        assertThat(templateInserted.getMappings().containsKey("tweet"), is(true));        // Update template        this.operations.createTemplate(DEFAULT_TEMPLATE_NAME, UPDATE_TEMPLATE_URL, true);        IndexTemplateMetaData templateUpdated = AdminClientUtils.getTemplate(client, DEFAULT_TEMPLATE_NAME);        assertThat(templateUpdated, is(notNullValue()));        assertThat(templateUpdated.getName(), is(equalTo("junit-template")));        assertThat(templateUpdated.getPatterns().size(), is(equalTo(1)));        assertThat(templateUpdated.getPatterns().get(0), is(equalTo("junit-*")));        settings = templateUpdated.getSettings();        assertThat(settings, is(notNullValue()));        assertThat(settings.get("index.refresh_interval"), is(equalTo("1s")));        assertThat(settings.get("index.number_of_shards"), is(equalTo("1")));        assertThat(settings.get("index.number_of_replicas"), is(equalTo("1")));        assertThat(settings.get("index.store.type"), is(equalTo("fs")));        assertThat(templateUpdated.getAliases().containsKey("junit-alias"), is(true));        assertThat(templateUpdated.getMappings().containsKey("tweet"), is(true));    }    @Test    public void updateTemplate_whenTemplateExists() throws IOException {        // Create template        this.operations.createTemplate(DEFAULT_TEMPLATE_NAME, DEFAULT_TEMPLATE_URL, false);        IndexTemplateMetaData templateInserted = AdminClientUtils.getTemplate(client, DEFAULT_TEMPLATE_NAME);        assertThat(templateInserted, is(notNullValue()));        assertThat(templateInserted.getName(), is(equalTo("junit-template")));        assertThat(templateInserted.getPatterns().size(), is(equalTo(1)));        assertThat(templateInserted.getPatterns().get(0), is(equalTo("junit-*")));        Settings settings = templateInserted.getSettings();        assertThat(settings, is(notNullValue()));        assertThat(settings.get("index.refresh_interval"), is(equalTo("1s")));        assertThat(settings.get("index.number_of_shards"), is(equalTo("1")));        assertThat(settings.get("index.number_of_replicas"), is(equalTo("1")));        assertThat(settings.get("index.store.type"), is(equalTo("fs")));        assertThat(templateInserted.getAliases().containsKey("junit-alias"), is(true));        assertThat(templateInserted.getMappings().containsKey("tweet"), is(true));        // Update template        this.operations.createTemplate(DEFAULT_TEMPLATE_NAME, UPDATE_TEMPLATE_URL, false);        IndexTemplateMetaData templateUpdated = AdminClientUtils.getTemplate(client, DEFAULT_TEMPLATE_NAME);        assertThat(templateUpdated, is(notNullValue()));        assertThat(templateUpdated.getName(), is(equalTo("junit-template")));        assertThat(templateUpdated.getPatterns().size(), is(equalTo(1)));        assertThat(templateUpdated.getPatterns().get(0), is(equalTo("junit-*")));        settings = templateUpdated.getSettings();        assertThat(settings, is(notNullValue()));        assertThat(settings.get("index.refresh_interval"), is(equalTo("1s")));        assertThat(settings.get("index.number_of_shards"), is(equalTo("2")));        assertThat(settings.get("index.number_of_replicas"), is(equalTo("1")));        assertThat(settings.get("index.store.type"), is(equalTo("fs")));        assertThat(templateUpdated.getAliases().containsKey("junit-alias"), is(true));        assertThat(templateUpdated.getMappings().containsKey("tweet"), is(true));    }    @Test    public void templateExists_withTemplateDefined() {        this.operations.createTemplate(DEFAULT_TEMPLATE_NAME, DEFAULT_TEMPLATE_URL, false);        assertTrue(this.operations.templateExists(DEFAULT_TEMPLATE_NAME));    }    @Test    public void templateExists_withTemplateUndefined() {        assertFalse(this.operations.templateExists("UNKNOWN"));    }    @Test    public void createIndex() {        assertThat(this.operations.createIndex(INDEX1_NAME), is(true));        assertThat(this.operations.indexExists(INDEX1_NAME), is(true));    }    @Test    public void createIndexWithSettingsAndMapping() throws Exception {        assertThat(this.operations.createIndexWithSettingsAndMapping(INDEX_BOOK_NAME, INDEX_BOOK_PATH), is(true));        assertThat(this.operations.indexExists(INDEX_BOOK_NAME), is(true));        GetMappingsRequest mappingsRequest = new GetMappingsRequest()                .indices(INDEX_BOOK_NAME);        GetMappingsResponse response = this.client.indices().getMapping(mappingsRequest, RequestOptions.DEFAULT);        assertThat(response.getMappings().containsKey(INDEX_BOOK_NAME), is(true));        ImmutableOpenMap<String, MappingMetaData> indexMapping = response.getMappings().get(INDEX_BOOK_NAME);        assertThat(indexMapping.containsKey("book"), is(true));        GetSettingsRequest settingsRequest = new GetSettingsRequest()                .indices(INDEX_BOOK_NAME);        GetSettingsResponse settingsResponse = this.client.indices().getSettings(settingsRequest, RequestOptions.DEFAULT);        ImmutableOpenMap<String, Settings> indexSettings = settingsResponse.getIndexToSettings();        assertThat(indexSettings.containsKey(INDEX_BOOK_NAME), is(true));        Settings settings = indexSettings.get(INDEX_BOOK_NAME);        assertThat(settings.get("index.refresh_interval"), is(equalTo("1s")));        assertThat(settings.get("index.number_of_shards"), is(equalTo("1")));        assertThat(settings.get("index.number_of_replicas"), is(equalTo("1")));        assertThat(settings.get("index.store.type"), is(equalTo("fs")));    }    @Test    public void bulkEntities() {        List entities = new ArrayList<>();        entities.add(createBook("1", "BOOK", "DESCRIPTION", 10d));        entities.add(createArticle("1", "ARTICLE", "DESCRIPTION", Article.EnumEntrepot.E1));        this.operations.bulkIndex(entities);        this.operations.refresh(Article.class);        this.operations.refresh(Book.class);        assertNotNull(this.operations.findById(Book.class, "1"));        assertNotNull(this.operations.findById(Article.class, "1"));    }    @Test    public void startScroll() {        this.operations.index(createBook("1", "BOOK_1", "DESCRIPTION", 10d), Book.class);        this.operations.index(createBook("2", "BOOK_2", "DESCRIPTION", 10d), Book.class);        this.operations.refresh(Book.class);        SearchQuery query = new NativeSearchQuery.NativeSearchQueryBuilder().withQuery(QueryBuilders.matchAllQuery()).build();        ScrolledPage<Book> result = this.operations.startScroll(Duration.ofMinutes(1), query, Book.class);        assertThat(result.getTotalElements(), is(equalTo(2L)));        assertThat(result.hasContent(), is(true));    }    @Test    public void startScrollWithIndexNotFound() {        SearchQuery query = new NativeSearchQuery.NativeSearchQueryBuilder().withQuery(QueryBuilders.matchAllQuery()).build();        ScrolledPage<Book> result = this.operations.startScroll(Duration.ofSeconds(1), query, Book.class);        assertThat(result.getTotalElements(), is(equalTo(0L)));        assertThat(result.hasContent(), is(false));    }    @Test    public void continueScrollWithIndexNotFound() {        ScrolledPage<Book> result = this.operations.continueScroll("DnF1ZXJ5VGhlbkZldGNoAgAAAAAAAAAEFjhMUUhIa1ZsVDVtdDhrZWVjQ05WeFEAAAAAAAAAAxY4TFFISGtWbFQ1bXQ4a2VlY0NOVnhR", Duration.ofSeconds(1), Book.class);        assertThat(result.getTotalElements(), is(equalTo(0L)));        assertThat(result.hasContent(), is(false));    }    @Test    public void clearScrollWithIndexNotFound() {        this.operations.clearScroll("DnF1ZXJ5VGhlbkZldGNoAgAAAAAAAAAEFjhMUUhIa1ZsVDVtdDhrZWVjQ05WeFEAAAAAAAAAAxY4TFFISGtWbFQ1bXQ4a2VlY0NOVnhR");    }    @Test    public void shouldReturnAggregatedResponseForGivenSearchQuery() {        List entities = new ArrayList<>();        entities.add(createArticle("1", "ARTICLE1", "DESCRIPTION1", Article.EnumEntrepot.E1));        entities.add(createArticle("2", "ARTICLE2", "DESCRIPTION2", Article.EnumEntrepot.E2));        entities.add(createArticle("3", "ARTICLE3", "DESCRIPTION3", Article.EnumEntrepot.E1));        this.operations.bulkIndex(entities);        this.operations.refresh(Article.class);        // given        SearchQuery searchQuery = new NativeSearchQuery.NativeSearchQueryBuilder()                .withQuery(matchAllQuery())                .withSearchType(SearchType.DEFAULT)                .withIndices("articles").withTypes("article")                .addAggregation(terms("entrepots").field("entrepot.keyword"))                .build();        // when        Aggregations aggregations = operations.search(searchQuery, new ResultsExtractor<Aggregations>() {            @Override            public Aggregations extract(SearchResponse response) {                return response.getAggregations();            }        });        // then        assertThat(aggregations, is(notNullValue()));        assertThat(aggregations.asMap().get("entrepots"), is(notNullValue()));        ParsedStringTerms aggregation = (ParsedStringTerms) aggregations.asMap().get("entrepots");        Terms.Bucket bucket_E1 = aggregation.getBucketByKey("E1");        assertThat(bucket_E1, is(notNullValue()));        assertThat(bucket_E1.getDocCount(), is(equalTo(2L)));        Terms.Bucket bucket_E2 = aggregation.getBucketByKey("E2");        assertThat(bucket_E2, is(notNullValue()));        assertThat(bucket_E2.getDocCount(), is(equalTo(1L)));    }    private Book createBook(String id, String title, String description, Double price) {        return Book.builder()                .documentId(id)                .title(title)                .description(description)                .price(price)                .build();    }    /**     * @param name     * @param description     * @param entrepot     * @return     */    private Article createArticle(String id, String name, String description, Article.EnumEntrepot entrepot) {        return Article.builder()                .documentId(id)                .name(name)                .description(description)                .entrepot(entrepot)                .build();    }}