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

package com.github.ydespreaux.spring.data.elasticsearch.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * @param <T> generic type
 * @author Yoann Despréaux
 * @since 1.0.0
 */
@Getter
@Setter
public class IndexTimeBasedParameter<T> {

    /**
     * Index pattern
     */
    private String indexPattern;
    /**
     * Current document
     */
    private T document;
    /**
     * Date time of the current event
     */
    private LocalDate timeEvent;

    /**
     * @param indexPattern
     * @param timeEvent
     * @param document
     */
    private IndexTimeBasedParameter(String indexPattern, @Nullable LocalDate timeEvent, @Nullable T document) {
        Objects.requireNonNull(indexPattern, "indexPattern paramater canno't be null !");
        this.setIndexPattern(indexPattern);
        this.setTimeEvent(timeEvent);
        this.setDocument(document);
    }

    /**
     * @param indexPattern the index pattern
     * @param timeEvent    the time event
     * @param document     the elasticsearch document
     * @param <T>          generic type
     * @return a new {@link IndexTimeBasedParameter}
     */
    public static <T> IndexTimeBasedParameter<T> of(String indexPattern, LocalDate timeEvent, @Nullable T document) {
        return new IndexTimeBasedParameter<>(indexPattern, timeEvent, document);
    }

    /**
     * @param indexPattern the index pattern
     * @param timeEvent    the time event
     * @param <T>          generic type
     * @return a new {@link IndexTimeBasedParameter}
     */
    public static <T> IndexTimeBasedParameter<T> of(String indexPattern, LocalDate timeEvent) {
        return new IndexTimeBasedParameter<>(indexPattern, timeEvent, null);
    }

    /**
     * @param indexPattern the index pattern
     * @param document     the elasticsearch document
     * @param <T>          generic type
     * @return a new {@link IndexTimeBasedParameter}
     */
    public static <T> IndexTimeBasedParameter<T> of(String indexPattern, T document) {
        return new IndexTimeBasedParameter<>(indexPattern, null, document);
    }

    /**
     * @return the index name
     */
    public String generateIndexWithTimeEvent() {
        Objects.requireNonNull(this.timeEvent, "timeEvent attribut canno't be null !!");
        return DateTimeFormatter.ofPattern(this.indexPattern).format(this.timeEvent);
    }

}
