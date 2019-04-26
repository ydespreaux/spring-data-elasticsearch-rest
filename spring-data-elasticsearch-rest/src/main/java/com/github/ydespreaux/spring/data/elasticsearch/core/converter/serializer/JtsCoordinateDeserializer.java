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
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.locationtech.jts.geom.Coordinate;

import java.io.IOException;

/**
 * JtsCoordinateDeserializer
 *
 * @author Yoann Despréaux
 * @since 1.0.2
 */
public class JtsCoordinateDeserializer extends JsonDeserializer<Coordinate> {
    @Override
    public Coordinate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ArrayNode node = jsonParser.readValueAs(ArrayNode.class);
        if (node.size() == 2) {
            return new Coordinate(node.get(0).asDouble(), node.get(1).asDouble());
        }
        return new Coordinate(node.get(0).asDouble(), node.get(1).asDouble(), node.get(2).asDouble());
    }
}
