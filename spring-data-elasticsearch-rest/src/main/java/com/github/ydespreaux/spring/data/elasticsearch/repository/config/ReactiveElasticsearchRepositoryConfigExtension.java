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

package com.github.ydespreaux.spring.data.elasticsearch.repository.config;

import com.github.ydespreaux.spring.data.elasticsearch.annotations.IndexedDocument;
import com.github.ydespreaux.spring.data.elasticsearch.annotations.ProjectionDocument;
import com.github.ydespreaux.spring.data.elasticsearch.annotations.RolloverDocument;
import com.github.ydespreaux.spring.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import com.github.ydespreaux.spring.data.elasticsearch.repository.support.ReactiveElasticsearchRepositoryFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;
import org.springframework.data.repository.config.XmlRepositoryConfigurationSource;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.w3c.dom.Element;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;

/**
 * {@link org.springframework.data.repository.config.RepositoryConfigurationExtension} implementation to configure
 * Elasticsearch repository configuration support, evaluating the {@link EnableReactiveElasticsearchRepositories} annotation or
 * the equivalent XML element.
 *
 * @author Yoann Despréaux
 * @since 1.0.0
 */
public class ReactiveElasticsearchRepositoryConfigExtension extends RepositoryConfigurationExtensionSupport {

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationExtension#getRepositoryFactoryBeanClassName()
     */
    @Override
    public String getRepositoryFactoryBeanClassName() {
        return ReactiveElasticsearchRepositoryFactoryBean.class.getName();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#getModulePrefix()
     */
    @Override
    protected String getModulePrefix() {
        return "reactiveElasticsearch";
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#postProcess(org.springframework.beans.factory.support.BeanDefinitionBuilder, org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource)
     */
    @Override
    public void postProcess(BeanDefinitionBuilder builder, AnnotationRepositoryConfigurationSource config) {

        AnnotationAttributes attributes = config.getAttributes();
        builder.addPropertyReference("elasticsearchOperations", attributes.getString("reactiveElasticsearchTemplateRef"));
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#postProcess(org.springframework.beans.factory.support.BeanDefinitionBuilder, org.springframework.data.repository.config.XmlRepositoryConfigurationSource)
     */
    @Override
    public void postProcess(BeanDefinitionBuilder builder, XmlRepositoryConfigurationSource config) {

        Element element = config.getElement();
        builder.addPropertyReference("elasticsearchOperations", element.getAttribute("reactive-elasticsearch-template-ref"));
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#getIdentifyingAnnotations()
     */
    @Override
    protected Collection<Class<? extends Annotation>> getIdentifyingAnnotations() {
        return Arrays.asList(IndexedDocument.class, RolloverDocument.class, ProjectionDocument.class);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#getIdentifyingTypes()
     */
    @Override
    protected Collection<Class<?>> getIdentifyingTypes() {
        return Arrays.asList(ReactiveElasticsearchRepository.class);
    }

    @Override
    protected boolean useRepositoryConfiguration(RepositoryMetadata metadata) {
        return metadata.isReactiveRepository();
    }

}
