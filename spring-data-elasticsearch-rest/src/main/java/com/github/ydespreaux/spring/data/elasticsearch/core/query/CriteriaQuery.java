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

import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;

/**
 * CriteriaQuery
 *
 * @author Yoann Despréaux
 * @since 0.1.0
 */
public class CriteriaQuery extends AbstractQuery {

    private Criteria criteria;

    public CriteriaQuery(Criteria criteria) {
        this(criteria, Pageable.unpaged());
    }

    public CriteriaQuery(Criteria criteria, Pageable pageable) {

        Assert.notNull(criteria, "Criteria must not be null!");
        Assert.notNull(pageable, "Pageable must not be null!");

        this.criteria = criteria;
        this.pageable = pageable;
        this.addSort(pageable.getSort());
    }

    public final <T extends CriteriaQuery> T addCriteria(Criteria criteria) {
        Assert.notNull(criteria, "Cannot add null criteria.");
        if (this.criteria == null) {
            this.criteria = criteria;
        } else {
            this.criteria.and(criteria);
        }
        return (T) this;
    }

    public Criteria getCriteria() {
        return this.criteria;
    }
}
