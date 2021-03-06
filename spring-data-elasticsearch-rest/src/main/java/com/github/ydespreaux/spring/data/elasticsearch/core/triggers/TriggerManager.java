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

package com.github.ydespreaux.spring.data.elasticsearch.core.triggers;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.scheduling.TaskScheduler;

import java.io.Closeable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public class TriggerManager implements Closeable {

    private final TaskScheduler taskScheduler;
    private final Map<KeyTrigger, ScheduledFuture<?>> schedulers = new ConcurrentHashMap<>();

    public TriggerManager(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    public void stopAll() {
        this.schedulers.keySet().forEach(this::cancelScheduler);
    }

    public void startTrigger(Trigger trigger) {
        KeyTrigger key = generateKey(trigger);
        if (schedulers.containsKey(key)) {
            cancelScheduler(key);
        }
        this.schedulers.put(key, taskScheduler.schedule(trigger.processor(), trigger.getCronTrigger()));
    }

    private void cancelScheduler(KeyTrigger key) {
        ScheduledFuture<?> scheduler = this.schedulers.get(key);
        if (scheduler != null) {
            if (!scheduler.isDone()) {
                scheduler.cancel(true);
            }
            this.schedulers.remove(key);
        }
    }

    public KeyTrigger generateKey(Trigger trigger) {
        return KeyTrigger.builder()
                .javaType(trigger.getPersistentEntity().getJavaType())
                .triggerType(trigger.getClass())
                .build();
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * <p> As noted in {@link AutoCloseable#close()}, cases where the
     * close may fail require careful attention. It is strongly advised
     * to relinquish the underlying resources and to internally
     * <em>mark</em> the {@code Closeable} as closed, prior to throwing
     * the {@code IOException}.
     *
     */
    @Override
    public void close() {
        this.schedulers.keySet().forEach(this::cancelScheduler);
    }

    @Getter
    @Setter
    @Builder
    public static class KeyTrigger {
        private Class<?> javaType;
        private Class<?> triggerType;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof KeyTrigger)) return false;
            KeyTrigger that = (KeyTrigger) o;
            return Objects.equals(getJavaType(), that.getJavaType()) &&
                    Objects.equals(getTriggerType(), that.getTriggerType());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getJavaType(), getTriggerType());
        }
    }
}
