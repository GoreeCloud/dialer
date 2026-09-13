# GoreeCloud Dialer

GoreeCloud Dialer is GoreeCloud's privacy-focused, intelligent Android calling application and call-management platform.

## Status

**Active Development / pre-Stable.** The repository contains a validated native Android foundation, `ACTION_DIAL` intake, a local keypad, default-dialer capability gating, a content-minimized `InCallService` lifecycle boundary, state-aware essential call-control contracts, a Development ongoing-call control surface, a dormant policy-gated `TelecomManager.placeCall` boundary, and Development incoming/ongoing `CallStyle` notification continuity. Carrier call placement is not accepted, caller identity is not projected into the call surfaces, ringtone ownership is not claimed, production incoming/ongoing UI acceptance is incomplete, and default-role requests remain disabled. Screening, voicemail, recording, transcription, translation, AI assistance, Privacy Shield acceptance, Wardveil acceptance, Everkeep backup, and cross-device calling are also **not** claimed as implemented until their runtime paths are built and verified.

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
- explicit default-dialer acceptance model for dial intent, service, placement, incoming UI, and ongoing UI
- dormant outgoing-call placement adapter requiring telephony, Telecom, GoreeCloud acceptance, active default-dialer role, and `CALL_PHONE`
- `InCallService` lifecycle boundary with process-local call sessions
- observable content-minimized call snapshots exposed as a `StateFlow`
- state-aware answer, decline/end, hold/resume, and DTMF execution contracts
- Development ongoing-call UI exposing only session IDs, lifecycle state, accepted controls, and explicit operation results
- Development incoming-call `CallStyle` notification/full-screen UI with Answer and Decline actions
- incoming notifications transition to ongoing `CallStyle` notifications with an explicit End action and route back to the in-call UI
- notification actions use an explicit non-exported receiver and process-local Telecom session IDs
- `POST_NOTIFICATIONS` and full-screen-intent capability are checked at runtime; unavailable full-screen access degrades to notification presentation
- ringtone ownership remains intentionally undeclared until a dedicated ringtone path is implemented and validated
- unit tests and Android CI covering unit tests plus debug assembly
- architecture, privacy, security, specifications, roadmap, feature, and user-manual documentation

## Runtime truth rule

A feature is never considered implemented merely because a UI surface, configuration switch, planned contract, issue, or mock exists. Runtime capability, platform/carrier support, permission/role state, privacy authorization, security acceptance, and operation result are separate facts.

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
