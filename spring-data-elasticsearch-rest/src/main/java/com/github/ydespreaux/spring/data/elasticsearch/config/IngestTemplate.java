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

package com.github.ydespreaux.spring.data.elasticsearch.config;

import com.github.ydespreaux.spring.data.elasticsearch.core.ElasticsearchOperations;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Yoann Despréaux
 * @since 1.0.0
 */
@Slf4j
public class IngestTemplate implements ApplicationContextAware {

    private final TemplateProperties properties;
    private final ElasticsearchOperations operations;
    private ApplicationContext applicationContext;

    public IngestTemplate(TemplateProperties properties, ElasticsearchOperations operations) {
        this.properties = properties;
        this.operations = operations;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     *
     */
    public void ingest() {
        final TemplateAction templateAction = this.properties.getAction();
        if (templateAction == TemplateAction.NONE) {
            return;
        }
        buildTemplates().forEach(template -> operations.createTemplate(template.getName(), template.getLocations(), templateAction == TemplateAction.CREATE_ONLY));
    }

    /**
     * @return the list of template settings
     */
    private List<TemplateSettings> buildTemplates() {
        Assert.notNull(this.applicationContext, "ApplicationContext is required");
        final List<TemplateSettings> templates = new ArrayList<>();
        if (properties.getAction() == TemplateAction.NONE) {
            return templates;
        }
        return properties.getScripts().stream()
                .map(applicationContext::getResource)
                .filter(resource -> {
                    if (!resource.exists()) {
                        if (log.isWarnEnabled()) {
                            log.warn("Resource {} not found", resource);
                        }
                        return false;
                    }
                    return true;
                })
                .map(this::buildTemplateSettings)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * @param resource
     * @return
     */
    private TemplateSettings buildTemplateSettings(Resource resource) {
        final String[] profiles = applicationContext.getEnvironment().getActiveProfiles();
        List<Resource> resources = buildResourcesWithProfils(resource, profiles);
        if (resources.isEmpty()) {
            return null;
        }
        return TemplateSettings.builder()
                .name(generateTemplateName(resource, profiles))
                .locations(resources)
                .build();
    }

    /**
     * @param resource
     * @param profiles
     * @return
     */
    private String generateTemplateName(Resource resource, final String[] profiles) {
        StringBuilder templateName = new StringBuilder(FilenameUtils.getBaseName(resource.getFilename()));
        if (profiles.length > 0) {
            templateName.append("-").append(profiles[0]);
        }
        return templateName.toString();
    }

    /**
     * @param resource
     * @param profiles
     * @return
     */
    private List<Resource> buildResourcesWithProfils(Resource resource, final String[] profiles) {
        List<Resource> locations = Arrays.asList(resource);
        try {
            String location = resource.getFile().getPath();
            String extension = FilenameUtils.getExtension(location);
            boolean hasExtension = StringUtils.hasLength(extension);
            String prefix = location.substring(0, location.length() - (hasExtension ? extension.length() + 1 : 0));
            for (String profile : profiles) {
                String profilPath = prefix + "-" + profile + (hasExtension ? "." + extension : "");
                Resource profilResource = applicationContext.getResource(profilPath);
                if (profilResource.exists()) {
                    locations.add(profilResource);
                }
            }
        } catch (IOException e) {
            if (log.isWarnEnabled()) {
                log.warn("Resource {} not found", resource);
            }
        }
        return locations;
    }


    /**
     * @author yoann.despreaux
     * @since 1.0.0
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TemplateSettings {

        /**
         * Définit le nom du template
         */
        private String name;
        /**
         * Définit la liste des ressources des scripts du template
         */
        private List<Resource> locations;

    }

}
