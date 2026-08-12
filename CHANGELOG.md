# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v26.2.2-mc26.2.x] - 2026-08-12

### Fixed

- Fix item duplication issues when interacting with storage item contents in backpacks from some mods

## [v26.2.1-mc26.2.x] - 2026-08-10

### Added

- Add support for viewing nested storage item contents, meaning e.g., shulker box contents in an ender chest can now be
  viewed by highlighting the shulker box and pressing any selection key (return, enter or space bar)
- Note that nested contents can only be viewed but not interacted with
- Going back to the previous storage item tooltip is possible by pressing the backspace key

### Changed

- Vanilla clients can no longer trigger any of the newly added item interactions
- The vanilla container contents tooltip (most commonly used for shulker boxes) is now only disabled when the mod is
  installed on both the client and server
- The control scheme (i.e., which mouse buttons to use for inserting and removing contents from storage items) is now
  configurable
- The previous scheme (from versions for Minecraft 1.21.11 and before) which only relied on the right mouse button is
  set as the default again

## [v26.2.0-mc26.2.x] - 2026-06-20

### Changed

- Update to Minecraft 26.2.x
