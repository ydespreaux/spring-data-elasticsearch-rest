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
import org.elasticsearch.index.query.InnerHitBuilder;
import org.springframework.util.Assert;

/**
 * HasChildQuery
 *
 * @author Yoann Despréaux
 * @since 1.0.2
 */
@Getter
@Setter
public class HasParentQuery extends JoinQuery<HasParentQuery> {

    private boolean score;
    private InnerHitBuilder innerHitBuilder;

    HasParentQuery() {
    }

    /**
     * @return
     */
    public static HasParentQueryBuilder builder() {
        return new HasParentQueryBuilder();
    }

    /**
     * @param score
     * @return
     */
    public HasParentQuery score(boolean score) {
        this.score = score;
        return self();
    }

    /**
     * @param innerHitBuilder
     * @return
     */
    public HasParentQuery innerHitBuilder(InnerHitBuilder innerHitBuilder) {
        this.innerHitBuilder = innerHitBuilder;
        return self();
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(getQuery(), "[has_parent] requires 'query' field");
        Assert.notNull(getType(), "[has_parent] requires 'type' field");
    }

    /**
     *
     */
    public static class HasParentQueryBuilder extends JoinQueryBuilder<HasParentQueryBuilder, HasParentQuery> {

        private boolean score;
        private InnerHitBuilder innerHitBuilder;

        /**
         * @param score
         * @return
         */
        public HasParentQueryBuilder score(boolean score) {
            this.score = score;
            return this;
        }

        /**
         * @param innerHitBuilder
         * @return
         */
        public HasParentQueryBuilder innerHitBuilder(InnerHitBuilder innerHitBuilder) {
            this.innerHitBuilder = innerHitBuilder;
            return this;
        }

        @Override
        public HasParentQuery build() {
            HasParentQuery query = new HasParentQuery()
                    .type(this.type)
                    .query(this.query)
                    .ignoreUnmapped(this.ignoreUnmapped)
                    .score(this.score)
                    .innerHitBuilder(this.innerHitBuilder);
            query.afterPropertiesSet();
            return query;
        }
    }
}
