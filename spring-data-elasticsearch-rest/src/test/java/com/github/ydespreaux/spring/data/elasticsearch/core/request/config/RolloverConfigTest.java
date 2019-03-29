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

import org.junit.jupiter.api.Test;

public class RolloverConfigTest {


    @Test
    void configWithRolloverAnnotation() {
//        Rollover annotation = new Rollover() {
//
//            /**
//             * Returns the annotation type of this annotation.
//             *
//             * @return the annotation type of this annotation
//             */
//            @Override
//            public Class<? extends Annotation> annotationType() {
//                return Rollover.class;
//            }
//
//            @Override
//            public Alias alias() {
//                return new Alias() {
//
//                    /**
//                     * Returns the annotation type of this annotation.
//                     *
//                     * @return the annotation type of this annotation
//                     */
//                    @Override
//                    public Class<? extends Annotation> annotationType() {
//                        return Alias.class;
//                    }
//
//                    @Override
//                    public String name() {
//                        return "my-alias";
//                    }
//
//                    @Override
//                    public String filter() {
//                        return null;
//                    }
//
//                    @Override
//                    public String indexRouting() {
//                        return null;
//                    }
//
//                    @Override
//                    public String searchRouting() {
//                        return null;
//                    }
//                };
//            }
//
//            /**
//             * @return
//             */
//            @Override
//            public String maxAge() {
//                return "7d";
//            }
//
//            /**
//             * @return
//             */
//            @Override
//            public long maxDoc() {
//                return 1000;
//            }
//
//            /**
//             * @return
//             */
//            @Override
//            public String maxSize() {
//                return "5gb";
//            }
//
//            /**
//             * @return
//             */
//            @Override
//            public Trigger trigger() {
//                return new Trigger() {
//
//                    /**
//                     * Returns the annotation type of this annotation.
//                     *
//                     * @return the annotation type of this annotation
//                     */
//                    @Override
//                    public Class<? extends Annotation> annotationType() {
//                        return Trigger.class;
//                    }
//
//                    @Override
//                    public boolean enabled() {
//                        return true;
//                    }
//
//                    @Override
//                    public String cronExpression() {
//                        return "*/30 * * * * *";
//                    }
//                };
//            }
//        };
//        RolloverConfig config = new RolloverConfig(annotation);
//        assertThat(config.hasConditions(), is(true));
//        assertThat(config.getAlias().getName(), is(equalTo("my-alias")));
//        assertThat(config.getConditions().getMaxAge().getDays(), is(equalTo(7L)));
//        assertThat(config.getConditions().getMaxSize().getGb(), is(equalTo(5L)));
//        assertThat(config.getConditions().getMaxDocs(), is(equalTo(1000L)));
//        assertThat(config.getTrigger().isEnabled(), is(true));
//        assertThat(config.getTrigger().getCronExpression(), is(equalTo("*/30 * * * * *")));
    }

}
