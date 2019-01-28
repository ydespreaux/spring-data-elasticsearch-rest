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

package com.github.ydespreaux.spring.data.elasticsearch.repository.support;

import com.github.ydespreaux.spring.data.elasticsearch.configuration.ElasticsearchRolloverConfiguration;
import com.github.ydespreaux.spring.data.elasticsearch.core.ElasticsearchOperations;
import com.github.ydespreaux.spring.data.elasticsearch.entities.VehicleEvent;
import com.github.ydespreaux.spring.data.elasticsearch.repositories.rollover.VehicleEventRepository;
import org.elasticsearch.common.geo.GeoPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        RestClientAutoConfiguration.class,
        ElasticsearchRolloverConfiguration.class})
@Profile("test-no-template")
public class ITVehicleEventRepositoryTest {

    private static final Integer CRON_DELAY_SECONDS = 4;

//    static {
//        System.setProperty("spring.elasticsearch.rest.uris", "http://localhost:9200");
//    }

    @Autowired
    private VehicleEventRepository repository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    private List<VehicleEvent> vehicles;

    //    @Test
    public void save() throws InterruptedException {
        VehicleEvent vehicle_1 = repository.save(VehicleEvent.builder()
                .vehicleId("v-001")
                .location(new GeoPoint(40, 70))
                .time(LocalDateTime.now(Clock.systemUTC())).build());

        assertThat(vehicle_1.getDocumentId(), is(notNullValue()));
        assertThat(vehicle_1.getVersion(), is(equalTo(1L)));
        assertThat(vehicle_1.getIndexName(), is(equalTo("vehicles-event-000001")));

        TimeUnit.SECONDS.sleep(CRON_DELAY_SECONDS);

        VehicleEvent vehicle_2 = repository.save(VehicleEvent.builder()
                .documentId("2")
                .vehicleId("v-002")
                .location(new GeoPoint(40, 70))
                .time(LocalDateTime.now(Clock.systemUTC())).build());

        assertThat(vehicle_2.getDocumentId(), is(equalTo("2")));
        assertThat(vehicle_2.getVersion(), is(equalTo(1L)));
        assertThat(vehicle_2.getIndexName(), is(equalTo("vehicles-event-000002")));
    }

    @Test
    public void findById() throws InterruptedException {
        this.vehicles = prepareData();
    }


    private List<VehicleEvent> prepareData() throws InterruptedException {
        // Clean data
        this.repository.deleteAll();
//        ElasticsearchPersistentEntity<VehicleEvent> persistentEntity = this.elasticsearchOperations.getPersistentEntityFor(VehicleEvent.class);
//        this.elasticsearchOperations.deleteIndexByAlias(persistentEntity.getAliasOrIndexName());
//        this.elasticsearchOperations.createRolloverIndexWithSettingsAndMapping(persistentEntity.getRolloverConfig(), persistentEntity.getIndexName(null), persistentEntity.getIndexPath());
        // Insert data
        List<VehicleEvent> vehicles = new ArrayList<>(5);
        vehicles.add(repository.save(VehicleEvent.builder()
                .vehicleId("v-001")
                .location(new GeoPoint(23.251, 60.189))
                .time(LocalDateTime.of(2019, 12, 31, 19, 30, 28))
                .build()));
        TimeUnit.SECONDS.sleep(CRON_DELAY_SECONDS);
        vehicles.add(repository.save(VehicleEvent.builder()
                .vehicleId("v-001")
                .location(new GeoPoint(23.2511, 60.1898))
                .time(LocalDateTime.of(2019, 12, 31, 19, 31, 28))
                .build()));
        vehicles.add(repository.save(VehicleEvent.builder()
                .vehicleId("v-002")
                .location(new GeoPoint(40, 70))
                .time(LocalDateTime.of(2019, 2, 12, 10, 25, 28))
                .build()));
        TimeUnit.SECONDS.sleep(CRON_DELAY_SECONDS);
        vehicles.add(repository.save(VehicleEvent.builder()
                .vehicleId("v-002")
                .location(new GeoPoint(40.0001, 70.001))
                .time(LocalDateTime.of(2019, 2, 12, 10, 25, 29))
                .build()));
        TimeUnit.SECONDS.sleep(CRON_DELAY_SECONDS);
        vehicles.add(repository.save(VehicleEvent.builder()
                .vehicleId("v-002")
                .location(new GeoPoint(40.0002, 70.0002))
                .time(LocalDateTime.of(2019, 2, 12, 10, 25, 30))
                .build()));
        this.repository.refresh();
        return vehicles;
    }
}
