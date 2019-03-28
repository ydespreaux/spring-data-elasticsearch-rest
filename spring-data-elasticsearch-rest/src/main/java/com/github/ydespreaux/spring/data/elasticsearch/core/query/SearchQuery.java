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

package com.github.ydespreaux.spring.data.elasticsearch.core.query;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;

import java.util.List;

/**
 * SearchQuery
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public interface SearchQuery extends Query {

    QueryBuilder getQuery();

    QueryBuilder getFilter();

    List<SortBuilder> getElasticsearchSorts();

    List<AbstractAggregationBuilder> getAggregations();

    HighlightBuilder getHighlightBuilder();

    HighlightBuilder.Field[] getHighlightFields();

    List<IndexBoost> getIndicesBoost();

    List<ScriptField> getScriptFields();

    default boolean hasHighlight() {
        return getHighlightBuilder() != null || getHighlightFields() != null;
    }

    default HighlightBuilder buildHighlight() {
        HighlightBuilder highlightBuilder = getHighlightBuilder();
        if (highlightBuilder == null) {
            highlightBuilder = new HighlightBuilder();
        }
        if (getHighlightFields() != null) {
            for (HighlightBuilder.Field highlightField : getHighlightFields()) {
                highlightBuilder.field(highlightField);
            }
        }
        return highlightBuilder;
    }

}
