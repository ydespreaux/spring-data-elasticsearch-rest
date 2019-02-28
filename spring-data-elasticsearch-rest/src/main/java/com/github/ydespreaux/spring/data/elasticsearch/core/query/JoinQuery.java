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

import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.beans.factory.InitializingBean;

/**
 * JoinQuery
 *
 * @author Yoann Despréaux
 * @since 1.0.2
 */
@Getter
@Setter
public abstract class JoinQuery<T extends JoinQuery> extends AbstractQuery implements InitializingBean {

    private QueryBuilder query;
    private String type;
    private boolean ignoreUnmapped;

    public T type(String type) {
        this.type = type;
        return self();
    }

    public T ignoreUnmapped(boolean ignoreUnmapped) {
        this.ignoreUnmapped = ignoreUnmapped;
        return self();
    }

    public T query(QueryBuilder query) {
        this.query = query;
        return self();
    }

    protected T self() {
        return (T) this;
    }


    /**
     * @param <B>
     */
    public abstract static class JoinQueryBuilder<B extends JoinQueryBuilder, T> {

        protected QueryBuilder query;
        protected String type;
        protected boolean ignoreUnmapped;

        public B type(String type) {
            this.type = type;
            return (B) this;
        }

        public B ignoreUnmapped(boolean ignoreUnmapped) {
            this.ignoreUnmapped = ignoreUnmapped;
            return (B) this;
        }

        public B query(QueryBuilder query) {
            this.query = query;
            return (B) this;
        }

        public abstract T build();
    }
}
