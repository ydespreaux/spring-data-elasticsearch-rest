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

package com.github.ydespreaux.spring.data.elasticsearch.core.request.config;

import com.github.ydespreaux.spring.data.elasticsearch.annotations.Rollover;
import com.github.ydespreaux.spring.data.elasticsearch.annotations.Trigger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Getter
@Setter
public class RolloverConfig {

    // Write aliasName
    private final RolloverAlias alias;

    // Conditions
    private final RolloverConditions rolloverConditions;

    // Trigger
    private final TriggerConfig trigger;

//    private String indexPath;

    private volatile Alias defaultAlias;

    public RolloverConfig(Rollover rollover) {
        Assert.notNull(rollover.alias(), "Rollover alias is mandatory");
        this.alias = new RolloverAliasBuilder().with(rollover.alias()).build();
        this.defaultAlias = this.alias.createDefaultAlias();
        this.rolloverConditions = new ConditionsBuilder().withRollover(rollover).build();
        this.trigger = new TriggerConfigBuilder().withTrigger(rollover.trigger()).build();
    }

    public boolean hasConditions() {
        return this.rolloverConditions.hasConditions();
    }

    @Getter
    @Setter
    @Builder
    public static class RolloverAlias {
        private String name;
        private String filter;
        private String indexRouting;
        private String searchRouting;

        public Alias createDefaultAlias() {
            return new Alias(this.name)
                    .writeIndex(true)
                    .filter(filter)
                    .indexRouting(indexRouting)
                    .searchRouting(searchRouting);
        }

    }


    @Getter
    @Setter
    @Builder
    public static class RolloverConditions {
        // Conditions
        private TimeValue maxAge;
        private long maxDocs;
        private ByteSizeValue maxSize;

        public boolean hasConditions() {
            return maxAge != null || maxSize != null || maxDocs > 0;
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class TriggerConfig {
        // Conditions
        private boolean enabled;
        private String cronExpression;
    }

    public static class RolloverAliasBuilder {
        private String name;
        private String filter;
        private String indexRouting;
        private String searchRouting;

        public RolloverAliasBuilder with(com.github.ydespreaux.spring.data.elasticsearch.annotations.Alias alias) {
            withName(alias.name());
            withFilter(alias.filter());
            withIndexRouting(alias.indexRouting());
            withSearchRouting(alias.searchRouting());
            return this;
        }

        private RolloverAliasBuilder withSearchRouting(String searchRouting) {
            this.searchRouting = searchRouting;
            return this;
        }

        private RolloverAliasBuilder withIndexRouting(String indexRouting) {
            this.indexRouting = indexRouting;
            return this;
        }

        private RolloverAliasBuilder withFilter(String filter) {
            this.filter = filter;
            return this;
        }

        private RolloverAliasBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public RolloverAlias build() {
            return RolloverAlias.builder()
                    .name(this.name)
                    .filter(this.filter)
                    .indexRouting(this.indexRouting)
                    .searchRouting(this.searchRouting)
                    .build();
        }

    }

    /**
     *
     */
    private static class ConditionsBuilder {

        private TimeValue maxAge;
        private long maxDocs;
        private ByteSizeValue maxSize;

        public ConditionsBuilder withRollover(Rollover rollover) {
            withMaxAge(rollover.maxAge());
            withMaxSize(rollover.maxSize());
            withMaxDocs(rollover.maxDoc());
            return this;
        }

        public ConditionsBuilder withMaxAge(String age) {
            this.maxAge = StringUtils.isEmpty(age) ? null : TimeValue.parseTimeValue(age, "max_age");
            return this;
        }

        public ConditionsBuilder withMaxDocs(long numDocs) {
            this.maxDocs = numDocs;
            return this;
        }

        public ConditionsBuilder withMaxSize(String size) {
            this.maxSize = StringUtils.isEmpty(size) ? null : ByteSizeValue.parseBytesSizeValue(size, "max_size");
            return this;
        }

        public RolloverConditions build() {
            return RolloverConditions.builder()
                    .maxAge(this.maxAge)
                    .maxSize(this.maxSize)
                    .maxDocs(this.maxDocs)
                    .build();
        }
    }

    private static class TriggerConfigBuilder {

        private boolean enabled = false;
        private String cronExpression;

        public TriggerConfigBuilder withEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public TriggerConfigBuilder withCronExpression(String cronExpression) {
            this.cronExpression = cronExpression;
            return this;
        }

        public TriggerConfigBuilder withTrigger(Trigger trigger) {
            withEnabled(trigger.enabled());
            withCronExpression(trigger.cronExpression());
            return this;
        }

        public TriggerConfig build() {
            return new TriggerConfig(enabled, cronExpression);
        }
    }

}
