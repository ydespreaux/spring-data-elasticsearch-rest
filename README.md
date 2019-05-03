# Spring Data Elasticsearch Rest

Spring Data implementation for ElasticSearch based on Elasticsearch Rest client

## Introduction

The spring-data-elasticsearch-rest library is a project based on the spring-data library and offers document indexing management and a reading API
for elasticsearch using the REST client provided by elasticsearch.
The current version of this library does not allow to perform a mapping of documents by annotations. The mapping of a document must be done by files
describing the mapping of a document.

The library supports the following types of indexes:
- Simple index (index name used for write and read operations) with a json file (optional) describing the index (mapping, settings, etc ...)
- Simple index with an alias (the alias is used for read operations, the index for write operations) with a json file (optional) describing the index (mapping, settings, alias, etc ... )
- Time based index (Index based on the timestamp). Read operations are done via an alias (mandatory) and write operations on a specified index at the time of indexing the document. The definition of a template is mandatory.
- Rollover index. Read operations are done via an alias and write operations are done on a write alias.

## Versions

|   spring-data-elasticsearch-rest   |   spring-boot    |   elasticsearch  |
|:----------------------------------:|:----------------:|:----------------:|
|   1.1.1                            |       2.1.x      |       6.5.x      |
|   1.0.2                            |       2.1.x      |       6.4.2      |
|   1.0.1                            |       2.1.x      |       6.4.2      |
|   1.0.0                            |       2.1.x      |       6.4.2      |

## Maven dependency

```xml
<dependency>
    <groupId>com.github.ydespreaux.spring.data</groupId>
    <artifactId>spring-data-elasticsearch-rest-starter</artifactId>
    <version>1.0.1</version>
</dependency>
```

## Mapping domain class

### Field annotations

##### @Id / @Version

The org.springframework.data.annotation.Id annotation is used to declare an attribute representing the identifier of the document. The type of the attribute must be of type String.
An annotated @Id attribute is not stored in the document (stored in the _id metadata).

```java
public class MyBean {
    @Id
    private String id;
}
```
 
The annotation org.springframework.data.annotation.Version is used to declare an attribute representing the version of the document. The type of the attribute must be of type Long.
An annotated @Version attribute is not stored in the document (stored in the metadata version).

```java
public class MyBean {
    @Version
    private Long version;
}
```

##### @IndexName

The IndexName annotation is used to declare an attribute representing the name of the index in which the document is indexed. The type of the attribute must be of type String.
An annotated attribute @IndexName is not stored in the document (stored in the metada _index), it's initialized when reading a document.
An annotated attribute @IndexName is read-only mode.

```java
public class MyBean {
    @IndexName
    private String indexName;
}
```

##### @Score

The Score annotation is used to inject the _score value of a document during a search (without sorting). The type of the attribute must be Float type.
An annotated attribute @Score is read-only mode.

```java
public class MyBean {
    @Score
    private Float score;
}
```
 
##### @CompletionField

The CompletionField annotation allows the use of elasticsearch completion. The completion offers a feature of autocomplete / search on demand.
It is a navigation feature that guides users to relevant results during typing, improving the accuracy of the search.
The type of the attribute must be of type Completion or Collection<Completion>.

```java
public class MyBean {
    @CompletionField
    private Completion suggest;
}
```

The mapping of the entity must be declared with the completion type.

```json
{
  "mappings": {
    "myType": {
      "properties": {
        "suggest": {
          "type": "completion"
        }
      }
    }
  }
}
```

##### @ParentId (version 1.0.2)

The ParentId annotation defines an attribute containing the identifier of the parent document. This annotaton can not be used in an @Child annotated document.
The type of the attribute must be String type.

```java
@Child(type = "child_type")
public class MyBean extends ParentBean{
    @ParentId
    private String parentId;
}
```

##### @ScriptedField (version 1.0.2)

The ScriptedField annotation defines an attribute whose value matches a custom expression. This attribute is not added to the document when it is indexed.

```java
@IndexedDocument(
        index = @Index(
                name = "my_bean",
                type = "_doc"
        )
)
public class MyBean {

    private String name;
    
    @ScriptedField
    private String scriptedName;
}
```

Sample query:

```java
SearchQuery searchQuery = new NativeSearchQuery.NativeSearchQueryBuilder()
        .withQuery(matchAllQuery())
        .withScriptField(new ScriptField("scriptedName",
                new Script(ScriptType.INLINE, "painless", "doc['name'].value.toUpperCase()", Collections.emptyMap())))
        .build();
List<MyBean> entities = this.operations.search(searchQuery, MyBean.class);
```

