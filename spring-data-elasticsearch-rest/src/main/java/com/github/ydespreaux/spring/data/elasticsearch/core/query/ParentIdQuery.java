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
import org.springframework.util.Assert;

/**
 * HasChildQuery
 *
 * @author Yoann Despréaux
 * @since 1.0.2
 */
@Getter
@Setter
public class ParentIdQuery extends JoinQuery<ParentIdQuery> {

    private String parentId;

    ParentIdQuery() {
    }

    public static ParentIdQueryBuilder builder() {
        return new ParentIdQueryBuilder();
    }

    /**
     * @param parentId
     * @return
     */
    public ParentIdQuery parentId(String parentId) {
        this.parentId = parentId;
        return self();
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(parentId, "[parent_id] requires 'parentId' field");
        Assert.notNull(getType(), "[parent_id] requires 'type' field");
    }

    /**
     *
     */
    public static class ParentIdQueryBuilder extends JoinQueryBuilder<ParentIdQueryBuilder, ParentIdQuery> {

        private String parentId;

        /**
         * @param parentId
         * @return
         */
        public ParentIdQueryBuilder parentId(String parentId) {
            this.parentId = parentId;
            return this;
        }

        @Override
        public ParentIdQuery build() {
            return new ParentIdQuery()
                    .type(this.type)
                    .query(this.query)
                    .ignoreUnmapped(this.ignoreUnmapped)
                    .parentId(this.parentId);
        }
    }
}
