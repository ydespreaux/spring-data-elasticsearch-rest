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

import com.github.ydespreaux.spring.data.elasticsearch.core.scroll.ScrolledPage;
import org.elasticsearch.action.search.SearchResponse;

/**
 * @author Yoann Despréaux
 * @since 0.0.1
 */
public interface SearchResultMapper {

    /**
     * @param response the response
     * @param clazz    the entity class
     * @param <T>      the generic type
     * @return a new {@link ScrolledPage}
     */
    <T> ScrolledPage<T> mapResults(SearchResponse response, Class<T> clazz);
}
