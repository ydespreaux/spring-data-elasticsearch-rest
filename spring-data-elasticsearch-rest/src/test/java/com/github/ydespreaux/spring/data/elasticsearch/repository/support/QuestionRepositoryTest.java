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

package com.github.ydespreaux.spring.data.elasticsearch.repository.support;

import com.github.ydespreaux.spring.data.elasticsearch.client.ClientLoggerAspect;
import com.github.ydespreaux.spring.data.elasticsearch.configuration.ElasticsearchConfigurationSupport;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.Criteria;
import com.github.ydespreaux.spring.data.elasticsearch.entities.Question;
import com.github.ydespreaux.spring.data.elasticsearch.repositories.parent.QuestionRepository;
import com.github.ydespreaux.spring.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
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
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.util.List;
import java.util.NoSuchElementException;

import static org.elasticsearch.index.query.Operator.AND;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("integration-nested")
@SpringBootTest(classes = {
        RestClientAutoConfiguration.class,
        QuestionRepositoryTest.ElasticsearchConfiguration.class})
@Profile("test-no-template")
public class QuestionRepositoryTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    void hasChild() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> this.questionRepository.hasChild());
    }

    @Test
    void hasChildWithCriteria() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> this.questionRepository.hasChildByQuery(new Criteria("description").contains("another")));
    }

    @Test
    void hasChildWithQuery() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> this.questionRepository.hasChildByQuery(QueryBuilders.matchPhraseQuery("description", "another")));
    }

    @Test
    void hasParentId() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> this.questionRepository.hasParentId("1"));
    }

    @Test
    void hasParentIdWithCriteria() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> this.questionRepository.hasParentId("1", new Criteria("description").contains("another")));
    }

    @Test
    void hasParentIdWithQueryBuilder() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> this.questionRepository.hasParentId("1", QueryBuilders.matchPhraseQuery("description", "another")));
    }

    @Test
    void hasParent() {
        // find all childs
        List<? extends Question> childs = questionRepository.hasParent();
        assertThat(childs, contains(
                hasProperty("id", is("4")),
                hasProperty("id", is("5")),
                hasProperty("id", is("6")),
                hasProperty("id", is("7")),
                hasProperty("id", is("8")),
                hasProperty("id", is("9"))));
        assertThat(childs, contains(
                hasProperty("parentId", is("1")),
                hasProperty("parentId", is("2")),
                hasProperty("parentId", is("2")),
                hasProperty("parentId", is("1")),
                hasProperty("parentId", is("1")),
                hasProperty("parentId", is("3"))));
    }

    @Test
    void hasParentBySearchQuery() {
        // find all childs for Question 2
        List<? extends Question> childs = questionRepository.hasParentByQuery(QueryBuilders.queryStringQuery("Question 2").field("description").defaultOperator(AND));
        assertThat(childs, contains(
                hasProperty("id", is("5")),
                hasProperty("id", is("6"))));
        assertThat(childs, contains(
                hasProperty("parentId", is("2")),
                hasProperty("parentId", is("2"))));
    }

    @Test
    void hasParentByCriteriaQuery() {
        // find all childs for Question 2
        List<? extends Question> childs = questionRepository.hasParentByQuery(Criteria.where("description").is("Question 2"));
        assertThat(childs, contains(
                hasProperty("id", is("5")),
                hasProperty("id", is("6"))));
        assertThat(childs, contains(
                hasProperty("parentId", is("2")),
                hasProperty("parentId", is("2"))));
    }

    @Test
    void hasParentByCriteriaQueryNull() {
        assertThrows(NoSuchElementException.class, () -> questionRepository.hasParentByQuery((Criteria) null));
    }

    @Configuration
    @EnableAspectJAutoProxy
    @EnableAutoConfiguration
    @EnableElasticsearchRepositories(
            basePackages = "com.github.ydespreaux.spring.data.elasticsearch.repositories.parent")
    static class ElasticsearchConfiguration extends ElasticsearchConfigurationSupport {

        @Bean
        ClientLoggerAspect clientLoggerAspect() {
            return new ClientLoggerAspect();
        }
    }

}
