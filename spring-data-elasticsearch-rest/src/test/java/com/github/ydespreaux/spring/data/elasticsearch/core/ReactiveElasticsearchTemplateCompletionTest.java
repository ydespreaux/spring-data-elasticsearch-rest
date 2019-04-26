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
import com.github.ydespreaux.spring.data.elasticsearch.configuration.ReactiveElasticsearchConfiguration;
import com.github.ydespreaux.spring.data.elasticsearch.core.completion.reactive.ReactiveStringSuggestExtractor;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.SuggestQuery;
import com.github.ydespreaux.spring.data.elasticsearch.entities.Music;
import com.github.ydespreaux.spring.data.elasticsearch.entities.MusicInfo;
import com.github.ydespreaux.spring.data.elasticsearch.reactive.repositories.completion.ReactiveMusicRepository;
import com.github.ydespreaux.spring.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;
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
import reactor.test.StepVerifier;

@Tag("integration-nested")
@SpringBootTest(classes = {
        RestClientAutoConfiguration.class,
        ReactiveElasticsearchTemplateCompletionTest.ElasticsearchConfiguration.class})
@Profile("test-no-template")
public class ReactiveElasticsearchTemplateCompletionTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ReactiveElasticsearchOperations template;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ReactiveMusicRepository repository;

    @Test
    void shouldFindSuggestionsUsingCompletion() {
        SuggestionBuilder completionSuggestionFuzzyBuilder = SuggestBuilders.completionSuggestion("suggest").prefix("m", Fuzziness.AUTO);
        SuggestQuery query = new SuggestQuery(new SuggestBuilder().addSuggestion("test-suggest", completionSuggestionFuzzyBuilder));
        StepVerifier.create(template.suggest(query, Music.class, new ReactiveStringSuggestExtractor()).sort(String::compareTo))
                .expectNext("Mickael Jackson", "Muse")
                .verifyComplete();
    }

    @Test
    void shouldFindSuggestionsUsingSearch() {
        SuggestionBuilder completionSuggestionFuzzyBuilder = SuggestBuilders.completionSuggestion("suggest").prefix("m", Fuzziness.AUTO);
        SuggestQuery query = new SuggestQuery(new SuggestBuilder().addSuggestion("test-suggest", completionSuggestionFuzzyBuilder));
        query.addIndices("musics");
        query.addTypes("music");
        StepVerifier.create(template.suggest(query, new ReactiveStringSuggestExtractor()).sort(String::compareTo))
                .expectNext("Mickael Jackson", "Muse")
                .verifyComplete();
    }

    @Test
    void shouldFindSuggestionsWithRepository() {
        StepVerifier.create(this.repository.suggest("m").map(Music::getTitle).sort(String::compareTo))
                .expectNext("Revolution", "Thriller")
                .verifyComplete();
    }

    @Test
    void shouldFindSuggestionsWithProjection() {
        StepVerifier.create(this.repository.suggest("m", MusicInfo.class).map(MusicInfo::getTitle).sort(String::compareTo))
                .expectNext("Revolution", "Thriller")
                .verifyComplete();
    }

    @Configuration
    @EnableAspectJAutoProxy
    @EnableAutoConfiguration
    @EnableReactiveElasticsearchRepositories(
            basePackages = "com.github.ydespreaux.spring.data.elasticsearch.reactive.repositories.completion")
    static class ElasticsearchConfiguration extends ReactiveElasticsearchConfiguration {

        @Bean
        ClientLoggerAspect clientLoggerAspect() {
            return new ClientLoggerAspect();
        }
    }
}
