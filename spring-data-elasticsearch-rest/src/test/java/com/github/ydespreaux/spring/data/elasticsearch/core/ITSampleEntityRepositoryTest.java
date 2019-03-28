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

import com.github.ydespreaux.spring.data.elasticsearch.AbstractElasticsearchTest;
import com.github.ydespreaux.spring.data.elasticsearch.Versions;
import com.github.ydespreaux.spring.data.elasticsearch.client.ClientLoggerAspect;
import com.github.ydespreaux.spring.data.elasticsearch.configuration.ElasticsearchConfigurationSupport;
import com.github.ydespreaux.spring.data.elasticsearch.entities.SampleEntity;
import com.github.ydespreaux.spring.data.elasticsearch.repositories.sampleindex.SampleEntityRepository;
import com.github.ydespreaux.spring.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import com.github.ydespreaux.testcontainers.elasticsearch.ElasticsearchContainer;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Before;
import org.junit.ClassRule;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        RestClientAutoConfiguration.class,
        ITSampleEntityRepositoryTest.ElasticsearchConfiguration.class})
@Profile("test-no-template")
public class ITSampleEntityRepositoryTest extends AbstractElasticsearchTest<SampleEntity> {

    @ClassRule
    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer(Versions.ELASTICSEARCH_VERSION);

    private static final String INDEX_NAME = "sample-entity-index";
    @Autowired
    private SampleEntityRepository repository;

    public ITSampleEntityRepositoryTest() {
        super(SampleEntity.class);
    }

    @Override
    protected List<SampleEntity> generateData() {
        return Arrays.asList(
                createSampleEntity(null, "Entity 1"),
                createSampleEntity(null, "Entity 2"),
                createSampleEntity(null, "Entity 3"),
                createSampleEntity(null, "Entity 4")
        );
    }

    @Before
    public void setUp() {
        cleanData();
    }

    @Test
    public void findById() {
        List<SampleEntity> data = insertData();
        SampleEntity myEntity = data.get(0);
        Optional<SampleEntity> entity = this.repository.findById(myEntity.getId());
        assertThat(entity.isPresent(), is(true));
    }

    @Test
    public void findByIdNotFound() {
        insertData();
        Optional<SampleEntity> entity = this.repository.findById("-1");
        assertThat(entity.isPresent(), is(false));
    }

    @Test
    public void count() {
        insertData();
        assertThat(this.repository.count(), is(equalTo(4L)));
    }

    @Test
    public void existsById() {
        List<SampleEntity> data = insertData();
        assertThat(this.repository.existsById(data.get(0).getId()), is(equalTo(true)));
    }

    @Test
    public void existsByIdNotExists() {
        insertData();
        assertThat(this.repository.existsById("-1"), is(equalTo(false)));
    }

    @Test
    public void findAll() {
        insertData();
        List<SampleEntity> entities = this.repository.findAll();
        assertThat(entities.size(), is(equalTo(4)));
        for (int i = 0; i < 4; i++) {
            assertThat(entities.get(i).getScore(), is(equalTo(1.0f)));
            assertThat(entities.get(i).getVersion(), is(equalTo(1L)));
            assertThat(entities.get(i).getIndexName(), is(equalTo(INDEX_NAME)));
        }
    }

    @Test
    public void findAllWithPageable() {
        insertData();
        Page<SampleEntity> entities = this.repository.findAll(PageRequest.of(0, 3));
        assertThat(entities.getTotalElements(), is(equalTo(4L)));
        assertThat(entities.hasContent(), is(true));
        assertThat(entities.getContent().size(), is(equalTo(3)));
        for (int i = 0; i < 3; i++) {
            assertThat(entities.getContent().get(i).getScore(), is(equalTo(1.0f)));
            assertThat(entities.getContent().get(i).getVersion(), is(equalTo(1L)));
            assertThat(entities.getContent().get(i).getIndexName(), is(equalTo(INDEX_NAME)));
        }
    }

