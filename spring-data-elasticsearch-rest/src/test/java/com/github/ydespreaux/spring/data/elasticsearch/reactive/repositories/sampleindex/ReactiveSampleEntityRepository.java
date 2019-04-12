package com.github.ydespreaux.spring.data.elasticsearch.reactive.repositories.sampleindex;

import com.github.ydespreaux.spring.data.elasticsearch.entities.SampleEntity;
import com.github.ydespreaux.spring.data.elasticsearch.repository.ReactiveElasticsearchRepository;

public interface ReactiveSampleEntityRepository extends ReactiveElasticsearchRepository<SampleEntity, String> {
}
