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

import java.util.Collection;
import java.util.List;

/**
 * Query
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public interface Query {

    int DEFAULT_PAGE_SIZE = 10;
    Pageable DEFAULT_PAGE = PageRequest.of(0, DEFAULT_PAGE_SIZE);

    /**
     * Get page settings if defined
     *
     * @return the pageable
     */
    Pageable getPageable();

    /**
     * restrict result to entries on given page. Corresponds to the 'start' and 'rows' parameter in elasticsearch
     *
     * @param pageable the pageable
     * @param <T>      generic type
     * @return the current object
     */
    <T extends Query> T setPageable(Pageable pageable);

    /**
     * Add {@link org.springframework.data.domain.Sort} to query
     *
     * @param sort the given sort
     * @param <T>  generic type
     * @return the current object
     */
    <T extends Query> T addSort(Sort sort);

    /**
     * @return null if not set
     */
    Sort getSort();

    /**
     * Get Indices to be searched
     *
     * @return indices
     */
    List<String> getIndices();

    /**
     * Add Indices to be added as part of continueScroll request
     *
     * @param indices indiced to add
     */
    void addIndices(String... indices);

    /**
     * Add types to be searched
     *
     * @param types types to add
     */
    void addTypes(String... types);

    /**
     * Get types to be searched
     *
     * @return types to be searched
     */
    List<String> getTypes();

    /**
     * Add fields to be added as part of continueScroll request
     *
     * @param fields fields
     */
    void addFields(String... fields);

    /**
     * Get fields to be returned as part of continueScroll request
     *
     * @return fields ti be returned as part of continueScroll request
     */
    List<String> getFields();

    /**
     * Add source filter to be added as part of continueScroll request
     *
     * @param sourceFilter the source filter
     */
    void addSourceFilter(SourceFilter sourceFilter);

    /**
     * Get SourceFilter to be returned to get include and exclude source fields as part of continueScroll request.
     *
     * @return SourceFilter
     */
    SourceFilter getSourceFilter();

    /**
     * Get minimum score
     *
     * @return min score
     */
    float getMinScore();

    /**
     * Get if scores will be computed and tracked, regardless of whether sorting on a field. Defaults to <tt>false</tt>.
     *
     * @return Get if scores will be computed and tracked, regardless of whether sorting on a field. Defaults to <tt>false</tt>.
     */
    boolean getTrackScores();

    /**
     * Get Ids
     *
     * @return ids
     */
    Collection<String> getIds();

    /**
     * Get route
     *
     * @return route
     */
    String getRoute();

    /**
     * Type of continueScroll
     *
     * @return continueScroll type
     */
    SearchType getSearchType();

    /**
     * Get indices options
     *
     * @return null if not set
     */
    IndicesOptions getIndicesOptions();
}