### Geo Shape data type

|   GeoShape type       |   Type                                                                                |
|:---------------------:|:-------------------------------------------------------------------------------------:|
|   point               |   com.github.ydespreaux.spring.data.elasticsearch.core.geo.PointShape                 |
|   multipoint          |   com.github.ydespreaux.spring.data.elasticsearch.core.geo.MultiPointShape            |
|   linestring          |   com.github.ydespreaux.spring.data.elasticsearch.core.geo.LinestringShape            |
|   multilinestring     |   com.github.ydespreaux.spring.data.elasticsearch.core.geo.MultiLinestringShape       |
|   polygon             |   com.github.ydespreaux.spring.data.elasticsearch.core.geo.PolygonShape               |
|   multipolygon        |   com.github.ydespreaux.spring.data.elasticsearch.core.geo.MultiPolygonShape          |
|   envelope            |   com.github.ydespreaux.spring.data.elasticsearch.core.geo.EnvelopeShape              |
|   circle              |   com.github.ydespreaux.spring.data.elasticsearch.core.geo.CircleShape                |
|   geometrycollection  |   com.github.ydespreaux.spring.data.elasticsearch.core.geo.GeometryCollectionShape    |

The use of GeoShape types requires the addition of the following dependencies:

```xml
<dependency>
    <groupId>org.locationtech.spatial4j</groupId>
    <artifactId>spatial4j</artifactId>
    <version>0.7</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>org.locationtech.jts</groupId>
    <artifactId>jts-core</artifactId>
    <version>1.16.1</version>
</dependency>
```

### Description of documents

#### @IndexedDocument

The @IndexedDocument annotation allows you to define elasticsearch documents for the following index types:
- Simple index with or without alias
- Time-based index

##### Attributes description

###### @IndexedDocument


|   Attribut   |   Type    |   Mandatory  |     Description     |
|:------------:|:---------:|:------------:|:-------------------:|
|   index      |   Index   |       Yes    | Index description   |
|   alias      |   Alias   |       No     | Alias description   |

The alias attribute is required for a time-based index.

###### @Index

|   Attribut              |                 Type                    |   Mandatory   |           Default value       |     Description                                                               |
|:-----------------------:|:---------------------------------------:|:-------------:|:-----------------------------:|:-----------------------------------------------------------------------------:|
| name                    | String                                  |     No        |                               |   Index name                                                                  |
| type                    | String                                  |     No        |   _doc                        |   Defined the document type                                                   |
| indexPattern            | String                                  |     No        |                               |   Index pattern for index time based                                          |
| indexTimeBasedSupport   | Class<? extends IndexTimeBasedSupport>  |     No        |   IndexTimeBasedSupport.class |   Defined the IndextimeBasedSupport for determinate the current index name    |
| createIndex             | boolean                                 |     No        |   true                        |   Create index on startup                                                     |
| settingsAndMappingPath  | String                                  |     No        |                               |   Settings json file path                                                     |


###### @Alias

|   Attribut      |    Type |   Mandatory   | Description    |
|:---------------:|:-------:|:-------------:|:--------------:|
| name            | String  |    Yes        | Alias name     |
| filter          | String  |    No         | Alias filter   |
| indexRouting    | String  |    No         | Index routing  |
| searchRouting   | String  |    No         | Search routing | 

The example below defines a document with an simple index, an alias, and a json mapping file

```java
@IndexedDocument(
        alias = @Alias(name = "read_products"),
        index = @Index(
                name = "write_products",
                type = "product",
                settingsAndMappingPath = "classpath:indices/product.json"
        )
)
public class Product {

    @Id
    private String id;
    private List<String> title;
    private String name;
    private String description;
    private String text;
    private List<String> categories;
    private Float price;
    private Integer popularity;
    private boolean available;
}
```

###### Simple index

Below is the corresponding json mapping file product.json:

```json
{
    "settings" : {
        "number_of_shards" : 1,
        "refresh_interval" : "1s",
        "number_of_replicas" : 1,
        "store.type": "fs"
    },
    "aliases" : {
        "write_products" : {}
    },
    "mappings": {
      "product": {
        "properties": {
          "description": {
            "type": "text"
          },
          "text": {
            "type": "text"
          },
          "categories": {
            "type": "text"
          },
          "price": {
            "type": "double"
          },
          "popularity": {
            "type": "integer"
          },
          "available": {
            "type": "boolean"
          }
        }
      }
    }
}
```

