# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]
### Added
- Created `CHANGELOG.md` and `README.md` files for project documentation.
- Integrated `PetService` launching logic in the Pet Library grid.
- Appended `loadSmartPet` function to deserialize `.vpet` objects efficiently.
- Allowed clickable interaction in the Pet Library to send serialized pet data to the foreground service via `Intent`.

### Fixed
- Fixed bug where newly compiled pets weren't automatically interactable without restarting the app state.