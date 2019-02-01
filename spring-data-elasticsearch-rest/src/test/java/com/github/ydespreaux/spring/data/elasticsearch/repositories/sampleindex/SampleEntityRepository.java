package com.github.ydespreaux.spring.data.elasticsearch.repositories.sampleindex;

import com.github.ydespreaux.spring.data.elasticsearch.entities.SampleEntity;
import com.github.ydespreaux.spring.data.elasticsearch.repository.ElasticsearchRepository;

public interface SampleEntityRepository extends ElasticsearchRepository<SampleEntity, String> {
}
