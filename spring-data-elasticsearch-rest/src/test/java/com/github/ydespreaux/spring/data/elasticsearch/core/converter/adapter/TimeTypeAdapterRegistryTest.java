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

package com.github.ydespreaux.spring.data.elasticsearch.core.converter.adapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.*;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link TimeTypeAdapterRegistry}.
 *
 * @author Yoann Despréaux
 * @since 0.0.1
 */
@RunWith(SpringRunner.class)
public class TimeTypeAdapterRegistryTest {
    /**
     * Tests that serialising to JSON works.
     */
    @Test
    public void testSerialisation() throws Exception {
        final Gson gson = TimeTypeAdapterRegistry.registerAll(new GsonBuilder()).create();

        final Container container = new Container();
        container.ld = LocalDate.of(1969, 7, 21);
        container.lt = LocalTime.of(12, 56, 0);
        container.ldt = LocalDateTime.of(container.ld, container.lt);
        container.odt = OffsetDateTime.of(container.ld, container.lt, ZoneOffset.ofHours(10));
        container.ot = OffsetTime.of(container.lt, ZoneOffset.ofHours(10));
        container.zdt = ZonedDateTime.of(container.ld, container.lt, ZoneId.of("Australia/Brisbane"));
        container.i = container.odt.toInstant();

        final String jsonString = gson.toJson(container);
        final JsonObject json = gson.fromJson(jsonString, JsonObject.class).getAsJsonObject();

        assertThat(json.get("ld").getAsString(), is("1969-07-21"));
        assertThat(json.get("lt").getAsString(), is("12:56:00"));
        assertThat(json.get("ldt").getAsString(), is("1969-07-21T12:56:00"));
        assertThat(json.get("odt").getAsString(), is("1969-07-21T12:56:00+10:00"));
        assertThat(json.get("ot").getAsString(), is("12:56:00+10:00"));
        assertThat(json.get("zdt").getAsString(), is("1969-07-21T12:56:00+10:00[Australia/Brisbane]"));
        assertThat(json.get("i").getAsString(), is("1969-07-21T02:56:00Z"));
    }

    /**
     * Tests that deserialising from JSON works.
     */
    @Test
    public void testDeserialisation() throws Exception {
        final Gson gson = TimeTypeAdapterRegistry.registerAll(new GsonBuilder()).create();

        final Container container = new Container();
        container.ld = LocalDate.of(1969, 7, 21);
        container.lt = LocalTime.of(12, 56, 0);
        container.ldt = LocalDateTime.of(container.ld, container.lt);
        container.odt = OffsetDateTime.of(container.ld, container.lt, ZoneOffset.ofHours(10));
        container.ot = OffsetTime.of(container.lt, ZoneOffset.ofHours(10));
        container.zdt = ZonedDateTime.of(container.ld, container.lt, ZoneId.of("Australia/Brisbane"));
        container.i = container.odt.toInstant();

        final JsonObject serialized = new JsonObject();
        serialized.add("ld", new JsonPrimitive("1969-07-21"));
        serialized.add("lt", new JsonPrimitive("12:56:00"));
        serialized.add("ldt", new JsonPrimitive("1969-07-21T12:56:00"));
        serialized.add("odt", new JsonPrimitive("1969-07-21T12:56:00+10:00"));
        serialized.add("ot", new JsonPrimitive("12:56:00+10:00"));
        serialized.add("zdt", new JsonPrimitive("1969-07-21T12:56:00+10:00[Australia/Brisbane]"));
        serialized.add("i", new JsonPrimitive("1969-07-21T02:56:00Z"));

        final String jsonString = gson.toJson(serialized);
        final Container deserialised = gson.fromJson(jsonString, Container.class);

        assertThat(deserialised.ld, is(container.ld));
        assertThat(deserialised.ldt, is(container.ldt));
        assertThat(deserialised.lt, is(container.lt));
        assertThat(deserialised.odt, is(container.odt));
        assertThat(deserialised.ot, is(container.ot));
        assertThat(deserialised.zdt, is(container.zdt));
        assertThat(deserialised.i, is(container.i));
    }

    /**
     * Container for serialising many fields.
     */
    private static class Container {
        private LocalDate ld;
        private LocalDateTime ldt;
        private LocalTime lt;
        private OffsetDateTime odt;
        private OffsetTime ot;
        private ZonedDateTime zdt;
        private Instant i;
    }
}
