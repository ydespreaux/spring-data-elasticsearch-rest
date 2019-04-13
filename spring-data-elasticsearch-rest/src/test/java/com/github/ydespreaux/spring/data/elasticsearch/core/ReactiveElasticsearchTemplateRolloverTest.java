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
import com.github.ydespreaux.spring.data.elasticsearch.configuration.ReactiveElasticsearchConfiguration;
import com.github.ydespreaux.spring.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import com.github.ydespreaux.spring.data.elasticsearch.entities.VehicleEvent;
import com.github.ydespreaux.spring.data.elasticsearch.reactive.repositories.rollover.ReactiveVehicleEventRepository;
import com.github.ydespreaux.spring.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;
import com.github.ydespreaux.testcontainers.elasticsearch.ElasticsearchContainer;
import org.elasticsearch.common.UUIDs;
import org.elasticsearch.common.geo.GeoPoint;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@Tag("integration")
@DirtiesContext
@SpringBootTest(classes = {
        RestClientAutoConfiguration.class,
        ReactiveElasticsearchTemplateRolloverTest.ElasticsearchConfiguration.class})
@Profile("test-no-template")
@Testcontainers
public class ReactiveElasticsearchTemplateRolloverTest {

    @Container
    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer(Versions.ELASTICSEARCH_VERSION);

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ReactiveVehicleEventRepository repository;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ReactiveElasticsearchOperations reactiveElasticsearchOperations;

    @BeforeEach
    void onSetup() {
        cleanData();
    }

    @Test
    void findById() {
        List<VehicleEvent> vehicles = insertData();
        VehicleEvent myVehicule = vehicles.get(0);
        StepVerifier.create(repository.findById(myVehicule.getDocumentId()))
                .consumeNextWith(vehicle -> {
                    assertThat(vehicle.getDocumentId(), is(equalTo(myVehicule.getDocumentId())));
                    assertThat(vehicle.getDocumentId(), is(notNullValue()));
                    assertThat(vehicle.getVersion(), is(equalTo(1L)));
                    assertThat(vehicle.getIndexName(), is(startsWith("vehicles-event-000001")));
                })
                .verifyComplete();
    }

    @Test
    void save() {
        StepVerifier.create(repository.save(VehicleEvent.builder()
                .vehicleId("v-100")
                .location(new GeoPoint(40, 70))
                .time(LocalDateTime.now(Clock.systemUTC())).build()))
                .consumeNextWith(vehicle -> {
                    assertThat(vehicle.getDocumentId(), is(notNullValue()));
                    assertThat(vehicle.getVersion(), is(equalTo(1L)));
                    assertThat(vehicle.getIndexName(), is(startsWith("vehicles-event-")));
                })
                .verifyComplete();
    }

    @Test
    void deleteByIdFromIndexReader() {
        List<VehicleEvent> vehicles = insertData();
        VehicleEvent myVehicule = vehicles.get(0);
        StepVerifier.create(this.repository.deleteById(myVehicule.getDocumentId()))
                .verifyComplete();
        StepVerifier.create(repository.refresh()).verifyComplete();
        StepVerifier.create(repository.findById(myVehicule.getDocumentId()))
                .expectNextCount(1L)
                .verifyComplete();
    }

    @Test
    void deleteById() {
        String documentID = UUIDs.randomBase64UUID();
        StepVerifier.create(repository.save(VehicleEvent.builder()
                .documentId(documentID)
                .vehicleId("v-101")
                .location(new GeoPoint(40, 70))
                .time(LocalDateTime.now(Clock.systemUTC())).build())
                .and(repository.refresh())
                .and(repository.deleteById(documentID))
                .and(repository.refresh())
                .and(repository.findById(documentID)))
                .verifyComplete();
    }

