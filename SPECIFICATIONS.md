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
- Android telephony/default-dialer role probe that reads platform capability without requesting the role
- telephony capability snapshot that keeps role state separate from unimplemented call functions
- unit tests proving unsupported/unavailable/role-required/active mapping
- architecture/privacy/security documentation
- CI unit-test and debug-assembly validation

## Not yet implemented

Default-dialer role request eligibility, `ACTION_DIAL` handling, `InCallService`, carrier call initiation/reception, in-call controls, call screening, visual voicemail, call recording, local persistence, caller reputation, Privacy Shield runtime integration, Wardveil runtime integration, Everkeep runtime integration, Call Assistant, local AI models, Glaze UI acceptance, and release validation.

## Product scope

The planned product scope is defined by the canonical GoreeCloud Dialer project specification and repository roadmap. Source documentation must continue to distinguish planned behavior from verified implementation.
