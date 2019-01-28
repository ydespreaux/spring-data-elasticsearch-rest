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

package com.github.ydespreaux.spring.data.elasticsearch.repository.support;

import com.github.ydespreaux.spring.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * Spring {@link org.springframework.beans.factory.FactoryBean} implementation to ease container based configuration for
 * XML namespace and JavaConfig.
 *
 * @author Yoann Despréaux
 * @since 0.0.1
 */
public class ElasticsearchRepositoryFactoryBean<T extends Repository<S, K>, S, K extends Serializable> extends
        RepositoryFactoryBeanSupport<T, S, K> {

    private ElasticsearchOperations operations;

    /**
     * Creates a new {@link ElasticsearchRepositoryFactoryBean} for the given repository interface.
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    public ElasticsearchRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    /**
     * Configures the {@link ElasticsearchOperations} to be used to create Elasticsearch repositories.
     *
     * @param operations the operations to set
     */
    public void setElasticsearchOperations(ElasticsearchOperations operations) {

        Assert.notNull(operations, "ElasticsearchOperations must not be null!");

        setMappingContext(operations.getElasticsearchConverter().getMappingContext());
        this.operations = operations;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        Assert.notNull(operations, "ElasticsearchOperations must be configured!");
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory() {
        return new ElasticsearchRepositoryFactory(operations);
    }
}
