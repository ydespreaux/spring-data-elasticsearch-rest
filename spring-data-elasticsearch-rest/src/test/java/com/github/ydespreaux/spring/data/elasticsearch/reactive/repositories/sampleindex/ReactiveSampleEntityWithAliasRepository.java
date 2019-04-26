package com.github.ydespreaux.spring.data.elasticsearch.reactive.repositories.sampleindex;

import com.github.ydespreaux.spring.data.elasticsearch.entities.SampleEntityWithAlias;
import com.github.ydespreaux.spring.data.elasticsearch.repository.ReactiveElasticsearchRepository;

public interface ReactiveSampleEntityWithAliasRepository extends ReactiveElasticsearchRepository<SampleEntityWithAlias, String> {
}
