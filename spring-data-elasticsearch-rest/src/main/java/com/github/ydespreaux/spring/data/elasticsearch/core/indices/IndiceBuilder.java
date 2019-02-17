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

package com.github.ydespreaux.spring.data.elasticsearch.core.indices;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.IndicesRequest;
import org.elasticsearch.common.xcontent.*;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * @param <T> generic type
 * @param <S> generic type
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public abstract class IndiceBuilder<T extends IndicesRequest, S extends IndiceBuilder> {

    public static final String INDEX_PATTERNS_CONFIG = "index_patterns";
    public static final String SETTINGS_CONFIG = "settings";
    public static final String ALIASES_CONFIG = "aliases";
    public static final String MAPPINGS_CONFIG = "mappings";
    public static final String ORDER_CONFIG = "order";

    private List<Resource> sources;
    private String name;

    /**
     * @param name the index name
     * @return the current object
     */
    public S name(String name) {
        this.name = name;
        return (S) this;
    }

    public String name() {
        return this.name;
    }


    /**
     * @param source the json source
     * @return the current object
     */
    public S source(Resource source) {
        this.sources = Collections.singletonList(source);
        return (S) this;
    }

    public List<Resource> source() {
        return this.sources;
    }

    /**
     * @param sources the json sources
     * @return the current object
     */
    public S sources(List<Resource> sources) {
        this.sources = sources;
        return (S) this;
    }

    /**
     * @return build new item
     */
    public abstract T build();

    public abstract T build(T request);

    /**
     * @return the list of arributes name
     */
    protected abstract List<String> getAttributeNames();

    /**
     * @param element the json element
     * @return a new {@link XContentBuilder}
     */
    protected XContentBuilder xContentBuilder(JsonElement element) {
        try (XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, LoggingDeprecationHandler.INSTANCE, element.toString())) {
            return jsonBuilder().copyCurrentStructure(parser);
        } catch (IOException e) {
            throw new ElasticsearchException("Invalid element: ", e);
        }
    }

    /**
     * @param scripts the list of scripts
     * @return the map of jeson elements
     */
    public Map<String, JsonElement> buildJsonElement(List<Resource> scripts) {
        if (scripts.isEmpty()) {
            return null;
        }
        final Map<String, JsonElement> settings = new LinkedHashMap<>();
        for (Resource script : scripts) {
            settings.putAll(buildJsonElement(script));
        }
        return settings;
    }

    /**
     * @param rootObject the root json element
     * @return the map of json elements
     */
    protected Map<String, JsonElement> buildJsonElement(JsonObject rootObject) {
        Map<String, JsonElement> elements = new HashMap<>();
        this.getAttributeNames().forEach(attribute -> {
            if (rootObject.has(attribute)) {
                elements.put(attribute, rootObject.get(attribute));
            }
        });
        return elements;
    }


    /**
     * @param script the script
     * @return the map of json elements
     */
    protected Map<String, JsonElement> buildJsonElement(Resource script) {
        String data = readInputStream(script);
        return buildJsonElement(new JsonParser().parse(data).getAsJsonObject());
    }

    /**
     * @param source the input stream
     * @return the string corresponding to inputstream
     */
    protected String readInputStream(InputStreamSource source) {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(source.getInputStream()))) {
            String line;
            String lineSeparator = System.getProperty("line.separator");
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append(lineSeparator);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            throw new ElasticsearchException(e);
        }
    }
}
