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

package com.github.ydespreaux.spring.data.elasticsearch.reactive.repository.support;

import com.github.ydespreaux.spring.data.elasticsearch.client.ClientLoggerAspect;
import com.github.ydespreaux.spring.data.elasticsearch.configuration.reactive.ReactiveElasticsearchConfiguration;
import com.github.ydespreaux.spring.data.elasticsearch.reactive.repositories.query.ReactiveProductRepository;
import com.github.ydespreaux.spring.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;
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
import org.springframework.test.annotation.DirtiesContext;
import reactor.test.StepVerifier;

import java.util.Arrays;


@DirtiesContext
@Tag("integration-nested")
@SpringBootTest(classes = {
        RestClientAutoConfiguration.class,
        ReactiveProductRepositoryTest.ElasticsearchConfiguration.class})
@Profile("test-no-template")
public class ReactiveProductRepositoryTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ReactiveProductRepository repository;

    @Test
    void countByAvailableTrue() {
        StepVerifier.create(this.repository.countByAvailableTrue())
                .expectNext(3L)
                .verifyComplete();
//        assertThat(this.repository.countByAvailableTrue(), is(equalTo(3L)));
    }

    @Test
    void countByAvailableFalse() {
        StepVerifier.create(this.repository.countByAvailableFalse())
                .expectNext(2L)
                .verifyComplete();
//        assertThat(this.repository.countByAvailableFalse(), is(equalTo(2L)));
    }

    @Test
    void findByAvailableTrue() {
        StepVerifier.create(this.repository.findByAvailableTrue())
                .expectNextCount(3L)
                .verifyComplete();
//        assertThat(this.repository.findByAvailableTrue().size(), is(equalTo(3)));
    }

    @Test
    void findByAvailableFalse() {
        StepVerifier.create(this.repository.findByAvailableFalse())
                .expectNextCount(2L)
                .verifyComplete();
//        assertThat(this.repository.findByAvailableFalse().size(), is(equalTo(2)));
    }

    @Test
    void findByNameAndText() {
        StepVerifier.create(this.repository.findByNameAndText("Sugar", "Cane sugar"))
                .expectNextCount(2L)
                .verifyComplete();
//        assertThat(this.repository.findByNameAndText("Sugar", "Cane sugar").size(), is(equalTo(2)));
    }

    @Test
    void findByNameAndPrice() {
        StepVerifier.create(this.repository.findByNameAndPrice("Sugar", 1.1f))
                .expectNextCount(1L)
                .verifyComplete();
//        assertThat(this.repository.findByNameAndPrice("Sugar", 1.1f).size(), is(equalTo(1)));
    }

    @Test
    void findByNameOrText() {
        StepVerifier.create(this.repository.findByNameOrText("Sugar", "Sea salt"))
                .expectNextCount(4L)
                .verifyComplete();
//        assertThat(this.repository.findByNameOrText("Sugar", "Sea salt").size(), is(equalTo(4)));
    }

    @Test
    void findByNameOrPrice() {
        StepVerifier.create(this.repository.findByNameOrPrice("Sugar", 2.1f))
                .expectNextCount(4L)
                .verifyComplete();
//        assertThat(this.repository.findByNameOrPrice("Sugar", 2.1f).size(), is(equalTo(4)));
    }

    @Test
    void findByPriceInWithArray() {
        StepVerifier.create(this.repository.findByPriceIn(new Float[]{1.2f, 1.1f}))
                .expectNextCount(2L)
                .verifyComplete();
//        assertThat(this.repository.findByPriceIn(new Float[]{1.2f, 1.1f}).size(), is(equalTo(2)));
    }

    @Test
    void findByPriceIn() {
        StepVerifier.create(this.repository.findByPriceIn(Arrays.asList(1.2f, 1.1f)))
                .expectNextCount(2L)
                .verifyComplete();
//        assertThat(this.repository.findByPriceIn(Arrays.asList(1.2f, 1.1f)).size(), is(equalTo(2)));
    }

    @Test
    void findByPriceNotIn() {
        StepVerifier.create(this.repository.findByPriceNotIn(Arrays.asList(1.2f, 1.1f)))
                .expectNextCount(3L)
                .verifyComplete();
//        assertThat(this.repository.findByPriceNotIn(Arrays.asList(1.2f, 1.1f)).size(), is(equalTo(3)));
    }

    @Test
    void findByPriceNot() {
        StepVerifier.create(this.repository.findByPriceNot(1.2f))
                .expectNextCount(4L)
                .verifyComplete();
//        assertThat(this.repository.findByPriceNot(1.2f).size(), is(equalTo(4)));
    }

    @Test
    void findByPriceBetween() {
        StepVerifier.create(this.repository.findByPriceBetween(1.0f, 2.0f))
                .expectNextCount(4L)
                .verifyComplete();
//        assertThat(this.repository.findByPriceBetween(1.0f, 2.0f).size(), is(equalTo(4)));
    }

    @Test
    void findByPriceLessThan() {
        StepVerifier.create(this.repository.findByPriceLessThan(1.1f))
                .expectNextCount(1L)
                .verifyComplete();
//        assertThat(this.repository.findByPriceLessThan(1.1f).size(), is(equalTo(1)));
    }

    @Test
    void findByPriceLessThanEqual() {
        StepVerifier.create(this.repository.findByPriceLessThanEqual(1.1f))
                .expectNextCount(2L)
                .verifyComplete();
//        assertThat(this.repository.findByPriceLessThanEqual(1.1f).size(), is(equalTo(2)));
    }

    @Test
    void findByPriceGreaterThan() {
        StepVerifier.create(this.repository.findByPriceGreaterThan(1.2f))
                .expectNextCount(2L)
                .verifyComplete();
//        assertThat(this.repository.findByPriceGreaterThan(1.2f).size(), is(equalTo(2)));
    }

    @Test
    void findByPriceGreaterThanEqual() {
        StepVerifier.create(this.repository.findByPriceGreaterThanEqual(1.2f))
                .expectNextCount(3L)
                .verifyComplete();
//        assertThat(this.repository.findByPriceGreaterThanEqual(1.2f).size(), is(equalTo(3)));
    }

    @Configuration
    @EnableAspectJAutoProxy
    @EnableAutoConfiguration
    @EnableReactiveElasticsearchRepositories(
            basePackages = "com.github.ydespreaux.spring.data.elasticsearch.reactive.repositories.query")
    static class ElasticsearchConfiguration extends ReactiveElasticsearchConfiguration {

        @Bean
        ClientLoggerAspect clientLoggerAspect() {
            return new ClientLoggerAspect();
        }
    }

}
