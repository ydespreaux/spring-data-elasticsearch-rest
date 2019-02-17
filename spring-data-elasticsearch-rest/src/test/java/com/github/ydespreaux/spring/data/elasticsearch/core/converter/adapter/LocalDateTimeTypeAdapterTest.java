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
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link LocalDateTimeTypeAdapter}.
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
public class LocalDateTimeTypeAdapterTest {
    /**
     * The specific genericized type for {@code LocalDateTime}.
     */
    private static final Type LOCAL_DATE_TIME_TYPE = new TypeToken<LocalDateTime>() {
    }.getType();

    /**
     * Registers the {@link LocalDateTimeTypeAdapter} converter.
     *
     * @param builder The GSON builder to register the converter with.
     * @return A reference to {@code builder}.
     */
    private static GsonBuilder registerLocalDateTime(GsonBuilder builder) {
        builder.registerTypeAdapter(LOCAL_DATE_TIME_TYPE, new LocalDateTimeTypeAdapter());

        return builder;
    }

    /**
     * Tests that serialising to JSON works.
     */
    @Test
    public void testSerialisation() throws Exception {
        final Gson gson = registerLocalDateTime(new GsonBuilder()).create();

        final LocalDateTime localDateTime = LocalDateTime.parse("1969-07-21T12:56:00");
        final String json = gson.toJson(localDateTime);

        assertThat(json, is("\"1969-07-21T12:56:00\""));
    }

    /**
     * Tests that deserialising from JSON works.
     */
    @Test
    public void testDeserialisation() throws Exception {
        final Gson gson = registerLocalDateTime(new GsonBuilder()).create();

        final String json = "\"1969-07-21T12:56:00\"";
        final LocalDateTime localDateTime = gson.fromJson(json, LocalDateTime.class);

        assertThat(localDateTime, is(LocalDateTime.parse("1969-07-21T12:56:00")));
    }
}
