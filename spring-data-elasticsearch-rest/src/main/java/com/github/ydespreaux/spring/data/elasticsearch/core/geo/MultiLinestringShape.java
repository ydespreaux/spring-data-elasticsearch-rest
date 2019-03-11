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
 * MultiLinestringShape
 *
 * @author Yoann Despréaux
 * @since 1.0.2
 */
@Getter
@Setter
@ToString
public class MultiLinestringShape extends AbstractShape implements CoordinatesShape<Coordinate[][]> {

    @JsonIgnore
    private transient List<LinestringShape> shapes;

    public MultiLinestringShape() {
    }

    public MultiLinestringShape(List<LinestringShape> shapes) {
        if (shapes != null) {
            this.shapes = new ArrayList<>(shapes);
        } else {
            this.shapes = new ArrayList<>();
        }
    }

    public MultiLinestringShape(LinestringShape... shapes) {
        if (shapes != null) {
            this.shapes = Arrays.asList(shapes);
        } else {
            this.shapes = new ArrayList<>();
        }
    }

    public MultiLinestringShape(Coordinate[][] coordinates) {
        Assert.notNull(coordinates, "coordinates must not be null!!");
        setCoordinates(coordinates);
    }

    @Override
    public GeoShapeType getType() {
        return GeoShapeType.MULTILINESTRING;
    }

    @Override
    public Coordinate[][] getCoordinates() {
        if (shapes == null) {
            return null;
        }
        Coordinate[][] coordinates = new Coordinate[shapes.size()][];
        for (int i = 0; i < coordinates.length; i++) {
            coordinates[i] = shapes.get(i).getCoordinates().toArray(new Coordinate[0]);
        }
        return coordinates;
    }

    @Override
    public void setCoordinates(Coordinate[][] coordinates) {
        shapes = new ArrayList<>(coordinates.length);
        for (int i = 0; i < coordinates.length; i++) {
            shapes.add(new LinestringShape(coordinates[i]));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MultiLinestringShape)) return false;
        if (!super.equals(o)) return false;
        MultiLinestringShape that = (MultiLinestringShape) o;
        return Objects.equals(shapes, that.shapes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), shapes);
    }
}
