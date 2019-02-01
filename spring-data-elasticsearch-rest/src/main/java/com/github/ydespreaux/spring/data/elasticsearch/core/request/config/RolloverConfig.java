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

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.util.StringUtils;

@Getter
@Setter
@Builder
public class RolloverConfig {

    // Write aliasName
    private final RolloverAlias alias;

    // Conditions
    private final RolloverConditions conditions;

    // Trigger
    private final TriggerConfig trigger;

    private volatile Alias defaultAlias;

    public boolean hasConditions() {
        return this.conditions.hasConditions();
    }

    /**
     * @return
     */
    public Alias getDefaultAlias() {
        if (this.defaultAlias == null) {
            this.defaultAlias = this.alias.createDefaultAlias();
        }
        return this.defaultAlias;
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
            Alias alias = new Alias(this.name)
                    .writeIndex(true)
                    .filter(StringUtils.hasText(filter) ? filter : null)
                    .indexRouting(StringUtils.hasText(indexRouting) ? indexRouting : null)
                    .searchRouting(StringUtils.hasText(searchRouting) ? searchRouting : null);
            return alias;
        }

    }


    @Getter
    @Setter
    public static class RolloverConditions {
        // Conditions
        private TimeValue maxAge;
        private long maxDocs;
        private ByteSizeValue maxSize;

        public boolean hasConditions() {
            return maxAge != null || maxSize != null || maxDocs > 0;
        }

        public static ConditionsBuilder builder() {
            return new ConditionsBuilder();
        }

        /**
         *
         */
        public static class ConditionsBuilder {

            private TimeValue maxAge;
            private long maxDocs;
            private ByteSizeValue maxSize;

            public ConditionsBuilder maxAge(String age) {
                this.maxAge = StringUtils.isEmpty(age) ? null : TimeValue.parseTimeValue(age, "max_age");
                return this;
            }

            public ConditionsBuilder maxDocs(long numDocs) {
                this.maxDocs = numDocs;
                return this;
            }

            public ConditionsBuilder maxSize(String size) {
                this.maxSize = StringUtils.isEmpty(size) ? null : ByteSizeValue.parseBytesSizeValue(size, "max_size");
                return this;
            }

            public RolloverConditions build() {
                RolloverConditions conditions = new RolloverConditions();
                conditions.setMaxAge(this.maxAge);
                conditions.setMaxDocs(this.maxDocs);
                conditions.setMaxSize(this.maxSize);
                return conditions;
            }
        }

    }

    @Getter
    @Setter
    @Builder
    public static class TriggerConfig {
        // Conditions
        private boolean enabled;
        private String cronExpression;
    }

}
