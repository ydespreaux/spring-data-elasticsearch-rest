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

import com.github.ydespreaux.spring.data.elasticsearch.core.query.Criteria;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.queryparser.flexible.core.util.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.elasticsearch.index.query.Operator.AND;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * CriteriaQueryProcessor generate query-related queries for a {@link Criteria} object
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
@Slf4j
public class CriteriaQueryProcessor {


    /**
     * Create a QueryBuilder from a criteria
     *
     * @param criteria
     * @return
     */
    public Optional<QueryBuilder> createQueryFromCriteria(@NonNull Criteria criteria) {
        if (isEmpty(criteria.getCriteriaChain()))
            return Optional.empty();
        ListQueryBuilder builder = new ListQueryBuilder();
        for (Criteria chainedCriteria : criteria.getCriteriaChain()) {
            builder.addCriteria(chainedCriteria);
        }
        return builder.build();
    }


    private class ListQueryBuilder {
        private List<QueryBuilder> shouldQueryBuilderList = new LinkedList<>();
        private List<QueryBuilder> mustNotQueryBuilderList = new LinkedList<>();
        private List<QueryBuilder> mustQueryBuilderList = new LinkedList<>();
        private QueryBuilder firstQuery = null;
        private boolean negateFirstQuery = false;

        public void addCriteria(Criteria chainedCriteria) {
            Optional<QueryBuilder> queryFragmentForCriteria = createQueryFragmentForCriteria(chainedCriteria);
            if (queryFragmentForCriteria.isPresent()) {
                if (firstQuery == null) {
                    firstQuery = queryFragmentForCriteria.get();
                    negateFirstQuery = chainedCriteria.isNegating();
                } else if (chainedCriteria.isOr()) {
                    shouldQueryBuilderList.add(queryFragmentForCriteria.get());
                } else if (chainedCriteria.isNegating()) {
                    mustNotQueryBuilderList.add(queryFragmentForCriteria.get());
                } else {
                    mustQueryBuilderList.add(queryFragmentForCriteria.get());
                }
            }
        }

        public Optional<QueryBuilder> build() {
            if (firstQuery != null) {
                if (!shouldQueryBuilderList.isEmpty() && mustNotQueryBuilderList.isEmpty() && mustQueryBuilderList.isEmpty()) {
                    shouldQueryBuilderList.add(0, firstQuery);
                } else if (negateFirstQuery) {
                    mustNotQueryBuilderList.add(0, firstQuery);
                } else {
                    mustQueryBuilderList.add(0, firstQuery);
                }
            }
            BoolQueryBuilder query = boolQuery();
            shouldQueryBuilderList.forEach(query::should);
            mustNotQueryBuilderList.forEach(query::mustNot);
            mustQueryBuilderList.forEach(query::must);
            return query.hasClauses() ? Optional.of(query) : Optional.empty();
        }

        /**
         * @param chainedCriteria
         * @return
         */
        private Optional<QueryBuilder> createQueryFragmentForCriteria(Criteria chainedCriteria) {
            if (chainedCriteria.getQueryCriteriaEntries().isEmpty())
                return Optional.empty();

            String fieldName = chainedCriteria.getField().getName();
            Assert.notNull(fieldName, "Unknown field");

            List<Criteria.CriteriaEntry> entries = new ArrayList<>(chainedCriteria.getQueryCriteriaEntries());
            if (entries.isEmpty()) {
                return Optional.empty();
            }

            QueryBuilder query = null;
            if (entries.size() == 1) {
                query = processCriteriaEntry(entries.get(0), fieldName);
            } else {
                query = boolQuery();
                for (Criteria.CriteriaEntry entry : entries) {
                    QueryBuilder queryBuilder = processCriteriaEntry(entry, fieldName);
                    if (queryBuilder != null) {
                        ((BoolQueryBuilder) query).must(queryBuilder);
                    }
                }
                if (!((BoolQueryBuilder) query).hasClauses()) {
                    return Optional.empty();
                }
            }
            if (query != null) {
                addBoost(query, chainedCriteria.getBoost());
            }
            return Optional.ofNullable(query);
        }

        /**
         * @param entry
         * @param fieldName
         * @return
         */
        @Nullable
        private QueryBuilder processCriteriaEntry(Criteria.CriteriaEntry entry, String fieldName) {
            Object value = entry.getValue();
            Criteria.OperationKey key = entry.getKey();
            String searchText = StringUtils.toString(value);
            QueryBuilder query = null;
            switch (key) {
                case EQUALS:
                    query = queryStringQuery(searchText).field(fieldName).defaultOperator(AND);
                    break;
                case CONTAINS:
                    query = matchPhraseQuery(fieldName, searchText);
                    break;
                case STARTS_WITH:
                    query = queryStringQuery(searchText + Criteria.WILDCARD).field(fieldName).analyzeWildcard(true);
                    break;
                case ENDS_WITH:
                    query = queryStringQuery(Criteria.WILDCARD + searchText).field(fieldName).analyzeWildcard(true);
                    break;
                case EXPRESSION:
                    query = queryStringQuery(searchText).field(fieldName);
                    break;
                case LESS_EQUAL:
                    query = rangeQuery(fieldName).lte(value);
                    break;
                case GREATER_EQUAL:
                    query = rangeQuery(fieldName).gte(value);
                    break;
                case BETWEEN:
                    Object[] ranges = (Object[]) value;
                    query = rangeQuery(fieldName).from(ranges[0]).to(ranges[1]);
                    break;
                case LESS:
                    query = rangeQuery(fieldName).lt(value);
                    break;
                case GREATER:
                    query = rangeQuery(fieldName).gt(value);
                    break;
                case FUZZY:
                    query = fuzzyQuery(fieldName, searchText);
                    break;
                case IN:
                    query = boolQuery();
                    for (Object item : (Iterable<Object>) value) {
                        ((BoolQueryBuilder) query).should(queryStringQuery(item.toString()).field(fieldName));
                    }
                    break;
                case NOT_IN:
                    query = boolQuery();
                    for (Object item : (Iterable<Object>) value) {
                        ((BoolQueryBuilder) query).mustNot(queryStringQuery(item.toString()).field(fieldName));
                    }
                    break;
                default:
                    if (log.isWarnEnabled()) {
                        log.warn("Key operator {} is not a query operator", key);
                    }

            }
            return query;
        }

        /**
         * @param query
         * @param boost
         */
        private void addBoost(QueryBuilder query, float boost) {
            if (query == null || Float.isNaN(boost)) {
                return;
            }
            query.boost(boost);
        }
    }
}
