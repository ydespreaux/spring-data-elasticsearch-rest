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

package com.github.ydespreaux.spring.data.elasticsearch.reactive.repository.support;

import com.github.ydespreaux.spring.data.elasticsearch.client.ClientLoggerAspect;
import com.github.ydespreaux.spring.data.elasticsearch.configuration.reactive.ReactiveElasticsearchConfiguration;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.Criteria;
import com.github.ydespreaux.spring.data.elasticsearch.entities.Question;
import com.github.ydespreaux.spring.data.elasticsearch.reactive.repositories.parent.ReactiveAnswerRepository;
import com.github.ydespreaux.spring.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;
import org.elasticsearch.index.query.QueryBuilders;
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
import reactor.test.StepVerifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@Tag("integration-nested")
@SpringBootTest(classes = {
        RestClientAutoConfiguration.class,
        ReactiveAnswerRepositoryTest.ElasticsearchConfiguration.class})
@Profile("test-no-template")
public class ReactiveAnswerRepositoryTest {

    @Autowired
    private ReactiveAnswerRepository answerRepository;

    @Test
    void hasChild() {
        StepVerifier.create(this.answerRepository.hasChild())
                .consumeNextWith(question -> assertThat(((Question) question).getId(), is(equalTo("1"))))
                .consumeNextWith(question -> assertThat(((Question) question).getId(), is(equalTo("2"))))
                .verifyComplete();
    }

    @Test
    void hasChildWithCriteria() {
        StepVerifier.create(this.answerRepository.hasChildByQuery(new Criteria("description").contains("java")))
                .consumeNextWith(question -> assertThat(((Question) question).getId(), is(equalTo("2"))))
                .verifyComplete();
    }

    @Test
    void hasChildWithQuery() {
        StepVerifier.create(this.answerRepository.hasChildByQuery(QueryBuilders.matchPhraseQuery("description", "java")))
                .consumeNextWith(question -> assertThat(((Question) question).getId(), is(equalTo("2"))))
                .verifyComplete();
    }

    @Test
    void hasParentId() {
        StepVerifier.create(this.answerRepository.hasParentId("1"))
                .consumeNextWith(answer -> {
                    assertThat(answer.getId(), is(equalTo("4")));
                    assertThat(answer.getParentId(), is(equalTo("1")));
                })
                .verifyComplete();
    }

    @Test
    void hasParentIdWithoutChild() {
        StepVerifier.create(this.answerRepository.hasParentId("3"))
                .verifyComplete();
    }

    @Test
    void hasParentIdWithCriteria() {
        StepVerifier.create(this.answerRepository.hasParentId("2", new Criteria("description").contains("java")))
                .consumeNextWith(answer -> {
                    assertThat(answer.getId(), is(equalTo("5")));
                    assertThat(answer.getParentId(), is(equalTo("2")));
                })
                .verifyComplete();
    }

    @Test
    void hasParentIdWithQueryBuilder() {
        StepVerifier.create(this.answerRepository.hasParentId("2", QueryBuilders.matchPhraseQuery("description", "angular")))
                .consumeNextWith(answer -> {
                    assertThat(answer.getId(), is(equalTo("6")));
                    assertThat(answer.getParentId(), is(equalTo("2")));
                })
                .verifyComplete();
    }

    @Test
    void hasParent() {
        StepVerifier.create(this.answerRepository.hasParent())
                .consumeNextWith(answer -> {
                    assertThat(((Question.Answer) answer).getId(), is(equalTo("10")));
                    assertThat(((Question.Answer) answer).getParentId(), is(equalTo("4")));
                })
                .consumeNextWith(answer -> {
                    assertThat(((Question.Answer) answer).getId(), is(equalTo("11")));
                    assertThat(((Question.Answer) answer).getParentId(), is(equalTo("6")));
                })
                .verifyComplete();
    }

    @Test
    void hasParentWithQuery() {
        StepVerifier.create(this.answerRepository.hasParentByQuery(QueryBuilders.matchAllQuery()))
                .consumeNextWith(answer -> {
                    assertThat(((Question.Answer) answer).getId(), is(equalTo("10")));
                    assertThat(((Question.Answer) answer).getParentId(), is(equalTo("4")));
                })
                .consumeNextWith(answer -> {
                    assertThat(((Question.Answer) answer).getId(), is(equalTo("11")));
                    assertThat(((Question.Answer) answer).getParentId(), is(equalTo("6")));
                })
                .verifyComplete();
    }

    @Test
    void hasParentWithCriteria() {
        StepVerifier.create(this.answerRepository.hasParentByQuery(Criteria.where("description").is("Answer 1 of Question 1")))
                .consumeNextWith(answer -> {
                    assertThat(((Question.Answer) answer).getId(), is(equalTo("10")));
                    assertThat(((Question.Answer) answer).getParentId(), is(equalTo("4")));
                })
                .verifyComplete();
    }

    @Configuration
    @EnableAspectJAutoProxy
    @EnableAutoConfiguration
    @EnableReactiveElasticsearchRepositories(
            basePackages = "com.github.ydespreaux.spring.data.elasticsearch.reactive.repositories.parent")
    static class ElasticsearchConfiguration extends ReactiveElasticsearchConfiguration {

        @Bean
        ClientLoggerAspect clientLoggerAspect() {
            return new ClientLoggerAspect();
        }
    }

}