    protected List<VehicleEvent> generateData() {
        // Insert data
        List<VehicleEvent> vehicles = new ArrayList<>(5);
        vehicles.add(VehicleEvent.builder()
                .documentId(UUIDs.base64UUID())
                .vehicleId("v-001")
                .location(new GeoPoint(23.251, 60.189))
                .time(LocalDateTime.of(2019, 12, 31, 19, 30, 28))
                .build());
        vehicles.add(VehicleEvent.builder()
                .documentId(UUIDs.base64UUID())
                .vehicleId("v-001")
                .location(new GeoPoint(23.2511, 60.1898))
                .time(LocalDateTime.of(2019, 12, 31, 19, 31, 28))
                .build());
        vehicles.add(VehicleEvent.builder()
                .documentId(UUIDs.base64UUID())
                .vehicleId("v-002")
                .location(new GeoPoint(40, 70))
                .time(LocalDateTime.of(2019, 2, 12, 10, 25, 28))
                .build());
        vehicles.add(VehicleEvent.builder()
                .documentId(UUIDs.base64UUID())
                .vehicleId("v-002")
                .location(new GeoPoint(40.0001, 70.001))
                .time(LocalDateTime.of(2019, 2, 12, 10, 25, 29))
                .build());
        vehicles.add(VehicleEvent.builder()
                .documentId(UUIDs.base64UUID())
                .vehicleId("v-002")
                .location(new GeoPoint(40.0002, 70.0002))
                .time(LocalDateTime.of(2019, 2, 12, 10, 25, 30))
                .build());
        return vehicles;
    }

    /**
     * @return
     */
    protected List<VehicleEvent> insertData() {
        List<VehicleEvent> data = generateData();
        if (data != null && !data.isEmpty()) {
            StepVerifier.create(reactiveElasticsearchOperations.bulkIndex(Arrays.asList(data.get(0), data.get(1)), VehicleEvent.class)).expectNextCount(2L).verifyComplete();
            StepVerifier.create(reactiveElasticsearchOperations.refresh(VehicleEvent.class)).verifyComplete();
            StepVerifier.create(reactiveElasticsearchOperations.rolloverIndex(VehicleEvent.class)).expectNextCount(1L).verifyComplete();
            StepVerifier.create(reactiveElasticsearchOperations.bulkIndex(Arrays.asList(data.get(2), data.get(3)), VehicleEvent.class)).expectNextCount(2L).verifyComplete();
            StepVerifier.create(reactiveElasticsearchOperations.refresh(VehicleEvent.class)).verifyComplete();
            StepVerifier.create(reactiveElasticsearchOperations.rolloverIndex(VehicleEvent.class)).expectNextCount(1L).verifyComplete();
            StepVerifier.create(reactiveElasticsearchOperations.bulkIndex(Arrays.asList(data.get(4)), VehicleEvent.class)).expectNextCount(1L).verifyComplete();
            StepVerifier.create(reactiveElasticsearchOperations.refresh(VehicleEvent.class)).verifyComplete();
            StepVerifier.create(reactiveElasticsearchOperations.rolloverIndex(VehicleEvent.class)).expectNextCount(1L).verifyComplete();
        }
        return data;
    }

    protected void cleanData() {
        ElasticsearchPersistentEntity<VehicleEvent> persistentEntity = reactiveElasticsearchOperations.getPersistentEntityFor(VehicleEvent.class);
        if (persistentEntity.getAlias() != null) {
            StepVerifier.create(reactiveElasticsearchOperations.deleteIndexByAlias(persistentEntity.getAlias().name())).expectNextCount(1L).verifyComplete();
        } else {
            StepVerifier.create(reactiveElasticsearchOperations.deleteIndexByName(persistentEntity.getIndexName())).expectNextCount(1L).verifyComplete();
        }
        StepVerifier.create(reactiveElasticsearchOperations.refresh(VehicleEvent.class)).verifyComplete();
        StepVerifier.create(reactiveElasticsearchOperations.createIndex(VehicleEvent.class)).expectNext(true).verifyComplete();
        StepVerifier.create(reactiveElasticsearchOperations.refresh(VehicleEvent.class)).verifyComplete();
    }

    @Configuration
    @EnableAspectJAutoProxy
    @EnableAutoConfiguration
    @EnableReactiveElasticsearchRepositories(
            basePackages = "com.github.ydespreaux.spring.data.elasticsearch.reactive.repositories.rollover")
    static class ElasticsearchConfiguration extends ReactiveElasticsearchConfiguration {

        @Bean
        ClientLoggerAspect clientLoggerAspect() {
            return new ClientLoggerAspect();
        }
    }

}
