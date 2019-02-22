/* * Copyright (C) 2018 Yoann Despréaux * * This program is free software; you can redistribute it and/or modify * it under the terms of the GNU General Public License as published by * the Free Software Foundation; either version 2 of the License, or * (at your option) any later version. * * This program is distributed in the hope that it will be useful, * but WITHOUT ANY WARRANTY; without even the implied warranty of * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the * GNU General Public License for more details. * * You should have received a copy of the GNU General Public License * along with this program; see the file COPYING . If not, write to the * Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA. * * Please send bugreports with examples or suggestions to yoann.despreaux@believeit.fr */package com.github.ydespreaux.spring.data.elasticsearch.core.indices;import com.fasterxml.jackson.core.TreeNode;import lombok.extern.slf4j.Slf4j;import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;import org.elasticsearch.common.xcontent.XContentType;import org.springframework.dao.InvalidDataAccessApiUsageException;import java.io.IOException;import java.util.Arrays;import java.util.Collections;import java.util.List;import java.util.Map;/** * @author Yoann Despréaux * @since 1.0.0 */@Slf4jpublic class CreateIndexBuilder extends IndiceBuilder<CreateIndexRequest, CreateIndexBuilder> {    private static List<String> attributeNames = Collections.unmodifiableList(            Arrays.asList(SETTINGS_CONFIG, ALIASES_CONFIG, MAPPINGS_CONFIG));    /**     * @return a new {@link CreateIndexRequest}     */    @Override    public CreateIndexRequest build() {        return build(new CreateIndexRequest(this.name()));    }    @Override    public CreateIndexRequest build(CreateIndexRequest request) {        try {            Map<String, TreeNode> settings = this.buildJsonElement(this.source());            // Settings            if (settings.containsKey(SETTINGS_CONFIG)) {                request.settings(settings.get(SETTINGS_CONFIG).toString(), XContentType.JSON);            }            // Mappings            if (settings.containsKey(MAPPINGS_CONFIG)) {                // Mappings                TreeNode mappingsElement = settings.get(MAPPINGS_CONFIG);                mappingsElement.fieldNames().forEachRemaining(field -> request.mapping(field, xContentBuilder(mappingsElement.get(field))));            }            // Aliases            if (settings.containsKey(ALIASES_CONFIG)) {                request.aliases(this.xContentBuilder(settings.get(ALIASES_CONFIG)));            }            return request;        } catch (IOException e) {            throw new InvalidDataAccessApiUsageException("Invalid json", e);        }    }    /**     * @return attributes name     */    @Override    protected List<String> getAttributeNames() {        return attributeNames;    }}