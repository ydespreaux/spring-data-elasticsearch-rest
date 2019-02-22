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

import com.github.ydespreaux.spring.data.elasticsearch.client.ClientLoggerAspect;
import com.github.ydespreaux.spring.data.elasticsearch.configuration.ElasticsearchConfigurationSupport;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.NativeSearchQuery;
import com.github.ydespreaux.spring.data.elasticsearch.entities.ParentEntity;
import com.github.ydespreaux.testcontainers.elasticsearch.ElasticsearchContainer;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.elasticsearch.join.query.JoinQueryBuilders.hasChildQuery;
import static org.elasticsearch.join.query.JoinQueryBuilders.hasParentQuery;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Philipp Jardas
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {
        RestClientAutoConfiguration.class,
        ITElasticsearchTemplateParentFieldChildTest.ElasticsearchConfiguration.class
})
public class ITElasticsearchTemplateParentFieldChildTest {

    @ClassRule
    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer("6.4.2");

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Before
    public void before() {
        clean();
        elasticsearchTemplate.createIndex(ParentEntity.class);
    }

    @Test
    public void parentSerialize() {
        EntityMapper mapper = elasticsearchTemplate.getResultsMapper().getEntityMapper();
        ParentEntity entity = new ParentEntity("1", "Parent1");
        String json = mapper.mapToString(entity);
        assertThat(json, is(equalTo("{\"name\":\"Parent1\",\"relation\":{\"name\":\"question\"}}")));
    }

    @Test
    public void childSerialize() {
        EntityMapper mapper = elasticsearchTemplate.getResultsMapper().getEntityMapper();
        ParentEntity.ChildEntity child = new ParentEntity.ChildEntity("1", "1", "Child1");
        String json = mapper.mapToString(child);
        assertThat(json, is(equalTo("{\"name\":\"Child1\",\"relation\":{\"name\":\"answer\",\"parent\":\"1\"}}")));

    }

    @Test
    public void parentDeserialize() {
        EntityMapper mapper = elasticsearchTemplate.getResultsMapper().getEntityMapper();
        String json = "{\"name\":\"Parent1\",\"relation\":{\"name\":\"question\"}}";
        ParentEntity parent = mapper.mapToObject(json, ParentEntity.class);
        assertThat(parent.getName(), is(equalTo("Parent1")));
    }

    @Test
    public void childDeserialize() {
        // register ChildEntity
        elasticsearchTemplate.getPersistentEntityFor(ParentEntity.ChildEntity.class);
        //
        EntityMapper mapper = elasticsearchTemplate.getResultsMapper().getEntityMapper();
        String json = "{\"name\":\"Child1\",\"relation\":{\"name\":\"answer\",\"parent\":\"1\"}}";
        ParentEntity.ChildEntity child = mapper.mapToObject(json, ParentEntity.ChildEntity.class);
        assertThat(child.getName(), is(equalTo("Child1")));
        assertThat(child.getParentId(), is(equalTo("1")));

    }

    @After
    public void clean() {
        elasticsearchTemplate.deleteIndexByName("parent-child");
    }

    @Test
    public void shouldIndexParentChildEntity() {
        // index two parents
        ParentEntity question1 = index("1", "Question 1");
        ParentEntity question2 = index("2", "Question 2");

        // index a child for each parent
        ParentEntity.ChildEntity answer1 = index("3", question1.getId(), "Answer 1 of question 1");
        ParentEntity.ChildEntity answer2 = index("4", question2.getId(), "Answer  1 of question 2");
        ParentEntity.ChildEntity answer3 = index("5", question2.getId(), "Answer  2 of question 2");

        elasticsearchTemplate.refresh(ParentEntity.class);
        // find all parents that have the first child
        QueryBuilder query = hasChildQuery("answer", QueryBuilders.matchPhraseQuery("name", "question"), ScoreMode.None);
        List<ParentEntity> parents = elasticsearchTemplate.search(new NativeSearchQuery(query), ParentEntity.class);
        assertThat(parents, contains(hasProperty("id", is(question1.getId())), hasProperty("id", is(question2.getId()))));

        // find all childs that have the first child
        QueryBuilder childQuery = hasParentQuery("question", QueryBuilders.matchPhraseQuery("name", question2.getName()), true);
        List<ParentEntity.ChildEntity> childs = elasticsearchTemplate.search(new NativeSearchQuery(childQuery), ParentEntity.ChildEntity.class);
        assertThat(childs, contains(hasProperty("id", is(answer2.getId())), hasProperty("id", is(answer3.getId()))));
        assertThat(childs, contains(hasProperty("parentId", is(question2.getId())), hasProperty("parentId", is(question2.getId()))));
        assertThat(childs.get(0).getParentId(), is(equalTo(question2.getId())));
        assertThat(childs.get(1).getParentId(), is(equalTo(question2.getId())));
    }

    private ParentEntity index(String parentId, String name) {
        ParentEntity parent = new ParentEntity(parentId, name);
        return elasticsearchTemplate.index(parent, ParentEntity.class);
    }

    private ParentEntity.ChildEntity index(String childId, String parentId, String name) {
        ParentEntity.ChildEntity child = new ParentEntity.ChildEntity(childId, parentId, name);
        return elasticsearchTemplate.index(child, ParentEntity.ChildEntity.class);
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
