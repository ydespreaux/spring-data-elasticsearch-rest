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

import com.github.ydespreaux.spring.data.elasticsearch.client.ClientLoggerAspect;
import com.github.ydespreaux.spring.data.elasticsearch.configuration.ElasticsearchConfigurationSupport;
import com.github.ydespreaux.spring.data.elasticsearch.entities.City;
import com.github.ydespreaux.spring.data.elasticsearch.repositories.template.CityRepository;
import com.github.ydespreaux.spring.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.elasticsearch.common.geo.GeoPoint;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Box;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Tag("integration-nested")
@SpringBootTest(classes = {
        RestClientAutoConfiguration.class,
        CityRepositoryTest.ElasticsearchConfiguration.class})
@Profile("test-no-template")
public class CityRepositoryTest {

    @Autowired
    private CityRepository repository;

    private static final String montpellier = "1";
    private static final String mauguio = "2";
    private static final String castries = "3";
    private static final String vendargues = "4";
    private static final String sommieres = "5";

    private static final GeoPoint castriesLocation = new GeoPoint(43.677644, 3.985277);

    private GeoPoint topLeftBox = new GeoPoint(43.679112, 3.958448);
    private GeoPoint bottomRightBox = new GeoPoint(43.608526, 4.021915);

    @Test
    void countByLocationNear() {
        assertThat(this.repository.countByLocationNear(transformLocation(topLeftBox), transformLocation(bottomRightBox)), is(equalTo(3L)));
    }

    @Test
    void countByLocationNearWithBox() {
        assertThat(this.repository.countByLocationNear(transformBox(topLeftBox, bottomRightBox)), is(equalTo(3L)));
    }

    @Test
    void countByLocationNearWithPoint() {
        assertThat(this.repository.countByLocationNear(transformLocationToPoint(topLeftBox), transformLocationToPoint(bottomRightBox)), is(equalTo(3L)));
    }

    @Test
    void countByLocationNearWithGeoHash() {
        assertThat(this.repository.countByLocationNear(transformLocationToGeoHash(topLeftBox), transformLocationToGeoHash(bottomRightBox)), is(equalTo(3L)));
    }

    @Test
    void countByLocationNearWithGeoPoint() {
        assertThat(this.repository.countByLocationNear(topLeftBox, bottomRightBox), is(equalTo(3L)));
    }

    @Test
    void existsByLocationNear() {
        assertThat(this.repository.existsByLocationNear(transformLocation(topLeftBox), transformLocation(bottomRightBox)), is(true));
    }

    @Test
    void existsByLocationNearWithBox() {
        assertThat(this.repository.existsByLocationNear(transformBox(topLeftBox, bottomRightBox)), is(true));
    }

    @Test
    void existsByLocationNearWithPoint() {
        assertThat(this.repository.existsByLocationNear(transformLocationToPoint(topLeftBox), transformLocationToPoint(bottomRightBox)), is(true));
    }

    @Test
    void existsByLocationNearWithGeoHash() {
        assertThat(this.repository.existsByLocationNear(transformLocationToGeoHash(topLeftBox), transformLocationToGeoHash(bottomRightBox)), is(true));
    }

    @Test
    void existsByLocationNearWithGeoPoint() {
        assertThat(this.repository.existsByLocationNear(topLeftBox, bottomRightBox), is(true));
    }

    @Test
    void findByLocationNear() {
        List<City> cities = this.repository.findCityByLocationNearOrderByNameAsc(transformLocation(topLeftBox), transformLocation(bottomRightBox));
        assertThat(cities.size(), is(equalTo(3)));
        assertThat(cities.get(0).getId(), is(equalTo(castries)));
        assertThat(cities.get(1).getId(), is(equalTo(mauguio)));
        assertThat(cities.get(2).getId(), is(equalTo(vendargues)));
    }

    @Test
    void findByLocationNearWithBox() {
        List<City> cities = this.repository.findCityByLocationNearOrderByNameAsc(transformBox(topLeftBox, bottomRightBox));
        assertThat(cities.size(), is(equalTo(3)));
        assertThat(cities.get(0).getId(), is(equalTo(castries)));
        assertThat(cities.get(1).getId(), is(equalTo(mauguio)));
        assertThat(cities.get(2).getId(), is(equalTo(vendargues)));
    }

