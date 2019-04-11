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

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.rest.RestStatus;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.lang.Nullable;

import java.net.ConnectException;

/**
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public class ElasticsearchExceptionTranslator {

    @Nullable
    public Throwable translateExceptionIfPossible(RuntimeException ex) {
        if (ex.getCause() instanceof ConnectException) {
            return new DataAccessResourceFailureException(ex.getMessage(), ex);
        }
        if (ex instanceof ElasticsearchStatusException) {
            RestStatus status = ((ElasticsearchStatusException) ex).status();
            if (status == RestStatus.NOT_FOUND) {
                return new IndexNotFoundException(ex.getMessage(), ex);
            }
            return ex.getCause();
        }
        return null;
    }
}
