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

package com.github.ydespreaux.spring.data.elasticsearch.core.geo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.elasticsearch.common.geo.GeoShapeType;
import org.elasticsearch.common.unit.DistanceUnit;
import org.locationtech.jts.geom.Coordinate;

import java.util.Objects;

/**
 * CircleShape
 *
 * @author Yoann Despréaux
 * @since 1.0.2
 */
@Getter
@Setter
@ToString
public class CircleShape extends AbstractShape implements CoordinatesShape<Coordinate> {

    @JsonIgnore
    private Coordinate center;

    private DistanceUnit.Distance radius;

    public CircleShape() {
    }

    public CircleShape(Coordinate center, double radius) {
        this(center, radius, DistanceUnit.METERS);
    }

    public CircleShape(Coordinate center, double radius, DistanceUnit unit) {
        this(center, new DistanceUnit.Distance(radius, unit));
    }

    public CircleShape(Coordinate center, DistanceUnit.Distance radius) {
        this.center = center;
        this.radius = radius;
    }


    @Override
    public GeoShapeType getType() {
        return GeoShapeType.CIRCLE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CircleShape)) return false;
        if (!super.equals(o)) return false;
        CircleShape that = (CircleShape) o;
        return Objects.equals(that.getRadius(), getRadius()) &&
                Objects.equals(getCoordinates(), that.getCoordinates());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getCoordinates(), getRadius());
    }

    @Override
    public Coordinate getCoordinates() {
        return this.center;
    }

    @Override
    public void setCoordinates(Coordinate coordinates) {
        this.center = coordinates;
    }
}
