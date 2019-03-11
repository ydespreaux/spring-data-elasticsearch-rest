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

package com.github.ydespreaux.spring.data.elasticsearch.core.converter.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ydespreaux.spring.data.elasticsearch.core.geo.*;
import org.elasticsearch.common.geo.GeoShapeType;

import java.io.IOException;

/**
 * ShapeDeserializer
 *
 * @author Yoann Despréaux
 * @since 1.0.2
 */
public class ShapeDeserializer extends JsonDeserializer<Shape> {
    @Override
    public Shape deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String type = node.get("type").asText();
        GeoShapeType geoshapeType = GeoShapeType.forName(type);
        Class<? extends Shape> shapeClazz = null;
        switch (geoshapeType) {
            case POINT:
                shapeClazz = PointShape.class;
                break;
            case ENVELOPE:
                shapeClazz = EnvelopeShape.class;
                break;
            case LINESTRING:
                shapeClazz = LinestringShape.class;
                break;
            case MULTIPOINT:
                shapeClazz = MultiPointShape.class;
                break;
            case MULTILINESTRING:
                shapeClazz = MultiLinestringShape.class;
                break;
            case POLYGON:
                shapeClazz = PolygonShape.class;
                break;
            case MULTIPOLYGON:
                shapeClazz = MultiPolygonShape.class;
                break;
            case CIRCLE:
                shapeClazz = CircleShape.class;
                break;
            case GEOMETRYCOLLECTION:
                shapeClazz = GeometryCollectionShape.class;
                break;
        }
        return ((ObjectMapper) jsonParser.getCodec()).readValue(node.toString(), shapeClazz);
    }
}
