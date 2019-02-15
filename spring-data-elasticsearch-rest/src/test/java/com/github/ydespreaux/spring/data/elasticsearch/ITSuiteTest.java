package com.github.ydespreaux.spring.data.elasticsearch;

import com.github.ydespreaux.spring.data.elasticsearch.core.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ITElasticsearchTemplateMappingTest.class,
        ITElasticsearchTemplateTest.class,
        ITReactiveElasticsearchTemplateTest.class,
        ITSampleEntityRepositoryTest.class,
        ITSampleEntityWithAliasRepositoryTest.class,
        ITElasticsearchTemplateRolloverTest.class
})
public class ITSuiteTest {

//    @ClassRule
//    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer("6.4.2")
//            .withConfigDirectory("elastic-config");

//    static {
//        System.setProperty("spring.elasticsearch.rest.uris", "http://localhost:9200");
//    }
}