###### Index Time-based

Declaring a document using time-based indexes requires the definition of a template and an alias for read operations. The index used during write operations is determined by a specified pattern.
The template must contain the attributes "index_patterns", "aliases" . The attributes "settings", "order" and "mapping" are optional.

Templates can be managed by the application or manually using kibana or curl queries for instance.

Sample template managed by the application:

Adding the article.template file in json format to the src/resources/templates directory:

```json
{
    "index_patterns": "article-*",
    "settings" : {
        "number_of_shards" : 1,
        "refresh_interval" : "1s",
        "number_of_replicas" : 1,
        "store.type": "fs"
    },
    "aliases" : {
        "articles" : {}
    },
    "mappings": {
      "article": {
        "properties": {
          "name": {
            "type": "text",
            "copy_to": "search_fields",
            "fielddata": true
          },
          "description": {
            "type": "text",
            "copy_to": "search_fields"
          },
          "entrepot": {
            "type": "text",
            "copy_to": "search_fields",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "search_fields": {
            "type": "text"
          }
        }
      }
    }
}
```

The extension of the template file can be any extension. The name of the template is the name of the file.

Adding configuration settings to the application.properties or application.yml file:

```text
spring:
  elasticsearch:
    template:
      action: CREATE
      scripts: classpath:templates/article.template
```

Attribute 'spring.elasticsearch.template.action' : defines the action to be performed on the templates.
The possible actions are: 
- NONE: No template will be added to the elasticsearch server
- CREATE: If the template does not exist on the server then the template is created
- CREATE_OR_UPDATE: The template is either created or updated.

Attribute 'spring.elasticsearch.template.scripts' : set the list of templates (separated by commas)

The example below defines a Java bean Article with an index time-based:

```java
@IndexedDocument(
        alias = @Alias(name = "${spring.elasticsearch.aliases.article"),
        index = @Index(
                type = "article",
                indexPattern = "${spring.elasticsearch.index-pattern.article}",
                indexTimeBasedSupport = ArticleTimeBasedSupport.class
        )
)
public class Article {

    @Id
    private String documentId;
    @Version
    private Long documentVersion;
    private String name;
    private String description;
    private EnumEntrepot entrepot;

    public enum EnumEntrepot {
        E1, E2, E3, UNKNOWN
    }

}
```

The value of the attributes of the annotation set can be externalized in the springboot configuration file.

```text
spring:
  elasticsearch:
    template:
      action: CREATE
      scripts: classpath:templates/article.template
    index-pattern:
      article: "'article-%s-'yyyy"
    aliases:
      article: articles
```

Items are indexed based on the current year and the name of the warehouse. For example, the name of an article index will look like 'article-e1-2019'.

By default, the library takes into account the date expressions. If the pattern takes into account attributes of the document, it is necessary to define a class that extends from the class IndexTimeBasedSupport.

```java
public class ArticleTimeBasedSupport extends IndexTimeBasedSupport<Article> {
    /**
     * Génère le nom d'un index de type time-based en fonction d'une date et d'un document à indexer.
     *
     * @param indexParameter
     * @return
     */
    @Override
    public String buildIndex(IndexTimeBasedParameter<Article> indexParameter) {
        return String.format(indexParameter.generateIndexWithTimeEvent(),
                indexParameter.getDocument() == null ? Article.EnumEntrepot.UNKNOWN : indexParameter.getDocument().getEntrepot())
                .toLowerCase();
    }
}
```

#### @RolloverDocument

Declaring a document using indexes rollover requires the definition of an alias for read operations and an another alias for write operations.

```java
@Getter
@Setter
@RolloverDocument(
        alias = @Alias(name = "read-tracks"),
        index = @Index(name = "tracks"),
        rollover = @Rollover(
                alias = @Alias(name = "write-tracks"),
                maxSize = "10gb",
                trigger = @Trigger(true)
        )
)
public class Track {

    private String name;
    private Integer number;
    private Integer length;

}
```

##### Manage index rollover

If you have the x-pack plugin then using policies to manage index rollover. 

If not, you can enable a trigger as in the above example using the trigger attribute.
The @Trigger annotation has the following attributes:

| Attribut | Data type | Description | Default value |
|:--------:|:-----------:|:--------------:|:---------:| 
| value | boolean | enabled the trigger | false |
| enabled | String | enabled the trigger | |
| cronExpression | String | The cron expression for management index rollover | 0 */1 * * * * |

#### @Parent / @Child (version 1.0.2)

