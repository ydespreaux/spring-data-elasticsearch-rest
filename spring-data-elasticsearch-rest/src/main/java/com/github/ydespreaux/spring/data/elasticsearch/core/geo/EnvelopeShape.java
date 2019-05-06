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

import java.util.Objects;

/**
 * EnvelopeShape
 *
 * @author Yoann Despréaux
 * @since 1.0.2
 */
@Getter
@Setter
@ToString
public class EnvelopeShape extends AbstractShape implements CoordinatesShape<Coordinate[]> {

    @JsonIgnore
    private Coordinate upperLeft;
    @JsonIgnore
    private Coordinate lowerRight;

    public EnvelopeShape() {
    }

    public EnvelopeShape(Coordinate upperLeft, Coordinate lowerRight) {
        Assert.notNull(upperLeft, "upperLeft must not be null !!");
        Assert.notNull(lowerRight, "lowerRight must not be null !!");
        this.upperLeft = upperLeft;
        this.lowerRight = lowerRight;
    }

    public EnvelopeShape(Coordinate[] coordinates) {
        Assert.notNull(coordinates, "coordinates must not be null !!");
        this.setCoordinates(coordinates);
    }

    @Override
    public GeoShapeType getType() {
        return GeoShapeType.ENVELOPE;
    }

    @Override
    public Coordinate[] getCoordinates() {
        if (upperLeft == null || lowerRight == null) {
            return new Coordinate[0];
        }
        return new Coordinate[]{this.upperLeft, this.lowerRight};
    }

    @Override
    public void setCoordinates(Coordinate[] coordinates) {
        Assert.notNull(coordinates, "coordinates must not be null !!");
        if (coordinates.length != 2) {
            throw new IllegalArgumentException("Envelope type must contains two points.");
        }
        this.upperLeft = coordinates[0];
        this.lowerRight = coordinates[1];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnvelopeShape)) return false;
        if (!super.equals(o)) return false;
        EnvelopeShape that = (EnvelopeShape) o;
        return Objects.equals(getUpperLeft(), that.getUpperLeft()) &&
                Objects.equals(getLowerRight(), that.getLowerRight());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getUpperLeft(), getLowerRight());
    }
}
