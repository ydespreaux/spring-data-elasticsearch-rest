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
import com.github.ydespreaux.spring.data.elasticsearch.core.geo.*;
import com.github.ydespreaux.spring.data.elasticsearch.entities.ShapeEntity;
import com.github.ydespreaux.spring.data.elasticsearch.repositories.shape.ShapeEntityRepository;
import com.github.ydespreaux.spring.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import com.github.ydespreaux.testcontainers.elasticsearch.ElasticsearchContainer;
import org.elasticsearch.common.unit.DistanceUnit;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@Tag("integration")
@SpringBootTest(classes = {
        RestClientAutoConfiguration.class,
        ShapeEntityRepositoryTest.ElasticsearchConfiguration.class})
@Testcontainers
public class ShapeEntityRepositoryTest {

    @Container
    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer(Versions.ELASTICSEARCH_VERSION);
    @Autowired
    private ShapeEntityRepository repository;

    @Test
    void insertGeoShapeWithPointType() {
        ShapeEntity shape = this.repository.save(ShapeEntity.builder()
                .name("plot with point")
                .point(new PointShape(new Coordinate(50, 20)))
                .build());
        this.repository.refresh();
        Optional<ShapeEntity> optional = this.repository.findById(shape.getId());
        assertThat(optional.isPresent(), is(true));
        ShapeEntity document = optional.get();
        assertThat(document.getId(), is(equalTo(shape.getId())));
        assertThat(document.getName(), is(equalTo(shape.getName())));
        assertThat(document.getPoint(), is(equalTo(shape.getPoint())));
    }

    @Test
    void insertGeoShapeWithLinestringType() {
        ShapeEntity shape = this.repository.save(ShapeEntity.builder()
                .name("plot with linestring")
                .linestring(new LinestringShape(
                        new Coordinate(50, 20),
                        new Coordinate(50.5, 20.5),
                        new Coordinate(51, 20.9)))
                .build());
        this.repository.refresh();
        Optional<ShapeEntity> optional = this.repository.findById(shape.getId());
        assertThat(optional.isPresent(), is(true));
        ShapeEntity document = optional.get();
        assertThat(document.getId(), is(equalTo(shape.getId())));
        assertThat(document.getName(), is(equalTo(shape.getName())));
        assertThat(document.getLinestring(), is(equalTo(shape.getLinestring())));
    }

    @Test
    void insertGeoShapeWithPolygonType() {
        ShapeEntity shape = this.repository.save(ShapeEntity.builder()
                .name("plot with polygone")
                .polygon(new PolygonShape(new Coordinate[]{
                        new Coordinate(100.0, 0.0),
                        new Coordinate(101.0, 0.0),
                        new Coordinate(101.0, 1.0),
                        new Coordinate(100.0, 1.0),
                        new Coordinate(100.0, 0.0)
                }, null))
                .build());
        this.repository.refresh();
        Optional<ShapeEntity> optional = this.repository.findById(shape.getId());
        assertThat(optional.isPresent(), is(true));
        ShapeEntity document = optional.get();
        assertThat(document.getId(), is(equalTo(shape.getId())));
        assertThat(document.getName(), is(equalTo(shape.getName())));
        assertThat(document.getPolygon(), is(equalTo(shape.getPolygon())));
    }

    @Test
    void insertGeoShapeWithPolygonTypeAndOrientation() {
        ShapeEntity shape = this.repository.save(ShapeEntity.builder()
                .name("plot with polygone and orientation")
                .polygon(new PolygonShape(new Coordinate[]{
                        new Coordinate(100.0, 0.0),
                        new Coordinate(101.0, 0.0),
                        new Coordinate(101.0, 1.0),
                        new Coordinate(100.0, 1.0),
                        new Coordinate(100.0, 0.0)
                }, null, GeoShapeOrientation.CLOCKWISE))
                .build());
        this.repository.refresh();
        Optional<ShapeEntity> optional = this.repository.findById(shape.getId());
        assertThat(optional.isPresent(), is(true));
        ShapeEntity document = optional.get();
        assertThat(document.getId(), is(equalTo(shape.getId())));
        assertThat(document.getName(), is(equalTo(shape.getName())));
        assertThat(document.getPolygon(), is(equalTo(shape.getPolygon())));
    }

