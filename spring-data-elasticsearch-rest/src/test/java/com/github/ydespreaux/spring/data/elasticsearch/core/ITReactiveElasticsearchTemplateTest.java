/* * Copyright (C) 2018 Yoann Despréaux * * This program is free software; you can redistribute it and/or modify * it under the terms of the GNU General Public License as published by * the Free Software Foundation; either version 2 of the License, or * (at your option) any later version. * * This program is distributed in the hope that it will be useful, * but WITHOUT ANY WARRANTY; without even the implied warranty of * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the * GNU General Public License for more details. * * You should have received a copy of the GNU General Public License * along with this program; see the file COPYING . If not, write to the * Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA. * * Please send bugreports with examples or suggestions to yoann.despreaux@believeit.fr */package com.github.ydespreaux.spring.data.elasticsearch.core;import com.github.ydespreaux.spring.data.elasticsearch.configuration.reactive.ReactiveElasticsearchConfiguration;import com.github.ydespreaux.spring.data.elasticsearch.entities.Book;import com.github.ydespreaux.spring.data.elasticsearch.entities.SampleEntity;import com.github.ydespreaux.testcontainers.elasticsearch.ElasticsearchContainer;import org.elasticsearch.client.RestHighLevelClient;import org.junit.Before;import org.junit.ClassRule;import org.junit.Test;import org.junit.runner.RunWith;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;import org.springframework.boot.test.context.SpringBootTest;import org.springframework.test.annotation.DirtiesContext;import org.springframework.test.context.junit4.SpringRunner;import reactor.test.StepVerifier;import java.time.LocalDate;import java.util.Arrays;import java.util.List;import static org.hamcrest.Matchers.*;import static org.junit.Assert.assertThat;/** * @author Yoann Despréaux * @since 1.0.0 */@DirtiesContext@RunWith(SpringRunner.class)@SpringBootTest(classes = {        RestClientAutoConfiguration.class,        ReactiveElasticsearchConfiguration.class})public class ITReactiveElasticsearchTemplateTest {    @ClassRule    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer("6.4.2");    private static final String INDEX_BOOK_NAME = "books";    @Autowired    private ReactiveElasticsearchOperations reactiveOperations;    @Autowired    private RestHighLevelClient client;    @Autowired    private ElasticsearchOperations operations;    private List<Book> data = null;    @Before    public void onSetup() {        this.data = initializeData();    }    @Test    public void indexWithId() {        Book expected = createBook("2", "Lucky Luck", "Lucky Luck chez les indiens", 10d, LocalDate.of(2010, 5, 12));        StepVerifier.create(this.reactiveOperations.index(expected, Book.class))                .assertNext(actual -> {                    assertThat(actual.getDocumentId(), is(equalTo(expected.getDocumentId())));                    assertThat(actual.getVersion(), is(notNullValue()));                })                .verifyComplete();    }    @Test    public void indexWithoutId() {        Book expected = createBook(null, "Lucky Luck", "Lucky Luck et les Daltons", 10d, LocalDate.of(2008, 5, 12));        StepVerifier.create(this.reactiveOperations.index(expected, Book.class))                .assertNext(actual -> {                    assertThat(actual.getDocumentId(), is(notNullValue()));                    assertThat(actual.getVersion(), is(equalTo(1L)));                })                .verifyComplete();    }    @Test    public void findById() {        Book expected = this.data.get(0);        StepVerifier.create(this.reactiveOperations.findById(expected.getDocumentId(), Book.class))                .assertNext(actual -> {                    assertThat(actual.getDocumentId(), is(equalTo(expected.getDocumentId())));                    assertThat(actual.getVersion(), is(notNullValue()));                    assertThat(actual.getTitle(), is(equalTo(expected.getTitle())));                    assertThat(actual.getDescription(), is(equalTo(expected.getDescription())));                    assertThat(actual.getPrice(), is(equalTo(expected.getPrice())));                    assertThat(actual.getPublication(), is(equalTo(expected.getPublication())));                })                .verifyComplete();    }    @Test    public void findByIdNotfound() {        StepVerifier.create(this.reactiveOperations.findById("-1", Book.class))                .verifyComplete();    }    @Test    public void findByIdWithIndexNotfound() {        StepVerifier.create(this.reactiveOperations.findById("1", SampleEntity.class))                .verifyComplete();    }    private List<Book> initializeData() {        List<Book> books = Arrays.asList(createBook("1", "Mon livre", "Ma description", 10d, null));        operations.deleteAll(Book.class);        operations.bulkIndex(books, Book.class);        operations.refresh(Book.class);        return books;    }    private Book createBook(String id, String title, String description, Double price, LocalDate publication) {        return Book.builder()                .documentId(id)                .title(title)                .description(description)                .price(price)                .publication(publication)                .build();    }}