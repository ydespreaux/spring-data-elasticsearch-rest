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
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.common.xcontent.XContentType;

import java.util.*;

/**
 * @author Yoann Despréaux
 * @since 0.0.1
 */
@Slf4j
public class TemplateBuilder extends IndiceBuilder<PutIndexTemplateRequest, TemplateBuilder> {

    private static List<String> attributNames = Collections.unmodifiableList(
            Arrays.asList(INDEX_PATTERNS_CONFIG, SETTINGS_CONFIG, ALIASES_CONFIG, MAPPINGS_CONFIG, ORDER_CONFIG));

    /**
     * @return a new {@link PutIndexTemplateRequest}
     */
    @Override
    public PutIndexTemplateRequest build() {
        return build(new PutIndexTemplateRequest().name(this.name()));
    }

    @Override
    public PutIndexTemplateRequest build(PutIndexTemplateRequest request) {
        Map<String, JsonElement> settings = this.buildJsonElement(this.source());
        // Index patterns
        List<String> indexPatterns = new ArrayList<>();
        JsonElement patternsElement = settings.get(INDEX_PATTERNS_CONFIG);
        if (patternsElement.isJsonArray()) {
            patternsElement.getAsJsonArray().forEach(pattern -> indexPatterns.add(pattern.getAsString()));
        } else {
            indexPatterns.add(patternsElement.getAsString());
        }
        request.patterns(indexPatterns);
        // Aliases
        request.aliases(xContentBuilder(settings.get(ALIASES_CONFIG)));
        // Settings
        request.settings(settings.get(SETTINGS_CONFIG).toString(), XContentType.JSON);
        // Mappings
        JsonObject mappingsElement = settings.get(MAPPINGS_CONFIG).getAsJsonObject();
        mappingsElement.entrySet().forEach(entry -> request.mapping(entry.getKey(), xContentBuilder(entry.getValue())));
        // Order
        if (settings.containsKey(ORDER_CONFIG)) {
            request.order(settings.get(ORDER_CONFIG).getAsInt());
        }
        return request;
    }

    /**
     * @return the attributes name
     */
    @Override
    protected List<String> getAttributeNames() {
        return attributNames;
    }

}