    @Test
    void insertGeoShapeWithInnerPolygonType() {
        ShapeEntity shape = this.repository.save(ShapeEntity.builder()
                .name("plot with inner polygone")
                .innerPolygon(new PolygonShape(
                        new Coordinate[]{
                                new Coordinate(100.0, 0.0),
                                new Coordinate(101.0, 0.0),
                                new Coordinate(101.0, 1.0),
                                new Coordinate(100.0, 1.0),
                                new Coordinate(100.0, 0.0)
                        },
                        new Coordinate[]{
                                new Coordinate(100.2, 0.20),
                                new Coordinate(100.8, 0.2),
                                new Coordinate(100.8, 0.8),
                                new Coordinate(100.2, 0.8),
                                new Coordinate(100.2, 0.2)
                        }))
                .build());
        this.repository.refresh();
        Optional<ShapeEntity> optional = this.repository.findById(shape.getId());
        assertThat(optional.isPresent(), is(true));
        ShapeEntity document = optional.get();
        assertThat(document.getId(), is(equalTo(shape.getId())));
        assertThat(document.getName(), is(equalTo(shape.getName())));
        assertThat(document.getInnerPolygon(), is(equalTo(shape.getInnerPolygon())));
    }

    @Test
    void insertGeoShapeWithMultipointType() {
        ShapeEntity shape = this.repository.save(ShapeEntity.builder()
                .name("plot with multipoint")
                .multipoint(new MultiPointShape(
                        new Coordinate(50, 20),
                        new Coordinate(50.5, 20.5),
                        new Coordinate(51, 20.9)))
                .build());
        this.repository.refresh();
        Optional<ShapeEntity> optional = this.repository.findById(shape.getId());
        assertThat(optional.isPresent(), is(true));
        ShapeEntity document = optional.get();
        assertThat(document.getId(), is(equalTo(shape.getId())));
        assertThat(document.getName(), is(equalTo(shape.getName())));
        assertThat(document.getMultipoint(), is(equalTo(shape.getMultipoint())));
    }

    @Test
    void insertGeoShapeWithMultilinestringType() {
        ShapeEntity shape = this.repository.save(ShapeEntity.builder()
                .name("plot with multilinestring")
                .multilinestring(new MultiLinestringShape(new Coordinate[][]{
                        new Coordinate[]{new Coordinate(102.0, 2.0), new Coordinate(103.0, 2.0), new Coordinate(103.0, 3.0), new Coordinate(102.0, 3.0)},
                        new Coordinate[]{new Coordinate(100.0, 0.0), new Coordinate(101.0, 0.0), new Coordinate(101.0, 1.0), new Coordinate(100.0, 1.0)},
                        new Coordinate[]{new Coordinate(100.2, 0.2), new Coordinate(100.8, 0.2), new Coordinate(100.8, 0.8), new Coordinate(100.2, 0.8)}
                }))
                .build());
        this.repository.refresh();
        Optional<ShapeEntity> optional = this.repository.findById(shape.getId());
        assertThat(optional.isPresent(), is(true));
        ShapeEntity document = optional.get();
        assertThat(document.getId(), is(equalTo(shape.getId())));
        assertThat(document.getName(), is(equalTo(shape.getName())));
        assertThat(document.getMultilinestring(), is(equalTo(shape.getMultilinestring())));
    }

    @Test
    void insertGeoShapeWithMultipolygonType() {
        ShapeEntity shape = this.repository.save(ShapeEntity.builder()
                .name("plot with inner multipolygon")
                .multipolygon(new MultiPolygonShape(new Coordinate[][][]{
                        new Coordinate[][]{
                                new Coordinate[]{new Coordinate(102.0, 2.0), new Coordinate(103.0, 2.0), new Coordinate(103.0, 3.0), new Coordinate(102.0, 3.0), new Coordinate(102.0, 2.0)}},
                        new Coordinate[][]{
                                new Coordinate[]{new Coordinate(100.0, 0.0), new Coordinate(101.0, 0.0), new Coordinate(101.0, 1.0), new Coordinate(100.0, 1.0), new Coordinate(100.0, 0.0)},
                                new Coordinate[]{new Coordinate(100.2, 0.2), new Coordinate(100.8, 0.2), new Coordinate(100.8, 0.8), new Coordinate(100.2, 0.8), new Coordinate(100.2, 0.2)}
                        }
                }))
                .build());
        this.repository.refresh();
        Optional<ShapeEntity> optional = this.repository.findById(shape.getId());
        assertThat(optional.isPresent(), is(true));
        ShapeEntity document = optional.get();
        assertThat(document.getId(), is(equalTo(shape.getId())));
        assertThat(document.getName(), is(equalTo(shape.getName())));
        assertThat(document.getMultipolygon(), is(equalTo(shape.getMultipolygon())));
    }

