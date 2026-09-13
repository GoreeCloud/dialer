# GoreeCloud Dialer

GoreeCloud Dialer is GoreeCloud's privacy-focused, intelligent Android calling application and call-management platform.

## Status

**Active Development / pre-Stable.** The repository contains a validated native Android foundation, `ACTION_DIAL` intake, a local keypad, default-dialer capability gating, a content-minimized `InCallService` lifecycle boundary, state-aware essential call-control contracts, and a fail-closed multi-SIM route-readiness path. Carrier call placement, incoming/ongoing call UI, user-facing in-call controls, default-role requests, screening, voicemail, recording, transcription, translation, AI assistance, Privacy Shield acceptance, Wardveil acceptance, Everkeep backup, and cross-device calling are **not** claimed as implemented until their runtime paths are built and verified.

## Canonical repository

`GoreeCloud/goreecloud-dialer` is the canonical source repository for GoreeCloud Dialer.

## Product principles

- **Communication:** ordinary calling must remain immediate, reliable, and usable without AI.
- **Privacy:** call content is highly sensitive; local processing is preferred where practical.
- **User control:** AI, automation, recording, blocking, cloud processing, and retention remain configurable and explainable.
- **Protection:** spam, scams, spoofing, impersonation, and nuisance calls are mitigated without overstating certainty.
- **Accessibility:** speech, text, transcription, translation, RTT, assistive technologies, and hands-free interaction are first-class.
- **Offline resilience:** core dialing must not depend on GoreeCloud servers.

## Android baseline

- Kotlin + Jetpack Compose
- namespace/application ID: `com.goreecloud.dialer`
- compile SDK: 36
- target SDK: 36
- minimum SDK: 29
- Java/Kotlin JVM target: 17
- development version: `0.1.0-dev`

## Current source foundation

- Compose application shell and local keypad
- `ACTION_DIAL` and `tel:` intent intake without call placement side effects
- Android telephony and `ROLE_DIALER` capability probe with the role-request gate still closed
- `InCallService` lifecycle boundary with process-local call sessions
- state-aware answer, decline/end, hold/resume, and DTMF execution contracts
- content-minimized public call snapshots containing only generated session IDs and lifecycle state
- fail-closed multi-SIM policy that preserves explicit user selection, requires choice when routes are ambiguous, and defers emergency routing to Android Telecom
- read-only active-subscription inventory exposing only normalized subscription IDs after Android grants `READ_PHONE_STATE`
- explicit user-triggered `READ_PHONE_STATE` permission request; the app does not prompt automatically
- non-emergency route-readiness preview that connects inventory to the pure routing policy without placing a call or changing Android's default SIM
- unit tests and Android CI covering unit tests plus debug assembly
- architecture, privacy, security, specifications, roadmap, feature, and user-manual documentation

## Runtime truth rule

A feature is never considered implemented merely because a UI surface, configuration switch, planned contract, issue, or mock exists. Runtime capability, platform/carrier support, permission/role state, privacy authorization, security acceptance, and operation result are separate facts.

The current SIM route preview is not call authorization. `ROLE_DIALER`, Telecom call placement, carrier behavior, emergency-number classification/routing, representative multi-SIM devices, permission-denial UX, physical-device accessibility, and production acceptance remain independent gates.

## Documentation

- [SPECIFICATIONS.md](SPECIFICATIONS.md)
- [FEATURES.md](FEATURES.md)
- [FEATURE-ROADMAP.md](FEATURE-ROADMAP.md)
- [USER-MANUAL.md](USER-MANUAL.md)
- [docs/architecture.md](docs/architecture.md)
- [docs/privacy.md](docs/privacy.md)
- [docs/security.md](docs/security.md)

## License

No repository license has been established yet. Do not infer licensing terms from other GoreeCloud repositories.
