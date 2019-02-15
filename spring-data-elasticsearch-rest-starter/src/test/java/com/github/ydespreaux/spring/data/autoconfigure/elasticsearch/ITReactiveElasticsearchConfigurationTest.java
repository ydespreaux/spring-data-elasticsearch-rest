/* * Copyright (C) 2018 Yoann Despréaux * * This program is free software; you can redistribute it and/or modify * it under the terms of the GNU General Public License as published by * the Free Software Foundation; either version 2 of the License, or * (at your option) any later version. * * This program is distributed in the hope that it will be useful, * but WITHOUT ANY WARRANTY; without even the implied warranty of * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the * GNU General Public License for more details. * * You should have received a copy of the GNU General Public License * along with this program; see the file COPYING . If not, write to the * Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA. * * Please send bugreports with examples or suggestions to yoann.despreaux@believeit.fr */package com.github.ydespreaux.spring.data.autoconfigure.elasticsearch;import com.github.ydespreaux.spring.data.elasticsearch.client.reactive.ReactiveRestElasticsearchClient;import com.github.ydespreaux.spring.data.elasticsearch.core.ReactiveElasticsearchOperations;import org.junit.Test;import org.junit.runner.RunWith;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;import org.springframework.boot.test.context.SpringBootTest;import org.springframework.context.ApplicationContext;import org.springframework.test.context.junit4.SpringRunner;import static org.hamcrest.Matchers.is;import static org.hamcrest.Matchers.notNullValue;import static org.junit.Assert.assertThat;/** * @author Yoann Despréaux * @since 1.0.0 */@RunWith(SpringRunner.class)@SpringBootTest(classes = {        RestClientAutoConfiguration.class,        ReactiveElasticsearchDataAutoConfiguration.class})public class ITReactiveElasticsearchConfigurationTest {    @Autowired    private ApplicationContext context;    @Test    public void context() {        assertThat(context.getBean(ReactiveRestElasticsearchClient.class), is(notNullValue()));        assertThat(context.getBean(ReactiveElasticsearchOperations.class), is(notNullValue()));    }}