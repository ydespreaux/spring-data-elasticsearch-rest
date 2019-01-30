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

import com.github.ydespreaux.spring.data.elasticsearch.AbstractElasticsearchTest;
import com.github.ydespreaux.spring.data.elasticsearch.client.ClientLoggerAspect;
import com.github.ydespreaux.spring.data.elasticsearch.configuration.ElasticsearchConfigurationSupport;
import com.github.ydespreaux.spring.data.elasticsearch.core.scroll.ScrolledPage;
import com.github.ydespreaux.spring.data.elasticsearch.entities.City;
import com.github.ydespreaux.spring.data.elasticsearch.repositories.template.CityRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Box;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        RestClientAutoConfiguration.class,
        ITCityRepositoryTest.ElasticsearchConfiguration.class})
public class ITCityRepositoryTest extends AbstractElasticsearchTest<City> {


    @ClassRule
    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer("6.4.2");

    public ITCityRepositoryTest() {
        super(City.class);
    }

    @Autowired
    private CityRepository repository;

    private City castries = null;
    private City mauguio = null;
    private City montpellier = null;
    private City sommieres = null;
    private City vendargues = null;

    private GeoPoint topLeftBox = new GeoPoint(43.679112, 3.958448);
    private GeoPoint bottomRightBox = new GeoPoint(43.608526, 4.021915);

    @Override
    protected List<City> generateData() {
        montpellier = createCity("Montpellier", new GeoPoint(43.613712, 3.872191), 275318L, "SE");
        mauguio = createCity("Mauguio", new GeoPoint(43.615463, 4.009743), 16795L, "SE");
        castries = createCity("Castries", new GeoPoint(43.677644, 3.985277), 6075L, "SE");
        vendargues = createCity("Vendargues", new GeoPoint(43.656904, 3.969566), 6186L, "SE");
        sommieres = createCity("Sommières", new GeoPoint(43.782759, 4.089557), 4644L, "SE");
        return Arrays.asList(montpellier, mauguio, castries, vendargues, sommieres);
    }

