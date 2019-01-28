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

import com.github.ydespreaux.spring.data.elasticsearch.configuration.ElasticsearchQueryConfiguration;
import com.github.ydespreaux.spring.data.elasticsearch.core.ElasticsearchOperations;
import com.github.ydespreaux.spring.data.elasticsearch.entities.Product;
import com.github.ydespreaux.spring.data.elasticsearch.repositories.query.ProductRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
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
        ElasticsearchQueryConfiguration.class})
@Profile("test-no-template")
public class ITProductRepositoryTest {

    private static final String PRODUCT_INDEX_NAME = "products";

    @Autowired
    private ProductRepository repository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Before
    public void setUp() {
        prepareData();
    }

    private void prepareData() {
        elasticsearchOperations.deleteIndexByName(PRODUCT_INDEX_NAME);
        elasticsearchOperations.createIndexWithSettingsAndMapping(PRODUCT_INDEX_NAME, "classpath:indices/product.index");
        repository.save(Arrays.asList(
                createProduct("1", "Sugar", "Cane sugar", false, 1.0f, 2, Arrays.asList("C1", "C2")),
                createProduct("2", "Sugar", "Cane sugar", true, 1.2f, 1, Arrays.asList("C1")),
                createProduct("3", "Sugar", "Beet sugar", true, 1.1f, 4, Arrays.asList("C2")),
                createProduct("4", "Salt", "Rock salt", true, 1.9f, 2, Arrays.asList("C3", "C4")),
                createProduct("5", "Salt", "Sea salt", false, 2.1f, 4, Arrays.asList("C1", "C3"))));
        elasticsearchOperations.refresh(Product.class);

    }

    @Test
    public void countByAvailableTrue() {
        assertThat(this.repository.countByAvailableTrue(), is(equalTo(3L)));
    }

    @Test
    public void countByAvailableFalse() {
        assertThat(this.repository.countByAvailableFalse(), is(equalTo(2L)));
    }

    @Test
    public void findByAvailableTrue() {
        assertThat(this.repository.findByAvailableTrue().size(), is(equalTo(3)));
    }

    @Test
    public void findByAvailableFalse() {
        assertThat(this.repository.findByAvailableFalse().size(), is(equalTo(2)));
    }

    @Test
    public void findByNameAndText() {
        assertThat(this.repository.findByNameAndText("Sugar", "Cane sugar").size(), is(equalTo(2)));
    }

    @Test
    public void findByNameAndPrice() {
        assertThat(this.repository.findByNameAndPrice("Sugar", 1.1f).size(), is(equalTo(1)));
    }

    @Test
    public void findByNameOrText() {
        assertThat(this.repository.findByNameOrText("Sugar", "Sea salt").size(), is(equalTo(4)));
    }

    @Test
    public void findByNameOrPrice() {
        assertThat(this.repository.findByNameOrPrice("Sugar", 2.1f).size(), is(equalTo(4)));
    }

    @Test
    public void findByPriceInWithArray() {
        assertThat(this.repository.findByPriceIn(new Float[]{1.2f, 1.1f}).size(), is(equalTo(2)));
    }

    @Test
    public void findByPriceIn() {
        assertThat(this.repository.findByPriceIn(Arrays.asList(1.2f, 1.1f)).size(), is(equalTo(2)));
    }

    @Test
    public void findByPriceNotIn() {
        assertThat(this.repository.findByPriceNotIn(Arrays.asList(1.2f, 1.1f)).size(), is(equalTo(3)));
    }

    @Test
    public void findByPriceNot() {
        assertThat(this.repository.findByPriceNot(1.2f).size(), is(equalTo(4)));
    }

    @Test
    public void findByPriceBetween() {
        assertThat(this.repository.findByPriceBetween(1.0f, 2.0f).size(), is(equalTo(4)));
    }

    @Test
    public void findByPriceLessThan() {
        assertThat(this.repository.findByPriceLessThan(1.1f).size(), is(equalTo(1)));
    }

    @Test
    public void findByPriceLessThanEqual() {
        assertThat(this.repository.findByPriceLessThanEqual(1.1f).size(), is(equalTo(2)));
    }

    @Test
    public void findByPriceGreaterThan() {
        assertThat(this.repository.findByPriceGreaterThan(1.2f).size(), is(equalTo(2)));
    }

    @Test
    public void findByPriceGreaterThanEqual() {
        assertThat(this.repository.findByPriceGreaterThanEqual(1.2f).size(), is(equalTo(3)));
    }

    @Test
    public void findByIdNotIn() {
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
