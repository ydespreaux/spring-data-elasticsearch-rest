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
 *
 */

package com.github.ydespreaux.spring.data.elasticsearch;

import com.github.ydespreaux.spring.data.elasticsearch.core.ReactiveElasticsearchTemplateCompletionTest;
import com.github.ydespreaux.spring.data.elasticsearch.core.ReactiveElasticsearchTemplateQueryTest;
import com.github.ydespreaux.spring.data.elasticsearch.reactive.repository.support.ReactiveAnswerRepositoryTest;
import com.github.ydespreaux.spring.data.elasticsearch.reactive.repository.support.ReactiveArticleRepositoryTest;
import com.github.ydespreaux.spring.data.elasticsearch.reactive.repository.support.ReactiveBookRepositoryTest;
import com.github.ydespreaux.spring.data.elasticsearch.reactive.repository.support.ReactiveProductRepositoryTest;
import com.github.ydespreaux.testcontainers.elasticsearch.ElasticsearchContainer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("integration")
@Testcontainers
public class ReactiveQuerySuiteTest {

    @Container
    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer(Versions.ELASTICSEARCH_VERSION)
            .withConfigDirectory("elastic-config")
            .withFileInitScript("scripts/reactive-queries.script");

    @Nested
    class ReactiveElasticsearchTemplateQueryNested extends ReactiveElasticsearchTemplateQueryTest {
    }

    @Nested
    class ReactiveAnswerRepositoryNested extends ReactiveAnswerRepositoryTest {
    }

    @Nested
    class ReactiveArticleRepositoryNested extends ReactiveArticleRepositoryTest {
    }

    @Nested
    class ReactiveBookRepositoryNested extends ReactiveBookRepositoryTest {
    }

    @Nested
    class ReactiveProductRepositoryNested extends ReactiveProductRepositoryTest {
    }

    @Nested
    class ReactiveElasticsearchTemplateCompletionNested extends ReactiveElasticsearchTemplateCompletionTest {
    }
}
