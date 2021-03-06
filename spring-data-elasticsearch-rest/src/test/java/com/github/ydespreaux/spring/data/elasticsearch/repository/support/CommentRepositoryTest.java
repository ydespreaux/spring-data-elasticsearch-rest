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
import com.github.ydespreaux.spring.data.elasticsearch.repositories.parent.CommentRepository;
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
        CommentRepositoryTest.ElasticsearchConfiguration.class})
@Profile("test-no-template")
public class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void hasChild() {
        List<Question> questions = this.commentRepository.hasChild();
        assertThat(questions, contains(hasProperty("id", is("1")), hasProperty("id", is("3"))));
    }

    @Test
    void hasChildWithCriteria() {
        List<Question> questions = this.commentRepository.hasChildByQuery(new Criteria("description").contains("another"));
        assertThat(questions, contains(hasProperty("id", is("1"))));
    }

    @Test
    void hasChildWithQuery() {
        List<Question> questions = this.commentRepository.hasChildByQuery(QueryBuilders.matchPhraseQuery("description", "another"));
        assertThat(questions, contains(hasProperty("id", is("1"))));
    }

    @Test
    void hasParentId() {
        List<Question.Comment> comments = this.commentRepository.hasParentId("1");
        assertThat(comments, contains(hasProperty("id", is("7")), hasProperty("id", is("8"))));
        assertThat(comments, contains(hasProperty("parentId", is("1")), hasProperty("parentId", is("1"))));
    }

    @Test
    void hasParentIdWithoutChild() {
        List<Question.Comment> comments = this.commentRepository.hasParentId("2");
        assertThat(comments.isEmpty(), is(true));
    }

    @Test
    void hasParentIdWithCriteria() {
        List<Question.Comment> comments = this.commentRepository.hasParentId("1", new Criteria("description").contains("another"));
        assertThat(comments, contains(hasProperty("id", is("8"))));
        assertThat(comments, contains(hasProperty("parentId", is("1"))));
    }

    @Test
    void hasParentIdWithQueryBuilder() {
        List<Question.Comment> comments = this.commentRepository.hasParentId("1", QueryBuilders.matchPhraseQuery("description", "another"));
        assertThat(comments, contains(hasProperty("id", is("8"))));
        assertThat(comments, contains(hasProperty("parentId", is("1"))));
    }

    @Test
    void hasParentIdWithoutChildWithQuery() {
        List<Question.Comment> comments = this.commentRepository.hasParentId("3", QueryBuilders.matchPhraseQuery("description", "another"));
        assertThat(comments.isEmpty(), is(true));
    }

    @Test
    void hasParent() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> this.commentRepository.hasParent());
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
