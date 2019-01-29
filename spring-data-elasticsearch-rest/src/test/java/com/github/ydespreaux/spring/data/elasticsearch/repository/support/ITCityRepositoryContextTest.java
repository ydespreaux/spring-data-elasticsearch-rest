/* * Copyright (C) 2018 Yoann Despréaux * * This program is free software; you can redistribute it and/or modify * it under the terms of the GNU General Public License as published by * the Free Software Foundation; either version 2 of the License, or * (at your option) any later version. * * This program is distributed in the hope that it will be useful, * but WITHOUT ANY WARRANTY; without even the implied warranty of * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the * GNU General Public License for more details. * * You should have received a copy of the GNU General Public License * along with this program; see the file COPYING . If not, write to the * Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA. * * Please send bugreports with examples or suggestions to yoann.despreaux@believeit.fr */package com.github.ydespreaux.spring.data.elasticsearch.repository.support;import com.github.ydespreaux.spring.data.elasticsearch.configuration.ElasticsearchTemplateConfiguration;import com.github.ydespreaux.spring.data.elasticsearch.core.ElasticsearchOperations;import com.github.ydespreaux.spring.data.elasticsearch.core.IndexTimeBasedParameter;import com.github.ydespreaux.spring.data.elasticsearch.entities.City;import com.github.ydespreaux.spring.data.elasticsearch.entities.CityTimeBasedSupport;import com.github.ydespreaux.spring.data.elasticsearch.utils.AdminClientUtils;import org.elasticsearch.client.RestHighLevelClient;import org.elasticsearch.cluster.metadata.IndexTemplateMetaData;import org.elasticsearch.common.settings.Settings;import org.hamcrest.Matchers;import org.junit.Test;import org.junit.runner.RunWith;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;import org.springframework.boot.test.context.SpringBootTest;import org.springframework.test.annotation.DirtiesContext;import org.springframework.test.context.junit4.SpringRunner;import static org.hamcrest.Matchers.equalTo;import static org.hamcrest.Matchers.notNullValue;import static org.hamcrest.core.Is.is;import static org.junit.Assert.assertThat;/** * @author Yoann Despréaux * @since 1.0.0 */@DirtiesContext@RunWith(SpringRunner.class)@SpringBootTest(classes = {        RestClientAutoConfiguration.class,        ElasticsearchTemplateConfiguration.class})public class ITCityRepositoryContextTest {    @Autowired    private RestHighLevelClient client;    @Autowired    private ElasticsearchOperations elasticsearchOperations;    @Test    public void context() throws Exception {        IndexTemplateMetaData template = AdminClientUtils.getTemplate(client, "city");        assertThat(template, Matchers.is(notNullValue()));        assertThat(template.getName(), Matchers.is(equalTo("city")));        assertThat(template.getPatterns().size(), Matchers.is(equalTo(2)));        assertThat(template.getPatterns().get(0), Matchers.is(equalTo("ville-*")));        assertThat(template.getPatterns().get(1), Matchers.is(equalTo("metropole-*")));        Settings settings = template.getSettings();        assertThat(settings, Matchers.is(notNullValue()));        assertThat(settings.get("index.refresh_interval"), Matchers.is(equalTo("1s")));        assertThat(settings.get("index.number_of_shards"), Matchers.is(equalTo("1")));        assertThat(settings.get("index.number_of_replicas"), Matchers.is(equalTo("1")));        assertThat(settings.get("index.store.type"), Matchers.is(equalTo("fs")));        assertThat(template.getAliases().containsKey("cities"), Matchers.is(true));        assertThat(template.getMappings().containsKey("city"), Matchers.is(true));        //        CityTimeBasedSupport timeBased = new CityTimeBasedSupport();        String indexName = timeBased.buildIndex(IndexTimeBasedParameter.of("%s-%s", (City) null));        assertThat(this.elasticsearchOperations.indexExists(indexName), is(true));    }}