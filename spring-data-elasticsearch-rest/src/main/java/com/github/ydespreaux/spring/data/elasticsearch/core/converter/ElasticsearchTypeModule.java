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

package com.github.ydespreaux.spring.data.elasticsearch.core.converter;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.ydespreaux.spring.data.elasticsearch.core.converter.serializer.*;
import com.github.ydespreaux.spring.data.elasticsearch.core.geo.GeoShapeOrientation;
import com.github.ydespreaux.spring.data.elasticsearch.core.geo.Shape;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.GeoShapeType;
import org.elasticsearch.common.unit.DistanceUnit;

/**
 * ElasticsearchTypeModule
 *
 * @author Yoann Despréaux
 * @since 1.0.1
 */
public class ElasticsearchTypeModule extends SimpleModule {

    public ElasticsearchTypeModule() {

        this.addSerializer(GeoPoint.class, new GeoPointSerializer());
        this.addSerializer(GeoShapeType.class, new GeoShapeTypeSerializer());
        this.addSerializer(DistanceUnit.Distance.class, new DistanceSerializer());
        this.addSerializer(GeoShapeOrientation.class, new GeoShapeOrientationSerializer());

        this.addDeserializer(GeoShapeType.class, new GeoShapeTypeDeserializer());
        this.addDeserializer(DistanceUnit.Distance.class, new DistanceDeserializer());
        this.addDeserializer(GeoShapeOrientation.class, new GeoShapeOrientationDeserializer());

        this.addDeserializer(Shape.class, new ShapeDeserializer());

    }
}
