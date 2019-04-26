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

package com.github.ydespreaux.spring.data.elasticsearch.reactive.repositories.template;

import com.github.ydespreaux.spring.data.elasticsearch.entities.City;
import com.github.ydespreaux.spring.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.elasticsearch.common.geo.GeoPoint;
import org.springframework.data.geo.Box;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveCityRepository extends ReactiveElasticsearchRepository<City, String> {

    Mono<Long> countByLocationWithin(GeoPoint origin, String distance);

    Mono<Long> countByLocationWithin(Point origin, Distance distance);

    Mono<Long> countByLocationWithin(String origin, String distance);

    Mono<Boolean> existsByLocationWithin(GeoPoint origin, String distance);

    Mono<Boolean> existsByLocationWithin(Point origin, Distance distance);

    Mono<Boolean> existsByLocationWithin(String origin, String distance);

    Flux<City> findCityByLocationWithinOrderByNameAsc(GeoPoint origin, String distance);

    Flux<City> findCityByLocationWithinOrderByNameAsc(Point origin, Distance distance);

    Flux<City> findCityByLocationWithinOrderByNameAsc(String origin, String distance);

    Mono<Long> countByLocationNear(Box box);

    Mono<Long> countByLocationNear(GeoPoint topLeftPoint, GeoPoint bottomRightPoint);

    Mono<Long> countByLocationNear(Point topLeftPoint, Point bottomRightPoint);

    Mono<Long> countByLocationNear(String topLeftPoint, String bottomRightPoint);

    Mono<Boolean> existsByLocationNear(Box box);

    Mono<Boolean> existsByLocationNear(GeoPoint topLeftPoint, GeoPoint bottomRightPoint);

    Mono<Boolean> existsByLocationNear(Point topLeftPoint, Point bottomRightPoint);

    Mono<Boolean> existsByLocationNear(String topLeftPoint, String bottomRightPoint);

    Flux<City> findCityByLocationNearOrderByNameAsc(Box box);

    Flux<City> findCityByLocationNearOrderByNameAsc(GeoPoint topLeftPoint, GeoPoint bottomRightPoint);

    Flux<City> findCityByLocationNearOrderByNameAsc(Point topLeftPoint, Point bottomRightPoint);

    Flux<City> findCityByLocationNearOrderByNameAsc(String topLeftPoint, String bottomRightPoint);

    Mono<Long> countByRegion(String region);

    Mono<Long> countByPopulationBetween(long min, long max);

    Mono<Long> countByPopulationGreaterThanEqual(long min);

    Mono<Long> countByPopulationGreaterThan(long min);

    Mono<Long> countByPopulationLessThanEqual(long max);

    Mono<Long> countByPopulationLessThan(long max);

    Mono<Boolean> existsByRegion(String region);

    Mono<Boolean> existsByPopulationBetween(long min, long max);

    Mono<Boolean> existsByPopulationGreaterThanEqual(long min);

    Mono<Boolean> existsByPopulationGreaterThan(long min);

    Mono<Boolean> existsByPopulationLessThanEqual(long max);

    Mono<Boolean> existsByPopulationLessThan(long max);

    Flux<City> findCityByRegionOrderByNameAsc(String region);

    Flux<City> findCityByPopulationBetweenOrderByNameAsc(long min, long max);

    Flux<City> findCityByPopulationGreaterThanEqualOrderByNameAsc(long min);

    Flux<City> findCityByPopulationGreaterThanOrderByNameAsc(long min);

    Flux<City> findCityByPopulationLessThanEqualOrderByNameAsc(long max);

    Flux<City> findCityByPopulationLessThanOrderByNameAsc(long max);

}
