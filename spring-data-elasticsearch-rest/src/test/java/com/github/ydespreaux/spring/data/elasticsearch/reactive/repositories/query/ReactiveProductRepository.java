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

package com.github.ydespreaux.spring.data.elasticsearch.reactive.repositories.query;

import com.github.ydespreaux.spring.data.elasticsearch.entities.Product;
import com.github.ydespreaux.spring.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 *
 */
public interface ReactiveProductRepository extends ReactiveElasticsearchRepository<Product, String> {

    Mono<Long> countByAvailableTrue();

    Mono<Long> countByAvailableFalse();

    Flux<Product> findByAvailableTrue();

    Flux<Product> findByAvailableFalse();

    Flux<Product> findByNameAndText(String name, String text);

    Flux<Product> findByNameAndPrice(String name, Float price);

    Flux<Product> findByNameOrText(String name, String text);

    Flux<Product> findByNameOrPrice(String name, Float price);


    Flux<Product> findByPriceIn(Float[] prices);

    Flux<Product> findByPriceIn(List<Float> prices);

    Flux<Product> findByPriceNotIn(List<Float> prices);

    Flux<Product> findByPriceNot(float price);

    Flux<Product> findByPriceBetween(float min, float max);

    Flux<Product> findByPriceLessThan(float price);

    Flux<Product> findByPriceLessThanEqual(float price);

    Flux<Product> findByPriceGreaterThan(float price);

    Flux<Product> findByPriceGreaterThanEqual(float price);

    Flux<Product> findByIdNotIn(List<String> strings);
}
