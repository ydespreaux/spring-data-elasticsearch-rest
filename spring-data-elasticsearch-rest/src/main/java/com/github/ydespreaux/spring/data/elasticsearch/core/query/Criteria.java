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

import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.query.Operator;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.geo.Box;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Criteria is the central class when constructing queries. It follows more or less a fluent API style, which allows to
 * easily chain together multiple criteria.
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public class Criteria {

    public static final String WILDCARD = "*";
    public static final String CRITERIA_VALUE_SEPARATOR = " ";

    private Field field;
    private float boost = Float.NaN;
    private boolean negating = false;

    private List<Criteria> criteriaChain = new ArrayList<>(1);

    private Set<CriteriaEntry> queryCriteria = new LinkedHashSet<>();

    private Set<CriteriaEntry> filterCriteria = new LinkedHashSet<>();

    /**
     * Creates a new Criteria for the given field
     *
     * @param fieldName
     */
    public Criteria(String fieldName) {
        this(new Field(fieldName));
    }

    /**
     * Creates a new Criteria for the given field
     *
     * @param field
     */
    public Criteria(Field field) {
        Assert.notNull(field, "Field for criteria must not be null");
        Assert.hasText(field.getName(), "Field.name for criteria must not be null/empty");
        this.criteriaChain.add(this);
        this.field = field;
    }

    /**
     * @param criteriaChain
     * @param fieldname
     */
    private Criteria(List<Criteria> criteriaChain, String fieldname) {
        this(criteriaChain, new Field(fieldname));
    }

    /**
     * @param criteriaChain
     * @param field
     */
    private Criteria(List<Criteria> criteriaChain, Field field) {
        Assert.notNull(criteriaChain, "CriteriaChain must not be null");
        Assert.notNull(field, "Field for criteria must not be null");
        Assert.hasText(field.getName(), "Field.name for criteria must not be null/empty");

        this.criteriaChain.addAll(criteriaChain);
        this.criteriaChain.add(this);
        this.field = field;
    }

    public Criteria() {

    }

    /**
     * Static factory method to create a new Criteria for field with given name
     *
     * @param field
     * @return
     */
    public static Criteria where(String field) {
        return where(new Field(field));
    }

    /**
     * Static factory method to create a new Criteria for provided field
     *
     * @param field
     * @return
     */
    public static Criteria where(Field field) {
        return new Criteria(field);
    }

    /**
     * Chain using {@code AND}
     *
     * @param field
     * @return
     */
    public Criteria and(Field field) {
        return new Criteria(this.criteriaChain, field);
    }

    /**
     * Chain using {@code AND}
     *
     * @param fieldName
     * @return
     */
    public Criteria and(String fieldName) {
        return new Criteria(this.criteriaChain, fieldName);
    }

    /**
     * Chain using {@code AND}
     *
     * @param criteria
     * @return
     */
    public Criteria and(Criteria criteria) {
        this.criteriaChain.add(criteria);
        return this;
    }

    /**
     * Chain using {@code AND}
     *
     * @param criterias
     * @return
     */
    public Criteria and(Criteria... criterias) {
        this.criteriaChain.addAll(Arrays.asList(criterias));
        return this;
    }

    /**
     * Chain using {@code OR}
     *
     * @param field
     * @return
     */
    public Criteria or(Field field) {
        return new OrCriteria(this.criteriaChain, field);
    }

    /**
     * Chain using {@code OR}
     *
     * @param criteria
     * @return
     */
    public Criteria or(Criteria criteria) {
        Assert.notNull(criteria, "Cannot chain 'null' criteria.");
        Criteria orConnectedCritiera = new OrCriteria(this.criteriaChain, criteria.getField());
        orConnectedCritiera.queryCriteria.addAll(criteria.queryCriteria);
        return orConnectedCritiera;
    }

    /**
     * Chain using {@code OR}
     *
     * @param fieldName
     * @return
     */
    public Criteria or(String fieldName) {
        return or(new Field(fieldName));
    }

    /**
     * Creates new CriteriaEntry without any wildcards
     *
     * @param value
     * @return
     */
    public Criteria is(Object value) {
        queryCriteria.add(new CriteriaEntry(OperationKey.EQUALS, value));
        return this;
    }

    /**
     * Creates new CriteriaEntry with leading and trailing wildcards
     *
     * @param value
     * @return
     */
    public Criteria contains(String value) {
        assertNoBlankInWildcardedQuery(value, true, true);
        queryCriteria.add(new CriteriaEntry(OperationKey.CONTAINS, value));
        return this;
    }

    /**
     * Creates new CriteriaEntry with trailing wildcard
     *
     * @param value
     * @return
     */
    public Criteria startsWith(String value) {
        assertNoBlankInWildcardedQuery(value, true, false);
        queryCriteria.add(new CriteriaEntry(OperationKey.STARTS_WITH, value));
        return this;
    }

    /**
     * Creates new CriteriaEntry with leading wildcard
     *
     * @param value
     * @return
     */
    public Criteria endsWith(String value) {
        assertNoBlankInWildcardedQuery(value, false, true);
        queryCriteria.add(new CriteriaEntry(OperationKey.ENDS_WITH, value));
        return this;
    }

    /**
     * Creates new CriteriaEntry with negating
     *
     * @return
     */
    public Criteria not() {
        this.negating = true;
        return this;
    }

    /**
     * Creates new CriteriaEntry with trailing ~
     *
     * @param value
     * @return
     */
    public Criteria fuzzy(String value) {
        queryCriteria.add(new CriteriaEntry(OperationKey.FUZZY, value));
        return this;
    }

    /**
     * Creates new CriteriaEntry allowing native elasticsearch expressions
     *
     * @param expression
     * @return
     */
    public Criteria expression(String expression) {
        queryCriteria.add(new CriteriaEntry(OperationKey.EXPRESSION, expression));
        return this;
    }

    /**
     * Boost positive hit with given factor.
     *
     * @param boost
     * @return
     */
    public Criteria boost(float boost) {
        if (boost < 0) {
            throw new InvalidDataAccessApiUsageException("Boost must not be negative.");
        }
        this.boost = boost;
        return this;
    }

    /**
     * Creates new CriteriaEntry for {@code RANGE [lowerBound TO upperBound]}
     *
     * @param lowerBound
     * @param upperBound
     * @return
     */
    public Criteria between(Object lowerBound, Object upperBound) {
        if (lowerBound == null && upperBound == null) {
            throw new InvalidDataAccessApiUsageException("Range [* TO *] is not allowed");
        }

        queryCriteria.add(new CriteriaEntry(OperationKey.BETWEEN, new Object[]{lowerBound, upperBound}));
        return this;
    }

    /**
     * Creates new CriteriaEntry for {@code RANGE [* TO upperBound]}
     *
     * @param upperBound
     * @return
     */
    public Criteria lessThanEqual(Object upperBound) {
        if (upperBound == null) {
            throw new InvalidDataAccessApiUsageException("UpperBound can't be null");
        }
        queryCriteria.add(new CriteriaEntry(OperationKey.LESS_EQUAL, upperBound));
        return this;
    }

    public Criteria lessThan(Object upperBound) {
        if (upperBound == null) {
            throw new InvalidDataAccessApiUsageException("UpperBound can't be null");
        }
        queryCriteria.add(new CriteriaEntry(OperationKey.LESS, upperBound));
        return this;
    }

    /**
     * Creates new CriteriaEntry for {@code RANGE [lowerBound TO *]}
     *
     * @param lowerBound
     * @return
     */
    public Criteria greaterThanEqual(Object lowerBound) {
        if (lowerBound == null) {
            throw new InvalidDataAccessApiUsageException("LowerBound can't be null");
        }
        queryCriteria.add(new CriteriaEntry(OperationKey.GREATER_EQUAL, lowerBound));
        return this;
    }

    public Criteria greaterThan(Object lowerBound) {
        if (lowerBound == null) {
            throw new InvalidDataAccessApiUsageException("LowerBound can't be null");
        }
        queryCriteria.add(new CriteriaEntry(OperationKey.GREATER, lowerBound));
        return this;
    }

    /**
     * Creates new CriteriaEntry for multiple values {@code (arg0 arg1 arg2 ...)}
     *
     * @param values
     * @return
     */
    public Criteria in(Object... values) {
        return in(toCollection(values));
    }

    /**
     * Creates new CriteriaEntry for multiple values {@code (arg0 arg1 arg2 ...)}
     *
     * @param values the collection containing the values to match against
     * @return
     */
    public Criteria in(Iterable<?> values) {
        Assert.notNull(values, "Collection of 'in' values must not be null");
        queryCriteria.add(new CriteriaEntry(OperationKey.IN, values));
        return this;
    }

    private List<Object> toCollection(Object... values) {
        if (values.length == 0 || (values.length > 1 && values[1] instanceof Collection)) {
            throw new InvalidDataAccessApiUsageException("At least one element "
                    + (values.length > 0 ? ("of argument of type " + values[1].getClass().getName()) : "")
                    + " has to be present.");
        }
        return Arrays.asList(values);
    }

    public Criteria notIn(Object... values) {
        return notIn(toCollection(values));
    }

    public Criteria notIn(Iterable<?> values) {
        Assert.notNull(values, "Collection of 'NotIn' values must not be null");
        queryCriteria.add(new CriteriaEntry(OperationKey.NOT_IN, values));
        return this;
    }

    /**
     * Creates new CriteriaEntry for {@code location WITHIN distance}
     *
     * @param location {@link GeoPoint} center coordinates
     * @param distance {@link String} radius as a string (e.g. : '100km').
     *                 Distance unit :
     *                 either mi/miles or km can be set
     * @return Criteria the chaind criteria with the new 'within' criteria included.
     */
    public Criteria within(GeoPoint location, String distance) {
        Assert.notNull(location, "Location value for near criteria must not be null");
        Assert.notNull(distance, "Distance value for near criteria must not be null");
        filterCriteria.add(new CriteriaEntry(OperationKey.WITHIN, new Object[]{location, distance}));
        return this;
    }

    /**
     * Creates new CriteriaEntry for {@code location WITHIN distance}
     *
     * @param location {@link org.springframework.data.geo.Point} center coordinates
     * @param distance {@link org.springframework.data.geo.Distance} radius
     *                 .
     * @return Criteria the chaind criteria with the new 'within' criteria included.
     */
    public Criteria within(Point location, Distance distance) {
        Assert.notNull(location, "Location value for near criteria must not be null");
        Assert.notNull(location, "Distance value for near criteria must not be null");
        filterCriteria.add(new CriteriaEntry(OperationKey.WITHIN, new Object[]{location, distance}));
        return this;
    }

    /**
     * Creates new CriteriaEntry for {@code geoLocation WITHIN distance}
     *
     * @param geoLocation {@link String} center point
     *                    supported formats:
     *                    lat on : "41.2,45.1",
     *                    geohash : "asd9as0d"
     * @param distance    {@link String} radius as a string (e.g. : '100km').
     *                    Distance unit :
     *                    either mi/miles or km can be set
     * @return
     */
    public Criteria within(String geoLocation, String distance) {
        Assert.isTrue(!StringUtils.isEmpty(geoLocation), "geoLocation value must not be null");
        filterCriteria.add(new CriteriaEntry(OperationKey.WITHIN, new Object[]{geoLocation, distance}));
        return this;
    }

    /**
     * Creates new CriteriaEntry for bounding box created from points
     *
     * @param topLeftGeohash     left top corner of bounding box as geohash
     * @param bottomRightGeohash right bottom corner of bounding box as geohash
     * @return Criteria the chaind criteria with the new 'boundedBy' criteria included.
     */
    public Criteria boundedBy(String topLeftGeohash, String bottomRightGeohash) {
        Assert.isTrue(!StringUtils.isEmpty(topLeftGeohash), "topLeftGeohash must not be empty");
        Assert.isTrue(!StringUtils.isEmpty(bottomRightGeohash), "bottomRightGeohash must not be empty");
        filterCriteria.add(new CriteriaEntry(OperationKey.BOX, new Object[]{topLeftGeohash, bottomRightGeohash}));
        return this;
    }

    /**
     * Creates new CriteriaEntry for bounding box created from points
     *
     * @param topLeftPoint     left top corner of bounding box
     * @param bottomRightPoint right bottom corner of bounding box
     * @return Criteria the chaind criteria with the new 'boundedBy' criteria included.
     */
    public Criteria boundedBy(GeoPoint topLeftPoint, GeoPoint bottomRightPoint) {
        Assert.notNull(topLeftPoint, "topLeftPoint must not be null");
        Assert.notNull(bottomRightPoint, "bottomRightPoint must not be null");
        filterCriteria.add(new CriteriaEntry(OperationKey.BOX, new Object[]{topLeftPoint, bottomRightPoint}));
        return this;
    }

    public Criteria boundedBy(Point topLeftPoint, Point bottomRightPoint) {
        Assert.notNull(topLeftPoint, "topLeftPoint must not be null");
        Assert.notNull(bottomRightPoint, "bottomRightPoint must not be null");
        filterCriteria.add(new CriteriaEntry(OperationKey.BOX, new Object[]{new GeoPoint(topLeftPoint.getX(), topLeftPoint.getY()), new GeoPoint(bottomRightPoint.getX(), bottomRightPoint.getY())}));
        return this;
    }

    public Criteria boundedBy(Box box) {
        Assert.notNull(box, "box must not be null");
        filterCriteria.add(new CriteriaEntry(OperationKey.BOX, new Object[]{box}));
        return this;
    }

    private void assertNoBlankInWildcardedQuery(String searchString, boolean leadingWildcard, boolean trailingWildcard) {
        if (searchString != null && searchString.contains(CRITERIA_VALUE_SEPARATOR)) {
            throw new InvalidDataAccessApiUsageException("Cannot constructQuery '" + (leadingWildcard ? "*" : "") + "\""
                    + searchString + "\"" + (trailingWildcard ? "*" : "") + "'. Use expression or multiple clauses instead.");
        }
    }

    /**
     * Field targeted by this Criteria
     *
     * @return
     */
    public Field getField() {
        return this.field;
    }

    public Set<CriteriaEntry> getQueryCriteriaEntries() {
        return Collections.unmodifiableSet(this.queryCriteria);
    }

    public Set<CriteriaEntry> getFilterCriteriaEntries() {
        return Collections.unmodifiableSet(this.filterCriteria);
    }

    public Set<CriteriaEntry> getFilterCriteria() {
        return filterCriteria;
    }

    /**
     * Conjunction to be used with this criteria (AND | OR)
     *
     * @return
     */
    public Operator getConjunctionOperator() {
        return Operator.AND;
    }

    public List<Criteria> getCriteriaChain() {
        return Collections.unmodifiableList(this.criteriaChain);
    }

    public boolean isNegating() {
        return this.negating;
    }

    public boolean isAnd() {
        return Operator.AND == getConjunctionOperator();
    }

    public boolean isOr() {
        return Operator.OR == getConjunctionOperator();
    }

    public float getBoost() {
        return this.boost;
    }

    /**
     *
     */
    public enum OperationKey {
        EQUALS, CONTAINS, STARTS_WITH, ENDS_WITH, EXPRESSION, BETWEEN, FUZZY, IN, NOT_IN, WITHIN, BOX, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL;
    }

    /**
     *
     */
    static class OrCriteria extends Criteria {

        private OrCriteria(List<Criteria> criteriaChain, Field field) {
            super(criteriaChain, field);
        }

        @Override
        public Operator getConjunctionOperator() {
            return Operator.OR;
        }
    }

    /**
     *
     */
    public static class CriteriaEntry {

        private OperationKey key;
        private Object value;

        CriteriaEntry(OperationKey key, Object value) {
            this.key = key;
            this.value = value;
        }

        public OperationKey getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "CriteriaEntry{" +
                    "key=" + key +
                    ", value=" + value +
                    '}';
        }
    }
}
