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

package com.github.ydespreaux.spring.data.elasticsearch.repositories.template;

import com.github.ydespreaux.spring.data.elasticsearch.entities.City;
import com.github.ydespreaux.spring.data.elasticsearch.repository.ElasticsearchRepository;
import org.elasticsearch.common.geo.GeoPoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Box;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;

import java.util.List;

public interface CityRepository extends ElasticsearchRepository<City, String> {

    Long countByLocationWithin(GeoPoint origin, String distance);

    Long countByLocationWithin(Point origin, Distance distance);

    Long countByLocationWithin(String origin, String distance);

    Boolean existsByLocationWithin(GeoPoint origin, String distance);

    Boolean existsByLocationWithin(Point origin, Distance distance);

    Boolean existsByLocationWithin(String origin, String distance);

    List<City> findCityByLocationWithinOrderByNameAsc(GeoPoint origin, String distance);

    List<City> findCityByLocationWithinOrderByNameAsc(Point origin, Distance distance);

    List<City> findCityByLocationWithinOrderByNameAsc(String origin, String distance);

    Page<City> findCityByLocationWithin(GeoPoint origin, String distance, Pageable pageable);

    Page<City> findCityByLocationWithin(Point origin, Distance distance, Pageable pageable);

    Page<City> findCityByLocationWithin(String origin, String distance, Pageable pageable);

    Long countByLocationNear(Box box);

    Long countByLocationNear(GeoPoint topLeftPoint, GeoPoint bottomRightPoint);

    Long countByLocationNear(Point topLeftPoint, Point bottomRightPoint);

    Long countByLocationNear(String topLeftPoint, String bottomRightPoint);

    Boolean existsByLocationNear(Box box);

    Boolean existsByLocationNear(GeoPoint topLeftPoint, GeoPoint bottomRightPoint);

    Boolean existsByLocationNear(Point topLeftPoint, Point bottomRightPoint);

    Boolean existsByLocationNear(String topLeftPoint, String bottomRightPoint);

    List<City> findCityByLocationNearOrderByNameAsc(Box box);

    List<City> findCityByLocationNearOrderByNameAsc(GeoPoint topLeftPoint, GeoPoint bottomRightPoint);

    List<City> findCityByLocationNearOrderByNameAsc(Point topLeftPoint, Point bottomRightPoint);

    List<City> findCityByLocationNearOrderByNameAsc(String topLeftPoint, String bottomRightPoint);

    Page<City> findCityByLocationNear(Box box, Pageable pageable);

    Page<City> findCityByLocationNear(GeoPoint topLeftPoint, GeoPoint bottomRightPoint, Pageable pageable);

    Page<City> findCityByLocationNear(Point topLeftPoint, Point bottomRightPoint, Pageable pageable);

    Page<City> findCityByLocationNear(String topLeftPoint, String bottomRightPoint, Pageable pageable);

    Long countByRegion(String region);

    Long countByPopulationBetween(long min, long max);

    Long countByPopulationGreaterThanEqual(long min);

    Long countByPopulationGreaterThan(long min);

    Long countByPopulationLessThanEqual(long max);

    Long countByPopulationLessThan(long max);

    Boolean existsByRegion(String region);

    Boolean existsByPopulationBetween(long min, long max);

    Boolean existsByPopulationGreaterThanEqual(long min);

    Boolean existsByPopulationGreaterThan(long min);

    Boolean existsByPopulationLessThanEqual(long max);

    Boolean existsByPopulationLessThan(long max);

    List<City> findCityByRegionOrderByNameAsc(String region);

    List<City> findCityByPopulationBetweenOrderByNameAsc(long min, long max);

    List<City> findCityByPopulationGreaterThanEqualOrderByNameAsc(long min);

    List<City> findCityByPopulationGreaterThanOrderByNameAsc(long min);

    List<City> findCityByPopulationLessThanEqualOrderByNameAsc(long max);

    List<City> findCityByPopulationLessThanOrderByNameAsc(long max);

    Page<City> findCityByRegion(String region, Pageable pageable);

    Page<City> findCityByPopulationBetween(long min, long max, Pageable pageable);

    Page<City> findCityByPopulationGreaterThanEqual(long min, Pageable pageable);

    Page<City> findCityByPopulationGreaterThan(long min, Pageable pageable);

    Page<City> findCityByPopulationLessThanEqual(long max, Pageable pageable);

    Page<City> findCityByPopulationLessThan(long max, Pageable pageable);
}