    @Test
    void findByLocationNearWithPoint() {
        List<City> cities = this.repository.findCityByLocationNearOrderByNameAsc(transformLocationToPoint(topLeftBox), transformLocationToPoint(bottomRightBox));
        assertThat(cities.size(), is(equalTo(3)));
        assertThat(cities.get(0).getId(), is(equalTo(castries)));
        assertThat(cities.get(1).getId(), is(equalTo(mauguio)));
        assertThat(cities.get(2).getId(), is(equalTo(vendargues)));
    }

    @Test
    void findByLocationNearWithGeoHash() {
        List<City> cities = this.repository.findCityByLocationNearOrderByNameAsc(transformLocationToGeoHash(topLeftBox), transformLocationToGeoHash(bottomRightBox));
        assertThat(cities.size(), is(equalTo(3)));
        assertThat(cities.get(0).getId(), is(equalTo(castries)));
        assertThat(cities.get(1).getId(), is(equalTo(mauguio)));
        assertThat(cities.get(2).getId(), is(equalTo(vendargues)));
    }

    @Test
    void findByLocationNearWithGeoPoint() {
        List<City> cities = this.repository.findCityByLocationNearOrderByNameAsc(topLeftBox, bottomRightBox);
        assertThat(cities.size(), is(equalTo(3)));
        assertThat(cities.get(0).getId(), is(equalTo(castries)));
        assertThat(cities.get(1).getId(), is(equalTo(mauguio)));
        assertThat(cities.get(2).getId(), is(equalTo(vendargues)));
    }

