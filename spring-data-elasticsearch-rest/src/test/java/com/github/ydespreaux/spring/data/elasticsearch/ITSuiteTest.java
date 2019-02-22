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
        ITElasticsearchTemplateRolloverTest.class,
        ITElasticsearchTemplateParentFieldChildTest.class
})
public class ITSuiteTest {

}
