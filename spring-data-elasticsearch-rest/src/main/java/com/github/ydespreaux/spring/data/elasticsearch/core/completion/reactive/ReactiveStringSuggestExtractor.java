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
 *
 */

package com.github.ydespreaux.spring.data.elasticsearch.core.completion.reactive;

import com.github.ydespreaux.spring.data.elasticsearch.core.ResultsExtractor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.suggest.Suggest;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public class ReactiveStringSuggestExtractor implements ResultsExtractor<Flux<String>> {

    @Override
    public Flux<String> extract(SearchResponse response) {
        Suggest suggest = response.getSuggest();
        if (suggest == null) {
            return Flux.empty();
        }
        Set<SuggestItem> values = new HashSet<>();
        suggest.forEach(suggestion ->
                suggestion.getEntries().forEach(entry -> entry.forEach(option ->
                        values.add(SuggestItem.builder()
                                .item(option.getText().string())
                                .score(option.getScore())
                                .build())
                )));
        return Flux.fromIterable(values.stream()
                .sorted()
                .map(SuggestItem::getItem)
                .collect(Collectors.toList()));
    }

    @Getter
    @Setter
    @Builder
    private static class SuggestItem implements Comparable<SuggestItem> {
        private String item;
        private Float score;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SuggestItem)) return false;
            SuggestItem that = (SuggestItem) o;
            return Objects.equals(getItem(), that.getItem()) &&
                    Objects.equals(getScore(), that.getScore());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getItem(), getScore());
        }

        @Override
        public int compareTo(SuggestItem other) {
            int scoreCompare = this.score.compareTo(other.getScore());
            if (scoreCompare == 0) {
                return this.item.compareTo(other.getItem());
            }
            return scoreCompare;
        }
    }

}
