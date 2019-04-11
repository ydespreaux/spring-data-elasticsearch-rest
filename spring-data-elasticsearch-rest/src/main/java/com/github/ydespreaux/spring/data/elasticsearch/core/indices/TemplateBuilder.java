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

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.io.IOException;
import java.util.*;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * @author Yoann Despréaux
 * @since 1.0.0
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
        try {
            if (isEmpty(this.source())) {
                return request;
            }
            Map<String, TreeNode> settings = this.buildJsonElement(this.source());
            // Index patterns
            List<String> indexPatterns = new ArrayList<>();
            TreeNode patternsElement = settings.get(INDEX_PATTERNS_CONFIG);
            if (patternsElement.isArray()) {
                ArrayNode arrayNode = (ArrayNode) patternsElement;
                arrayNode.elements().forEachRemaining(node -> indexPatterns.add(node.textValue()));
            } else {
                indexPatterns.add(((JsonNode) patternsElement).asText());
            }
            request.patterns(indexPatterns);
            // Aliases
            if (settings.containsKey(ALIASES_CONFIG)) {
                request.aliases(xContentBuilder(settings.get(ALIASES_CONFIG)));
            }
            // Settings
            if (settings.containsKey(SETTINGS_CONFIG)) {
                request.settings(settings.get(SETTINGS_CONFIG).toString(), XContentType.JSON);
            }
            // Mappings
            if (settings.containsKey(MAPPINGS_CONFIG)) {
                TreeNode mappingsElement = settings.get(MAPPINGS_CONFIG);
                mappingsElement.fieldNames().forEachRemaining(field -> request.mapping(field, xContentBuilder(mappingsElement.get(field))));
            }
            // Order
            if (settings.containsKey(ORDER_CONFIG)) {
                request.order(((JsonNode) settings.get(ORDER_CONFIG)).asInt());
            }
            return request;
        } catch (IOException e) {
            throw new InvalidDataAccessApiUsageException("Invalid json", e);
        }
    }

    /**
     * @return the attributes name
     */
    @Override
    protected List<String> getAttributeNames() {
        return attributNames;
    }

}
