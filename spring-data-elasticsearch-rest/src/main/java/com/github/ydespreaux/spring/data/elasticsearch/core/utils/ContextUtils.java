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

package com.github.ydespreaux.spring.data.elasticsearch.core.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ContextUtils
 *
 * @author Yoann Despréaux
 * @since 1.0.2
 */
public final class ContextUtils {

    private static final Pattern pattern = Pattern.compile("\\Q${\\E(.+?)\\Q}\\E");

    private ContextUtils() {
    }

    /**
     * @param expression the SPel expression
     * @return evaluate the expression
     */
    public static String getEnvironmentValue(Environment environment, String expression) {
        if (environment == null || StringUtils.isEmpty(expression)) {
            return expression;
        }
        String value = null;
        // Create the matcher
        Matcher matcher = pattern.matcher(expression);
        // If the matching is there, then add it to the map and return the value
        if (matcher.find()) {
            value = environment.getProperty(matcher.group(1));
        }
        return value == null ? expression : value;
    }

    /**
     * @param expression the SPel expression
     * @return evaluate the expression
     */
    public static String getEnvironmentValue(@Nullable ApplicationContext context, String expression) {
        if (context == null) {
            return expression;
        }
        return getEnvironmentValue(context.getEnvironment(), expression);
    }
}