    @Test
    public void findByQuery() {
        insertData();
        List<SampleEntity> entities = this.repository.findByQuery(QueryBuilders.matchAllQuery(), Sort.by(Sort.Direction.ASC, "name.keyword"));
        assertThat(entities.size(), is(equalTo(4)));
        assertThat(entities.get(0).getName(), is(equalTo("Entity 1")));
        assertThat(Float.isNaN(entities.get(0).getScore()), is(true));
        assertThat(entities.get(0).getVersion(), is(equalTo(1L)));
        assertThat(entities.get(0).getIndexName(), is(equalTo(INDEX_NAME)));

        assertThat(entities.get(1).getName(), is(equalTo("Entity 2")));
        assertThat(Float.isNaN(entities.get(1).getScore()), is(true));
        assertThat(entities.get(1).getVersion(), is(equalTo(1L)));
        assertThat(entities.get(0).getIndexName(), is(equalTo(INDEX_NAME)));

        assertThat(entities.get(2).getName(), is(equalTo("Entity 3")));
        assertThat(Float.isNaN(entities.get(2).getScore()), is(true));
        assertThat(entities.get(2).getVersion(), is(equalTo(1L)));
        assertThat(entities.get(2).getIndexName(), is(equalTo(INDEX_NAME)));

        assertThat(entities.get(3).getName(), is(equalTo("Entity 4")));
        assertThat(Float.isNaN(entities.get(3).getScore()), is(true));
        assertThat(entities.get(3).getVersion(), is(equalTo(1L)));
        assertThat(entities.get(3).getIndexName(), is(equalTo(INDEX_NAME)));
    }

    @Test
    public void save() {
        insertData();
        SampleEntity newEntity = SampleEntity.builder().name("My new entity").build();
        SampleEntity entityIndexed = this.repository.save(newEntity);
        this.repository.refresh();
        assertThat(entityIndexed.getId(), is(notNullValue()));
        assertThat(entityIndexed.getName(), is(equalTo(newEntity.getName())));
        assertThat(entityIndexed.getScore(), is(nullValue()));
        assertThat(entityIndexed.getVersion(), is(equalTo(1L)));
        assertThat(entityIndexed.getIndexName(), is(equalTo(INDEX_NAME)));

    }

    @Test
    public void deleteById() {
        List<SampleEntity> data = insertData();
        SampleEntity myEntity = data.get(0);
        this.repository.deleteById(myEntity.getId());
        this.repository.refresh();
        assertThat(this.repository.findById(myEntity.getId()).isPresent(), is(false));
    }

    @Test
    public void delete() {
        List<SampleEntity> data = insertData();
        SampleEntity myEntity = data.get(0);
        this.repository.delete(myEntity);
        this.repository.refresh();
        assertThat(this.repository.findById(myEntity.getId()).isPresent(), is(false));
    }

    @Test
    public void deleteAllWithCollection() {
        List<SampleEntity> data = insertData();
        List<SampleEntity> entities = Arrays.asList(data.get(0), data.get(1));
        this.repository.deleteAll(entities);
        this.repository.refresh();
        assertThat(this.repository.count(), is(equalTo(2L)));
    }

    @Test
    public void deleteAll() {
        insertData();
        this.repository.deleteAll();
        this.repository.refresh();
        assertThat(this.repository.count(), is(equalTo(0L)));
    }

    private SampleEntity createSampleEntity(String id, String name) {
        return SampleEntity.builder()
                .id(id)
                .name(name)
                .build();
    }

    @Configuration
    @EnableAspectJAutoProxy
    @EnableAutoConfiguration
    @EnableElasticsearchRepositories(
            basePackages = "com.github.ydespreaux.spring.data.elasticsearch.repositories.sampleindex")
    static class ElasticsearchConfiguration extends ElasticsearchConfigurationSupport {

        @Bean
        ClientLoggerAspect clientLoggerAspect() {
            return new ClientLoggerAspect();
        }
    }

}