    @Test
    void findByLocationNearPageable() {
        Page<City> cities = this.repository.findCityByLocationNear(transformLocation(topLeftBox), transformLocation(bottomRightBox),
                PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(3L)));
        assertThat(cities.getContent().get(0).getId(), is(equalTo(castries)));
        assertThat(cities.getContent().get(1).getId(), is(equalTo(mauguio)));
        assertThat(cities.getContent().get(2).getId(), is(equalTo(vendargues)));
    }

    @Test
    void findByLocationNearPageableWithBox() {
        Page<City> cities = this.repository.findCityByLocationNear(transformBox(topLeftBox, bottomRightBox),
                PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(3L)));
        assertThat(cities.getContent().get(0).getId(), is(equalTo(castries)));
        assertThat(cities.getContent().get(1).getId(), is(equalTo(mauguio)));
        assertThat(cities.getContent().get(2).getId(), is(equalTo(vendargues)));
    }

    @Test
    void findByLocationNearPageableWithPoint() {
        Page<City> cities = this.repository.findCityByLocationNear(transformLocationToPoint(topLeftBox), transformLocationToPoint(bottomRightBox),
                PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(3L)));
        assertThat(cities.getContent().get(0).getId(), is(equalTo(castries)));
        assertThat(cities.getContent().get(1).getId(), is(equalTo(mauguio)));
        assertThat(cities.getContent().get(2).getId(), is(equalTo(vendargues)));
    }

    @Test
    void findByLocationNearPageableWithGeoHash() {
        Page<City> cities = this.repository.findCityByLocationNear(transformLocationToGeoHash(topLeftBox), transformLocationToGeoHash(bottomRightBox),
                PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(3L)));
        assertThat(cities.getContent().get(0).getId(), is(equalTo(castries)));
        assertThat(cities.getContent().get(1).getId(), is(equalTo(mauguio)));
        assertThat(cities.getContent().get(2).getId(), is(equalTo(vendargues)));
    }

    @Test
    void findByLocationNearPageableWithGeoPoint() {
        Page<City> cities = this.repository.findCityByLocationNear(topLeftBox, bottomRightBox,
                PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(3L)));
        assertThat(cities.getContent().get(0).getId(), is(equalTo(castries)));
        assertThat(cities.getContent().get(1).getId(), is(equalTo(mauguio)));
        assertThat(cities.getContent().get(2).getId(), is(equalTo(vendargues)));
    }

    @Test
    void countByLocationWithinWithPoint() {
        assertThat(this.repository.countByLocationWithin(transformLocationToPoint(castriesLocation), new Distance(10, Metrics.KILOMETERS)), is(equalTo(3L)));
    }

    @Test
    void countByLocationWithinWithPointAndMiles() {
        assertThat(this.repository.countByLocationWithin(transformLocationToPoint(castriesLocation), new Distance(6.21371, Metrics.MILES)), is(equalTo(3L)));
    }

    @Test
    void countByLocationWithinWithGeoHash() {
        assertThat(this.repository.countByLocationWithin(transformLocationToGeoHash(castriesLocation), "10km"), is(equalTo(3L)));
    }

    @Test
    void countByLocationWithin() {
        assertThat(this.repository.countByLocationWithin(transformLocation(castriesLocation), "10km"), is(equalTo(3L)));
    }

    @Test
    void countByLocationWithinWithGeoPoint() {
        assertThat(this.repository.countByLocationWithin(castriesLocation, "10km"), is(equalTo(3L)));
    }

    @Test
    void existsByLocationWithin() {
        assertThat(this.repository.existsByLocationWithin(transformLocation(castriesLocation), "10km"), is(true));
    }

    @Test
    void existsByLocationWithinGeoHash() {
        assertThat(this.repository.existsByLocationWithin(transformLocationToGeoHash(castriesLocation), "10km"), is(true));
    }

    @Test
    void existsByLocationWithinWithPoint() {
        assertThat(this.repository.existsByLocationWithin(transformLocationToPoint(castriesLocation), new Distance(10, Metrics.KILOMETERS)), is(true));
    }

    @Test
    void existsByLocationWithinWithGeoPoint() {
        assertThat(this.repository.existsByLocationWithin(castriesLocation, "10km"), is(true));
    }

    @Test
    void findByLocationWithin() {
        List<City> cities = this.repository.findCityByLocationWithinOrderByNameAsc(transformLocation(castriesLocation), "10km");
        assertThat(cities.size(), is(equalTo(3)));
        assertThat(cities.get(0).getId(), is(equalTo(castries)));
        assertThat(cities.get(1).getId(), is(equalTo(mauguio)));
        assertThat(cities.get(2).getId(), is(equalTo(vendargues)));
    }

    @Test
    void findByLocationWithinWithGeoHash() {
        List<City> cities = this.repository.findCityByLocationWithinOrderByNameAsc(transformLocationToGeoHash(castriesLocation), "10km");
        assertThat(cities.size(), is(equalTo(3)));
        assertThat(cities.get(0).getId(), is(equalTo(castries)));
        assertThat(cities.get(1).getId(), is(equalTo(mauguio)));
        assertThat(cities.get(2).getId(), is(equalTo(vendargues)));
    }

    @Test
    void findByLocationWithinWithPoint() {
        List<City> cities = this.repository.findCityByLocationWithinOrderByNameAsc(transformLocationToPoint(castriesLocation), new Distance(10, Metrics.KILOMETERS));
        assertThat(cities.size(), is(equalTo(3)));
        assertThat(cities.get(0).getId(), is(equalTo(castries)));
        assertThat(cities.get(1).getId(), is(equalTo(mauguio)));
        assertThat(cities.get(2).getId(), is(equalTo(vendargues)));
    }

    @Test
    void findByLocationWithinWithGeoPoint() {
        List<City> cities = this.repository.findCityByLocationWithinOrderByNameAsc(castriesLocation, "10km");
        assertThat(cities.size(), is(equalTo(3)));
        assertThat(cities.get(0).getId(), is(equalTo(castries)));
        assertThat(cities.get(1).getId(), is(equalTo(mauguio)));
        assertThat(cities.get(2).getId(), is(equalTo(vendargues)));
    }

    @Test
    void findByLocationWithinPageable() {
        Page<City> cities = this.repository.findCityByLocationWithin(transformLocation(castriesLocation), "10km",
                PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(3L)));
        assertThat(cities.getContent().get(0).getId(), is(equalTo(castries)));
        assertThat(cities.getContent().get(1).getId(), is(equalTo(mauguio)));
        assertThat(cities.getContent().get(2).getId(), is(equalTo(vendargues)));
    }

    @Test
    void findByLocationWithinPageableWithGeoHash() {
        Page<City> cities = this.repository.findCityByLocationWithin(transformLocationToGeoHash(castriesLocation), "10km",
                PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(3L)));
        assertThat(cities.getContent().get(0).getId(), is(equalTo(castries)));
        assertThat(cities.getContent().get(1).getId(), is(equalTo(mauguio)));
        assertThat(cities.getContent().get(2).getId(), is(equalTo(vendargues)));
    }

    @Test
    void findByLocationWithinPageableWithPoint() {
        Page<City> cities = this.repository.findCityByLocationWithin(
                transformLocationToPoint(castriesLocation),
                new Distance(10, Metrics.KILOMETERS),
                PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(3L)));
        assertThat(cities.getContent().get(0).getId(), is(equalTo(castries)));
        assertThat(cities.getContent().get(1).getId(), is(equalTo(mauguio)));
        assertThat(cities.getContent().get(2).getId(), is(equalTo(vendargues)));
    }

    @Test
    void findByLocationWithinPageableWithGeoPoint() {
        Page<City> cities = this.repository.findCityByLocationWithin(
                castriesLocation,
                "10km",
                PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(3L)));
        assertThat(cities.getContent().get(0).getId(), is(equalTo(castries)));
        assertThat(cities.getContent().get(1).getId(), is(equalTo(mauguio)));
        assertThat(cities.getContent().get(2).getId(), is(equalTo(vendargues)));
    }

    @Test
    void findById() {
        Optional<City> optional = this.repository.findById(montpellier);
        assertTrue(optional.isPresent());
        assertThat(optional.get().getId(), is(equalTo(montpellier)));
    }

    @Test
    void countByRegion() {
        assertThat(this.repository.countByRegion("SE"), is(equalTo(5L)));
    }

    @Test
    void existsByRegion() {
        assertThat(this.repository.existsByRegion("SE"), is(true));
    }

    @Test
    void existsByRegionNotFound() {
        assertThat(this.repository.existsByRegion("ES"), is(false));
    }

    @Test
    void findCityByRegion() {
        List<City> cities = this.repository.findCityByRegionOrderByNameAsc("SE");
        assertThat(cities.size(), is(equalTo(5)));
        assertThat(cities.get(0).getId(), is(equalTo(castries)));
        assertThat(cities.get(1).getId(), is(equalTo(mauguio)));
        assertThat(cities.get(2).getId(), is(equalTo(montpellier)));
        assertThat(cities.get(3).getId(), is(equalTo(sommieres)));
        assertThat(cities.get(4).getId(), is(equalTo(vendargues)));
    }

    @Test
    void findCityByRegionWithPageable() {
        Page<City> cities = this.repository.findCityByRegion("SE", PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(5L)));
        assertThat(cities.getContent().get(0).getId(), is(equalTo(castries)));
        assertThat(cities.getContent().get(1).getId(), is(equalTo(mauguio)));
        assertThat(cities.getContent().get(2).getId(), is(equalTo(montpellier)));
        assertThat(cities.getContent().get(3).getId(), is(equalTo(sommieres)));
        assertThat(cities.getContent().get(4).getId(), is(equalTo(vendargues)));
    }

    @Test
    void countByPopulationBetween() {
        assertThat(this.repository.countByPopulationBetween(5000L, 10000L), is(equalTo(2L)));
    }

    @Test
    void existsByPopulationBetween() {
        assertThat(this.repository.existsByPopulationBetween(5000L, 10000L), is(true));
    }

    @Test
    void findCityByPopulationBetweenOrderByNameAsc() {
        List<City> cities = this.repository.findCityByPopulationBetweenOrderByNameAsc(5000L, 10000L);
        assertThat(cities.size(), is(equalTo(2)));
        assertThat(cities.get(0).getId(), is(equalTo(castries)));
        assertThat(cities.get(1).getId(), is(equalTo(vendargues)));
    }

    @Test
    void findCityByPopulationBetween() {
        Page<City> cities = this.repository.findCityByPopulationBetween(5000L, 10000L, PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(2L)));
        assertThat(cities.getContent().get(0).getId(), is(equalTo(castries)));
        assertThat(cities.getContent().get(1).getId(), is(equalTo(vendargues)));
    }

    @Test
    void countByPopulationGreaterThanEqual() {
        assertThat(this.repository.countByPopulationGreaterThanEqual(6186L), is(equalTo(3L)));
    }

    @Test
    void existsByPopulationGreaterThanEqual() {
        assertThat(this.repository.existsByPopulationGreaterThanEqual(6186L), is(true));
    }

    @Test
    void findCityByPopulationGreaterThanEqualOrderByNameAsc() {
        List<City> cities = this.repository.findCityByPopulationGreaterThanEqualOrderByNameAsc(6186L);
        assertThat(cities.size(), is(equalTo(3)));
        assertThat(cities.get(0).getId(), is(equalTo(mauguio)));
        assertThat(cities.get(1).getId(), is(equalTo(montpellier)));
        assertThat(cities.get(2).getId(), is(equalTo(vendargues)));
    }

    @Test
    void findCityByPopulationGreaterThanEqual() {
        Page<City> cities = this.repository.findCityByPopulationGreaterThanEqual(6186L, PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(3L)));
        assertThat(cities.getContent().get(0).getId(), is(equalTo(mauguio)));
        assertThat(cities.getContent().get(1).getId(), is(equalTo(montpellier)));
        assertThat(cities.getContent().get(2).getId(), is(equalTo(vendargues)));
    }

    @Test
    void countByPopulationGreaterThan() {
        assertThat(this.repository.countByPopulationGreaterThan(6186L), is(equalTo(2L)));
    }

    @Test
    void existsByPopulationGreaterThan() {
        assertThat(this.repository.existsByPopulationGreaterThan(6186L), is(true));
    }

    @Test
    void findCityByPopulationGreaterThanOrderByNameAsc() {
        List<City> cities = this.repository.findCityByPopulationGreaterThanOrderByNameAsc(6186L);
        assertThat(cities.size(), is(equalTo(2)));
        assertThat(cities.get(0).getId(), is(equalTo(mauguio)));
        assertThat(cities.get(1).getId(), is(equalTo(montpellier)));
    }

    @Test
    void findCityByPopulationGreaterThan() {
        Page<City> cities = this.repository.findCityByPopulationGreaterThan(6186L, PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(2L)));
        assertThat(cities.getContent().get(0).getId(), is(equalTo(mauguio)));
        assertThat(cities.getContent().get(1).getId(), is(equalTo(montpellier)));
    }

    @Test
    void countByPopulationLessThanEqual() {
        assertThat(this.repository.countByPopulationLessThanEqual(4644L), is(equalTo(1L)));
    }

    @Test
    void existsByPopulationLessThanEqual() {
        assertThat(this.repository.existsByPopulationLessThanEqual(4644L), is(true));
    }

    @Test
    void findCityByPopulationLessThanEqualOrderByNameAsc() {
        List<City> cities = this.repository.findCityByPopulationLessThanEqualOrderByNameAsc(4644L);
        assertThat(cities.size(), is(equalTo(1)));
        assertThat(cities.get(0).getId(), is(equalTo(sommieres)));
    }

    @Test
    void findCityByPopulationLessThanEqual() {
        Page<City> cities = this.repository.findCityByPopulationLessThanEqual(4644L, PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(1L)));
        assertThat(cities.getContent().get(0).getId(), is(equalTo(sommieres)));
    }

    @Test
    void countByPopulationLessThan() {
        assertThat(this.repository.countByPopulationLessThan(4644L), is(equalTo(0L)));
    }

    @Test
    void existsByPopulationLessThan() {
        assertThat(this.repository.existsByPopulationLessThan(4644L), is(false));
    }

    @Test
    void findCityByPopulationLessThanOrderByNameAsc() {
        List<City> cities = this.repository.findCityByPopulationLessThanOrderByNameAsc(4644L);
        assertThat(cities.isEmpty(), is(true));
    }

    @Test
    void findCityByPopulationLessThan() {
        Page<City> cities = this.repository.findCityByPopulationLessThan(4644L, PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.hasContent(), is(false));
    }

    private String transformLocation(GeoPoint point) {
        return point.toString();
    }

    private Point transformLocationToPoint(GeoPoint point) {
        return new Point(point.getLat(), point.getLon());
    }

    private String transformLocationToGeoHash(GeoPoint point) {
        return point.geohash();
    }

    private Box transformBox(GeoPoint topLeft, GeoPoint bottomRight) {
        return new Box(transformLocationToPoint(topLeft), transformLocationToPoint(bottomRight));
    }

    @Configuration
    @EnableAspectJAutoProxy
    @EnableAutoConfiguration
    @EnableElasticsearchRepositories(
            basePackages = "com.github.ydespreaux.spring.data.elasticsearch.repositories.template",
            namedQueriesLocation = "classpath:named-queries/*-named-queries.properties")
    static class ElasticsearchConfiguration extends ElasticsearchConfigurationSupport {

        @Bean
        ClientLoggerAspect clientLoggerAspect() {
            return new ClientLoggerAspect();
        }
    }

}
