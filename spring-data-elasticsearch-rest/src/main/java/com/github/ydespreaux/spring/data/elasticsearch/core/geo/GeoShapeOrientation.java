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

import org.elasticsearch.common.geo.builders.ShapeBuilder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * GeoShapeOrientation
 *
 * @author Yoann Despréaux
 * @since 1.0.2
 */
public enum GeoShapeOrientation {

    LEFT("left"),
    RIGHT("right"),
    CLOCKWISE("clockwise"),
    COUNTER_CLOCKWISE("counterclockwise"),
    CW("cw"),
    CCW("ccw");

    private static Map<String, GeoShapeOrientation> orientationMap = new HashMap();

    static {
        for (GeoShapeOrientation orientation : values()) {
            orientationMap.put(orientation.orientationName, orientation);
        }
    }

    private final String orientationName;

    GeoShapeOrientation(String orientationName) {
        this.orientationName = orientationName;
    }

    public static GeoShapeOrientation forName(String orientationName) {
        String value = orientationName.toLowerCase(Locale.ROOT);
        if (orientationMap.containsKey(value)) {
            return orientationMap.get(value);
        } else {
            throw new IllegalArgumentException("unknown orientiation [" + orientationName + "]");
        }
    }

    public String orientationName() {
        return this.orientationName;
    }

    public ShapeBuilder.Orientation orientation() {
        return ShapeBuilder.Orientation.fromString(this.orientationName);
    }
}
