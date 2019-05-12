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

package com.github.ydespreaux.spring.data.elasticsearch.reactive.repositories.synonyms;

import com.github.ydespreaux.spring.data.elasticsearch.entities.Book;
import com.github.ydespreaux.spring.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

/**
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public interface ReactiveBookRepository extends ReactiveElasticsearchRepository<Book, UUID> {

    Flux<Book> findByDescriptionContains(String value);

    Flux<Book> findByDescriptionNotContains(String value);

    Flux<Book> findByDescriptionContaining(String value);

    Flux<Book> findByDescriptionNotContaining(String value);

    Flux<Book> findByDescriptionEndsWith(String value);

    Flux<Book> findByDescriptionEndingWith(String value);

    Flux<Book> findByDescriptionStartsWith(String value);

    Flux<Book> findByDescriptionMatches(String title);

    Flux<Book> findByDescriptionLike(String title);

    Flux<Book> findByDescriptionNotLike(String title);

    Flux<Book> findByTitleIn(String[] titles);

    Flux<Book> findByTitleIn(Collection<String> titles);

    Flux<Book> findByTitleNotIn(String[] titles);

    Flux<Book> findByTitleNotIn(Collection<String> titles);

    Flux<Book> findByPriceGreaterThan(double price);

    Flux<Book> findByPriceLessThan(double price);

    Flux<Book> findByPriceBetween(double minPrice, double maxPrice);

    Flux<Book> findByPublication(LocalDate value);

    Flux<Book> findByPublicationAfter(LocalDate value);

    Flux<Book> findByPublicationBefore(LocalDate value);

    Flux<Book> findByPublicationBetween(LocalDate startDate, LocalDate endDate);

}
