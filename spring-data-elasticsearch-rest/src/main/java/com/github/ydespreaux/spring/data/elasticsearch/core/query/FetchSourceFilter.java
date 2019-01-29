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

package com.github.ydespreaux.spring.data.elasticsearch.core.query;

/**
 * SourceFilter implementation for providing includes and excludes.
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public class FetchSourceFilter implements SourceFilter {

    private final String[] includes;
    private final String[] excludes;

    public FetchSourceFilter(final String[] includes, final String[] excludes) {
        this.includes = includes;
        this.excludes = excludes;
    }

    @Override
    public String[] getIncludes() {
        return includes;
    }

    @Override
    public String[] getExcludes() {
        return excludes;
    }

    /**
     * SourceFilter builder
     *
     * @author Yoann Despréaux
     * @since 1.0.0
     */
    public static class FetchSourceFilterBuilder {

        private String[] includes;
        private String[] excludes;

        public FetchSourceFilterBuilder withIncludes(String... includes) {
            this.includes = includes;
            return this;
        }

        public FetchSourceFilterBuilder withExcludes(String... excludes) {
            this.excludes = excludes;
            return this;
        }

        public SourceFilter build() {
            if (includes == null) includes = new String[0];
            if (excludes == null) excludes = new String[0];

            return new FetchSourceFilter(includes, excludes);
        }
    }
}
