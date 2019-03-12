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
import org.locationtech.jts.geom.Coordinate;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * PolygonShape
 *
 * @author Yoann Despréaux
 * @since 1.0.2
 */
@Getter
@Setter
@ToString
public class PolygonShape extends AbstractShape implements CoordinatesShape<Coordinate[][]> {

    @JsonIgnore
    private List<Coordinate> outerCoordinates;

    @JsonIgnore
    private List<Coordinate> innerCoordinates;

    /**
     *
     */
    private GeoShapeOrientation orientation;

    public PolygonShape() {
    }

    public PolygonShape(List<Coordinate> outerCoordinates, List<Coordinate> innerCoordinates) {
        this(outerCoordinates, innerCoordinates, null);
    }

    public PolygonShape(Coordinate[] outerCoordinates, Coordinate[] innerCoordinates) {
        this(outerCoordinates, innerCoordinates, null);
    }

    public PolygonShape(Coordinate[] outerCoordinates, Coordinate[] innerCoordinates, GeoShapeOrientation orientation) {
        this(Arrays.asList(outerCoordinates), innerCoordinates != null ? Arrays.asList(innerCoordinates) : new ArrayList<>(), orientation);
    }

    public PolygonShape(Coordinate[][] coordinates) {
        this(coordinates, null);
    }

    public PolygonShape(Coordinate[][] coordinates, GeoShapeOrientation orientation) {
        if (coordinates != null) {
            this.setCoordinates(coordinates);
        }
        this.orientation = orientation;
    }

    public PolygonShape(List<Coordinate> outerCoordinates, List<Coordinate> innerCoordinates, GeoShapeOrientation orientation) {
        Assert.notNull(outerCoordinates, "outerCoordinates must not be null!!");
        this.outerCoordinates = outerCoordinates;
        this.innerCoordinates = innerCoordinates != null ? innerCoordinates : new ArrayList<>();
        this.orientation = orientation;
    }

    @Override
    public GeoShapeType getType() {
        return GeoShapeType.POLYGON;
    }

    @Override
    public Coordinate[][] getCoordinates() {
        if (innerCoordinates == null || innerCoordinates.isEmpty()) {
            return new Coordinate[][]{
                    outerCoordinates.toArray(new Coordinate[0])
            };
        }
        return new Coordinate[][]{
                outerCoordinates.toArray(new Coordinate[0]), innerCoordinates.toArray(new Coordinate[0])
        };
    }

    @Override
    public void setCoordinates(Coordinate[][] coordinates) {
        if (coordinates == null) {
            outerCoordinates = new ArrayList<>();
            innerCoordinates = new ArrayList<>();
        } else {
            if (coordinates.length > 0) {
                outerCoordinates = Arrays.asList(coordinates[0]);
                if (coordinates.length == 2) {
                    innerCoordinates = Arrays.asList(coordinates[1]);
                } else {
                    innerCoordinates = new ArrayList<>();
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PolygonShape)) return false;
        if (!super.equals(o)) return false;
        PolygonShape that = (PolygonShape) o;
        return Objects.equals(getOuterCoordinates(), that.getOuterCoordinates()) &&
                Objects.equals(getInnerCoordinates(), that.getInnerCoordinates()) &&
                Objects.equals(getOrientation(), that.getOrientation());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getOuterCoordinates(), getInnerCoordinates(), getOrientation());
    }
}
