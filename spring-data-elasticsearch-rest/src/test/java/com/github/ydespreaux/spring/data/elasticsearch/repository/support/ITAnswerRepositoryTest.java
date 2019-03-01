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
import com.github.ydespreaux.spring.data.elasticsearch.repositories.parent.AnswerRepository;
import com.github.ydespreaux.spring.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.elasticsearch.index.query.QueryBuilders;
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
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        RestClientAutoConfiguration.class,
        ITAnswerRepositoryTest.ElasticsearchConfiguration.class})
@Profile("test-no-template")
public class ITAnswerRepositoryTest {

    @Autowired
    private AnswerRepository answerRepository;

    @Test
    public void hasChild() {
        List<Question> questions = this.answerRepository.hasChild();
        assertThat(questions, contains(hasProperty("id", is("1")), hasProperty("id", is("2"))));
    }

    @Test
    public void hasChildWithCriteria() {
        List<Question> questions = this.answerRepository.hasChildByQuery(new Criteria("description").contains("java"));
        assertThat(questions, contains(hasProperty("id", is("2"))));
    }

    @Test
    public void hasChildWithQuery() {
        List<Question> questions = this.answerRepository.hasChildByQuery(QueryBuilders.matchPhraseQuery("description", "java"));
        assertThat(questions, contains(hasProperty("id", is("2"))));
    }

    @Test
    public void hasParentId() {
        List<Question.Answer> answers = this.answerRepository.hasParentId("1");
        assertThat(answers, contains(hasProperty("id", is("4"))));
        assertThat(answers, contains(hasProperty("parentId", is("1"))));
    }

    @Test
    public void hasParentIdWithoutChild() {
        List<Question.Answer> answers = this.answerRepository.hasParentId("3");
        assertThat(answers.isEmpty(), is(true));
    }

    @Test
    public void hasParentIdWithCriteria() {
        List<Question.Answer> answers = this.answerRepository.hasParentId("2", new Criteria("description").contains("java"));
        assertThat(answers, contains(hasProperty("id", is("5"))));
        assertThat(answers, contains(hasProperty("parentId", is("2"))));
    }

    @Test
    public void hasParentIdWithQueryBuilder() {
        List<Question.Answer> answers = this.answerRepository.hasParentId("2", QueryBuilders.matchPhraseQuery("description", "angular"));
        assertThat(answers, contains(hasProperty("id", is("6"))));
        assertThat(answers, contains(hasProperty("parentId", is("2"))));
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void hasParent() {
        this.answerRepository.hasParent();
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void hasParentWithQuery() {
        this.answerRepository.hasParentByQuery(QueryBuilders.matchAllQuery());
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void hasParentWithCriteria() {
        this.answerRepository.hasParentByQuery(Criteria.where("description").is("Question 2"));
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
