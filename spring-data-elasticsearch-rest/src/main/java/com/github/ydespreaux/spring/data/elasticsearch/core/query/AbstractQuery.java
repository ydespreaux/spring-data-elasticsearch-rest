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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.addAll;

/**
 * AbstractQuery
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
abstract class AbstractQuery implements Query {

    private static final Pageable DEFAULT_PAGE = PageRequest.of(0, 50);

    protected Pageable pageable = DEFAULT_PAGE;
    protected Sort sort;
    protected List<String> indices = new ArrayList<>();
    protected List<String> types = new ArrayList<>();
    protected List<String> fields = new ArrayList<>();
    protected SourceFilter sourceFilter;
    protected float minScore;
    protected Collection<String> ids;
    protected String route;
    protected SearchType searchType = SearchType.DFS_QUERY_THEN_FETCH;
    protected IndicesOptions indicesOptions;
    protected boolean trackScores;

    @Nullable
    @Override
    public Sort getSort() {
        return this.sort;
    }

    @Override
    public Pageable getPageable() {
        return this.pageable;
    }

    @Override
    public final <T extends Query> T setPageable(Pageable pageable) {

        Assert.notNull(pageable, "Pageable must not be null!");

        this.pageable = pageable;
        return this.addSort(pageable.getSort());
    }

    @Override
    public void addFields(String... fields) {
        addAll(this.fields, fields);
    }

    @Override
    public List<String> getFields() {
        return fields;
    }

    @Override
    public List<String> getIndices() {
        return indices;
    }

    @Override
    public void addIndices(String... indices) {
        addAll(this.indices, indices);
    }

    @Override
    public void addTypes(String... types) {
        addAll(this.types, types);
    }

    @Override
    public List<String> getTypes() {
        return types;
    }

    @Override
    public void addSourceFilter(SourceFilter sourceFilter) {
        this.sourceFilter = sourceFilter;
    }

    /*

     */
    protected static <T> T requireValue(@Nullable T value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        } else {
            return value;
        }
    }

    @SuppressWarnings("unchecked")
    public final <T extends Query> T addSort(Sort sort) {
        if (this.sort == null) {
            this.sort = sort;
        } else {
            this.sort = this.sort.and(sort);
        }

        return (T) this;
    }

    public float getMinScore() {
        return minScore;
    }

    public void setMinScore(float minScore) {
        this.minScore = minScore;
    }

    public Collection<String> getIds() {
        return ids;
    }

    public void setIds(Collection<String> ids) {
        this.ids = ids;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public SearchType getSearchType() {
        return searchType;
    }

    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
    }

    public IndicesOptions getIndicesOptions() {
        return indicesOptions;
    }

    public void setIndicesOptions(IndicesOptions indicesOptions) {
        this.indicesOptions = indicesOptions;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.elasticsearch.core.query.Query#getTrackScores()
     */
    @Override
    public boolean getTrackScores() {
        return trackScores;
    }

    /**
     * Configures whether to track scores.
     *
     * @param trackScores the track scores
     */
    public void setTrackScores(boolean trackScores) {
        this.trackScores = trackScores;
    }

    @Nullable
    @Override
    public SourceFilter getSourceFilter() {
        return sourceFilter;
    }
}
