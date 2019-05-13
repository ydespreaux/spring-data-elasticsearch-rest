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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.ydespreaux.spring.data.elasticsearch.core.DefaultEntityMapper;
import com.github.ydespreaux.spring.data.elasticsearch.entities.City;
import org.elasticsearch.common.geo.GeoPoint;
import org.junit.Test;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class GeoPointSerializerTest {

    @Test
    public void serializeGeoPoint() throws JsonProcessingException {
        City city = City.builder()
                .name("Castries")
                .region(null)
                .location(new GeoPoint(40.2, 53.65))
                .build();
        DefaultEntityMapper mapper = new DefaultEntityMapper(new JacksonProperties());
        assertThat(mapper.mapToString(city), is(equalTo("{\"name\":\"Castries\",\"location\":{\"lat\":40.2,\"lon\":53.65}}")));
    }
}
