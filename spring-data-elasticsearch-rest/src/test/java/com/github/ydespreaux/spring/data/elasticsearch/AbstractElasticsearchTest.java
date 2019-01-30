package com.github.ydespreaux.spring.data.elasticsearch;

import com.github.ydespreaux.spring.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public abstract class AbstractElasticsearchTest<T> {

    private final Class<T> entityClass;

    protected List<T> data;

    @Autowired
    protected ElasticsearchOperations elasticsearchOperations;

    public AbstractElasticsearchTest(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected abstract List<T> generateData();

    /**
     * @return
     */
    protected void cleanAndInsertData() {
        this.cleanData();
        this.data = this.insertData();
    }

    /**
     *
     */
    protected void cleanData() {
        elasticsearchOperations.deleteAll(this.entityClass);
        elasticsearchOperations.refresh(this.entityClass);
    }

    /**
     * @return
     */
    protected List<T> insertData() {
        List<T> data = generateData();
        if (data != null && !data.isEmpty()) {
            elasticsearchOperations.bulkIndex(data, entityClass);
            elasticsearchOperations.refresh(entityClass);
        }
        return data;
    }
}
