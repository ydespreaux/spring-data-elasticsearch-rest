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
import java.time.LocalTime;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link LocalTimeTypeAdapter}.
 *
 * @author Yoann Despréaux
 * @since 0.0.1
 */
@RunWith(SpringRunner.class)
public class LocalTimeTypeAdapterTest {
    /**
     * The specific genericized type for {@code LocalTime}.
     */
    private static final Type LOCAL_TIME_TYPE = new TypeToken<LocalTime>() {
    }.getType();

    /**
     * Registers the {@link LocalTimeTypeAdapter} converter.
     *
     * @param builder The GSON builder to register the converter with.
     * @return A reference to {@code builder}.
     */
    private static GsonBuilder registerLocalTime(GsonBuilder builder) {
        builder.registerTypeAdapter(LOCAL_TIME_TYPE, new LocalTimeTypeAdapter());

        return builder;
    }

    /**
     * Tests that serialising to JSON works.
     */
    @Test
    public void testSerialisation() throws Exception {
        final Gson gson = registerLocalTime(new GsonBuilder()).create();

        final LocalTime localTime = LocalTime.parse("12:56:00");
        final String json = gson.toJson(localTime);

        assertThat(json, is("\"12:56:00\""));
    }

    /**
     * Tests that deserialising from JSON works.
     */
    @Test
    public void testDeserialisation() throws Exception {
        final Gson gson = registerLocalTime(new GsonBuilder()).create();

        final String json = "\"12:56:00\"";
        final LocalTime localTime = gson.fromJson(json, LocalTime.class);

        assertThat(localTime, is(LocalTime.parse("12:56:00")));
    }
}
