# Changelog

## [Unreleased]

### Added

### Changed
- Pageable : Modified default value of parameter page.size to 10000 

- Dependency elasticsearch version from 6.5.0 to 6.7.0
- Changes break:
    - Annotation @Index : remove type attribut (index type fixed to '_doc')

### Fixed


## [1.1.2]
### Added
- Dependency spring-boot version from 2.1.0 to 2.1.4
- Dependency elasticsearch version from 6.4.2 to 6.5.0
- Reactive repository
### Fixed

## [1.0.3]

### Added
- Elasticsearch v6.4.2
- Time based index
- Rollover index
- Criteria query for elasticsearch
- Query named method
- Query annotation
- Add mapping annotations for parent / child
- ElasticsearchRepository : Add methods for search parent / child  
- Added multiple levels of join parent  
- Added Geo-shape type
- Script field
- Parent relationship with @Parent annotation
