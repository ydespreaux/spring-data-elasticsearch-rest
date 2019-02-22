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
|   1.0.0                            |       2.1.0      |       6.4.2      |

## Maven dependency

```xml
<dependency>
    <groupId>com.github.ydespreaux.spring.data</groupId>
    <artifactId>spring-data-elasticsearch-rest-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Mapping domain class

### Field annotations

##### Annotation @Id / @Version

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

##### Annotation @IndexName

The IndexName annotation is used to declare an attribute representing the name of the index in which the document is indexed. The type of the attribute must be of type String.
An annotated attribute @IndexName is not stored in the document, it is initialized when reading a document.

```java
public class MyBean {
    @IndexName
    private String indexName;
}
```

##### Annotation @Score

The Score annotation is used to inject the _score value of a document during a search (without sorting). The type of the attribute must be Float type.

```java
public class MyBean {
    @Score
    private Float score;
}
```
 
##### Annotation @CompletionField


The CompletionField annotation allows the use of elasticsearch completion. The completion offers a feature of autocomplete / search on demand.
It is a navigation feature that guides users to relevant results during typing, improving the accuracy of the search.
The type of the attribute must be of type Completion or Collection<Completion>.

```java
public class MyBean {
    @CompletionField
    private Completion suggest;
}
```

Le mapping de l'entité doit être déclaré avec le type completion d'elasticsearch.

```json
"mappings": {
  "myType": {
    "properties": {
      "suggest": {
        "type": "completion"
      }
    }
  }
}
```

### Documents


#### Annotation @IndexedDocument

@IndexedDocument

|   Attribut   |   Type    |   Mandatory  |     Description     |
|:------------:|:---------:|:------------:|:-------------------:|
|   index      |   Index   |       Yes    | Index description   |
|   alias      |   Alias   |       No     | Alias description   |

@Index

|   Attribut              |                 Type                    |   Mandatory   |           Default value       |     Description                                                               |
|:-----------------------:|:---------------------------------------:|:-------------:|:-----------------------------:|:-----------------------------------------------------------------------------:|
| name                    | String                                  |     No        |                               |   Index name                                                                  |
| type                    | String                                  |     Yes       |                               |   Defined the document type                                                   |
| indexPattern            | String                                  |     No        |                               |   Index pattern for index time based                                          |
| indexTimeBasedSupport   | Class<? extends IndexTimeBasedSupport>  |     No        |   IndexTimeBasedSupport.class |   Defined the IndextimeBasedSupport for determinate the current index name    |
| createIndex             | boolean                                 |     No        |   true                        |   Create index on startup                                                     |
| settingsAndMappingPath  | String                                  |     No        |                               |   Settings json file path                                                     |

@Alias

|   Attribut      |    Type |   Mandatory   |           Default value       |   Description
|:---------------:|:-------:|:-------------:|:-------------:|:-----------------------------:|
| name            | String  |    No         |     No        |                               |
| filter          | String  |    No                            |     Yes       |                               |
| indexRouting    | String  |    No                            |     No        |                               |
| searchRouting   | String  |    No        |   IndexTimeBasedSupport.class |



### Timebased Index

@IndexedDocument

### Rollover index

@RolloverDocument

### Projection

@ProjectionDocument

## Repository
### CRUD operations
### Query named method
### Query
### Custom implementation
