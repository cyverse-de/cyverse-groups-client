# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions
of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]
### Added
- A new function `lookup-subjects` that can be used to look up multiple subjects by ID.

## [0.1.4]
### Added
- A new parameter, `inheritance-level`, to `list-group-privileges` that allows privileges to be filtered by whether
  they're assigned directly or indirectly to a subject.

## [0.1.3]
### Added
- An additional arity for `list-group-privileges` that allows privileges to be filtered by privilege name, subject
  ID, subject type ID, or any combination thereof.

## [0.1.2]
### Added
- An additional arity for `revoke-group-privileges` that allows privileges to be revoked for several users in a single
  API call.
### Changed
- Fixed some typos in the README.

## [0.1.1]
### Added
- An additional arity for `update-group-privileges` that accepts additional parameters for the request.

## 0.1.0
### Initial Release

[Unreleased]: https://github.com/cyverse-de/cyverse-groups-client/compare/0.1.4...HEAD
[0.1.4]: https://github.com/cyverse-de/cyverse-groups-client/compare/0.1.3...0.1.4
[0.1.3]: https://github.com/cyverse-de/cyverse-groups-client/compare/0.1.2...0.1.3
[0.1.2]: https://github.com/cyverse-de/cyverse-groups-client/compare/0.1.1...0.1.2
[0.1.1]: https://github.com/cyverse-de/cyverse-groups-client/compare/0.1.0...0.1.1
