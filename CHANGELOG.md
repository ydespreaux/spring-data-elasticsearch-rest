# Changelog

## [Unreleased]
### Added
- Dependency spring-boot version from 2.1.0 to 2.1.3
- Dependency elasticsearch version from 6.4.2 to 6.5.0
- Reactive repository (o)

### Changed

- Migration Junit 4 to Junit 5

### Fixed

## [1.0.2]
### Added
- Add mapping annotations for parent / child
- ElasticsearchRepository : Add methods for search parent / child  
- Added multiple levels of join parent  
- Added Geo-shape type
- Script field

### Changed
- Externalized @Parent attributes
- Migrate to JDK 11

### Fixed

## [1.0.1]
### Added
- Parent relationship with @Parent annotation

### Changed
- Migrate Gson to Jackson

### Fixed

## [1.0.0]
### Added
- Elasticsearch v6.4.2
- Time based index
- Rollover index
- Criteria query for elasticsearch
- Query named method
- Query annotation

### Changed

### Fixed
