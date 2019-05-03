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
import com.github.ydespreaux.spring.data.elasticsearch.core.query.NativeSearchQuery;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.ScriptField;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.SearchQuery;
import com.github.ydespreaux.spring.data.elasticsearch.entities.Article;
import com.github.ydespreaux.spring.data.elasticsearch.entities.Book;
import com.github.ydespreaux.spring.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import com.github.ydespreaux.spring.data.elasticsearch.utils.AdminClientUtils;
import com.github.ydespreaux.testcontainers.elasticsearch.ElasticsearchContainer;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.metadata.IndexTemplateMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.domain.Page;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Yoann Despréaux
 * @since 1.0.0
 */
@Tag("integration")
@DirtiesContext
@SpringBootTest(classes = {
        RestClientAutoConfiguration.class,
        ElasticsearchTemplateTest.ElasticsearchConfiguration.class})
@Testcontainers
public class ElasticsearchTemplateTest {

    @Container
    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer(Versions.ELASTICSEARCH_VERSION)
            .withConfigDirectory("elastic-config");
    private static final String DEFAULT_TEMPLATE_NAME = "junit-template";
    private static final String DEFAULT_TEMPLATE_URL = "classpath:templates/junit.template";
    private static final String UPDATE_TEMPLATE_URL = "classpath:templates/junit-update.template";
    private static final String INDEX1_NAME = "index1";
    private static final String INDEX_BOOK_PATH = "classpath:indices/book.index";
    private static final String INDEX_BOOK_NAME = "books";

    @Autowired
    private ElasticsearchOperations operations;
    @Autowired
    private RestHighLevelClient client;

    @BeforeEach
    void initialize() {
        this.operations.deleteTemplate(DEFAULT_TEMPLATE_NAME);
        this.operations.deleteIndexByName(INDEX1_NAME);
        this.operations.deleteIndexByName(INDEX_BOOK_NAME);
        this.operations.deleteAll(Article.class);
    }

    @Test
    void createTemplate() throws IOException {
        this.operations.createTemplate(DEFAULT_TEMPLATE_NAME, DEFAULT_TEMPLATE_URL, false);

        IndexTemplateMetaData template = AdminClientUtils.getTemplate(client, DEFAULT_TEMPLATE_NAME);
        assertThat(template, is(notNullValue()));
        assertThat(template.getName(), is(equalTo("junit-template")));
        assertThat(template.getPatterns().size(), is(equalTo(1)));
        assertThat(template.getPatterns().get(0), is(equalTo("junit-*")));
        Settings settings = template.getSettings();
        assertThat(settings, is(notNullValue()));
        assertThat(settings.get("index.refresh_interval"), is(equalTo("1s")));
        assertThat(settings.get("index.number_of_shards"), is(equalTo("1")));
        assertThat(settings.get("index.number_of_replicas"), is(equalTo("1")));
        assertThat(settings.get("index.store.type"), is(equalTo("fs")));
        assertThat(template.getAliases().containsKey("junit-alias"), is(true));
        assertThat(template.getMappings().containsKey("tweet"), is(true));
        assertThat(template.getOrder(), is(equalTo(0)));
    }

