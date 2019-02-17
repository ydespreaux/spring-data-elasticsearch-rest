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
package com.github.ydespreaux.spring.data.elasticsearch.repository.query;

import com.github.ydespreaux.spring.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;

/**
 * AbstractElasticsearchRepositoryQuery
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */

public abstract class AbstractElasticsearchRepositoryQuery implements RepositoryQuery {

    protected ElasticsearchQueryMethod queryMethod;
    protected ElasticsearchOperations elasticsearchOperations;

    public AbstractElasticsearchRepositoryQuery(ElasticsearchQueryMethod queryMethod,
                                                ElasticsearchOperations elasticsearchOperations) {
        this.queryMethod = queryMethod;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public QueryMethod getQueryMethod() {
        return queryMethod;
    }
}