    @Test
    void insertGeoShapeWithMultipolygonTypeAndOrientation() {
        ShapeEntity shape = this.repository.save(ShapeEntity.builder()
                .name("plot with inner multipolygon")
                .multipolygon(new MultiPolygonShape(new Coordinate[][][]{
                        new Coordinate[][]{
                                new Coordinate[]{new Coordinate(102.0, 2.0), new Coordinate(103.0, 2.0), new Coordinate(103.0, 3.0), new Coordinate(102.0, 3.0), new Coordinate(102.0, 2.0)}},
                        new Coordinate[][]{
                                new Coordinate[]{new Coordinate(100.0, 0.0), new Coordinate(101.0, 0.0), new Coordinate(101.0, 1.0), new Coordinate(100.0, 1.0), new Coordinate(100.0, 0.0)},
                                new Coordinate[]{new Coordinate(100.2, 0.2), new Coordinate(100.8, 0.2), new Coordinate(100.8, 0.8), new Coordinate(100.2, 0.8), new Coordinate(100.2, 0.2)}
                        }
                }, GeoShapeOrientation.LEFT))
                .build());
        this.repository.refresh();
        Optional<ShapeEntity> optional = this.repository.findById(shape.getId());
        assertThat(optional.isPresent(), is(true));
        ShapeEntity document = optional.get();
        assertThat(document.getId(), is(equalTo(shape.getId())));
        assertThat(document.getName(), is(equalTo(shape.getName())));
        assertThat(document.getMultipolygon(), is(equalTo(shape.getMultipolygon())));
    }

    @Test
    void insertGeoShapeWithEnvelopeType() {
        ShapeEntity shape = this.repository.save(ShapeEntity.builder()
                .name("plot with envelope")
                .envelope(new EnvelopeShape(new Coordinate(50, 21), new Coordinate(50.5, 20.5)))
                .build());
        this.repository.refresh();
        Optional<ShapeEntity> optional = this.repository.findById(shape.getId());
        assertThat(optional.isPresent(), is(true));
        ShapeEntity document = optional.get();
        assertThat(document.getId(), is(equalTo(shape.getId())));
        assertThat(document.getName(), is(equalTo(shape.getName())));
        assertThat(document.getEnvelope(), is(equalTo(shape.getEnvelope())));
    }

    @Test
    void insertGeoShapeWithCircleType() {
        ShapeEntity shape = this.repository.save(ShapeEntity.builder()
                .name("plot with circle")
                .circle(new CircleShape(new Coordinate(25.65, 35.20), new DistanceUnit.Distance(50.0, DistanceUnit.METERS)))
                .build());
        this.repository.refresh();
        Optional<ShapeEntity> optional = this.repository.findById(shape.getId());
        assertThat(optional.isPresent(), is(true));
        ShapeEntity document = optional.get();
        assertThat(document.getId(), is(equalTo(shape.getId())));
        assertThat(document.getName(), is(equalTo(shape.getName())));
        assertThat(document.getCircle(), is(equalTo(shape.getCircle())));
    }

    @Test
    void insertGeoShapeWithGeometrycollectionType() {
        ShapeEntity shape = this.repository.save(ShapeEntity.builder()
                .name("plot with geometrycollection")
                .geometry(new GeometryCollectionShape(
                        new PointShape(new Coordinate(20, 50)),
                        new CircleShape(new Coordinate(25.65, 35.20), 50.0, DistanceUnit.METERS)))
                .build());
        this.repository.refresh();
        Optional<ShapeEntity> optional = this.repository.findById(shape.getId());
        assertThat(optional.isPresent(), is(true));
        ShapeEntity document = optional.get();
        assertThat(document.getId(), is(equalTo(shape.getId())));
        assertThat(document.getName(), is(equalTo(shape.getName())));
        assertThat(document.getGeometry(), is(equalTo(shape.getGeometry())));
    }