    @Test
    void updateTemplate_whenTemplateExists_withCreateOnly() throws IOException {
        // Create template
        this.operations.createTemplate(DEFAULT_TEMPLATE_NAME, DEFAULT_TEMPLATE_URL, false);
        IndexTemplateMetaData templateInserted = AdminClientUtils.getTemplate(client, DEFAULT_TEMPLATE_NAME);
        assertThat(templateInserted, is(notNullValue()));
        assertThat(templateInserted.getName(), is(equalTo("junit-template")));
        assertThat(templateInserted.getPatterns().size(), is(equalTo(1)));
        assertThat(templateInserted.getPatterns().get(0), is(equalTo("junit-*")));
        Settings settings = templateInserted.getSettings();
        assertThat(settings, is(notNullValue()));
        assertThat(settings.get("index.refresh_interval"), is(equalTo("1s")));
        assertThat(settings.get("index.number_of_shards"), is(equalTo("1")));
        assertThat(settings.get("index.number_of_replicas"), is(equalTo("1")));
        assertThat(settings.get("index.store.type"), is(equalTo("fs")));
        assertThat(templateInserted.getAliases().containsKey("junit-alias"), is(true));
        assertThat(templateInserted.getMappings().containsKey("tweet"), is(true));

        // Update template
        this.operations.createTemplate(DEFAULT_TEMPLATE_NAME, UPDATE_TEMPLATE_URL, true);

        IndexTemplateMetaData templateUpdated = AdminClientUtils.getTemplate(client, DEFAULT_TEMPLATE_NAME);
        assertThat(templateUpdated, is(notNullValue()));
        assertThat(templateUpdated.getName(), is(equalTo("junit-template")));
        assertThat(templateUpdated.getPatterns().size(), is(equalTo(1)));
        assertThat(templateUpdated.getPatterns().get(0), is(equalTo("junit-*")));
        settings = templateUpdated.getSettings();
        assertThat(settings, is(notNullValue()));
        assertThat(settings.get("index.refresh_interval"), is(equalTo("1s")));
        assertThat(settings.get("index.number_of_shards"), is(equalTo("1")));
        assertThat(settings.get("index.number_of_replicas"), is(equalTo("1")));
        assertThat(settings.get("index.store.type"), is(equalTo("fs")));
        assertThat(templateUpdated.getAliases().containsKey("junit-alias"), is(true));
        assertThat(templateUpdated.getMappings().containsKey("tweet"), is(true));
    }

    @Test
    void updateTemplate_whenTemplateExists() throws IOException {
        // Create template
        this.operations.createTemplate(DEFAULT_TEMPLATE_NAME, DEFAULT_TEMPLATE_URL, false);
        IndexTemplateMetaData templateInserted = AdminClientUtils.getTemplate(client, DEFAULT_TEMPLATE_NAME);
        assertThat(templateInserted, is(notNullValue()));
        assertThat(templateInserted.getName(), is(equalTo("junit-template")));
        assertThat(templateInserted.getPatterns().size(), is(equalTo(1)));
        assertThat(templateInserted.getPatterns().get(0), is(equalTo("junit-*")));
        Settings settings = templateInserted.getSettings();
        assertThat(settings, is(notNullValue()));
        assertThat(settings.get("index.refresh_interval"), is(equalTo("1s")));
        assertThat(settings.get("index.number_of_shards"), is(equalTo("1")));
        assertThat(settings.get("index.number_of_replicas"), is(equalTo("1")));
        assertThat(settings.get("index.store.type"), is(equalTo("fs")));
        assertThat(templateInserted.getAliases().containsKey("junit-alias"), is(true));
        assertThat(templateInserted.getMappings().containsKey("tweet"), is(true));

        // Update template
        this.operations.createTemplate(DEFAULT_TEMPLATE_NAME, UPDATE_TEMPLATE_URL, false);
        IndexTemplateMetaData templateUpdated = AdminClientUtils.getTemplate(client, DEFAULT_TEMPLATE_NAME);
        assertThat(templateUpdated, is(notNullValue()));
        assertThat(templateUpdated.getName(), is(equalTo("junit-template")));
        assertThat(templateUpdated.getPatterns().size(), is(equalTo(1)));
        assertThat(templateUpdated.getPatterns().get(0), is(equalTo("junit-*")));
        settings = templateUpdated.getSettings();
        assertThat(settings, is(notNullValue()));
        assertThat(settings.get("index.refresh_interval"), is(equalTo("1s")));
        assertThat(settings.get("index.number_of_shards"), is(equalTo("2")));
        assertThat(settings.get("index.number_of_replicas"), is(equalTo("1")));
        assertThat(settings.get("index.store.type"), is(equalTo("fs")));
        assertThat(templateUpdated.getAliases().containsKey("junit-alias"), is(true));
        assertThat(templateUpdated.getMappings().containsKey("tweet"), is(true));
    }

    @Test
    void templateExists_withTemplateDefined() {
        this.operations.createTemplate(DEFAULT_TEMPLATE_NAME, DEFAULT_TEMPLATE_URL, false);
        assertThat(this.operations.templateExists(DEFAULT_TEMPLATE_NAME), is(true));
    }

    @Test
    void templateExists_withTemplateUndefined() {
        assertThat(this.operations.templateExists("UNKNOWN"), is(false));
    }

