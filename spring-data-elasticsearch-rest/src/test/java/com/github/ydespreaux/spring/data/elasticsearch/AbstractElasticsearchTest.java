package com.github.ydespreaux.spring.data.elasticsearch;

import com.github.ydespreaux.spring.data.elasticsearch.core.ElasticsearchOperations;
import com.github.ydespreaux.spring.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import org.elasticsearch.ElasticsearchException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class AbstractElasticsearchTest<T> {

    private final Class<T> entityClass;

    @Autowired
    protected ElasticsearchOperations elasticsearchOperations;

    public AbstractElasticsearchTest(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected abstract List<T> generateData();

    /**
     *
     */
    protected void cleanData() {
        ElasticsearchPersistentEntity<T> persistentEntity = elasticsearchOperations.getPersistentEntityFor(this.entityClass);
        if (persistentEntity.getAlias() != null) {
            elasticsearchOperations.deleteIndexByAlias(persistentEntity.getAlias().name());
        } else {
            elasticsearchOperations.deleteIndexByName(persistentEntity.getIndexName());
        }
        elasticsearchOperations.createIndex(this.entityClass);
        elasticsearchOperations.refresh(this.entityClass);
    }

    protected List<T> insertData() {
        return insertData(5);
    }

    /**
     * @return
     */
    protected List<T> insertData(int tryCount) {
        List<T> data = generateData();
        if (data != null && !data.isEmpty()) {
            try {
                elasticsearchOperations.bulkIndex(data, entityClass);
            } catch (ElasticsearchException e) {
                if (tryCount > 0) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(2000);
                    } catch (InterruptedException e1) {
                        throw e;
                    }
                    return insertData(tryCount - 1);
                } else {
                    throw e;
                }
            }
            elasticsearchOperations.refresh(entityClass);
        }
        return data;
    }

}