    @Test
    void insertGeoShapeWithArrayGeometrycollectionType() {
        ShapeEntity shape = this.repository.save(ShapeEntity.builder()
                .name("plot with array geometrycollection")
                .arrayGeometry(new GeometryCollectionShape(
                        new PointShape(new Coordinate(50, 20)),
                        new CircleShape(new Coordinate(25.65, 35.20), 50.0, DistanceUnit.METERS),
                        new EnvelopeShape(new Coordinate[]{
                                new Coordinate(50, 21),
                                new Coordinate(50.5, 20.5)
                        }),
                        new LinestringShape(new Coordinate[]{
                                new Coordinate(50, 20),
                                new Coordinate(50.5, 20.5),
                                new Coordinate(51, 20.9),
                        }),
                        new MultiLinestringShape(new Coordinate[][]{
                                new Coordinate[]{new Coordinate(102.0, 2.0), new Coordinate(103.0, 2.0), new Coordinate(103.0, 3.0), new Coordinate(102.0, 3.0)},
                                new Coordinate[]{new Coordinate(100.0, 0.0), new Coordinate(101.0, 0.0), new Coordinate(101.0, 1.0), new Coordinate(100.0, 1.0)},
                                new Coordinate[]{new Coordinate(100.2, 0.2), new Coordinate(100.8, 0.2), new Coordinate(100.8, 0.8), new Coordinate(100.2, 0.8)}
                        }),
                        new MultiPointShape(Arrays.asList(
                                new Coordinate(50, 20),
                                new Coordinate(50.5, 20.5),
                                new Coordinate(51, 20.9)
                        )),
                        new MultiPolygonShape(new Coordinate[][][]{
                                new Coordinate[][]{
                                        new Coordinate[]{new Coordinate(102.0, 2.0), new Coordinate(103.0, 2.0), new Coordinate(103.0, 3.0), new Coordinate(102.0, 3.0), new Coordinate(102.0, 2.0)}},
                                new Coordinate[][]{
                                        new Coordinate[]{new Coordinate(100.0, 0.0), new Coordinate(101.0, 0.0), new Coordinate(101.0, 1.0), new Coordinate(100.0, 1.0), new Coordinate(100.0, 0.0)},
                                        new Coordinate[]{new Coordinate(100.2, 0.2), new Coordinate(100.8, 0.2), new Coordinate(100.8, 0.8), new Coordinate(100.2, 0.8), new Coordinate(100.2, 0.2)}
                                }
                        }),
                        new PolygonShape(new Coordinate[]{
                                new Coordinate(100.0, 0.0),
                                new Coordinate(101.0, 0.0),
                                new Coordinate(101.0, 1.0),
                                new Coordinate(100.0, 1.0),
                                new Coordinate(100.0, 0.0)
                        }, null),
                        new PolygonShape(
                                new Coordinate[]{
                                        new Coordinate(100.0, 0.0),
                                        new Coordinate(101.0, 0.0),
                                        new Coordinate(101.0, 1.0),
                                        new Coordinate(100.0, 1.0),
                                        new Coordinate(100.0, 0.0)
                                },
                                new Coordinate[]{
                                        new Coordinate(100.2, 0.20),
                                        new Coordinate(100.8, 0.2),
                                        new Coordinate(100.8, 0.8),
                                        new Coordinate(100.2, 0.8),
                                        new Coordinate(100.2, 0.2)
                                }
                        ),
                        new GeometryCollectionShape(new PointShape(new Coordinate(20, 50)))
                )).build());
        this.repository.refresh();
        Optional<ShapeEntity> optional = this.repository.findById(shape.getId());
        assertThat(optional.isPresent(), is(true));
        ShapeEntity document = optional.get();
        assertThat(document.getId(), is(equalTo(shape.getId())));
        assertThat(document.getName(), is(equalTo(shape.getName())));
        assertThat(document.getArrayGeometry(), is(equalTo(shape.getArrayGeometry())));
    }

    @Configuration
    @EnableAspectJAutoProxy
    @EnableAutoConfiguration
    @EnableElasticsearchRepositories(
            basePackages = "com.github.ydespreaux.spring.data.elasticsearch.repositories.shape")
    static class ElasticsearchConfiguration extends ElasticsearchConfigurationSupport {

        @Bean
        ClientLoggerAspect clientLoggerAspect() {
            return new ClientLoggerAspect();
        }
    }


}
