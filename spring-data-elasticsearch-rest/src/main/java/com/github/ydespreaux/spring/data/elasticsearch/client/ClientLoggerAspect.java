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

/**
 *
 */
package com.github.ydespreaux.spring.data.elasticsearch.client;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.elasticsearch.action.ActionRequest;

/**
 * Lagstash aspect
 *
 * @author xpax624
 */
@Slf4j
@Aspect
public class ClientLoggerAspect {

    @Pointcut("target(com.github.ydespreaux.spring.data.elasticsearch.client.RestElasticsearchClient)")
    private void restElactisearchClientInterface() {
        // Nothing to do
    }

    @Pointcut("execution(public * *(..)) && args(request, ..)")
    private <T extends ActionRequest> void restElactisearchClientMethod(T request) {
        // Nothing to do
    }

    /**
     * Point cut for RestController methods
     */
    @Pointcut("execution(public org.springframework.http.ResponseEntity *(..))")
    public void restSyncController() {
        // Nothing to do
    }

    @Pointcut("execution(public java.util.concurrent.Callable *(..))")
    private void restAsyncController() {
        // Nothing to do
    }


    /**
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around("restElactisearchClientInterface() && restElactisearchClientMethod(request)")
    public <T extends ActionRequest> Object logger(ProceedingJoinPoint pjp, T request) throws Throwable {
        String logId = ClientLogger.newLogId();
        ClientLogger.logRequest(logId, request);
        try {
            Object result = pjp.proceed();
            ClientLogger.logResponse(logId, result.toString());
            return result;
        } catch (Exception e) {
            ClientLogger.logFailure(logId, e);
            throw e;
        }
    }

}
