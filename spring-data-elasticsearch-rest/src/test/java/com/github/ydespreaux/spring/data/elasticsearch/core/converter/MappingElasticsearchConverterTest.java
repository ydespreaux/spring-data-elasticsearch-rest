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

import com.github.ydespreaux.spring.data.elasticsearch.core.DefaultEntityMapper;
import com.github.ydespreaux.spring.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.junit.Test;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.data.mapping.context.MappingContext;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public class MappingElasticsearchConverterTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToInitializeGivenMappingContextIsNull() {
        // given
        new MappingElasticsearchConverter(null, new DefaultEntityMapper(new JacksonProperties()));
    }

    @Test
    public void shouldReturnMappingContextWithWhichItWasInitialized() {
        // given
        MappingContext mappingContext = new SimpleElasticsearchMappingContext();
        MappingElasticsearchConverter converter = new MappingElasticsearchConverter(mappingContext, new DefaultEntityMapper(new JacksonProperties()));
        // then
        assertThat(converter.getMappingContext(), is(notNullValue()));
        assertThat(converter.getMappingContext(), is(sameInstance(mappingContext)));
    }
}
