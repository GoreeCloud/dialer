# GoreeCloud Dialer

GoreeCloud Dialer is GoreeCloud's privacy-focused, intelligent Android calling application and call-management platform.

## Status

**Active Development / pre-Stable.** The repository contains a validated native Android foundation, `ACTION_DIAL` intake, a local keypad, default-dialer capability gating, a content-minimized `InCallService` lifecycle boundary, state-aware essential call-control contracts, fail-closed active-subscription inventory, explicit multi-SIM route policy, and a pre-call Telecom-account readiness coordinator.

Carrier call placement, incoming/ongoing call UI, user-facing in-call controls, default-role requests, screening, voicemail, recording, transcription, translation, AI assistance, Privacy Shield acceptance, Wardveil acceptance, Everkeep backup/recovery, GoreeCloud Identity runtime, GoreeCloud Sync runtime, GoreeCloud Mesh coordination, and cross-device calling are **not** claimed as implemented until their runtime paths are built and independently verified.

## Canonical repository

`GoreeCloud/goreecloud-dialer` is the canonical source repository for GoreeCloud Dialer.

## Product principles

- **Communication:** ordinary calling must remain immediate, reliable, and usable without AI.
- **Privacy:** call content is highly sensitive; local processing is preferred where practical.
- **User control:** AI, automation, recording, blocking, cloud processing, and retention remain configurable and explainable.
- **Protection:** spam, scams, spoofing, impersonation, and nuisance calls are mitigated without overstating certainty.
- **Accessibility:** speech, text, transcription, translation, RTT, assistive technologies, and hands-free interaction are first-class.
- **Offline resilience:** core dialing must not depend on GoreeCloud servers.
- **Fail-closed authority:** role, permission, SIM, Telecom-account, emergency, privacy, security, identity, and synchronization state are independent facts and must never be inferred from UI availability or presentation.

## Android baseline

- Kotlin + Jetpack Compose
- namespace/application ID: `com.goreecloud.dialer`
- compile SDK: 36
- target SDK: 36
- minimum SDK: 29
- Java/Kotlin JVM target: 17
- development version: `0.1.0-dev`

## GLAZE UI V1.4.1 Development mapping

Dialer has a bounded first-party mapping to current Official Stable **GLAZE UI V1.4.1 / `1.4.1`** at exact signed main revision `4fab9da0fad2e5c974e0e66ec88632c61745751c`, with V1.4.0 retained as the immediate shared rollback baseline.

The mapping is intentionally narrow for a sensitive calling surface:

- ordinary interaction floor: 48 dp;
- Touch Assistance reference floor: 56 dp;
- Environmental Color Memory influence: 0%;
- phone numbers, contacts, SIM/carrier identity, call state, privacy/security state, telemetry, and remote context cannot drive optical adaptation;
- Reduced Transparency and forced-color behavior fail closed to a solid-accessible source state;
- application-level Optical Engine, Reduced Transparency, Increased Contrast, physical-device, manual assistive-technology, human-visual, and representative-performance acceptance flags remain false;
- GLAZE UI presentation cannot manufacture emergency classification, carrier route, role, permission, call authorization, privacy, security, Identity, Sync, recording, or account truth.

Shared V1.4.1 qualification does not auto-certify Dialer. This is **Development source evidence only** and does not establish complete rendered/accessibility/device acceptance, production approval, Release Candidate, or Stable qualification.

## GoreeCloud Platform Contract 0.3

`goreecloud.platform.yaml` evaluates all eight Integral Platform Systems independently:

- GoreeCloud Manager
- Privacy Shield
- Wardveil Security
- Everkeep
- GLAZE UI
- GoreeCloud Mesh
- GoreeCloud Identity
- GoreeCloud Sync

Manager, Privacy Shield, Wardveil Security, Everkeep, Mesh, Identity, and Sync remain `applicable-blocked`; Glaze remains `applicable-migration-required` until Dialer-local acceptance is complete; overall product conformance remains `nonconformant`.

The reusable validator is pinned to accepted central Contract 0.3 main revision `908701c6795ffcd608bd3d8a1e787395a04f1d62`. GoreeCloud Sync is not inferred from Android Telecom state, SIM inventory, future call history, local portability, Mesh coordination, or Everkeep backup/recovery.

## Current source foundation

- Compose application shell and local keypad
- `ACTION_DIAL` and `tel:` intent intake without call placement side effects
- Android telephony and `ROLE_DIALER` capability probe with the role-request gate still closed
- `InCallService` lifecycle boundary with process-local call sessions
- state-aware answer, decline/end, hold/resume, and DTMF execution contracts
- content-minimized public call snapshots containing only generated session IDs and lifecycle state
- permission-aware active carrier-subscription inventory
- fail-closed multi-SIM routing policy
- exact subscription-to-enabled-`PhoneAccountHandle` resolver
- pre-call readiness coordinator that re-reads current subscriptions, re-runs routing policy, and requires one exact enabled Telecom account before reporting readiness
- current Stable GLAZE UI V1.4.1 source/theme contract with calling-sensitive optical inputs prohibited
- Platform Contract 0.3 declaration with all eight systems independently represented
- unit tests and Android CI covering unit tests plus debug assembly
- architecture, privacy, security, specifications, roadmap, feature, and user-manual documentation

## Route-readiness boundary

The Development UI can inspect current SIM inventory, require explicit selection when multiple active subscriptions are present, and run a pre-call Telecom route-readiness check.

A readiness result is **not** call authorization. The current coordinator does not call `TelecomManager.placeCall()`, does not enable the Call button, does not persist `PhoneAccountHandle` details, and does not silently switch SIMs when an explicit selection disappears. Emergency routing remains delegated to Android Telecom.

## Runtime truth rule

A feature is never considered implemented merely because a UI surface, configuration switch, planned contract, issue, test, source mapping, or mock exists. Runtime capability, platform/carrier support, permission/role state, privacy authorization, security acceptance, Identity authority, Sync authority, recovery authority, operation result, and release acceptance are separate facts.

## Documentation

- [SPECIFICATIONS.md](SPECIFICATIONS.md)
- [FEATURES.md](FEATURES.md)
- [FEATURE-ROADMAP.md](FEATURE-ROADMAP.md)
- [USER-MANUAL.md](USER-MANUAL.md)
- [docs/architecture.md](docs/architecture.md)
- [docs/privacy.md](docs/privacy.md)
- [docs/security.md](docs/security.md)
- [goreecloud.platform.yaml](goreecloud.platform.yaml)

## License

No repository license has been established yet. Do not infer licensing terms from other GoreeCloud repositories.
