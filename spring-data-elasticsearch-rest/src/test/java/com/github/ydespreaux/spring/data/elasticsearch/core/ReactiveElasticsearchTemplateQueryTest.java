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
 *
 */

package com.github.ydespreaux.spring.data.elasticsearch.core;

import com.github.ydespreaux.spring.data.elasticsearch.client.ClientLoggerAspect;
import com.github.ydespreaux.spring.data.elasticsearch.configuration.ReactiveElasticsearchConfiguration;
import com.github.ydespreaux.spring.data.elasticsearch.core.completion.reactive.ReactiveStringSuggestExtractor;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.*;
import com.github.ydespreaux.spring.data.elasticsearch.entities.Article;
import com.github.ydespreaux.spring.data.elasticsearch.entities.Book;
import com.github.ydespreaux.spring.data.elasticsearch.entities.Music;
import com.github.ydespreaux.spring.data.elasticsearch.entities.Product;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@Tag("integration-nested")
@SpringBootTest(classes = {
        RestClientAutoConfiguration.class,
        ReactiveElasticsearchTemplateQueryTest.ElasticsearchConfiguration.class})
@Profile("test-no-template")
@Testcontainers
public class ReactiveElasticsearchTemplateQueryTest
{

    private static final Article article1 = Article.builder().documentId("1").name("Article1").description("Description de l'article 1 - City").entrepot(Article.EnumEntrepot.E1).build();
    private static final Article article2 = Article.builder().documentId("2").name("Article2").description("Description de l'article 2 - Product").entrepot(Article.EnumEntrepot.E2).build();

    @Configuration
    @EnableAspectJAutoProxy
    static class ElasticsearchConfiguration extends ReactiveElasticsearchConfiguration {

        @Bean
        ClientLoggerAspect clientLoggerAspect() {
            return new ClientLoggerAspect();
        }
    }

    @Autowired
    private ReactiveElasticsearchOperations reactiveOperations;

    @Test
    void findOneBySearchQuery(){
        SearchQuery query = new NativeSearchQuery(QueryBuilders.matchQuery("name", "Article1"));
        StepVerifier.create(reactiveOperations.findOne(query, Article.class))
            .assertNext(result -> {
                assertArticle(result, article1);
            })
            .verifyComplete();
    }

    @Test
    void findOneByCriteriaQuery(){
        CriteriaQuery query = new CriteriaQuery(Criteria.where("name").is("Article1"));
        StepVerifier.create(reactiveOperations.findOne(query, Article.class))
                .assertNext(result -> {
                    assertArticle(result, article1);
                })
                .verifyComplete();
    }

    @Test
    void findOneByStringQuery(){
        StringQuery query = new StringQuery("{\"match\" : {\"name\" : \"Article1\"}}");
        StepVerifier.create(reactiveOperations.findOne(query, Article.class))
                .assertNext(result -> {
                    assertArticle(result, article1);
                })
                .verifyComplete();
    }

    @Test
    void countBySearchQuery(){
        SearchQuery query = new NativeSearchQuery(QueryBuilders.matchAllQuery());
        StepVerifier.create(reactiveOperations.count(query, Article.class))
                .expectNext(50L)
                .verifyComplete();
    }

    @Test
    void countByCriteriaQuery(){
        CriteriaQuery query = new CriteriaQuery(Criteria.where("description").contains("description"));
        StepVerifier.create(reactiveOperations.count(query, Article.class))
                .expectNext(50L)
                .verifyComplete();
    }

    @Test
    void searchBySearchQuery(){
        SearchQuery query = new NativeSearchQuery(QueryBuilders.matchAllQuery());
        StepVerifier.create(reactiveOperations.search(query, Article.class))
                .expectNextCount(50)
                .verifyComplete();

    }

    @Test
    void searchByCriteriaQuery(){
        CriteriaQuery query = new CriteriaQuery(Criteria.where("description").contains("description"));
        StepVerifier.create(reactiveOperations.search(query, Article.class))
                .expectNextCount(50)
                .verifyComplete();
    }