Parent/Child annotation defines a parent-child relationship of a document. 

The parent's document and the child's document must be contained in the same index and must have the same type.

A parent document must be annotated with the @Parent annotation. A child document must be annotated with the @Child annotation.
The Java bean representing the child document must extend from the Java bean representing the parent.

In order to define a parent-child relationship, the document must have an attribute of type Join.

The mapping defines the relations between parent and child as below:

```json
{
  "mappings": {
    "myType": {
      "properties": {
        "description" : {
          "type": "text"
        },
        "join_field": {
          "type": "join",
          "relations": {
            "question": "answer",
            "answer": "vote"
          }
        }
      }
     }
  }
}
```

Here is an example describing a parent document:

```java
@IndexedDocument(index=@Index(name="my_index", type="my_type", settingsAndMappingPath = "classpath:scripts/my_index.index"))
@Parent(name="join_field", type="question")
public class Question {
    private String description;
}
```

When indexing a child document, the join attribute is automatically injected into the source of the document as follow:

```text
PUT my_index/my_type/1
{
  "description": "This is an question",
  "join_field": {
    "name": "question"
  }
}
```

Here is an example describing a child document:

```java
@Child(type="answer", routing = "1")
public class Answer extends Question{

    @ParentId
    private String parentId;
}
```

The routing value is mandatory because parent and child documents must be indexed on the same shard. For the same reason, the class of the child is not annotated by @IndexedDocument because the child inherits the mapping from the parent.

The attribute parentId is not mapping. The type of the attribute must be of type String.
When indexing a child document, the value of the attribute is automatically injected into the source of the document as follow:

```text
PUT my_index/my_type/2?routing=1 
{
  "description": "This is an answer",
  "join_field": {
    "name": "answer", 
    "parent": "1" 
  }
}
```

If multiple parent join levels are defined, the child document must be annotated with the child annotation and the isParent attribute must be true.

Here is an example describing a child document:

```java
@Child(type="vote", routing = "1", isParent = true)
public class Vote extends Answer{

    private Integer stars;
}
```

## Elasticsearch Repositories

### Introduction

Repository support can be enabled by annotating through JavaConfig.

```java
@Configuration
@EnableElasticsearchRepositories(basePackages = {
        "com.github.ydespreaux.sample.elasticsearch.repositories"
})
public class ElasticsearchConfiguration {
}
```

Repositories are enabled using the @EnableElasticsearchRepositories annotation, which has essentially the same attributes as the XML namespace. If no base package is configured, it will use the one in which the configuration class resides.

### Query methods

#### Query lookup strategies

The Elasticsearch module supports all basic query building feature as String,Abstract,Criteria or have it being derived from the method name.

Declared queries
Deriving the query from the method name is not always sufficient and/or may result in unreadable method names. In this case one might make either use of @Query annotation (see Using @Query Annotation ).

#### Query creation

Generally the query creation mechanism for Elasticsearch works as described in Query methods . Here’s a short example of what a Elasticsearch query method translates into:

```java
public interface BookRepository extends ElasticsearchRepository<Book, String> {

    Book findByName(String value);
}
```

The method name above will be translated into the following Elasticsearch json query:

```json
{"bool" : {"must" : {"field" : {"name" : "?"}}}}
```

A list of supported keywords for Elasticsearch is shown below.

