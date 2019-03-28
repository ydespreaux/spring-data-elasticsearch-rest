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

import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * NativeSearchQuery
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public class NativeSearchQuery extends AbstractQuery implements SearchQuery {

    private QueryBuilder query;
    private QueryBuilder filter;
    private List<SortBuilder> sorts;
    private List<AbstractAggregationBuilder> aggregations;
    private HighlightBuilder highlightBuilder;
    private HighlightBuilder.Field[] highlightFields;
    private List<IndexBoost> indicesBoost;
    private List<ScriptField> scriptFields = new ArrayList<>();


    public NativeSearchQuery(QueryBuilder query) {
        this.query = query;
    }

    public NativeSearchQuery(QueryBuilder query, QueryBuilder filter) {
        this.query = query;
        this.filter = filter;
    }

    public NativeSearchQuery(QueryBuilder query, QueryBuilder filter, List<SortBuilder> sorts) {
        this.query = query;
        this.filter = filter;
        this.sorts = sorts;
    }

    public NativeSearchQuery(QueryBuilder query, QueryBuilder filter, List<SortBuilder> sorts, HighlightBuilder.Field[] highlightFields) {
        this.query = query;
        this.filter = filter;
        this.sorts = sorts;
        this.highlightFields = highlightFields;
    }

    public NativeSearchQuery(QueryBuilder query, QueryBuilder filter, List<SortBuilder> sorts,
                             HighlightBuilder highlighBuilder, HighlightBuilder.Field[] highlightFields) {
        this.query = query;
        this.filter = filter;
        this.sorts = sorts;
        this.highlightBuilder = highlighBuilder;
        this.highlightFields = highlightFields;
    }

    public QueryBuilder getQuery() {
        return query;
    }

    public QueryBuilder getFilter() {
        return filter;
    }

    public List<SortBuilder> getElasticsearchSorts() {
        return sorts;
    }

    @Override
    public HighlightBuilder getHighlightBuilder() {
        return highlightBuilder;
    }

    @Override
    public HighlightBuilder.Field[] getHighlightFields() {
        return highlightFields;
    }

    @Override
    public List<AbstractAggregationBuilder> getAggregations() {
        return aggregations;
    }

    public void setAggregations(List<AbstractAggregationBuilder> aggregations) {
        this.aggregations = aggregations;
    }

    public void addAggregation(AbstractAggregationBuilder aggregationBuilder) {
        if (aggregations == null) {
            aggregations = new ArrayList<>();
        }
        aggregations.add(aggregationBuilder);
    }

    @Override
    public List<IndexBoost> getIndicesBoost() {
        return indicesBoost;
    }

    public void setIndicesBoost(List<IndexBoost> indicesBoost) {
        this.indicesBoost = indicesBoost;
    }

    @Override
    public List<ScriptField> getScriptFields() {
        return scriptFields;
    }

    public void setScriptFields(List<ScriptField> scriptFields) {
        this.scriptFields.addAll(scriptFields);
    }

    public void addScriptField(ScriptField... scriptField) {
        scriptFields.addAll(Arrays.asList(scriptField));
    }

    /**
     * NativeSearchQuery Builder
     */
    public static class NativeSearchQueryBuilder {

        private QueryBuilder queryBuilder;
        private QueryBuilder filterBuilder;
        private List<SortBuilder> sortBuilders = new ArrayList<>();
        private List<AbstractAggregationBuilder> aggregationBuilders = new ArrayList<>();
        private HighlightBuilder highlightBuilder;
        private HighlightBuilder.Field[] highlightFields;
        private Pageable pageable = Pageable.unpaged();
        private String[] indices;
        private String[] types;
        private String[] fields;
        private SourceFilter sourceFilter;
        private List<IndexBoost> indicesBoost;
        private float minScore;
        private boolean trackScores;
        private Collection<String> ids;
        private String route;
        private SearchType searchType;
        private IndicesOptions indicesOptions;
        private List<ScriptField> scriptFields = new ArrayList<>();

        public NativeSearchQueryBuilder withQuery(QueryBuilder queryBuilder) {
            this.queryBuilder = queryBuilder;
            return this;
        }

        public NativeSearchQueryBuilder withFilter(QueryBuilder filterBuilder) {
            this.filterBuilder = filterBuilder;
            return this;
        }

        public NativeSearchQueryBuilder withSort(SortBuilder sortBuilder) {
            this.sortBuilders.add(sortBuilder);
            return this;
        }

        public NativeSearchQueryBuilder addAggregation(AbstractAggregationBuilder aggregationBuilder) {
            this.aggregationBuilders.add(aggregationBuilder);
            return this;
        }

        public NativeSearchQueryBuilder withHighlightBuilder(HighlightBuilder highlightBuilder) {
            this.highlightBuilder = highlightBuilder;
            return this;
        }

        public NativeSearchQueryBuilder withHighlightFields(HighlightBuilder.Field... highlightFields) {
            this.highlightFields = highlightFields;
            return this;
        }

        public NativeSearchQueryBuilder withIndicesBoost(List<IndexBoost> indicesBoost) {
            this.indicesBoost = indicesBoost;
            return this;
        }

        public NativeSearchQueryBuilder withPageable(Pageable pageable) {
            this.pageable = pageable;
            return this;
        }

        public NativeSearchQueryBuilder withIndices(String... indices) {
            this.indices = indices;
            return this;
        }

        public NativeSearchQueryBuilder withTypes(String... types) {
            this.types = types;
            return this;
        }

        public NativeSearchQueryBuilder withFields(String... fields) {
            this.fields = fields;
            return this;
        }

        public NativeSearchQueryBuilder withSourceFilter(SourceFilter sourceFilter) {
            this.sourceFilter = sourceFilter;
            return this;
        }

        public NativeSearchQueryBuilder withMinScore(float minScore) {
            this.minScore = minScore;
            return this;
        }

        /**
         * @param trackScores whether to track scores.
         * @return the current instance
         */
        public NativeSearchQueryBuilder withTrackScores(boolean trackScores) {
            this.trackScores = trackScores;
            return this;
        }

        public NativeSearchQueryBuilder withIds(Collection<String> ids) {
            this.ids = ids;
            return this;
        }

        public NativeSearchQueryBuilder withRoute(String route) {
            this.route = route;
            return this;
        }

        public NativeSearchQueryBuilder withSearchType(SearchType searchType) {
            this.searchType = searchType;
            return this;
        }

        public NativeSearchQueryBuilder withIndicesOptions(IndicesOptions indicesOptions) {
            this.indicesOptions = indicesOptions;
            return this;
        }

        public NativeSearchQueryBuilder withScriptField(ScriptField scriptField) {
            this.scriptFields.add(scriptField);
            return this;
        }

        public NativeSearchQuery build() {
            NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryBuilder, filterBuilder, sortBuilders,
                    highlightBuilder, highlightFields);

            nativeSearchQuery.setPageable(pageable);
            nativeSearchQuery.setTrackScores(trackScores);

            if (indices != null) {
                nativeSearchQuery.addIndices(indices);
            }

            if (types != null) {
                nativeSearchQuery.addTypes(types);
            }

            if (fields != null) {
                nativeSearchQuery.addFields(fields);
            }

            if (sourceFilter != null) {
                nativeSearchQuery.addSourceFilter(sourceFilter);
            }

            if (indicesBoost != null) {
                nativeSearchQuery.setIndicesBoost(indicesBoost);
            }

            if (!isEmpty(aggregationBuilders)) {
                nativeSearchQuery.setAggregations(aggregationBuilders);
            }

            if (minScore > 0) {
                nativeSearchQuery.setMinScore(minScore);
            }

            if (ids != null) {
                nativeSearchQuery.setIds(ids);
            }

            if (route != null) {
                nativeSearchQuery.setRoute(route);
            }

            if (searchType != null) {
                nativeSearchQuery.setSearchType(searchType);
            }

            if (indicesOptions != null) {
                nativeSearchQuery.setIndicesOptions(indicesOptions);
            }
            if (!isEmpty(scriptFields)) {
                nativeSearchQuery.setScriptFields(scriptFields);
            }
            return nativeSearchQuery;
        }
    }
}