    @Test
    void createIndex() {
        assertThat(this.operations.createIndex(INDEX1_NAME), is(true));
        assertThat(this.operations.indexExists(INDEX1_NAME), is(true));
    }

    @Test
    void createIndexWithSettingsAndMapping() throws Exception {
        assertThat(this.operations.createIndexWithSettingsAndMapping(INDEX_BOOK_NAME, INDEX_BOOK_PATH), is(true));
        assertThat(this.operations.indexExists(INDEX_BOOK_NAME), is(true));

        GetMappingsRequest mappingsRequest = new GetMappingsRequest()
                .indices(INDEX_BOOK_NAME);
        GetMappingsResponse response = this.client.indices().getMapping(mappingsRequest, RequestOptions.DEFAULT);
        assertThat(response.getMappings().containsKey(INDEX_BOOK_NAME), is(true));
        ImmutableOpenMap<String, MappingMetaData> indexMapping = response.getMappings().get(INDEX_BOOK_NAME);
        assertThat(indexMapping.containsKey("book"), is(true));

        GetSettingsRequest settingsRequest = new GetSettingsRequest()
                .indices(INDEX_BOOK_NAME);
        GetSettingsResponse settingsResponse = this.client.indices().getSettings(settingsRequest, RequestOptions.DEFAULT);

        ImmutableOpenMap<String, Settings> indexSettings = settingsResponse.getIndexToSettings();
        assertThat(indexSettings.containsKey(INDEX_BOOK_NAME), is(true));

        Settings settings = indexSettings.get(INDEX_BOOK_NAME);
        assertThat(settings.get("index.refresh_interval"), is(equalTo("1s")));
        assertThat(settings.get("index.number_of_shards"), is(equalTo("1")));
        assertThat(settings.get("index.number_of_replicas"), is(equalTo("1")));
        assertThat(settings.get("index.store.type"), is(equalTo("fs")));
    }

    @Test
    void bulkEntities() {
        List entities = new ArrayList<>();
        entities.add(createBook("1", "BOOK", "DESCRIPTION", 10d));
        entities.add(createArticle("1", "ARTICLE", "DESCRIPTION", Article.EnumEntrepot.E1));
        this.operations.bulkIndex(entities);

        this.operations.refresh(Article.class);
        this.operations.refresh(Book.class);
        assertThat(this.operations.findById(Book.class, "1"), is(notNullValue()));
        assertThat(this.operations.findById(Article.class, "1"), is(notNullValue()));
    }

    @Test
    void startScroll() {
        this.operations.index(createBook("1", "BOOK_1", "DESCRIPTION", 10d), Book.class);
        this.operations.index(createBook("2", "BOOK_2", "DESCRIPTION", 10d), Book.class);
        this.operations.refresh(Book.class);

        SearchQuery query = new NativeSearchQuery.NativeSearchQueryBuilder().withQuery(QueryBuilders.matchAllQuery()).build();
        Page<Book> result = this.operations.startScroll(Duration.ofMinutes(1), query, Book.class);
        assertThat(result.getTotalElements(), is(equalTo(2L)));
        assertThat(result.hasContent(), is(true));
    }

    @Test
    void startScrollWithIndexNotFound() {
        SearchQuery query = new NativeSearchQuery.NativeSearchQueryBuilder().withQuery(QueryBuilders.matchAllQuery()).build();
        Page<Book> result = this.operations.startScroll(Duration.ofSeconds(1), query, Book.class);
        assertThat(result.getTotalElements(), is(equalTo(0L)));
        assertThat(result.hasContent(), is(false));
    }

    @Test
    void continueScrollWithIndexNotFound() {
        Page<Book> result = this.operations.continueScroll("DnF1ZXJ5VGhlbkZldGNoAgAAAAAAAAAEFjhMUUhIa1ZsVDVtdDhrZWVjQ05WeFEAAAAAAAAAAxY4TFFISGtWbFQ1bXQ4a2VlY0NOVnhR", Duration.ofSeconds(1), Book.class);
        assertThat(result.getTotalElements(), is(equalTo(0L)));
        assertThat(result.hasContent(), is(false));
    }

