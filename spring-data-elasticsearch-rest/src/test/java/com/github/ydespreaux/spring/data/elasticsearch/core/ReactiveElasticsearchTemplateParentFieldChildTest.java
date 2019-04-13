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
import com.github.ydespreaux.spring.data.elasticsearch.configuration.ReactiveElasticsearchConfiguration;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.HasChildQuery;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.HasParentQuery;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.ParentIdQuery;
import com.github.ydespreaux.spring.data.elasticsearch.entities.Question;
import com.github.ydespreaux.testcontainers.elasticsearch.ElasticsearchContainer;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Yoann Despréaux
 */
@Tag("integration")
@SpringBootTest(classes = {
        RestClientAutoConfiguration.class,
        ReactiveElasticsearchTemplateParentFieldChildTest.ElasticsearchConfiguration.class
})
@Testcontainers
public class ReactiveElasticsearchTemplateParentFieldChildTest {

    @Container
    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer(Versions.ELASTICSEARCH_VERSION)
            .withFileInitScript("scripts/parent-child.script");
    private ReactiveElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    void setElasticsearchTemplate(ReactiveElasticsearchTemplate elasticsearchTemplate) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        EntityMapper mapper = elasticsearchTemplate.getResultsMapper().getEntityMapper();
        mapper.register(elasticsearchTemplate.getPersistentEntityFor(Question.class));
        mapper.register(elasticsearchTemplate.getPersistentEntityFor(Question.Answer.class));
        mapper.register(elasticsearchTemplate.getPersistentEntityFor(Question.Comment.class));
        mapper.register(elasticsearchTemplate.getPersistentEntityFor(Question.Vote.class));
    }

    @Test
    void hasChildAnswer() {
        QueryBuilder query = QueryBuilders.matchPhraseQuery("description", "question");
        StepVerifier.create(elasticsearchTemplate.hasChild(HasChildQuery.builder().type("answer").query(query).scoreMode(ScoreMode.None).build(), Question.Answer.class))
                .consumeNextWith(question -> {
                    assertThat(((Question) question).getId(), is(equalTo("1")));
                })
                .consumeNextWith(question -> {
                    assertThat(((Question) question).getId(), is(equalTo("2")));
                })
                .verifyComplete();
    }

    @Test
    void hasChildAnswerWithNoQuery() {
        assertThrows(IllegalArgumentException.class, () -> elasticsearchTemplate.hasChild(null, Question.Answer.class));
    }

    @Test
    void hasChildComment() {
        QueryBuilder query = QueryBuilders.matchPhraseQuery("description", "question");
        StepVerifier.create(elasticsearchTemplate.hasChild(HasChildQuery.builder().type("comment").query(query).scoreMode(ScoreMode.None).build(), Question.Answer.class))
                .consumeNextWith(question -> {
                    assertThat(((Question) question).getId(), is(equalTo("1")));
                })
                .consumeNextWith(question -> {
                    assertThat(((Question) question).getId(), is(equalTo("3")));
                })
                .verifyComplete();
    }

    @Test
    void hasParent() {
        // find all childs
        StepVerifier.create(elasticsearchTemplate.hasParent(HasParentQuery.builder().type("question").query(QueryBuilders.matchAllQuery()).build(), Question.class))
                .expectNextCount(6)
                .verifyComplete();
    }

    @Test
    void hasParentIdAnswer() {
        ParentIdQuery query = ParentIdQuery.builder().type("answer").parentId("1").build();
        StepVerifier.create(elasticsearchTemplate.hasParentId(query, Question.Answer.class))
                .consumeNextWith(answer -> {
                    assertThat(((Question.Answer) answer).getId(), is(equalTo("4")));
                })
                .verifyComplete();
    }

    @Test
    void hasParentIdAnswerWithQuery() {
        ParentIdQuery query = ParentIdQuery.builder().type("answer").parentId("2")
                .query(QueryBuilders.matchPhraseQuery("description", "java")).build();
        StepVerifier.create(elasticsearchTemplate.hasParentId(query, Question.Answer.class))
                .consumeNextWith(answer -> {
                    assertThat(((Question.Answer) answer).getId(), is(equalTo("5")));
                })
                .verifyComplete();
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class ElasticsearchConfiguration extends ReactiveElasticsearchConfiguration {

        @Bean
        ClientLoggerAspect clientLoggerAspect() {
            return new ClientLoggerAspect();
        }
    }

}