|Keyword |	Sample	| Elasticsearch Query String|
|:-------:|:--------:|:-------------------------:|
| And | findByNameAndPrice | {"bool" : {"must" : [ {"field" : {"name" : "?"}}, {"field" : {"price" : "?"}} ]}} |
| Or | findByNameOrPrice | {"bool" : {"should" : [ {"field" : {"name" : "?"}}, {"field" : {"price" : "?"}} ]}} |
| Is | findByName | {"bool" : {"must" : {"field" : {"name" : "?"}}}} |
| Not | findByNameNot | {"bool" : {"must_not" : {"field" : {"name" : "?"}}}} |
| Between | findByPriceBetween | {"bool" : {"must" : {"range" : {"price" : {"from" : ?,"to" : ?,"include_lower" : true,"include_upper" : true}}}}} |
| LessThanEqual | findByPriceLessThan | {"bool" : {"must" : {"range" : {"price" : {"from" : null,"to" : ?,"include_lower" : true,"include_upper" : true}}}}} |
| GreaterThanEqual | findByPriceGreaterThan | {"bool" : {"must" : {"range" : {"price" : {"from" : ?,"to" : null,"include_lower" : true,"include_upper" : true}}}}} |
| Before | findByPriceBefore | {"bool" : {"must" : {"range" : {"price" : {"from" : null,"to" : ?,"include_lower" : true,"include_upper" : true}}}}} |
| After | findByPriceAfter | {"bool" : {"must" : {"range" : {"price" : {"from" : ?,"to" : null,"include_lower" : true,"include_upper" : true}}}}} |
| Like | findByNameLike | {"bool" : {"must" : {"field" : {"name" : {"query" : "?*","analyze_wildcard" : true}}}}} |
| StartingWith | findByNameStartingWith | {"bool" : {"must" : {"field" : {"name" : {"query" : "?*","analyze_wildcard" : true}}}}} |
| EndingWith | findByNameEndingWith | {"bool" : {"must" : {"field" : {"name" : {"query" : "*?","analyze_wildcard" : true}}}}} |
| Contains/Containing | findByNameContaining | {"bool" : {"must" : {"field" : {"name" : {"query" : "?","analyze_wildcard" : true}}}}} |
| In | findByNameIn(Collection<String>names) | {"bool" : {"must" : {"bool" : {"should" : [ {"field" : {"name" : "?"}}, {"field" : {"name" : "?"}} ]}}}} |
| NotIn | findByNameNotIn(Collection<String>names) | {"bool" : {"must_not" : {"bool" : {"should" : {"field" : {"name" : "?"}}}}} |
| Near | findByLocationNear | {"match_all":{"boost":1.0}},"post_filter":{"geo_bounding_box":{"location":{"top_left":[?],"bottom_right":[?]}}} |
| Within | findByLocationWithin | {"query":{"match_all":{"boost":1.0}},"post_filter":{"geo_distance":{"location":[?],"distance":10000.0,"distance_type":"plane"}}} |
| True | findByAvailableTrue | {"bool" : {"must" : {"field" : {"available" : true}}}} |
| False | findByAvailableFalse | {"bool" : {"must" : {"field" : {"available" : false}}}} |
| OrderBy | findByAvailableTrueOrderByNameDesc | {"sort" : [{ "name" : {"order" : "desc"} }],"bool" : {"must" : {"field" : {"available" : true}}}} |

#### Using @Query Annotation

Declare query at the method using the @Query annotation.

```java
public interface ArticleRepository extends ElasticsearchRepository<Article, String> {
    @Query("{\"match\" : {\"entrepot\" : \"?0\"}}")
    List<Article> findByEntrepot(EnumEntrepot entrepot);
}
```

### Miscellaneous Elasticsearch Operation Support

This chapter covers additional support for Elasticsearch operations that cannot be directly accessed via the repository interface. It is recommended to add those operations as custom implementation as described in [Custom Implementations for Spring Data Repositories](https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/#repositories.custom-implementations).

#### Filter Builder
Filter Builder improves query speed.

```java
private ElasticsearchTemplate elasticsearchTemplate;

SearchQuery searchQuery = new NativeSearchQueryBuilder()
    .withQuery(matchAllQuery())
    .withFilter(boolFilter().must(termFilter("id", documentId)))
    .build();

List<SampleEntity> sampleEntities =
    elasticsearchTemplate.search(searchQuery,SampleEntity.class);
```

#### Using Scroll For Big Result Set

Elasticsearch has a scroll API for getting big result set in chunks. ElasticsearchTemplate has startScroll and continueScroll methods that can be used as below.

Using startScroll and continueScroll

```java
SearchQuery searchQuery = new NativeSearchQueryBuilder()
    .withQuery(matchAllQuery())
    .withIndices(INDEX_NAME)
    .withTypes(TYPE_NAME)
    .withFields("message")
    .withPageable(PageRequest.of(0, 10))
    .build();

Page<SampleEntity> scroll = elasticsearchTemplate.startScroll(Duration.ofSeconds(60), searchQuery, SampleEntity.class);

String scrollId = ((ScrolledPage) scroll).getScrollId();
List<SampleEntity> sampleEntities = new ArrayList<>();
while (scroll.hasContent()) {
    sampleEntities.addAll(scroll.getContent());
    scrollId = ((ScrolledPage) scroll).getScrollId();
    scroll = elasticsearchTemplate.continueScroll(scrollId, Duration.ofSeconds(60), SampleEntity.class);
}
elasticsearchTemplate.clearScroll(scrollId);
```

## Samples

https://github.com/ydespreaux/sample-project/tree/master/sample-spring-data-elasticsearch-rest
