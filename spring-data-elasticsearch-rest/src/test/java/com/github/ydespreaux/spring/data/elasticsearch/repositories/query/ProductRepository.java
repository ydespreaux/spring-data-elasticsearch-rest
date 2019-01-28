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

package com.github.ydespreaux.spring.data.elasticsearch.repositories.query;

import com.github.ydespreaux.spring.data.elasticsearch.entities.Product;
import com.github.ydespreaux.spring.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 *
 */
public interface ProductRepository extends ElasticsearchRepository<Product, String> {

    Long countByAvailableTrue();

    Long countByAvailableFalse();

    List<Product> findByAvailableTrue();

    List<Product> findByAvailableFalse();

    List<Product> findByNameAndText(String name, String text);

    List<Product> findByNameAndPrice(String name, Float price);

    List<Product> findByNameOrText(String name, String text);

    List<Product> findByNameOrPrice(String name, Float price);


    List<Product> findByPriceIn(Float[] prices);

    List<Product> findByPriceIn(List<Float> prices);

    List<Product> findByPriceNotIn(List<Float> prices);

    List<Product> findByPriceNot(float price);

    List<Product> findByPriceBetween(float min, float max);

    List<Product> findByPriceLessThan(float price);

    List<Product> findByPriceLessThanEqual(float price);

    List<Product> findByPriceGreaterThan(float price);

    List<Product> findByPriceGreaterThanEqual(float price);

    List<Product> findByIdNotIn(List<String> strings);
}
