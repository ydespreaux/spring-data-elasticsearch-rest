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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.elasticsearch.common.geo.GeoShapeType;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * MultiPointShape
 *
 * @author Yoann Despréaux
 * @since 1.0.2
 */
@Getter
@Setter
@ToString
public class MultiPointShape extends AbstractShape implements CoordinatesShape<List<Coordinate>> {

    private List<Coordinate> coordinates;

    public MultiPointShape() {
    }

    public MultiPointShape(List<Coordinate> coordinates) {
        Assert.notNull(coordinates, "coordinates must not be null!!");
        this.coordinates = coordinates;
    }

    public MultiPointShape(Coordinate... coordinates) {
        this(Arrays.asList(coordinates));
    }

    @Override
    public GeoShapeType getType() {
        return GeoShapeType.MULTIPOINT;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MultiPointShape)) return false;
        if (!super.equals(o)) return false;
        MultiPointShape that = (MultiPointShape) o;
        return Objects.equals(coordinates, that.coordinates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), coordinates);
    }
}
