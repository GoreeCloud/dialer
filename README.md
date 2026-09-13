# GoreeCloud Dialer

GoreeCloud Dialer is GoreeCloud's privacy-focused, intelligent Android calling application and call-management platform.

## Status

**Active Development / pre-Stable.** The repository now contains the native Android foundation and capability-state contracts. Carrier calling, default-dialer role integration, call screening, voicemail, recording, transcription, translation, AI assistance, Privacy Shield acceptance, Wardveil acceptance, Everkeep backup, and cross-device calling are **not** claimed as implemented until their runtime paths are built and verified.

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

This baseline follows current GoreeCloud Android conventions and is not itself a claim of feature completeness.

## Current source foundation

- Compose application shell
- explicit Development status UI
- reusable capability-state model separating unsupported, unavailable, permission/role-required, available, active, failed, and successful states
- initial telephony capability snapshot contract
- architecture, privacy, security, specifications, roadmap, feature, and user-manual documentation
- Android CI workflow

## Architecture direction

Capability domains are intentionally independent:

- `telephony` — Android Telecom role/state, calls, SIM routing, DTMF, audio routes, emergency-safe boundaries
- `contacts` — T9 search, favorites, contact preferences
- `history` — Recents, unified timeline, notes, search, retention
- `screening` — Call Screen prompts, transcripts, and rules
- `safety` — spam/scam signals, block/allow policy, explanations
- `voicemail` — visual/direct voicemail capability adapters
- `assistant` — Text Call, transcription, translation, Hold Assistant, menu navigation, summaries
- `intelligence` — local models and explicitly authorized remote processors
- `privacy` — Privacy Shield authorization, purpose, locality, retention, revocation
- `security` — Wardveil storage/trust/integrity evidence
- `continuity` — Everkeep backup/restore boundaries
- `ui` — Glaze UI calling, safety, privacy, voicemail, assistant, and accessibility surfaces

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

License file will be added as part of repository bootstrap completion; until then, do not infer licensing terms from other GoreeCloud repositories.
