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
import com.github.ydespreaux.spring.data.elasticsearch.repositories.parent.VoteRepository;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("integration-nested")
@SpringBootTest(classes = {
        RestClientAutoConfiguration.class,
        VoteRepositoryTest.ElasticsearchConfiguration.class})
@Profile("test-no-template")
public class VoteRepositoryTest {

    @Autowired
    private VoteRepository voteRepository;

    @Test
    void hasChild() {
        List<Question.Answer> answers = this.voteRepository.hasChild();
        assertThat(answers, contains(hasProperty("id", is("4")), hasProperty("id", is("6"))));
    }

    @Test
    void hasChildWithCriteria() {
        List<Question.Answer> answers = this.voteRepository.hasChildByQuery(new Criteria("stars").is(4));
        assertThat(answers, contains(hasProperty("id", is("4"))));
    }

    @Test
    void hasChildWithQuery() {
        List<Question.Answer> answers = this.voteRepository.hasChildByQuery(QueryBuilders.termQuery("stars", 4));
        assertThat(answers, contains(hasProperty("id", is("4"))));
    }

    @Test
    void hasParentId() {
        List<Question.Vote> votes = this.voteRepository.hasParentId("4");
        assertThat(votes, contains(hasProperty("id", is("10"))));
        assertThat(votes, contains(hasProperty("parentId", is("4"))));
    }

    @Test
    void hasParentIdWithoutChild() {
        List<Question.Vote> votes = this.voteRepository.hasParentId("5");
        assertThat(votes.isEmpty(), is(true));
    }

    @Test
    void hasParentIdWithCriteria() {
        List<Question.Vote> votes = this.voteRepository.hasParentId("4", new Criteria("stars").is(4));
        assertThat(votes, contains(hasProperty("id", is("10"))));
        assertThat(votes, contains(hasProperty("parentId", is("4"))));
    }

    @Test
    void hasParentIdWithQueryBuilder() {
        List<Question.Vote> votes = this.voteRepository.hasParentId("4", QueryBuilders.termQuery("stars", 4));
        assertThat(votes, contains(hasProperty("id", is("10"))));
        assertThat(votes, contains(hasProperty("parentId", is("4"))));
    }

    @Test
    void hasParent() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> this.voteRepository.hasParent());
    }

    @Test
    void hasParentWithQuery() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> this.voteRepository.hasParentByQuery(QueryBuilders.matchAllQuery()));
    }

    @Test
    void hasParentWithCriteria() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> this.voteRepository.hasParentByQuery(Criteria.where("description").is("Answer 1 of Question 1")));
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
