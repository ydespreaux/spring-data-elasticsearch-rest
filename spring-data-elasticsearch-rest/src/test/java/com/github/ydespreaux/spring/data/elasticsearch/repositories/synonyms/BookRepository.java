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

package com.github.ydespreaux.spring.data.elasticsearch.repositories.synonyms;

import com.github.ydespreaux.spring.data.elasticsearch.entities.Book;
import com.github.ydespreaux.spring.data.elasticsearch.repository.ElasticsearchRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * @author Yoann Despréaux
 * @since 0.0.1
 */
public interface BookRepository extends ElasticsearchRepository<Book, String> {

    List<Book> findByDescriptionContains(String value);

    List<Book> findByDescriptionNotContains(String value);

    List<Book> findByDescriptionContaining(String value);

    List<Book> findByDescriptionNotContaining(String value);

    List<Book> findByDescriptionEndsWith(String value);

    List<Book> findByDescriptionEndingWith(String value);

    List<Book> findByDescriptionStartsWith(String value);

    List<Book> findByDescriptionMatches(String title);

    List<Book> findByDescriptionLike(String title);

    List<Book> findByDescriptionNotLike(String title);

    List<Book> findByTitleIn(String[] titles);

    List<Book> findByTitleIn(Collection<String> titles);

    List<Book> findByTitleNotIn(String[] titles);

    List<Book> findByTitleNotIn(Collection<String> titles);

    List<Book> findByPriceGreaterThan(double price);

    List<Book> findByPriceLessThan(double price);

    List<Book> findByPriceBetween(double minPrice, double maxPrice);

    List<Book> findByPublication(LocalDate value);

    List<Book> findByPublicationAfter(LocalDate value);

    List<Book> findByPublicationBefore(LocalDate value);

    List<Book> findByPublicationBetween(LocalDate startDate, LocalDate endDate);

}
