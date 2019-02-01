package com.github.ydespreaux.spring.data.elasticsearch.repositories.sampleindex;

import com.github.ydespreaux.spring.data.elasticsearch.entities.SampleEntityWithAlias;
import com.github.ydespreaux.spring.data.elasticsearch.repository.ElasticsearchRepository;

public interface SampleEntityWithAliasRepository extends ElasticsearchRepository<SampleEntityWithAlias, String> {
}