    @Before
    public void setUp() {
        cleanAndInsertData();
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

    @Test
    public void countByLocationNear() {
        assertThat(this.repository.countByLocationNear(transformLocation(topLeftBox), transformLocation(bottomRightBox)), is(equalTo(3L)));
    }

    @Test
    public void countByLocationNearWithBox() {
        assertThat(this.repository.countByLocationNear(transformBox(topLeftBox, bottomRightBox)), is(equalTo(3L)));
    }

    @Test
    public void countByLocationNearWithPoint() {
        assertThat(this.repository.countByLocationNear(transformLocationToPoint(topLeftBox), transformLocationToPoint(bottomRightBox)), is(equalTo(3L)));
    }

    @Test
    public void countByLocationNearWithGeoHash() {
        assertThat(this.repository.countByLocationNear(transformLocationToGeoHash(topLeftBox), transformLocationToGeoHash(bottomRightBox)), is(equalTo(3L)));
    }

    @Test
    public void countByLocationNearWithGeoPoint() {
        assertThat(this.repository.countByLocationNear(topLeftBox, bottomRightBox), is(equalTo(3L)));
    }

    @Test
    public void existsByLocationNear() {
        assertThat(this.repository.existsByLocationNear(transformLocation(topLeftBox), transformLocation(bottomRightBox)), is(true));
    }

    @Test
    public void existsByLocationNearWithBox() {
        assertThat(this.repository.existsByLocationNear(transformBox(topLeftBox, bottomRightBox)), is(true));
    }

    @Test
    public void existsByLocationNearWithPoint() {
        assertThat(this.repository.existsByLocationNear(transformLocationToPoint(topLeftBox), transformLocationToPoint(bottomRightBox)), is(true));
    }

    @Test
    public void existsByLocationNearWithGeoHash() {
        assertThat(this.repository.existsByLocationNear(transformLocationToGeoHash(topLeftBox), transformLocationToGeoHash(bottomRightBox)), is(true));
    }

    @Test
    public void existsByLocationNearWithGeoPoint() {
        assertThat(this.repository.existsByLocationNear(topLeftBox, bottomRightBox), is(true));
    }

    @Test
    public void deleteByLocationNear() {
        assertThat(this.repository.deleteByLocationNear(transformLocation(topLeftBox), transformLocation(bottomRightBox)), is(equalTo(3L)));
        this.repository.refresh();
        assertThat(this.repository.count(), is(equalTo(2L)));
    }

    @Test
    public void deleteByLocationNearWithBox() {
        assertThat(this.repository.deleteByLocationNear(transformBox(topLeftBox, bottomRightBox)), is(equalTo(3L)));
        this.repository.refresh();
        assertThat(this.repository.count(), is(equalTo(2L)));
    }

    @Test
    public void deleteByLocationNearWithPoint() {
        assertThat(this.repository.deleteByLocationNear(transformLocationToPoint(topLeftBox), transformLocationToPoint(bottomRightBox)), is(equalTo(3L)));
        this.repository.refresh();
        assertThat(this.repository.count(), is(equalTo(2L)));
    }

    @Test
    public void deleteByLocationNearWithGeoHash() {
        assertThat(this.repository.deleteByLocationNear(transformLocationToGeoHash(topLeftBox), transformLocationToGeoHash(bottomRightBox)), is(equalTo(3L)));
        this.repository.refresh();
        assertThat(this.repository.count(), is(equalTo(2L)));
    }

    @Test
    public void deleteByLocationNearWithGeoPoint() {
        assertThat(this.repository.deleteByLocationNear(topLeftBox, bottomRightBox), is(equalTo(3L)));
        this.repository.refresh();
        assertThat(this.repository.count(), is(equalTo(2L)));
    }

    @Test
    public void findByLocationNear() {
        List<City> cities = this.repository.findCityByLocationNearOrderByNameAsc(transformLocation(topLeftBox), transformLocation(bottomRightBox));
        assertThat(cities.size(), is(equalTo(3)));
        assertCity(cities.get(0), castries);
        assertCity(cities.get(1), mauguio);
        assertCity(cities.get(2), vendargues);
    }

    @Test
    public void findByLocationNearWithBox() {
        List<City> cities = this.repository.findCityByLocationNearOrderByNameAsc(transformBox(topLeftBox, bottomRightBox));
        assertThat(cities.size(), is(equalTo(3)));
        assertCity(cities.get(0), castries);
        assertCity(cities.get(1), mauguio);
        assertCity(cities.get(2), vendargues);
    }

    @Test
    public void findByLocationNearWithPoint() {
        List<City> cities = this.repository.findCityByLocationNearOrderByNameAsc(transformLocationToPoint(topLeftBox), transformLocationToPoint(bottomRightBox));
        assertThat(cities.size(), is(equalTo(3)));
        assertCity(cities.get(0), castries);
        assertCity(cities.get(1), mauguio);
        assertCity(cities.get(2), vendargues);
    }

    @Test
    public void findByLocationNearWithGeoHash() {
        List<City> cities = this.repository.findCityByLocationNearOrderByNameAsc(transformLocationToGeoHash(topLeftBox), transformLocationToGeoHash(bottomRightBox));
        assertThat(cities.size(), is(equalTo(3)));
        assertCity(cities.get(0), castries);
        assertCity(cities.get(1), mauguio);
        assertCity(cities.get(2), vendargues);
    }

    @Test
    public void findByLocationNearWithGeoPoint() {
        List<City> cities = this.repository.findCityByLocationNearOrderByNameAsc(topLeftBox, bottomRightBox);
        assertThat(cities.size(), is(equalTo(3)));
        assertCity(cities.get(0), castries);
        assertCity(cities.get(1), mauguio);
        assertCity(cities.get(2), vendargues);
    }

    @Test
    public void findByLocationNearPageable() {
        ScrolledPage<City> cities = this.repository.findCityByLocationNear(transformLocation(topLeftBox), transformLocation(bottomRightBox),
                PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(3L)));
        assertCity(cities.getContent().get(0), castries);
        assertCity(cities.getContent().get(1), mauguio);
        assertCity(cities.getContent().get(2), vendargues);
    }

