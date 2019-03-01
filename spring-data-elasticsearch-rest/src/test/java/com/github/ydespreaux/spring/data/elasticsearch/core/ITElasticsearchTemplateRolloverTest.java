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

import com.github.ydespreaux.spring.data.elasticsearch.AbstractElasticsearchTest;
import com.github.ydespreaux.spring.data.elasticsearch.Versions;
import com.github.ydespreaux.spring.data.elasticsearch.client.ClientLoggerAspect;
import com.github.ydespreaux.spring.data.elasticsearch.configuration.ElasticsearchConfigurationSupport;
import com.github.ydespreaux.spring.data.elasticsearch.entities.VehicleEvent;
import com.github.ydespreaux.spring.data.elasticsearch.repositories.rollover.VehicleEventRepository;
import com.github.ydespreaux.spring.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import com.github.ydespreaux.testcontainers.elasticsearch.ElasticsearchContainer;
import org.elasticsearch.common.geo.GeoPoint;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        RestClientAutoConfiguration.class,
        ITElasticsearchTemplateRolloverTest.ElasticsearchConfiguration.class})
@Profile("test-no-template")
public class ITElasticsearchTemplateRolloverTest extends AbstractElasticsearchTest<VehicleEvent> {

    @ClassRule
    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer(Versions.ELASTICSEARCH_VERSION);

    public ITElasticsearchTemplateRolloverTest() {
        super(VehicleEvent.class);
    }

    @Autowired
    private VehicleEventRepository repository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Before
    public void onSetup() {
        cleanData();
    }

    @Test
    public void findById() {
        List<VehicleEvent> vehicles = insertData();
        VehicleEvent myVehicule = vehicles.get(0);
        Optional<VehicleEvent> vehicleEvent = repository.findById(myVehicule.getDocumentId());
        assertThat(vehicleEvent.isPresent(), is(true));
    }

    @Test
    public void save() {
        VehicleEvent vehicle_1 = repository.save(VehicleEvent.builder()
                .vehicleId("v-100")
                .location(new GeoPoint(40, 70))
                .time(LocalDateTime.now(Clock.systemUTC())).build());
        assertThat(vehicle_1.getDocumentId(), is(notNullValue()));
        assertThat(vehicle_1.getVersion(), is(equalTo(1L)));
        assertThat(vehicle_1.getIndexName(), is(startsWith("vehicles-event-")));
    }

    @Test
    public void deleteByIdFromIndexReader() {
        List<VehicleEvent> vehicles = insertData();
        VehicleEvent myVehicule = vehicles.get(0);
        this.repository.deleteById(myVehicule.getDocumentId());
        repository.refresh();
        assertThat(repository.findById(myVehicule.getDocumentId()).isPresent(), is(true));
    }

    @Test
    public void deleteById() {
        VehicleEvent vehicle_1 = repository.save(VehicleEvent.builder()
                .vehicleId("v-101")
                .location(new GeoPoint(40, 70))
                .time(LocalDateTime.now(Clock.systemUTC())).build());
        repository.refresh();
        this.repository.deleteById(vehicle_1.getDocumentId());
        repository.refresh();
        assertThat(repository.findById(vehicle_1.getDocumentId()).isPresent(), is(false));
    }

    @Override
    protected List<VehicleEvent> generateData() {
        // Insert data
        List<VehicleEvent> vehicles = new ArrayList<>(5);
        vehicles.add(VehicleEvent.builder()
                .vehicleId("v-001")
                .location(new GeoPoint(23.251, 60.189))
                .time(LocalDateTime.of(2019, 12, 31, 19, 30, 28))
                .build());
        vehicles.add(VehicleEvent.builder()
                .vehicleId("v-001")
                .location(new GeoPoint(23.2511, 60.1898))
                .time(LocalDateTime.of(2019, 12, 31, 19, 31, 28))
                .build());
        vehicles.add(VehicleEvent.builder()
                .vehicleId("v-002")
                .location(new GeoPoint(40, 70))
                .time(LocalDateTime.of(2019, 2, 12, 10, 25, 28))
                .build());
        vehicles.add(VehicleEvent.builder()
                .vehicleId("v-002")
                .location(new GeoPoint(40.0001, 70.001))
                .time(LocalDateTime.of(2019, 2, 12, 10, 25, 29))
                .build());
        vehicles.add(VehicleEvent.builder()
                .vehicleId("v-002")
                .location(new GeoPoint(40.0002, 70.0002))
                .time(LocalDateTime.of(2019, 2, 12, 10, 25, 30))
                .build());
        return vehicles;
    }

    /**
     * @param tryCount
     * @return
     */
    @Override
    protected List<VehicleEvent> insertData(int tryCount) {
        List<VehicleEvent> data = generateData();
        if (data != null && !data.isEmpty()) {
            elasticsearchOperations.bulkIndex(Arrays.asList(data.get(0), data.get(1)), VehicleEvent.class);
            elasticsearchOperations.refresh(VehicleEvent.class);
            elasticsearchOperations.rolloverIndex(VehicleEvent.class);
            elasticsearchOperations.bulkIndex(Arrays.asList(data.get(2), data.get(3)), VehicleEvent.class);
            elasticsearchOperations.refresh(VehicleEvent.class);
            elasticsearchOperations.rolloverIndex(VehicleEvent.class);
            elasticsearchOperations.bulkIndex(Arrays.asList(data.get(4)), VehicleEvent.class);
            elasticsearchOperations.refresh(VehicleEvent.class);
            elasticsearchOperations.rolloverIndex(VehicleEvent.class);
        }
        return data;
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
