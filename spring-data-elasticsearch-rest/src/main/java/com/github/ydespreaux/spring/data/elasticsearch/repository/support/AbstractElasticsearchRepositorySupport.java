package com.github.ydespreaux.spring.data.elasticsearch.repository.support;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AbstractElasticsearchRepositorySupport<T, K> {

    protected ElasticsearchEntityInformation<T, K> entityInformation;
    private Class<T> entityClass;

    public AbstractElasticsearchRepositorySupport() {
    }

    public AbstractElasticsearchRepositorySupport(ElasticsearchEntityInformation<T, K> metadata) {
        //
        this.entityInformation = metadata;
        this.entityClass = this.entityInformation.getJavaType();
    }

    /**
     * @param id the identifier
     * @return the string representation
     */
    protected abstract String stringIdRepresentation(K id);

    /**
     * @return entity class
     */
    public Class<T> getEntityClass() {
        if (!isEntityClassSet()) {
            try {
                this.entityClass = resolveReturnedClassFromGenericType();
            } catch (Exception e) {
                throw new InvalidDataAccessApiUsageException("Unable to resolve EntityClass. Please use according setter!", e);
            }
        }
        return entityClass;
    }

    /**
     * @return
     */
    private boolean isEntityClassSet() {
        return entityClass != null;
    }

    /**
     * @return
     */
    private Class<T> resolveReturnedClassFromGenericType() {
        ParameterizedType parameterizedType = resolveReturnedClassFromGenericType(getClass());
        return (Class<T>) parameterizedType.getActualTypeArguments()[0];
    }

    /**
     * @param clazz
     * @return
     */
    private ParameterizedType resolveReturnedClassFromGenericType(Class<?> clazz) {
        Object genericSuperclass = clazz.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
            Type rawtype = parameterizedType.getRawType();
            if (SimpleElasticsearchRepository.class.equals(rawtype)) {
                return parameterizedType;
            }
        }
        return resolveReturnedClassFromGenericType(clazz.getSuperclass());
    }

    /**
     * @param id
     */
    protected void assertNotNullId(K id) {
        Assert.notNull(id, "id must not be null!");
    }

    protected void assertNotNullPageable(Pageable pageable) {
        Assert.notNull(pageable, "pageable is required");
    }

    protected void assertChildDocument() {
        if (!this.entityInformation.isChildDocument()) {
            throw new InvalidDataAccessApiUsageException("The document must be a child document !!!");
        }
    }

    protected void assertParentDocument() {
        if (!this.entityInformation.isParentDocument()) {
            throw new InvalidDataAccessApiUsageException("The document must be a parent document !!!");
        }
    }

}
