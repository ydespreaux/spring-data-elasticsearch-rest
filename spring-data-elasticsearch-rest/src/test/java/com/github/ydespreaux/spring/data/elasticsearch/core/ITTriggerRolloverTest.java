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

import com.github.ydespreaux.spring.data.elasticsearch.Versions;
import com.github.ydespreaux.spring.data.elasticsearch.client.ClientLoggerAspect;
import com.github.ydespreaux.spring.data.elasticsearch.configuration.ElasticsearchConfigurationSupport;
import com.github.ydespreaux.spring.data.elasticsearch.entities.VehicleEvent;
import com.github.ydespreaux.spring.data.elasticsearch.repositories.rollover.VehicleEventRepository;
import com.github.ydespreaux.spring.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import com.github.ydespreaux.testcontainers.elasticsearch.ElasticsearchContainer;
import org.elasticsearch.common.geo.GeoPoint;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;


@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        RestClientAutoConfiguration.class,
        ITTriggerRolloverTest.ElasticsearchConfiguration.class})
@Profile({"test-no-template", "test-trigger-rollover"})
public class ITTriggerRolloverTest {

    @ClassRule
    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer(Versions.ELASTICSEARCH_VERSION);

    @Autowired
    private VehicleEventRepository repository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Test
    public void trigger() throws InterruptedException {
        this.elasticsearchOperations.index(VehicleEvent.builder()
                .vehicleId("v-101")
                .location(new GeoPoint(40, 70))
                .time(LocalDateTime.now(Clock.systemUTC())).build(), VehicleEvent.class);
        this.elasticsearchOperations.index(VehicleEvent.builder()
                .vehicleId("v-001")
                .location(new GeoPoint(23.2511, 60.1898))
                .time(LocalDateTime.of(2019, 12, 31, 19, 31, 28))
                .build(), VehicleEvent.class);
        TimeUnit.SECONDS.sleep(3);
        this.elasticsearchOperations.indexExists("vehicles-event-000001");
        this.elasticsearchOperations.indexExists("vehicles-event-000002");
    }

    @Configuration
    @EnableAspectJAutoProxy
    @EnableAutoConfiguration
    @EnableElasticsearchRepositories(
            basePackages = "com.github.ydespreaux.spring.data.elasticsearch.repositories.rollover")
    static class ElasticsearchConfiguration extends ElasticsearchConfigurationSupport {

        @Bean
        ClientLoggerAspect clientLoggerAspect() {
            return new ClientLoggerAspect();
        }
    }

}
