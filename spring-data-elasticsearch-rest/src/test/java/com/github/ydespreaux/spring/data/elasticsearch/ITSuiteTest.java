package com.github.ydespreaux.spring.data.elasticsearch;

import com.github.ydespreaux.spring.data.elasticsearch.repository.support.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ITElasticsearchTemplateCompletionTest.class,
        ITElasticsearchTemplateMappingTest.class,
        ITElasticsearchTemplateTest.class,
        ITReactiveElasticsearchTemplateTest.class,
        ITArticleRepositoryTest.class,
        ITBookRepositoryTest.class,
        ITCityRepositoryTest.class,
        ITProductRepositoryTest.class,
        ITSampleEntityRepositoryTest.class,
        ITSampleEntityWithAliasRepositoryTest.class,
        ITVehicleEventRepositoryTest.class
})
public class ITSuiteTest {

//    @ClassRule
//    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer("6.4.2")
//            .withConfigDirectory("elastic-config");

    static {
        System.setProperty("spring.elasticsearch.rest.uris", "http://localhost:9200");
    }
}