    @Test
    void clearScrollWithIndexNotFound() {
        this.operations.clearScroll("DnF1ZXJ5VGhlbkZldGNoAgAAAAAAAAAEFjhMUUhIa1ZsVDVtdDhrZWVjQ05WeFEAAAAAAAAAAxY4TFFISGtWbFQ1bXQ4a2VlY0NOVnhR");
    }

    @Test
    void shouldReturnAggregatedResponseForGivenSearchQuery() {
        this.operations.deleteAll(Article.class);
        List entities = new ArrayList<>();
        entities.add(createArticle("1", "ARTICLE1", "DESCRIPTION1", Article.EnumEntrepot.E1));
        entities.add(createArticle("2", "ARTICLE2", "DESCRIPTION2", Article.EnumEntrepot.E2));
        entities.add(createArticle("3", "ARTICLE3", "DESCRIPTION3", Article.EnumEntrepot.E1));
        this.operations.bulkIndex(entities);
        this.operations.refresh(Article.class);

        // given
        SearchQuery searchQuery = new NativeSearchQuery.NativeSearchQueryBuilder()
                .withQuery(matchAllQuery())
                .withSearchType(SearchType.DEFAULT)
                .withIndices("articles").withTypes("article")
                .addAggregation(terms("entrepots").field("entrepot.keyword"))
                .build();
        // when
        Aggregations aggregations = operations.search(searchQuery, new ResultsExtractor<Aggregations>() {
            @Override
            public Aggregations extract(SearchResponse response) {
                return response.getAggregations();
            }
        });
        // then
        assertThat(aggregations, is(notNullValue()));
        assertThat(aggregations.asMap().get("entrepots"), is(notNullValue()));

        ParsedStringTerms aggregation = (ParsedStringTerms) aggregations.asMap().get("entrepots");
        Terms.Bucket bucket_E1 = aggregation.getBucketByKey("E1");
        assertThat(bucket_E1, is(notNullValue()));
        assertThat(bucket_E1.getDocCount(), is(equalTo(2L)));
        Terms.Bucket bucket_E2 = aggregation.getBucketByKey("E2");
        assertThat(bucket_E2, is(notNullValue()));
        assertThat(bucket_E2.getDocCount(), is(equalTo(1L)));
    }

    @Test
    void saveArticle() {
        Article article = createArticle(null, "MyArticle", "MyArticle description", Article.EnumEntrepot.E1);
        Article articleIndexed = this.operations.index(article, Article.class);
        this.operations.refresh(Article.class);
        assertThat(articleIndexed.getDocumentId(), is(notNullValue()));
        assertThat(articleIndexed.getName(), is(equalTo(article.getName())));
        assertThat(articleIndexed.getDescription(), is(equalTo(article.getDescription())));
        assertThat(articleIndexed.getEntrepot(), is(equalTo(article.getEntrepot())));
        assertThat(articleIndexed.getDocumentVersion(), is(equalTo(1L)));
    }

    //------------------------------------------------
    // ARTICLE
    //------------------------------------------------

    @Test
    void saveArticle_bulk() {
        List<Article> articles = new ArrayList<>();
        articles.add(createArticle(null, "MyArticle1", "MyArticle 1 description", Article.EnumEntrepot.E1));
        articles.add(createArticle(null, "MyArticle2", "MyArticle 2 description", Article.EnumEntrepot.E2));
        articles.add(createArticle(null, "MyArticle3", "MyArticle 3 description", Article.EnumEntrepot.E1));
        articles.add(createArticle(null, "MyArticle4", "MyArticle 4 description", Article.EnumEntrepot.E3));
        articles.add(createArticle(null, "MyArticle5", "MyArticle 5 description", Article.EnumEntrepot.E1));
        List<Article> articlesIndexed = this.operations.bulkIndex(articles, Article.class);
        assertThat(articlesIndexed.size(), is(equalTo(articles.size())));
        for (Article articleIndexed : articlesIndexed) {
            assertThat(articleIndexed.getDocumentId(), is(notNullValue()));
            assertThat(articleIndexed.getDocumentVersion(), is(equalTo(1L)));
        }
    }