    @Test
    public void findByLocationNearPageableWithBox() {
        ScrolledPage<City> cities = this.repository.findCityByLocationNear(transformBox(topLeftBox, bottomRightBox),
                PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(3L)));
        assertCity(cities.getContent().get(0), castries);
        assertCity(cities.getContent().get(1), mauguio);
        assertCity(cities.getContent().get(2), vendargues);
    }

    @Test
    public void findByLocationNearPageableWithPoint() {
        ScrolledPage<City> cities = this.repository.findCityByLocationNear(transformLocationToPoint(topLeftBox), transformLocationToPoint(bottomRightBox),
                PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(3L)));
        assertCity(cities.getContent().get(0), castries);
        assertCity(cities.getContent().get(1), mauguio);
        assertCity(cities.getContent().get(2), vendargues);
    }

    @Test
    public void findByLocationNearPageableWithGeoHash() {
        ScrolledPage<City> cities = this.repository.findCityByLocationNear(transformLocationToGeoHash(topLeftBox), transformLocationToGeoHash(bottomRightBox),
                PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(3L)));
        assertCity(cities.getContent().get(0), castries);
        assertCity(cities.getContent().get(1), mauguio);
        assertCity(cities.getContent().get(2), vendargues);
    }

    @Test
    public void findByLocationNearPageableWithGeoPoint() {
        ScrolledPage<City> cities = this.repository.findCityByLocationNear(topLeftBox, bottomRightBox,
                PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(3L)));
        assertCity(cities.getContent().get(0), castries);
        assertCity(cities.getContent().get(1), mauguio);
        assertCity(cities.getContent().get(2), vendargues);
    }

    @Test
    public void countByLocationWithinWithPoint() {
        GeoPoint point = castries.getLocation();
        assertThat(this.repository.countByLocationWithin(transformLocationToPoint(point), new Distance(10, Metrics.KILOMETERS)), is(equalTo(3L)));
    }

    @Test
    public void countByLocationWithinWithPointAndMiles() {
        GeoPoint point = castries.getLocation();
        assertThat(this.repository.countByLocationWithin(transformLocationToPoint(point), new Distance(6.21371, Metrics.MILES)), is(equalTo(3L)));
    }

    @Test
    public void countByLocationWithinWithGeoHash() {
        GeoPoint point = castries.getLocation();
        assertThat(this.repository.countByLocationWithin(transformLocationToGeoHash(point), "10km"), is(equalTo(3L)));
    }

    @Test
    public void countByLocationWithin() {
        GeoPoint point = castries.getLocation();
        assertThat(this.repository.countByLocationWithin(transformLocation(point), "10km"), is(equalTo(3L)));
    }

    @Test
    public void countByLocationWithinWithGeoPoint() {
        assertThat(this.repository.countByLocationWithin(castries.getLocation(), "10km"), is(equalTo(3L)));
    }

    @Test
    public void existsByLocationWithin() {
        GeoPoint point = castries.getLocation();
        assertThat(this.repository.existsByLocationWithin(transformLocation(point), "10km"), is(true));
    }

    @Test
    public void existsByLocationWithinGeoHash() {
        GeoPoint point = castries.getLocation();
        assertThat(this.repository.existsByLocationWithin(transformLocationToGeoHash(point), "10km"), is(true));
    }

    @Test
    public void existsByLocationWithinWithPoint() {
        GeoPoint point = castries.getLocation();
        assertThat(this.repository.existsByLocationWithin(transformLocationToPoint(point), new Distance(10, Metrics.KILOMETERS)), is(true));
    }

    @Test
    public void existsByLocationWithinWithGeoPoint() {
        assertThat(this.repository.existsByLocationWithin(castries.getLocation(), "10km"), is(true));
    }

    @Test
    public void deleteByLocationWithin() {
        GeoPoint point = castries.getLocation();
        assertThat(this.repository.deleteByLocationWithin(transformLocation(point), "10km"), is(equalTo(3L)));
        this.repository.refresh();
        assertThat(this.repository.count(), is(equalTo(2L)));
    }

    @Test
    public void deleteByLocationWithinWithGeoHash() {
        GeoPoint point = castries.getLocation();
        assertThat(this.repository.deleteByLocationWithin(transformLocationToGeoHash(point), "10km"), is(equalTo(3L)));
        this.repository.refresh();
        assertThat(this.repository.count(), is(equalTo(2L)));
    }

    @Test
    public void deleteByLocationWithinWithPoint() {
        GeoPoint point = castries.getLocation();
        assertThat(this.repository.deleteByLocationWithin(transformLocationToPoint(point), new Distance(10, Metrics.KILOMETERS)), is(equalTo(3L)));
        this.repository.refresh();
        assertThat(this.repository.count(), is(equalTo(2L)));
    }

    @Test
    public void deleteByLocationWithinWithGeoPoint() {
        assertThat(this.repository.deleteByLocationWithin(castries.getLocation(), "10km"), is(equalTo(3L)));
        this.repository.refresh();
        assertThat(this.repository.count(), is(equalTo(2L)));
    }

    @Test
    public void findByLocationWithin() {
        GeoPoint point = castries.getLocation();
        List<City> cities = this.repository.findCityByLocationWithinOrderByNameAsc(transformLocation(point), "10km");
        assertThat(cities.size(), is(equalTo(3)));
        assertCity(cities.get(0), castries);
        assertCity(cities.get(1), mauguio);
        assertCity(cities.get(2), vendargues);
    }

    @Test
    public void findByLocationWithinWithGeoHash() {
        GeoPoint point = castries.getLocation();
        List<City> cities = this.repository.findCityByLocationWithinOrderByNameAsc(transformLocationToGeoHash(point), "10km");
        assertThat(cities.size(), is(equalTo(3)));
        assertCity(cities.get(0), castries);
        assertCity(cities.get(1), mauguio);
        assertCity(cities.get(2), vendargues);
    }

    @Test
    public void findByLocationWithinWithPoint() {
        GeoPoint point = castries.getLocation();
        List<City> cities = this.repository.findCityByLocationWithinOrderByNameAsc(transformLocationToPoint(point), new Distance(10, Metrics.KILOMETERS));
        assertThat(cities.size(), is(equalTo(3)));
        assertCity(cities.get(0), castries);
        assertCity(cities.get(1), mauguio);
        assertCity(cities.get(2), vendargues);
    }

    @Test
    public void findByLocationWithinWithGeoPoint() {
        List<City> cities = this.repository.findCityByLocationWithinOrderByNameAsc(castries.getLocation(), "10km");
        assertThat(cities.size(), is(equalTo(3)));
        assertCity(cities.get(0), castries);
        assertCity(cities.get(1), mauguio);
        assertCity(cities.get(2), vendargues);
    }

    @Test
    public void findByLocationWithinPageable() {
        GeoPoint point = castries.getLocation();
        ScrolledPage<City> cities = this.repository.findCityByLocationWithin(transformLocation(point), "10km",
                PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(3L)));
        assertCity(cities.getContent().get(0), castries);
        assertCity(cities.getContent().get(1), mauguio);
        assertCity(cities.getContent().get(2), vendargues);
    }

    @Test
    public void findByLocationWithinPageableWithGeoHash() {
        GeoPoint point = castries.getLocation();
        ScrolledPage<City> cities = this.repository.findCityByLocationWithin(transformLocationToGeoHash(point), "10km",
                PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(3L)));
        assertCity(cities.getContent().get(0), castries);
        assertCity(cities.getContent().get(1), mauguio);
        assertCity(cities.getContent().get(2), vendargues);
    }

    @Test
    public void findByLocationWithinPageableWithPoint() {
        GeoPoint point = castries.getLocation();
        ScrolledPage<City> cities = this.repository.findCityByLocationWithin(
                transformLocationToPoint(point),
                new Distance(10, Metrics.KILOMETERS),
                PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(3L)));
        assertCity(cities.getContent().get(0), castries);
        assertCity(cities.getContent().get(1), mauguio);
        assertCity(cities.getContent().get(2), vendargues);
    }

    @Test
    public void findByLocationWithinPageableWithGeoPoint() {
        ScrolledPage<City> cities = this.repository.findCityByLocationWithin(
                castries.getLocation(),
                "10km",
                PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(3L)));
        assertCity(cities.getContent().get(0), castries);
        assertCity(cities.getContent().get(1), mauguio);
        assertCity(cities.getContent().get(2), vendargues);
    }

    @Test
    public void findById() {
        Optional<City> optional = this.repository.findById(montpellier.getId());
        assertTrue(optional.isPresent());
        assertCity(optional.get(), montpellier);
    }

    @Test
    public void countByRegion() {
        assertThat(this.repository.countByRegion("SE"), is(equalTo(5L)));
    }

    @Test
    public void existsByRegion() {
        assertThat(this.repository.existsByRegion("SE"), is(true));
    }

    @Test
    public void deleteByRegion() {
        assertThat(this.repository.deleteByRegion("SE"), is(equalTo(5L)));
        this.repository.refresh();
        assertThat(this.repository.count(), is(equalTo(0L)));
    }

    @Test
    public void existsByRegionNotFound() {
        assertThat(this.repository.existsByRegion("ES"), is(false));
    }

    @Test
    public void findCityByRegion() {
        List<City> cities = this.repository.findCityByRegionOrderByNameAsc("SE");
        assertThat(cities.size(), is(equalTo(5)));
        assertCity(cities.get(0), castries);
        assertCity(cities.get(1), mauguio);
        assertCity(cities.get(2), montpellier);
        assertCity(cities.get(3), sommieres);
        assertCity(cities.get(4), vendargues);
    }

    @Test
    public void findCityByRegionWithPageable() {
        ScrolledPage<City> cities = this.repository.findCityByRegion("SE", PageRequest.of(0, 50, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(5L)));
        assertCity(cities.getContent().get(0), castries);
        assertCity(cities.getContent().get(1), mauguio);
        assertCity(cities.getContent().get(2), montpellier);
        assertCity(cities.getContent().get(3), sommieres);
        assertCity(cities.getContent().get(4), vendargues);
    }

    @Test
    public void countByPopulationBetween() {
        assertThat(this.repository.countByPopulationBetween(5000L, 10000L), is(equalTo(2L)));
    }

    @Test
    public void existsByPopulationBetween() {
        assertThat(this.repository.existsByPopulationBetween(5000L, 10000L), is(true));
    }

    @Test
    public void deleteByPopulationBetween() {
        assertThat(this.repository.deleteByPopulationBetween(5000L, 10000L), is(equalTo(2L)));
        this.repository.refresh();
        assertThat(this.repository.count(), is(equalTo(3L)));
    }

    @Test
    public void findCityByPopulationBetweenOrderByNameAsc() {
        List<City> cities = this.repository.findCityByPopulationBetweenOrderByNameAsc(5000L, 10000L);
        assertThat(cities.size(), is(equalTo(2)));
        assertCity(cities.get(0), castries);
        assertCity(cities.get(1), vendargues);
    }

    @Test
    public void findCityByPopulationBetween() {
        ScrolledPage<City> cities = this.repository.findCityByPopulationBetween(5000L, 10000L, PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(2L)));
        assertCity(cities.getContent().get(0), castries);
        assertCity(cities.getContent().get(1), vendargues);
    }

    @Test
    public void countByPopulationGreaterThanEqual() {
        assertThat(this.repository.countByPopulationGreaterThanEqual(6186L), is(equalTo(3L)));
    }

    @Test
    public void existsByPopulationGreaterThanEqual() {
        assertThat(this.repository.existsByPopulationGreaterThanEqual(6186L), is(true));
    }

    @Test
    public void deleteByPopulationGreaterThanEqual() {
        assertThat(this.repository.deleteByPopulationGreaterThanEqual(6186L), is(equalTo(3L)));
        this.repository.refresh();
        assertThat(this.repository.count(), is(equalTo(2L)));
    }

    @Test
    public void findCityByPopulationGreaterThanEqualOrderByNameAsc() {
        List<City> cities = this.repository.findCityByPopulationGreaterThanEqualOrderByNameAsc(6186L);
        assertThat(cities.size(), is(equalTo(3)));
        assertCity(cities.get(0), mauguio);
        assertCity(cities.get(1), montpellier);
        assertCity(cities.get(2), vendargues);
    }

    @Test
    public void findCityByPopulationGreaterThanEqual() {
        ScrolledPage<City> cities = this.repository.findCityByPopulationGreaterThanEqual(6186L, PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(3L)));
        assertCity(cities.getContent().get(0), mauguio);
        assertCity(cities.getContent().get(1), montpellier);
        assertCity(cities.getContent().get(2), vendargues);
    }

    @Test
    public void countByPopulationGreaterThan() {
        assertThat(this.repository.countByPopulationGreaterThan(6186L), is(equalTo(2L)));
    }

    @Test
    public void existsByPopulationGreaterThan() {
        assertThat(this.repository.existsByPopulationGreaterThan(6186L), is(true));
    }

    @Test
    public void deleteByPopulationGreaterThan() {
        assertThat(this.repository.deleteByPopulationGreaterThan(6186L), is(equalTo(2L)));
        this.repository.refresh();
        assertThat(this.repository.count(), is(equalTo(3L)));
    }

    @Test
    public void findCityByPopulationGreaterThanOrderByNameAsc() {
        List<City> cities = this.repository.findCityByPopulationGreaterThanOrderByNameAsc(6186L);
        assertThat(cities.size(), is(equalTo(2)));
        assertCity(cities.get(0), mauguio);
        assertCity(cities.get(1), montpellier);
    }

    @Test
    public void findCityByPopulationGreaterThan() {
        ScrolledPage<City> cities = this.repository.findCityByPopulationGreaterThan(6186L, PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(2L)));
        assertCity(cities.getContent().get(0), mauguio);
        assertCity(cities.getContent().get(1), montpellier);
    }

    @Test
    public void countByPopulationLessThanEqual() {
        assertThat(this.repository.countByPopulationLessThanEqual(4644L), is(equalTo(1L)));
    }

    @Test
    public void existsByPopulationLessThanEqual() {
        assertThat(this.repository.existsByPopulationLessThanEqual(4644L), is(true));
    }

    @Test
    public void deleteByPopulationLessThanEqual() {
        assertThat(this.repository.deleteByPopulationLessThanEqual(4644L), is(equalTo(1L)));
        this.repository.refresh();
        assertThat(this.repository.count(), is(equalTo(4L)));
    }

    @Test
    public void findCityByPopulationLessThanEqualOrderByNameAsc() {
        List<City> cities = this.repository.findCityByPopulationLessThanEqualOrderByNameAsc(4644L);
        assertThat(cities.size(), is(equalTo(1)));
        assertCity(cities.get(0), sommieres);
    }

    @Test
    public void findCityByPopulationLessThanEqual() {
        ScrolledPage<City> cities = this.repository.findCityByPopulationLessThanEqual(4644L, PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.getTotalElements(), is(equalTo(1L)));
        assertCity(cities.getContent().get(0), sommieres);
    }

    @Test
    public void countByPopulationLessThan() {
        assertThat(this.repository.countByPopulationLessThan(4644L), is(equalTo(0L)));
    }

    @Test
    public void existsByPopulationLessThan() {
        assertThat(this.repository.existsByPopulationLessThan(4644L), is(false));
    }

    @Test
    public void deleteByPopulationLessThan() {
        assertThat(this.repository.deleteByPopulationLessThan(4644L), is(equalTo(0L)));
        this.repository.refresh();
        assertThat(this.repository.count(), is(equalTo(5L)));
    }

    @Test
    public void findCityByPopulationLessThanOrderByNameAsc() {
        List<City> cities = this.repository.findCityByPopulationLessThanOrderByNameAsc(4644L);
        assertThat(cities.isEmpty(), is(true));
    }

    @Test
    public void findCityByPopulationLessThan() {
        ScrolledPage<City> cities = this.repository.findCityByPopulationLessThan(4644L, PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")));
        assertThat(cities.hasContent(), is(false));
    }


    private void assertCity(City actual, City expected) {
        assertThat(actual.getName(), is(equalTo(expected.getName())));
        assertThat(actual.getLocation(), is(equalTo(expected.getLocation())));
        assertThat(actual.getRegion(), is(equalTo(expected.getRegion())));
        assertThat(actual.getPopulation(), is(equalTo(expected.getPopulation())));
        assertThat(actual.getId(), is(equalTo(expected.getId())));
    }

    private City createCity(String name, GeoPoint location, Long population, String region) {
        return City.builder()
                .name(name)
                .location(location)
                .population(population)
                .region(region)
                .build();
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

}
