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
package com.github.ydespreaux.spring.data.elasticsearch.repository.query;

import com.github.ydespreaux.spring.data.elasticsearch.core.ElasticsearchOperations;
import com.github.ydespreaux.spring.data.elasticsearch.core.converter.DateTimeConverters;
import com.github.ydespreaux.spring.data.elasticsearch.core.query.StringQuery;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.util.Assert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ElasticsearchStringQuery
 *
 * @author Yoann Despréaux
 * @since 0.1.0
 */
public class ElasticsearchStringQuery extends AbstractElasticsearchRepositoryQuery {

    private static final Pattern PARAMETER_PLACEHOLDER = Pattern.compile("\\?(\\d+)");
    private final GenericConversionService conversionService = new GenericConversionService();
    private String query;

    public ElasticsearchStringQuery(ElasticsearchQueryMethod queryMethod, ElasticsearchOperations elasticsearchOperations,
                                    String query) {
        super(queryMethod, elasticsearchOperations);
        Assert.notNull(query, "Query cannot be empty");
        this.query = query;
        if (!conversionService.canConvert(java.util.Date.class, String.class)) {
            conversionService.addConverter(DateTimeConverters.JavaDateConverter.INSTANCE);
        }
        if (!conversionService.canConvert(org.joda.time.LocalDateTime.class, String.class)) {
            conversionService.addConverter(DateTimeConverters.LocalDateTimeConverter.INSTANCE);
        }
        if (!conversionService.canConvert(org.joda.time.LocalDate.class, String.class)) {
            conversionService.addConverter(DateTimeConverters.LocalDateConverter.INSTANCE);
        }
    }

    @Override
    public Object execute(Object[] parameters) {
        ParametersParameterAccessor accessor = new ParametersParameterAccessor(queryMethod.getParameters(), parameters);
        StringQuery stringQuery = createQuery(accessor);
        if (queryMethod.isPageQuery()) {
            stringQuery.setPageable(accessor.getPageable());
            return elasticsearchOperations.startScroll(stringQuery, queryMethod.getEntityInformation().getJavaType());
        } else if (queryMethod.isCollectionQuery()) {
            if (accessor.getPageable().isPaged()) {
                stringQuery.setPageable(accessor.getPageable());
            }
            return elasticsearchOperations.search(stringQuery, queryMethod.getEntityInformation().getJavaType());
        }

        return elasticsearchOperations.findOne(stringQuery, queryMethod.getEntityInformation().getJavaType());
    }

    protected StringQuery createQuery(ParametersParameterAccessor parameterAccessor) {
        String queryString = replacePlaceholders(this.query, parameterAccessor);
        return new StringQuery(queryString);
    }

    private String replacePlaceholders(String input, ParametersParameterAccessor accessor) {
        Matcher matcher = PARAMETER_PLACEHOLDER.matcher(input);
        String result = input;
        while (matcher.find()) {
            String group = matcher.group();
            int index = Integer.parseInt(matcher.group(1));
            result = result.replace(group, getParameterWithIndex(accessor, index));
        }
        return result;
    }

    private String getParameterWithIndex(ParametersParameterAccessor accessor, int index) {
        Object parameter = accessor.getBindableValue(index);
        if (parameter == null) {
            return "null";
        }
        if (conversionService.canConvert(parameter.getClass(), String.class)) {
            return conversionService.convert(parameter, String.class);
        }
        return parameter.toString();
    }
}
