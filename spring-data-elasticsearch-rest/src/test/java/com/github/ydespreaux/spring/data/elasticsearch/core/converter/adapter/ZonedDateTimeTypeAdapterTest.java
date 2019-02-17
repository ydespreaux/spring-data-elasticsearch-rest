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
import com.google.gson.reflect.TypeToken;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Type;
import java.time.ZonedDateTime;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link ZonedDateTimeTypeAdapter}.
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
public class ZonedDateTimeTypeAdapterTest {
    /**
     * The specific genericized type for {@code ZonedDateTime}.
     */
    private static final Type ZONED_DATE_TIME_TYPE = new TypeToken<ZonedDateTime>() {
    }.getType();

    /**
     * Registers the {@link ZonedDateTimeTypeAdapter} converter.
     *
     * @param builder The GSON builder to register the converter with.
     * @return A reference to {@code builder}.
     */
    private static GsonBuilder registerZonedDateTime(GsonBuilder builder) {
        builder.registerTypeAdapter(ZONED_DATE_TIME_TYPE, new ZonedDateTimeTypeAdapter());

        return builder;
    }

    /**
     * Tests that serialising to JSON works.
     */
    @Test
    public void testSerialisation() throws Exception {
        final Gson gson = registerZonedDateTime(new GsonBuilder()).create();

        final ZonedDateTime zonedDateTime = ZonedDateTime.parse("1969-07-21T12:56:00+10:00[Australia/Brisbane]");
        final String json = gson.toJson(zonedDateTime);

        assertThat(json, is("\"1969-07-21T12:56:00+10:00[Australia/Brisbane]\""));
    }

    /**
     * Tests that deserialising from JSON works.
     */
    @Test
    public void testDeserialisation() throws Exception {
        final Gson gson = registerZonedDateTime(new GsonBuilder()).create();

        final String json = "\"1969-07-21T12:56:00+10:00[Australia/Brisbane]\"";
        final ZonedDateTime zonedDateTime = gson.fromJson(json, ZonedDateTime.class);

        assertThat(zonedDateTime, is(ZonedDateTime.parse("1969-07-21T12:56:00+10:00[Australia/Brisbane]")));
    }
}
