# GoreeCloud Dialer Specifications

## Repository

Canonical repository: `GoreeCloud/goreecloud-dialer`.

## Status

Active Development / pre-Stable.

## Platform baseline

- Android native application
- Kotlin + Jetpack Compose
- `com.goreecloud.dialer`
- compile/target SDK 36
- minimum SDK 29
- JVM 17

## Source-complete foundation in this revision

- Android project/build shell
- launchable Compose Development surface
- capability-state contract
- telephony capability snapshot placeholder that explicitly reports currently unimplemented runtime integrations
- unit test proving the placeholder does not claim call capability
- architecture/privacy/security documentation
- CI definition

## Not yet implemented

Default-dialer role acquisition, carrier call initiation/reception, in-call service, call screening, visual voicemail, call recording, local persistence, caller reputation, Privacy Shield runtime integration, Wardveil runtime integration, Everkeep runtime integration, Call Assistant, local AI models, Glaze UI acceptance, and release validation.

## Product scope

The planned product scope is defined by the canonical GoreeCloud Dialer project specification and repository roadmap. Source documentation must continue to distinguish planned behavior from verified implementation.
