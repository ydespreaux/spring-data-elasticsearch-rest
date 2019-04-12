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
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * MultiPolygonShape
 *
 * @author Yoann Despréaux
 * @since 1.0.2
 */
@Getter
@Setter
@ToString
public class MultiPolygonShape extends AbstractShape implements CoordinatesShape<Coordinate[][][]> {

    @JsonIgnore
    private List<PolygonShape> shapes;

    private GeoShapeOrientation orientation;

    public MultiPolygonShape() {
    }

    public MultiPolygonShape(List<PolygonShape> shapes) {
        this(shapes, null);
    }

    public MultiPolygonShape(List<PolygonShape> shapes, @Nullable GeoShapeOrientation orientation) {
        Assert.notNull(shapes, "shapes must not be null!!");
        this.shapes = shapes;
        this.orientation = orientation;
    }

    public MultiPolygonShape(Coordinate[][][] coordinates) {
        this(coordinates, null);
    }

    public MultiPolygonShape(Coordinate[][][] coordinates, @Nullable GeoShapeOrientation orientation) {
        this.setCoordinates(coordinates);
        this.orientation = orientation;
    }

    @Override
    public GeoShapeType getType() {
        return GeoShapeType.MULTIPOLYGON;
    }

    @Override
    public Coordinate[][][] getCoordinates() {
        if (shapes == null) {
            return new Coordinate[0][][];
        }
        Coordinate[][][] coordinates = new Coordinate[this.shapes.size()][][];
        for (int i = 0; i < this.shapes.size(); i++) {
            coordinates[i] = this.shapes.get(i).getCoordinates();
        }
        return coordinates;
    }

    @Override
    public void setCoordinates(@Nullable Coordinate[][][] coordinates) {
        if (coordinates != null) {
            this.shapes = new ArrayList<>(coordinates.length);
            for (int i = 0; i < coordinates.length; i++) {
                this.shapes.add(new PolygonShape(coordinates[i]));
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MultiPolygonShape)) return false;
        if (!super.equals(o)) return false;
        MultiPolygonShape that = (MultiPolygonShape) o;
        return Objects.equals(shapes, that.shapes) && Objects.equals(orientation, that.orientation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), shapes, orientation);
    }

}
