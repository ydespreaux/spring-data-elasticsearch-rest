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

import com.github.ydespreaux.spring.data.elasticsearch.client.ClientLoggerAspect;
import com.github.ydespreaux.spring.data.elasticsearch.configuration.ElasticsearchConfigurationSupport;
import com.github.ydespreaux.spring.data.elasticsearch.core.ElasticsearchOperations;
import com.github.ydespreaux.spring.data.elasticsearch.core.completion.Completion;
import com.github.ydespreaux.spring.data.elasticsearch.core.completion.StringSuggestExtractor;
import com.github.ydespreaux.spring.data.elasticsearch.entities.Music;
import com.github.ydespreaux.spring.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import com.github.ydespreaux.testcontainers.elasticsearch.ElasticsearchContainer;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        RestClientAutoConfiguration.class,
        ITElasticsearchTemplateCompletionTest.ElasticsearchConfiguration.class})
@Profile("test-no-template")
public class ITElasticsearchTemplateCompletionTest extends AbstractElasticsearchTest<Music> {


    @ClassRule
    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer("6.4.2");

    public ITElasticsearchTemplateCompletionTest() {
        super(Music.class);
    }

    @Autowired
    private ElasticsearchOperations template;

    @Override
    protected List<Music> generateData() {
        return Arrays.asList(
                Music.builder()
                        .id("1")
                        .title("Nevermind")
                        .suggest(Completion.builder()
                                .input(new String[]{"Nevermind", "Nirvana"})
                                .build())
                        .build(),
                Music.builder()
                        .id("2")
                        .title("Thriller")
                        .suggest(Completion.builder()
                                .input(new String[]{"Thriller", "Mickael Jackson"})
                                .build())
                        .build(),
                Music.builder()
                        .id("3")
                        .title("Revolution")
                        .suggest(Completion.builder()
                                .input(new String[]{"Revolution", "Muse"})
                                .build())
                        .build()
        );
    }

    @Before
    public void onSetup() {
        this.cleanAndInsertData();
    }

    @Test
    public void shouldFindSuggestionsUsingCompletion() {
        SuggestionBuilder completionSuggestionFuzzyBuilder = SuggestBuilders.completionSuggestion("suggest").prefix("m", Fuzziness.AUTO);
        final List<String> suggestions = template.suggest(new SuggestBuilder().addSuggestion("test-suggest", completionSuggestionFuzzyBuilder), Music.class, new StringSuggestExtractor());
        assertThat(suggestions.size(), is(2));
        assertThat(suggestions.get(0), is(equalTo("Mickael Jackson")));
        assertThat(suggestions.get(1), is(equalTo("Muse")));
    }

    @Test
    public void shouldFindSuggestionsUsingSearch() {
        SuggestionBuilder completionSuggestionFuzzyBuilder = SuggestBuilders.completionSuggestion("suggest").prefix("m", Fuzziness.AUTO);
        SearchResponse response = template.suggest(new SuggestBuilder().addSuggestion("test-suggest", completionSuggestionFuzzyBuilder), "musics");
        final List<String> suggestions = new StringSuggestExtractor().extract(response);
        assertThat(suggestions.size(), is(2));
        assertThat(suggestions.get(0), is(equalTo("Mickael Jackson")));
        assertThat(suggestions.get(1), is(equalTo("Muse")));
    }

    @Configuration
    @EnableAspectJAutoProxy
    @EnableAutoConfiguration
    @EnableElasticsearchRepositories(
            basePackages = "com.github.ydespreaux.spring.data.elasticsearch.repositories.completion")
    static class ElasticsearchConfiguration extends ElasticsearchConfigurationSupport {

        @Bean
        ClientLoggerAspect clientLoggerAspect() {
            return new ClientLoggerAspect();
        }
    }
}
