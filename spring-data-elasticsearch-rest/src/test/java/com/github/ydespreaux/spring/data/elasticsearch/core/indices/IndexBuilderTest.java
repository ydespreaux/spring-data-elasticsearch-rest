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

package com.github.ydespreaux.spring.data.elasticsearch.core.indices;

import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.Map;
import java.util.Set;

public class IndexBuilderTest {

    @Test
    public void shouldFailToInitializeGivenMappingContextIsNull() {
        // given
        CreateIndexBuilder builder = new CreateIndexBuilder().name("books").source(new ClassPathResource("indices/book.index"));
        CreateIndexRequest request = builder.build();
        Set<Alias> aliases = request.aliases();
        Settings settings = request.settings();
        Map<String, String> mappings = request.mappings();
        System.out.println(mappings);
    }

}
