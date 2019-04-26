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

package com.github.ydespreaux.spring.data.elasticsearch.core.completion;

import com.github.ydespreaux.spring.data.elasticsearch.core.ResultsExtractor;
import com.github.ydespreaux.spring.data.elasticsearch.core.ResultsMapper;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public class EntitySuggestExtractor<T> implements ResultsExtractor<List<T>> {

    private final Class<T> entityType;
    private final ResultsMapper mapper;

    public EntitySuggestExtractor(Class<T> entityType, ResultsMapper mapper) {
        this.entityType = entityType;
        this.mapper = mapper;
    }

    @Override
    public List<T> extract(SearchResponse response) {
        Suggest suggest = response.getSuggest();
        if (suggest == null) {
            return Collections.emptyList();
        }
        List<T> values = new ArrayList<>();
        suggest.forEach(suggestion ->
                suggestion.getEntries().forEach(entry -> values.addAll(
                        entry.getOptions().stream()
                                .filter(option -> option instanceof CompletionSuggestion.Entry.Option)
                                .map(CompletionSuggestion.Entry.Option.class::cast)
                                .map(option -> (T) mapper.mapEntity(option.getHit(), entityType))
                                .collect(Collectors.toList()))
                ));
        return values;
    }

}