    @Test
    void searchByStringQuery(){
        StringQuery query = new StringQuery("{\"match\" : {\"name\" : \"Article1\"}}");
        StepVerifier.create(reactiveOperations.search(query, Article.class))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void existsByIdWithIndexNotFound(){
        StepVerifier.create(reactiveOperations.existsById(Product.class, "-1"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void existsByIdWithIndexTimebased(){
        StepVerifier.create(reactiveOperations.existsById(Article.class, "1"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void existsByIdWithSampleIndex(){
        StepVerifier.create(reactiveOperations.existsById(Book.class, "981ac3c2-3c5f-45c3-a0dc-389354b9d9f3"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void existsByIdNotFoundWithIndexTimeBased(){
        StepVerifier.create(reactiveOperations.existsById(Article.class, "-1"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void existsByIdNotFoundWithSampleIndex(){
        StepVerifier.create(reactiveOperations.existsById(Book.class, UUID.randomUUID().toString()))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void existsByCriteriaQuery(){
        CriteriaQuery query = new CriteriaQuery(Criteria.where("name").is("Article1"));
        StepVerifier.create(reactiveOperations.existsByQuery(query, Article.class))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void existsBySearchQuery(){
        SearchQuery query = new NativeSearchQuery(QueryBuilders.matchQuery("name", "Article1"));
        StepVerifier.create(reactiveOperations.existsByQuery(query, Article.class))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void existsByStringQuery(){
        StringQuery query = new StringQuery("{\"match\" : {\"name\" : \"Article1\"}}");
        StepVerifier.create(reactiveOperations.existsByQuery(query, Article.class))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void existsByCriteriaQueryNotFound(){
        SearchQuery query = new NativeSearchQuery(QueryBuilders.matchQuery("name", "Article-1"));
        StepVerifier.create(reactiveOperations.existsByQuery(query, Article.class))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void existsByCriteriaQueryWithIndexNotFound(){
        SearchQuery query = new NativeSearchQuery(QueryBuilders.matchQuery("name", "Article1"));
        StepVerifier.create(reactiveOperations.existsByQuery(query, Product.class))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void shouldFindSuggestionsUsingCompletion() {
        SuggestionBuilder completionSuggestionFuzzyBuilder = SuggestBuilders.completionSuggestion("suggest").prefix("m", Fuzziness.AUTO);
        SuggestQuery query = new SuggestQuery(new SuggestBuilder().addSuggestion("test-suggest", completionSuggestionFuzzyBuilder));
        StepVerifier.create(reactiveOperations.suggest(query, Music.class, new ReactiveStringSuggestExtractor()))
                .expectNext("Mickael Jackson", "Muse")
                .verifyComplete();
    }

    @Test
    void shouldFindSuggestionsUsingSearch() {
        SuggestionBuilder completionSuggestionFuzzyBuilder = SuggestBuilders.completionSuggestion("suggest").prefix("m", Fuzziness.AUTO);
        SuggestQuery query = new SuggestQuery(new SuggestBuilder().addSuggestion("test-suggest", completionSuggestionFuzzyBuilder));
        query.addIndices("musics");
        query.addTypes("music");
        StepVerifier.create(reactiveOperations.suggest(query, new ReactiveStringSuggestExtractor()))
                .expectNext("Mickael Jackson", "Muse")
                .verifyComplete();
    }

    private void assertArticle(Article actual, Article expected) {
        assertThat(actual.getDocumentId(), is(equalTo(expected.getDocumentId())));
        assertThat(actual.getDocumentVersion(), is(equalTo(1L)));
        assertThat(actual.getName(), is(equalTo(expected.getName())));
        assertThat(actual.getDescription(), is(equalTo(expected.getDescription())));
        assertThat(actual.getEntrepot(), is(equalTo(expected.getEntrepot())));
    }
}
