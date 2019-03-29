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
import com.github.ydespreaux.spring.data.elasticsearch.core.query.HasChildQuery;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.HasParentQuery;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.NativeSearchQuery;
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

import java.util.List;

import static org.elasticsearch.index.query.Operator.AND;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Yoann Despréaux
 */
@Tag("integration")
@SpringBootTest(classes = {
        RestClientAutoConfiguration.class,
        ElasticsearchTemplateParentFieldChildTest.ElasticsearchConfiguration.class
})
@Testcontainers
public class ElasticsearchTemplateParentFieldChildTest {

    @Container
    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer(Versions.ELASTICSEARCH_VERSION)
            .withFileInitScript("scripts/parent-child.script");
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    void setElasticsearchTemplate(ElasticsearchTemplate elasticsearchTemplate) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        EntityMapper mapper = elasticsearchTemplate.getResultsMapper().getEntityMapper();
        mapper.register(elasticsearchTemplate.getPersistentEntityFor(Question.class));
        mapper.register(elasticsearchTemplate.getPersistentEntityFor(Question.Answer.class));
        mapper.register(elasticsearchTemplate.getPersistentEntityFor(Question.Comment.class));
        mapper.register(elasticsearchTemplate.getPersistentEntityFor(Question.Vote.class));
    }

    @Test
    void parentSerialize() {
        EntityMapper mapper = elasticsearchTemplate.getResultsMapper().getEntityMapper();
        Question entity = new Question("1", "Parent1");
        String json = mapper.mapToString(entity);
        assertThat(json, is(equalTo("{\"description\":\"Parent1\",\"join_field\":{\"name\":\"question\"}}")));
    }

    @Test
    void childSerialize() {
        EntityMapper mapper = elasticsearchTemplate.getResultsMapper().getEntityMapper();
        Question.Answer child = new Question.Answer("1", "1", "Child1");
        String json = mapper.mapToString(child);
        assertThat(json, is(equalTo("{\"description\":\"Child1\",\"join_field\":{\"name\":\"answer\",\"parent\":\"1\"}}")));

    }

    @Test
    void parentDeserialize() {
        EntityMapper mapper = elasticsearchTemplate.getResultsMapper().getEntityMapper();
        String json = "{\"description\":\"Parent1\",\"join_field\":{\"name\":\"question\"}}";
        Question parent = mapper.mapToObject(json, Question.class);
        assertThat(parent.getDescription(), is(equalTo("Parent1")));
    }

    @Test
    void childDeserialize() {
        // register ChildEntity
        elasticsearchTemplate.getPersistentEntityFor(Question.Answer.class);
        //
        EntityMapper mapper = elasticsearchTemplate.getResultsMapper().getEntityMapper();
        String json = "{\"description\":\"Child1\",\"join_field\":{\"name\":\"answer\",\"parent\":\"1\"}}";
        Question.Answer child = mapper.mapToObject(json, Question.Answer.class);
        assertThat(child.getDescription(), is(equalTo("Child1")));
        assertThat(child.getParentId(), is(equalTo("1")));

    }

    @Test
    void hasChildAnswer() {
        QueryBuilder query = QueryBuilders.matchPhraseQuery("description", "question");
        List<Question> questionsWithAnswer =
                elasticsearchTemplate.hasChild(HasChildQuery.builder().type("answer").query(query).scoreMode(ScoreMode.None).build(), Question.Answer.class);
        assertThat(questionsWithAnswer, contains(hasProperty("id", is("1")), hasProperty("id", is("2"))));
    }

    @Test
    void hasChildAnswerWithNoQuery() {
        assertThrows(IllegalArgumentException.class, () -> elasticsearchTemplate.hasChild(null, Question.Answer.class));
    }

    @Test
    void hasChildComment() {
        QueryBuilder query = QueryBuilders.matchPhraseQuery("description", "question");
        List<Question> questionsWithComment =
                elasticsearchTemplate.hasChild(HasChildQuery.builder().type("comment").query(query).scoreMode(ScoreMode.None).build(), Question.Comment.class);
        assertThat(questionsWithComment, contains(hasProperty("id", is("1")), hasProperty("id", is("3"))));
    }

    @Test
    void hasParent() {
        // find all childs
        List<? extends Question> childs = elasticsearchTemplate.hasParent(HasParentQuery.builder().type("question").query(QueryBuilders.matchAllQuery()).build(), Question.class);
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
        List<? extends Question> childs = elasticsearchTemplate.hasParent(HasParentQuery.builder().type("question").query(QueryBuilders.queryStringQuery("Question 2").field("description").defaultOperator(AND)).build(), Question.class);
        assertThat(childs, contains(
                hasProperty("id", is("5")),
                hasProperty("id", is("6"))));
        assertThat(childs, contains(
                hasProperty("parentId", is("2")),
                hasProperty("parentId", is("2"))));
    }

    @Test
    void hasParentMultiLevel() {
        // find all childs
        List<? extends Question.Answer> childs = elasticsearchTemplate.hasParent(HasParentQuery.builder().type("answer").query(QueryBuilders.matchAllQuery()).build(), Question.Answer.class);
        assertThat(childs, contains(
                hasProperty("id", is("10")),
                hasProperty("id", is("11"))));
        assertThat(childs, contains(
                hasProperty("parentId", is("4")),
                hasProperty("parentId", is("6"))));
    }

    @Test
    void hasParentBySearchQueryMultiLevel() {
        // find all childs
        List<? extends Question.Answer> childs = elasticsearchTemplate.hasParent(HasParentQuery.builder().type("answer").query(QueryBuilders.queryStringQuery("Answer 1 of Question 1").field("description").defaultOperator(AND)).build(), Question.Answer.class);
        assertThat(childs, contains(
                hasProperty("id", is("10"))));
        assertThat(childs, contains(
                hasProperty("parentId", is("4"))));
    }

    @Test
    void hasParentIdAnswer() {
        ParentIdQuery query = ParentIdQuery.builder().type("answer").parentId("1").build();
        List<Question.Answer> answersWithParentId = elasticsearchTemplate.hasParentId(query, Question.Answer.class);
        assertThat(answersWithParentId, contains(hasProperty("id", is("4"))));
    }

    @Test
    void hasParentIdAnswerWithQuery() {
        ParentIdQuery query = ParentIdQuery.builder().type("answer").parentId("2")
                .query(QueryBuilders.matchPhraseQuery("description", "java")).build();
        List<Question.Answer> answersWithParentId = elasticsearchTemplate.hasParentId(query, Question.Answer.class);
        assertThat(answersWithParentId, contains(hasProperty("id", is("5"))));
    }

    @Test
    void hasParentIdComment() {
        ParentIdQuery query = ParentIdQuery.builder().type("comment").parentId("1").build();
        List<Question.Comment> commentsWithParentId = elasticsearchTemplate.hasParentId(query, Question.Comment.class);
        assertThat(commentsWithParentId, contains(
                hasProperty("id", is("7")),
                hasProperty("id", is("8"))));
    }

    @Test
    void findAll() {
        List<? extends Question> entities = elasticsearchTemplate.search(new NativeSearchQuery(QueryBuilders.matchAllQuery()), Question.class);
        assertThat(entities, contains(
                hasProperty("id", is("1")),
                hasProperty("id", is("2")),
                hasProperty("id", is("3")),
                hasProperty("id", is("4")),
                hasProperty("id", is("5")),
                hasProperty("id", is("6")),
                hasProperty("id", is("7")),
                hasProperty("id", is("8")),
                hasProperty("id", is("9")),
                hasProperty("id", is("10")),
                hasProperty("id", is("11"))
        ));
        assertThat(entities, contains(
                hasProperty("description", is("Question 1")),
                hasProperty("description", is("Question 2")),
                hasProperty("description", is("Question 3")),
                hasProperty("description", is("Answer 1 of Question 1")),
                hasProperty("description", is("Answer 1 of Question 2 with Java")),
                hasProperty("description", is("Answer 2 of Question 2 with Angular")),
                hasProperty("description", is("This is a comment for question 1")),
                hasProperty("description", is("This is another comment for question 1")),
                hasProperty("description", is("This is a comment for question 3")),
                hasProperty("stars", is(4)),
                hasProperty("stars", is(1))
        ));

    }

    @Configuration
    @EnableAspectJAutoProxy
    static class ElasticsearchConfiguration extends ElasticsearchConfigurationSupport {

        @Bean
        ClientLoggerAspect clientLoggerAspect() {
            return new ClientLoggerAspect();
        }
    }

}