    @Test
    void deleteArticleById() {
        Article article = createArticle(null, "MyArticle", "MyArticle description", Article.EnumEntrepot.E1);
        Article articleIndexed = this.operations.index(article, Article.class);
        this.operations.refresh(Article.class);
        this.operations.deleteById(articleIndexed.getDocumentId(), Article.class);
        this.operations.refresh(Article.class);
        Optional<Article> response = this.operations.findById(Article.class, articleIndexed.getDocumentId());
        assertThat(response.isPresent(), is(false));
    }

    @Test
    void deleteArticle() {
        Article article = createArticle(null, "MyArticle", "MyArticle description", Article.EnumEntrepot.E1);
        Article articleIndexed = this.operations.index(article, Article.class);
        this.operations.refresh(Article.class);
        this.operations.delete(articleIndexed, Article.class);
        this.operations.refresh(Article.class);
        Optional<Article> response = this.operations.findById(Article.class, articleIndexed.getDocumentId());
        assertThat(response.isPresent(), is(false));
    }

    @Test
    void deleteAllArticle() {
        List<Article> articles = this.operations.bulkIndex(Arrays.asList(
                createArticle(null, "MyArticle1", "MyArticle 1 description", Article.EnumEntrepot.E1),
                createArticle(null, "MyArticle2", "MyArticle 2 description", Article.EnumEntrepot.E1),
                createArticle(null, "MyArticle3", "MyArticle 3 description", Article.EnumEntrepot.E1)
        ), Article.class);
        this.operations.refresh(Article.class);
        this.operations.deleteAll(articles, Article.class);
        this.operations.refresh(Article.class);
        for (Article articleIndexed : articles) {
            Optional<Article> response = this.operations.findById(Article.class, articleIndexed.getDocumentId());
            assertThat(response.isPresent(), is(false));
        }
    }

    @Test
    void findByQueryWithScript() {
        this.operations.bulkIndex(Arrays.asList(
                createArticle(null, "MyArticle1", "MyArticle 1 description", Article.EnumEntrepot.E1),
                createArticle(null, "MyArticle2", "MyArticle 2 description", Article.EnumEntrepot.E1),
                createArticle(null, "MyArticle3", "MyArticle 3 description", Article.EnumEntrepot.E1)
        ), Article.class);
        this.operations.refresh(Article.class);

        SearchQuery searchQuery = new NativeSearchQuery.NativeSearchQueryBuilder()
                .withQuery(matchAllQuery())
                .withScriptField(new ScriptField("scriptedName",
                        new Script(ScriptType.INLINE, "painless", "doc['name'].value.toUpperCase()", Collections.emptyMap())))
                .build();
        List<Article> entities = this.operations.search(searchQuery, Article.class);
        assertThat(entities.size(), is(equalTo(3)));

        Collections.sort(entities, Comparator.comparing(Article::getScriptedName));
        for (int i = 0; i < entities.size(); i++) {
            assertThat(entities.get(i).getScriptedName(), is(equalTo("MYARTICLE" + (i + 1))));
        }
    }


    /**
     * @param title
     * @param description
     * @param price
     * @return
     */
    private Book createBook(String title, String description, Double price, LocalDate publication) {
        return Book.builder()
                .title(title)
                .description(description)
                .price(price)
                .publication(publication)
                .lastUpdated(LocalDateTime.now(Clock.systemUTC()))
                .build();
    }

    /**
     * @param name
     * @param description
     * @param entrepot
     * @return
     */
    private Article createArticle(String id, String name, String description, Article.EnumEntrepot entrepot) {
        return Article.builder()
                .documentId(id)
                .name(name)
                .description(description)
                .entrepot(entrepot)
                .build();
    }

    //------------------------------------------------
    // BOOK
    //------------------------------------------------

    @Test
    void saveBook() {
        Book book = createBook("New book", "Description", 10.5d, LocalDate.now(Clock.systemUTC()));
        Book bookIndexed = this.operations.index(book, Book.class);
        this.operations.refresh(Book.class);
        assertThat(bookIndexed.getTitle(), is(equalTo(book.getTitle())));
        assertThat(bookIndexed.getDescription(), is(equalTo(book.getDescription())));
        assertThat(bookIndexed.getPrice(), is(equalTo(book.getPrice())));
        assertThat(bookIndexed.getPublication(), is(equalTo(book.getPublication())));
        assertThat(bookIndexed.getVersion(), is(equalTo(1L)));
    }

