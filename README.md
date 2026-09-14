# GoreeCloud Dialer

GoreeCloud Dialer is GoreeCloud's privacy-focused, intelligent Android calling application and call-management platform.

## Status

**Active Development / pre-Stable.** The repository contains a native Android foundation, `ACTION_DIAL` intake, a local keypad, default-dialer capability gating, a content-minimized `InCallService` lifecycle boundary, state- and capability-aware essential call controls, Development incoming/ongoing call presentation, a user-driven in-call DTMF keypad, live conference-capability/relationship tracking with guarded Development conference controls, generic Telecom call-direction/terminal-outcome evidence, a dormant policy-gated `TelecomManager.placeCall` boundary, a fail-closed default-dialer role-consent surface, Telecom-evidenced mute/unmute control, API 34+ `CallEndpoint` routing, permission-aware call-capable phone-account discovery with anonymous explicit route selection, and explicit Development capability evidence for outgoing calls, multi-SIM routing, Wi-Fi Calling/IMS state, and supplementary services. Emergency and indeterminate-emergency numbers always delegate phone-account routing back to Android Telecom.

Carrier call placement is not accepted, conference operations are not production-accepted, call direction/outcome evidence is transient rather than durable call history, caller identity, endpoint device names, phone-account labels, phone numbers, carrier names, SIM identifiers, subscription identifiers, and provider-specific disconnect reason strings are not projected into these Development surfaces, ringtone ownership is not claimed, and production incoming/ongoing UI acceptance is incomplete. The role-consent surface can launch Android's prompt only after the capability model reaches `RoleRequired` and the independent GoreeCloud preparer returns `Prepared`; current Development acceptance keeps that path blocked. Precise Wi-Fi Calling/IMS and supplementary-service state are not inferred from connectivity or carrier heuristics; they remain unavailable until GoreeCloud has an authorized Android platform signal and an accepted observer. Legacy pre-Android-14 speaker/Bluetooth routing is intentionally unavailable rather than relying on deprecated APIs. Screening, voicemail, recording, transcription, translation, AI assistance, Privacy Shield acceptance, Wardveil acceptance, Everkeep backup, and cross-device calling are also **not** claimed as implemented until their runtime paths are built and verified.

CI now includes executable managed-emulator acceptance on Android 11 / API 30 and Android 14 / API 34. Those lanes verify deterministic Android runtime contracts and modern API presence, but they do not substitute for physical-device, carrier, SIM/eSIM, emergency-network, OEM, live audio-route, or human acceptance.

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
- Android Gradle Plugin: 8.10.1
- Java/Kotlin JVM target: 17
- development version: `0.1.0-dev`

A future move to compile SDK 37 / AGP 9.1+ is tracked as a separate platform migration because current stable AndroidX/Compose releases require that newer build baseline. The project intentionally retains its last verified SDK-36-compatible dependency set until that migration is accepted as a complete toolchain change.

## Current source foundation

