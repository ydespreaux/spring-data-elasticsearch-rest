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
package com.github.ydespreaux.spring.data.elasticsearch.repository.query.parser;

import com.github.ydespreaux.spring.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.Criteria;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.CriteriaQuery;
import org.elasticsearch.common.geo.GeoPoint;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Box;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mapping.PersistentPropertyPath;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;

import java.util.Collection;
import java.util.Iterator;

/**
 * ElasticsearchQueryCreator
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public class ElasticsearchQueryCreator extends AbstractQueryCreator<CriteriaQuery, CriteriaQuery> {

    private static final String ILLEGAL_CRITERIA_ERROR = "Illegal criteria found '%s'.";
    private final MappingContext<?, ElasticsearchPersistentProperty> context;

    public ElasticsearchQueryCreator(PartTree tree, ParameterAccessor parameters,
                                     MappingContext<?, ElasticsearchPersistentProperty> context) {
        super(tree, parameters);
        this.context = context;
    }

    public ElasticsearchQueryCreator(PartTree tree, MappingContext<?, ElasticsearchPersistentProperty> context) {
        super(tree);
        this.context = context;
    }

    @Override
    protected CriteriaQuery create(Part part, Iterator<Object> iterator) {
        PersistentPropertyPath<ElasticsearchPersistentProperty> path = context
                .getPersistentPropertyPath(part.getProperty());
        return new CriteriaQuery(from(part,
                new Criteria(path.toDotPath(ElasticsearchPersistentProperty.PropertyToFieldNameConverter.INSTANCE)), iterator));
    }

    @Override
    protected CriteriaQuery and(Part part, CriteriaQuery base, Iterator<Object> iterator) {
        if (base == null) {
            return create(part, iterator);
        }
        PersistentPropertyPath<ElasticsearchPersistentProperty> path = context
                .getPersistentPropertyPath(part.getProperty());
        return base.addCriteria(from(part,
                new Criteria(path.toDotPath(ElasticsearchPersistentProperty.PropertyToFieldNameConverter.INSTANCE)), iterator));
    }

    @Override
    protected CriteriaQuery or(CriteriaQuery base, CriteriaQuery query) {
        return new CriteriaQuery(base.getCriteria().or(query.getCriteria()));
    }

    @Override
    protected CriteriaQuery complete(CriteriaQuery query, Sort sort) {
        if (query == null) {
            return null;
        }
        return query.addSort(sort);
    }

    private Criteria from(Part part, Criteria instance, Iterator<?> parameters) {
        Part.Type type = part.getType();

        Criteria criteria = instance;
        if (criteria == null) {
            criteria = new Criteria();
        }
        switch (type) {
            case TRUE:
                return criteria.is(true);
            case FALSE:
                return criteria.is(false);
            case NEGATING_SIMPLE_PROPERTY:
                return criteria.is(parameters.next()).not();
            case REGEX:
                return criteria.expression(parameters.next().toString());
            case NOT_LIKE:
                return criteria.contains(parameters.next().toString()).not();
            case LIKE:
                return criteria.contains(parameters.next().toString());
            case STARTING_WITH:
                return criteria.startsWith(parameters.next().toString());
            case ENDING_WITH:
                return criteria.endsWith(parameters.next().toString());
            case CONTAINING:
                return criteria.contains(parameters.next().toString());
            case NOT_CONTAINING:
                return criteria.contains(parameters.next().toString()).not();
            case GREATER_THAN:
                return criteria.greaterThan(parameters.next());
            case AFTER:
            case GREATER_THAN_EQUAL:
                return criteria.greaterThanEqual(parameters.next());
            case LESS_THAN:
                return criteria.lessThan(parameters.next());
            case BEFORE:
            case LESS_THAN_EQUAL:
                return criteria.lessThanEqual(parameters.next());
            case BETWEEN:
                return criteria.between(parameters.next(), parameters.next());
            case IN:
                return criteria.in(asArray(parameters.next()));
            case NOT_IN:
                return criteria.notIn(asArray(parameters.next()));
            case SIMPLE_PROPERTY:
            case WITHIN:
                return withinOrSimplePropertyCriteria(part, instance, parameters);
            case NEAR:
                return nearCriteria(part, instance, parameters);
            default:
                throw new InvalidDataAccessApiUsageException(String.format(ILLEGAL_CRITERIA_ERROR, type));
        }
    }

    private Criteria withinOrSimplePropertyCriteria(Part part, Criteria criteria, Iterator<?> parameters) {
        Object firstParameter = parameters.next();
        Object secondParameter = null;
        if (part.getType() == Part.Type.SIMPLE_PROPERTY) {
            if (part.getProperty().getType() != GeoPoint.class)
                return criteria.is(firstParameter);
            else {
                // it means it's a simple find with exact geopoint matching (e.g. findByLocation)
                // and because Elasticsearch does not have any kind of query with just a geopoint
                // as argument we use a "geo distance" query with a distance of one meter.
                secondParameter = ".001km";
            }
        } else {
            secondParameter = parameters.next();
        }

        if (firstParameter instanceof GeoPoint && secondParameter instanceof String)
            return criteria.within((GeoPoint) firstParameter, (String) secondParameter);

        if (firstParameter instanceof Point && secondParameter instanceof Distance)
            return criteria.within((Point) firstParameter, (Distance) secondParameter);

        if (firstParameter instanceof String && secondParameter instanceof String)
            return criteria.within((String) firstParameter, (String) secondParameter);
        throw new InvalidDataAccessApiUsageException(String.format(ILLEGAL_CRITERIA_ERROR, part.getType()));
    }

    private Criteria nearCriteria(Part part, Criteria criteria, Iterator<?> parameters) {
        Object firstParameter = parameters.next();

        if (firstParameter instanceof Box) {
            return criteria.boundedBy((Box) firstParameter);
        }

        Object secondParameter = parameters.next();
        if (firstParameter instanceof Point && secondParameter instanceof Point) {
            return criteria.boundedBy((Point) firstParameter, (Point) secondParameter);
        }
        if (firstParameter instanceof GeoPoint && secondParameter instanceof GeoPoint) {
            return criteria.boundedBy((GeoPoint) firstParameter, (GeoPoint) secondParameter);
        }
        if (firstParameter instanceof String && secondParameter instanceof String) {
            return criteria.boundedBy((String) firstParameter, (String) secondParameter);
        }


        // "near" query can be the same query as the "within" query
        if (firstParameter instanceof Point && secondParameter instanceof String)
            return criteria.within((GeoPoint) firstParameter, (String) secondParameter);

        if (firstParameter instanceof Point && secondParameter instanceof Distance)
            return criteria.within((Point) firstParameter, (Distance) secondParameter);
        throw new InvalidDataAccessApiUsageException(String.format(ILLEGAL_CRITERIA_ERROR, part.getType()));
    }

    private Object[] asArray(Object o) {
        if (o instanceof Collection) {
            return ((Collection<?>) o).toArray();
        } else if (o.getClass().isArray()) {
            return (Object[]) o;
        }
        return new Object[]{o};
    }
}
