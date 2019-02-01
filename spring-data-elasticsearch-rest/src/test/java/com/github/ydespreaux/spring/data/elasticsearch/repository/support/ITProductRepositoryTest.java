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

import com.github.ydespreaux.spring.data.elasticsearch.AbstractElasticsearchTest;
import com.github.ydespreaux.spring.data.elasticsearch.client.ClientLoggerAspect;
import com.github.ydespreaux.spring.data.elasticsearch.configuration.ElasticsearchConfigurationSupport;
import com.github.ydespreaux.spring.data.elasticsearch.entities.Product;
import com.github.ydespreaux.spring.data.elasticsearch.repositories.query.ProductRepository;
import com.github.ydespreaux.spring.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import com.github.ydespreaux.testcontainers.elasticsearch.ElasticsearchContainer;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        RestClientAutoConfiguration.class,
        ITProductRepositoryTest.ElasticsearchConfiguration.class})
@Profile("test-no-template")
public class ITProductRepositoryTest extends AbstractElasticsearchTest<Product> {

    @ClassRule
    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer("6.4.2");

    public ITProductRepositoryTest() {
        super(Product.class);
    }

    private static final String PRODUCT_INDEX_NAME = "products";

    @Autowired
    private ProductRepository repository;

    @Override
    protected List<Product> generateData() {
        return Arrays.asList(
                createProduct("1", "Sugar", "Cane sugar", false, 1.0f, 2, Arrays.asList("C1", "C2")),
                createProduct("2", "Sugar", "Cane sugar", true, 1.2f, 1, Arrays.asList("C1")),
                createProduct("3", "Sugar", "Beet sugar", true, 1.1f, 4, Arrays.asList("C2")),
                createProduct("4", "Salt", "Rock salt", true, 1.9f, 2, Arrays.asList("C3", "C4")),
                createProduct("5", "Salt", "Sea salt", false, 2.1f, 4, Arrays.asList("C1", "C3"))
        );
    }

    @Before
    public void setUp() {
        cleanData();
    }

    @Configuration
    @EnableAspectJAutoProxy
    @EnableAutoConfiguration
    @EnableElasticsearchRepositories(
            basePackages = "com.github.ydespreaux.spring.data.elasticsearch.repositories.query")
    static class ElasticsearchConfiguration extends ElasticsearchConfigurationSupport {

        @Bean
        ClientLoggerAspect clientLoggerAspect() {
            return new ClientLoggerAspect();
        }
    }

    @Test
    public void countByAvailableTrue() {
        insertData();
        assertThat(this.repository.countByAvailableTrue(), is(equalTo(3L)));
    }

    @Test
    public void countByAvailableFalse() {
        insertData();
        assertThat(this.repository.countByAvailableFalse(), is(equalTo(2L)));
    }

    @Test
    public void findByAvailableTrue() {
        insertData();
        assertThat(this.repository.findByAvailableTrue().size(), is(equalTo(3)));
    }

    @Test
    public void findByAvailableFalse() {
        insertData();
        assertThat(this.repository.findByAvailableFalse().size(), is(equalTo(2)));
    }

    @Test
    public void findByNameAndText() {
        insertData();
        assertThat(this.repository.findByNameAndText("Sugar", "Cane sugar").size(), is(equalTo(2)));
    }

    @Test
    public void findByNameAndPrice() {
        insertData();
        assertThat(this.repository.findByNameAndPrice("Sugar", 1.1f).size(), is(equalTo(1)));
    }

    @Test
    public void findByNameOrText() {
        insertData();
        assertThat(this.repository.findByNameOrText("Sugar", "Sea salt").size(), is(equalTo(4)));
    }

    @Test
    public void findByNameOrPrice() {
        insertData();
        assertThat(this.repository.findByNameOrPrice("Sugar", 2.1f).size(), is(equalTo(4)));
    }

    @Test
    public void findByPriceInWithArray() {
        insertData();
        assertThat(this.repository.findByPriceIn(new Float[]{1.2f, 1.1f}).size(), is(equalTo(2)));
    }

    @Test
    public void findByPriceIn() {
        insertData();
        assertThat(this.repository.findByPriceIn(Arrays.asList(1.2f, 1.1f)).size(), is(equalTo(2)));
    }

    @Test
    public void findByPriceNotIn() {
        insertData();
        assertThat(this.repository.findByPriceNotIn(Arrays.asList(1.2f, 1.1f)).size(), is(equalTo(3)));
    }

    @Test
    public void findByPriceNot() {
        insertData();
        assertThat(this.repository.findByPriceNot(1.2f).size(), is(equalTo(4)));
    }

    @Test
    public void findByPriceBetween() {
        insertData();
        assertThat(this.repository.findByPriceBetween(1.0f, 2.0f).size(), is(equalTo(4)));
    }

    @Test
    public void findByPriceLessThan() {
        insertData();
        assertThat(this.repository.findByPriceLessThan(1.1f).size(), is(equalTo(1)));
    }

    @Test
    public void findByPriceLessThanEqual() {
        insertData();
        assertThat(this.repository.findByPriceLessThanEqual(1.1f).size(), is(equalTo(2)));
    }

    @Test
    public void findByPriceGreaterThan() {
        insertData();
        assertThat(this.repository.findByPriceGreaterThan(1.2f).size(), is(equalTo(2)));
    }

    @Test
    public void findByPriceGreaterThanEqual() {
        insertData();
        assertThat(this.repository.findByPriceGreaterThanEqual(1.2f).size(), is(equalTo(3)));
    }

    @Test
    public void findByIdNotIn() {
        insertData();
        assertThat(this.repository.findByIdNotIn(Arrays.asList("1", "2", "3")).size(), is(equalTo(2)));
    }


    private Product createProduct(String id, String name, String text, Boolean available, Float price, Integer popularity, List<String> categories) {
        return Product.builder()
                .id(id)
                .name(name)
                .text(text)
                .available(available)
                .price(price)
                .popularity(popularity)
                .categories(categories)
                .build();
    }

}
