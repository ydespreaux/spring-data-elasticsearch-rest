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

package com.github.ydespreaux.spring.data.elasticsearch.core;

import com.github.ydespreaux.spring.data.elasticsearch.client.ClientLoggerAspect;
import com.github.ydespreaux.spring.data.elasticsearch.configuration.ElasticsearchConfigurationSupport;
import com.github.ydespreaux.spring.data.elasticsearch.core.completion.StringSuggestExtractor;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.SuggestQuery;
import com.github.ydespreaux.spring.data.elasticsearch.entities.Music;
import com.github.ydespreaux.spring.data.elasticsearch.entities.MusicInfo;
import com.github.ydespreaux.spring.data.elasticsearch.repositories.completion.MusicRepository;
import com.github.ydespreaux.spring.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@Tag("integration-nested")
@SpringBootTest(classes = {
        RestClientAutoConfiguration.class,
        ElasticsearchTemplateCompletionTest.ElasticsearchConfiguration.class})
@Profile("test-no-template")
public class ElasticsearchTemplateCompletionTest {

    @Autowired
    private ElasticsearchOperations template;

    @Autowired
    private MusicRepository repository;

    @Test
    void shouldFindSuggestionsUsingCompletion() {
        SuggestionBuilder completionSuggestionFuzzyBuilder = SuggestBuilders.completionSuggestion("suggest").prefix("m", Fuzziness.AUTO);
        SuggestQuery query = new SuggestQuery(new SuggestBuilder().addSuggestion("test-suggest", completionSuggestionFuzzyBuilder));
        final List<String> suggestions = template.suggest(query, Music.class, new StringSuggestExtractor());
        assertThat(suggestions.size(), is(2));
        assertThat(suggestions.get(0), is(equalTo("Mickael Jackson")));
        assertThat(suggestions.get(1), is(equalTo("Muse")));
    }

    @Test
    void shouldFindSuggestionsUsingSearch() {
        SuggestionBuilder completionSuggestionFuzzyBuilder = SuggestBuilders.completionSuggestion("suggest").prefix("m", Fuzziness.AUTO);
        SuggestQuery query = new SuggestQuery(new SuggestBuilder().addSuggestion("test-suggest", completionSuggestionFuzzyBuilder));
        query.addIndices("musics");
        query.addTypes("music");
        List<String> suggestions = template.suggest(query, new StringSuggestExtractor());
        assertThat(suggestions.size(), is(2));
        assertThat(suggestions.get(0), is(equalTo("Mickael Jackson")));
        assertThat(suggestions.get(1), is(equalTo("Muse")));
    }

    @Test
    void shouldFindSuggestionsWithRepository() {
        List<Music> suggestions = this.repository.suggest("m");
        assertThat(suggestions.size(), is(2));
        List<String> titles = suggestions.stream().map(Music::getTitle).collect(Collectors.toList());
        assertThat(titles.contains("Revolution"), is(true));
        assertThat(titles.contains("Thriller"), is(true));
    }

    @Test
    void shouldFindSuggestionsWithProjection() {
        List<MusicInfo> suggestions = this.repository.suggest("m", MusicInfo.class);
        assertThat(suggestions.size(), is(2));
        List<String> titles = suggestions.stream().map(MusicInfo::getTitle).collect(Collectors.toList());
        assertThat(titles.contains("Revolution"), is(true));
        assertThat(titles.contains("Thriller"), is(true));
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
