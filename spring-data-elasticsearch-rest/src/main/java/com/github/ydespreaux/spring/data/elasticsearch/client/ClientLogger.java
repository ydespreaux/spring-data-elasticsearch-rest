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
package com.github.ydespreaux.spring.data.elasticsearch.client;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionRequest;
import org.springframework.util.ObjectUtils;

/**
 * Logging Utility to log client requests and responses. Logs client requests and responses to Elasticsearch to a
 * dedicated logger: {@code com.github.ydespreaux.spring.data.elasticsearch.client.ClientLogger} on {@link org.slf4j.event.Level#TRACE}
 * level.
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
@Slf4j
public final class ClientLogger {

    private ClientLogger() {
    }

    /**
     * Returns {@literal true} if the logger is enabled.
     *
     * @return {@literal true} if the logger is enabled.
     */
    public static boolean isEnabled() {
        return log.isTraceEnabled();
    }


    public static void logRequest(String logId, ActionRequest request) {
        if (isEnabled()) {
            log.trace("[{}] Sending request : {}", logId, request.toString());
        }
    }

    /**
     * @param response
     */
    public static void logResponse(String logId, String response) {
        if (isEnabled()) {
            log.trace("[{}] Received response: {}", logId, response);
        }
    }

    public static void logFailure(String logId, Exception e) {
        if (isEnabled()) {
            log.trace("[{}] Request Failed : {}", logId, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        }
    }

    /**
     * Creates a new, unique correlation Id to improve tracing across log events.
     *
     * @return a new, unique correlation Id.
     */
    public static String newLogId() {

        if (!isEnabled()) {
            return "-";
        }

        return ObjectUtils.getIdentityHexString(new Object());
    }
}
