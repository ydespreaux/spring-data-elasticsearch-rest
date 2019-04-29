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

import org.elasticsearch.client.indices.CreateIndexRequest;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.ArrayList;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class IndexBuilderTest {

    @Test
    void createIndexRequestToInitializeGivenResource() {
        // given
        CreateIndexBuilder builder = new CreateIndexBuilder().name("music").source(new ClassPathResource("indices/music.index"));
        CreateIndexRequest request = builder.build();
        assertThat(request.index(), is(equalTo(builder.name())));
        assertThat(new ArrayList<>(request.aliases()).get(0).name(), is(equalTo("musics")));
        assertThat(request.settings().get("number_of_shards"), is(equalTo("1")));
        assertThat(request.settings().get("refresh_interval"), is(equalTo("1s")));
        assertThat(request.settings().get("number_of_replicas"), is(equalTo("1")));
        assertThat(request.settings().get("store.TYPE"), is(equalTo("fs")));
        assertThat(request.mappings(), is(notNullValue()));
    }

    @Test
    void createIndexRequestToInitializeGivenNoResource() {
        // given
        CreateIndexBuilder builder = new CreateIndexBuilder().name("index_name").sources(Collections.emptyList());
        CreateIndexRequest request = builder.build();
        assertThat(request.aliases().isEmpty(), is(true));
        assertThat(request.settings().isEmpty(), is(true));
        assertThat(request.mappings(), is(nullValue()));
    }
}