    @Test
    void saveBook_bulk() {
        List<Book> books = new ArrayList<>();
        books.add(createBook("new_Livre1", "Description du livre 1", 10.5d, LocalDate.now(Clock.systemUTC())));
        books.add(createBook("new_Livre2", "Description du livre 2", 8d, LocalDate.now(Clock.systemUTC())));
        books.add(createBook("new_Livre3", "Description du livre 3", 20d, LocalDate.now(Clock.systemUTC())));
        books.add(createBook("new_Livre4", "Description du livre 4", 5d, LocalDate.now(Clock.systemUTC())));
        books.add(createBook("new_Livre5", "Description du livre 5", 8.5d, LocalDate.now(Clock.systemUTC())));
        List<Book> booksIndexed = this.operations.bulkIndex(books, Book.class);
        this.operations.refresh(Book.class);
        assertThat(booksIndexed.size(), is(equalTo(books.size())));
        for (Book bookIndexed : booksIndexed) {
            assertThat(bookIndexed.getDocumentId(), is(notNullValue()));
            assertThat(bookIndexed.getVersion(), is(equalTo(1L)));
        }
    }

    @Test
    void deleteBookById() {
        Book book = createBook("New book", "Description", 10.5d, LocalDate.now(Clock.systemUTC()));
        Book bookIndexed = this.operations.index(book, Book.class);
        this.operations.refresh(Book.class);
        this.operations.deleteById(bookIndexed.getDocumentId(), Book.class);
        this.operations.refresh(Book.class);
        Optional<Book> response = this.operations.findById(Book.class, bookIndexed.getDocumentId());
        assertThat(response.isPresent(), is(false));
    }

    @Test
    void deleteBook() {
        Book book = createBook("New book", "Description", 10.5d, LocalDate.now(Clock.systemUTC()));
        Book bookIndexed = this.operations.index(book, Book.class);
        this.operations.refresh(Book.class);
        this.operations.delete(bookIndexed, Book.class);
        this.operations.refresh(Book.class);
        Optional<Book> response = this.operations.findById(Book.class, bookIndexed.getDocumentId());
        assertThat(response.isPresent(), is(false));
    }

    @Test
    void deleteAllBook() {
        List<Book> books = this.operations.bulkIndex(Arrays.asList(
                createBook("Book1", "Description", 10.5d, LocalDate.now(Clock.systemUTC())),
                createBook("Book2", "Description", 10.5d, LocalDate.now(Clock.systemUTC())),
                createBook("Book3", "Description", 10.5d, LocalDate.now(Clock.systemUTC())),
                createBook("Book4", "Description", 10.5d, LocalDate.now(Clock.systemUTC())),
                createBook("Book5", "Description", 10.5d, LocalDate.now(Clock.systemUTC()))
        ), Book.class);
        this.operations.refresh(Book.class);

        List<Book> booksToDelete = Arrays.asList(books.get(0), books.get(1), books.get(2));
        this.operations.deleteAll(books, Book.class);
        this.operations.refresh(Book.class);
        for (Book bookIndexed : booksToDelete) {
            assertThat(this.operations.findById(Book.class, bookIndexed.getDocumentId()).isPresent(), is(false));
        }
    }


    private Book createBook(String id, String title, String description, Double price) {
        return Book.builder()
                .documentId(id)
                .title(title)
                .description(description)
                .price(price)
                .build();
    }

    @Configuration
    @EnableAspectJAutoProxy
    @EnableAutoConfiguration
    @EnableElasticsearchRepositories(
            basePackages = {
                    "com.github.ydespreaux.spring.data.elasticsearch.repositories.query",
                    "com.github.ydespreaux.spring.data.elasticsearch.repositories.template",
                    "com.github.ydespreaux.spring.data.elasticsearch.repositories.synonyms"
            },
            namedQueriesLocation = "classpath:named-queries/*-named-queries.properties")
    static class ElasticsearchConfiguration extends ElasticsearchConfigurationSupport {

        @Bean
        ClientLoggerAspect clientLoggerAspect() {
            return new ClientLoggerAspect();
        }
    }

}
