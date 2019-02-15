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

package com.github.ydespreaux.spring.data.elasticsearch;

import com.github.ydespreaux.spring.data.elasticsearch.core.ITElasticsearchTemplateCompletionTest;
import com.github.ydespreaux.spring.data.elasticsearch.repository.support.ITArticleRepositoryTest;
import com.github.ydespreaux.spring.data.elasticsearch.repository.support.ITBookRepositoryTest;
import com.github.ydespreaux.spring.data.elasticsearch.repository.support.ITCityRepositoryTest;
import com.github.ydespreaux.spring.data.elasticsearch.repository.support.ITProductRepositoryTest;
import com.github.ydespreaux.testcontainers.elasticsearch.ElasticsearchContainer;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ITArticleRepositoryTest.class,
        ITBookRepositoryTest.class,
        ITCityRepositoryTest.class,
        ITProductRepositoryTest.class,
        ITElasticsearchTemplateCompletionTest.class
})
public class ITQuerySuiteTest {

    @ClassRule
    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer("6.4.2")
            .withConfigDirectory("elastic-config")
            .withFileInitScript("scripts/queries.script");

}
