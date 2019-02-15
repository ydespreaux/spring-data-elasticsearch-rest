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

package com.github.ydespreaux.spring.data.elasticsearch.entities;

import com.github.ydespreaux.spring.data.elasticsearch.annotations.*;
import lombok.*;
import org.elasticsearch.common.geo.GeoPoint;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import java.time.LocalDateTime;

/**
 * @author Yoann Despréaux
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@RolloverDocument(
        alias = @Alias(name = "search-vehicles-alias"),
        index = @Index(
                name = "vehicles-event",
                type = "position",
                settingsAndMappingPath = "classpath:indices/vehicles-event.index"
        ),
        rollover = @Rollover(
                alias = @Alias(name = "write-vehicles-alias"),
                maxDoc = 2,
                trigger = @Trigger(enabled = "${index.vehicles.trigger.enabled}", cronExpression = "*/2 * * * * *")
        )
)
public class VehicleEvent {

    @Id
    private String documentId;
    @IndexName
    private String indexName;
    @Version
    private Long version;

    private String vehicleId;
    private GeoPoint location;
    private LocalDateTime time;
}
