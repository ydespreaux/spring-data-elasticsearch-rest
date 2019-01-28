# Changelog

## [Unreleased]
### Added
- Criteria query for elasticsearch
- Query method name for elastricsearch repository
- Query annotation for elastricsearch repository
- Rollover index

### Changed
- Criteria query for jpa

### Fixed

## [0.0.3] - 2018-12-30
### Added
- JpaCriteriaRepository add generic methods for search with Criteria
- Class BeanValidation for checking constraints bean (spring-data-validation library)
- @Projection annotation

### Changed

### Fixed
- No more exception when start or continue an scroll that the index does not exist

## [0.0.2] - 2018-12-29
### Added
- spring-boot-starter-json dependency
- CHANGELOG.md file

### Changed
- Elasticsearch v6.4.2

### Fixed
- No more exception when refreshing an index that does not exist
- No more exception when clear an scroll that does not exist

## [0.0.1] - 2018-12-27
### Added
