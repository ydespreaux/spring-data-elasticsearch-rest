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
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.InnerHitBuilder;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * HasChildQuery
 *
 * @author Yoann Despréaux
 * @since 1.0.2
 */
@Getter
@Setter
public class HasChildQuery extends JoinQuery<HasChildQuery> {

    private ScoreMode scoreMode;
    @Nullable
    private InnerHitBuilder innerHitBuilder;
    private int minChildren = 0;
    private int maxChildren = Integer.MAX_VALUE;

    HasChildQuery() {
    }

    /**
     * @return
     */
    public static HasChildQueryBuilder builder() {
        return new HasChildQueryBuilder();
    }

    /**
     * @param scoreMode
     * @return
     */
    public HasChildQuery scoreMode(ScoreMode scoreMode) {
        this.scoreMode = scoreMode;
        return self();
    }

    /**
     * @param minChildren
     * @return
     */
    public HasChildQuery minChildren(int minChildren) {
        this.minChildren = minChildren;
        return self();
    }

    /**
     * @param maxChildren
     * @return
     */
    public HasChildQuery maxChildren(int maxChildren) {
        this.maxChildren = maxChildren;
        return self();
    }

    /**
     * @param innerHitBuilder
     * @return
     */
    public HasChildQuery innerHitBuilder(InnerHitBuilder innerHitBuilder) {
        this.innerHitBuilder = innerHitBuilder;
        return self();
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(this.getType(), "[has_child] requires 'type' field");
        Assert.notNull(this.getQuery(), "[has_child] requires 'query' field");
        Assert.notNull(this.getScoreMode(), "[has_child] requires 'score_mode' field");
    }

    /**
     *
     */
    public static class HasChildQueryBuilder extends JoinQueryBuilder<HasChildQueryBuilder, HasChildQuery> {

        private ScoreMode scoreMode;
        private InnerHitBuilder innerHitBuilder;
        private int minChildren = 0;
        private int maxChildren = Integer.MAX_VALUE;

        /**
         * @param scoreMode
         * @return
         */
        public HasChildQueryBuilder scoreMode(ScoreMode scoreMode) {
            this.scoreMode = scoreMode;
            return this;
        }

        /**
         * @param minChildren
         * @return
         */
        public HasChildQueryBuilder minChildren(int minChildren) {
            this.minChildren = minChildren;
            return this;
        }

        /**
         * @param maxChildren
         * @return
         */
        public HasChildQueryBuilder maxChildren(int maxChildren) {
            this.maxChildren = maxChildren;
            return this;
        }

        /**
         * @param innerHitBuilder
         * @return
         */
        public HasChildQueryBuilder innerHitBuilder(InnerHitBuilder innerHitBuilder) {
            this.innerHitBuilder = innerHitBuilder;
            return this;
        }

        @Override
        public HasChildQuery build() {
            HasChildQuery query = new HasChildQuery()
                    .type(this.type)
                    .query(this.query)
                    .ignoreUnmapped(this.ignoreUnmapped)
                    .scoreMode(this.scoreMode)
                    .minChildren(this.minChildren)
                    .maxChildren(this.maxChildren)
                    .innerHitBuilder(this.innerHitBuilder);
            query.afterPropertiesSet();
            return query;
        }
    }
}