- Compose application shell and local keypad
- `ACTION_DIAL` and `tel:` intent intake without call placement side effects
- Android telephony and `ROLE_DIALER` capability probe with the role-request acceptance gate still closed
- explicit default-dialer acceptance model for dial intent, service, placement, incoming UI, and ongoing UI
- role-request preparation policy that cannot produce Android's consent intent until all GoreeCloud application requirements are accepted
- Development default-dialer role surface that enables a request only for `RoleRequired`, then re-checks the preparer before launching Android's explicit consent prompt
- Development capability and phone-account evidence is re-probed when the Activity resumes and after phone-state permission results so role/permission/platform state is not frozen from initial composition
- dormant outgoing-call placement adapter requiring telephony, Telecom, GoreeCloud acceptance, active default-dialer role, and `CALL_PHONE`
- truthful outgoing-call capability evidence that reports the placement boundary as source-present while keeping carrier-call runtime acceptance incomplete
- `READ_PHONE_STATE`-guarded discovery of Android call-capable phone accounts
- process-local opaque phone-account route IDs; account handles, labels, phone numbers, carrier names, SIM identifiers, and subscription identifiers are not projected
- phone-account handle authority is revoked from process memory when permission, telephony, or Telecom availability is lost
- Development calling-account surface with explicit `System default` or anonymous `Phone account N` selection
- truthful multi-SIM capability evidence that reports the routing boundary as source-present while keeping production multi-SIM acceptance incomplete
- precise Wi-Fi Calling/IMS state remains explicitly unavailable rather than inferred; the current manifest does not request `READ_PRECISE_PHONE_STATE`
- supplementary-service state remains explicitly unavailable until Android exposes an authorized platform signal consumed by an accepted GoreeCloud observer
- stale explicit phone-account selections fail closed rather than silently falling back to another account
- emergency-number classification uses Android telephony evidence; emergency and unknown classification force Android-managed phone-account routing
- explicit phone-account selection is added to `TelecomManager.placeCall` only for numbers Android classifies as non-emergency
- `InCallService` lifecycle boundary with process-local call sessions
- API-aware lifecycle-state reader using `Call.Details.state` on API 31+ with the deprecated `Call.state` path isolated to the supported API 29-30 compatibility fallback
- observable content-minimized call snapshots exposed as a `StateFlow`
- Android Telecom direction evidence mapped only to `INCOMING`, `OUTGOING`, or `UNKNOWN`
- generic terminal outcomes derived only after `DISCONNECTED` from `DisconnectCause.code`, including missed, rejected, busy, local, remote, error, canceled, restricted, answered-elsewhere, and pulled-call categories
- provider-specific disconnect labels/descriptions/reason strings remain outside public runtime state, and terminal outcomes are transient rather than durable Recents history
- state-aware answer, decline/end, hold/resume, and DTMF execution contracts
- live `Call.Details` capability evidence for hold, mute, and conference management/merge/swap/separate; missing details fail closed
- user-driven in-call DTMF keypad with bounded tone pulses, overlap prevention, and guaranteed stop attempts from coroutine cleanup
- conference relationships projected only as generated session IDs: parent, children, and Telecom-reported conferenceable sessions
- pairwise conference requests allowed only when `Call.getConferenceableCalls()` identifies the target; merge/swap/separate requests require their live Telecom capability bits
- conference operations report request submission rather than claiming network completion
- process-local mute-state evidence from Telecom; mute UI is exposed only when an active/held call reports mute support
- API 34+ call endpoint discovery through `onAvailableCallEndpointsChanged` and routing through `requestCallEndpointChange`
- endpoint UI exposes only generated route IDs/categories; Telecom endpoint names and device identities are not projected
- endpoint requests distinguish submitted, succeeded, and failed evidence
- pre-API-34 endpoint switching remains explicitly unavailable instead of using deprecated `setAudioRoute`
- Development ongoing-call UI exposing session IDs, lifecycle state, generic direction/terminal outcome, accepted controls, capability explanations, DTMF, conference controls, mute state, endpoint categories, and explicit operation results
- Development incoming-call `CallStyle` notification/full-screen UI with Answer and Decline actions
- incoming notifications transition to ongoing `CallStyle` notifications with an explicit End action and route back to the in-call UI
- notification actions use an explicit non-exported receiver and process-local Telecom session IDs
- `POST_NOTIFICATIONS` and full-screen-intent capability are checked at runtime; unavailable full-screen access degrades to notification presentation
- ringtone ownership remains intentionally undeclared until a dedicated ringtone path is implemented and validated
- Android-managed automatic backup is disabled and explicit platform rules exclude app-managed storage from cloud backup and device-to-device transfer; this is not Everkeep implementation or recovery acceptance
- Android CI uses Node-24-capable Action lines and branch-scoped concurrency so newer commits cancel superseded runs
- CI validates JVM unit tests, Android lint, debug APK assembly, minified release-variant assembly, and instrumentation-test APK compilation
- CI then executes deterministic managed-emulator acceptance on Android 11 / API 30 and Android 14 / API 34
- API 34 acceptance checks modern `CallEndpoint`/`InCallService` API availability and full-screen-intent capability probing without claiming successful endpoint switching on a live call
- release-variant assembly exercises release resources, R8/minification, and packaging configuration; it does **not** prove signing, distribution readiness, device acceptance, or Stable status
- physical-device and carrier acceptance is tracked separately in `docs/physical-device-acceptance.md` and Issue #14
- Android platform modernization, CI evidence boundaries, testing layers, release gates, and physical-device evidence requirements are documented separately
- architecture, privacy, security, specifications, roadmap, feature, and user-manual documentation

## Runtime truth rule

A feature is never considered implemented merely because a UI surface, configuration switch, planned contract, issue, mock, compiled test APK, release-variant build, managed-emulator pass, or green CI run exists. Runtime capability, platform/carrier support, permission/role state, privacy authorization, security acceptance, request submission, verified operation result, physical-device validation, carrier validation, signing/distribution evidence, and human release validation are separate facts.

## Documentation

- [SPECIFICATIONS.md](SPECIFICATIONS.md)
- [FEATURES.md](FEATURES.md)
- [FEATURE-ROADMAP.md](FEATURE-ROADMAP.md)
- [USER-MANUAL.md](USER-MANUAL.md)
- [docs/architecture.md](docs/architecture.md)
- [docs/privacy.md](docs/privacy.md)
- [docs/security.md](docs/security.md)
- [docs/platform-modernization.md](docs/platform-modernization.md)
- [docs/ci-validation.md](docs/ci-validation.md)
- [docs/testing.md](docs/testing.md)
- [docs/release-gates.md](docs/release-gates.md)
- [docs/physical-device-acceptance.md](docs/physical-device-acceptance.md)

## License

No repository license has been established yet. Do not infer licensing terms from other GoreeCloud repositories.
