# GoreeCloud Dialer Feature Roadmap

## Phase 0 — Repository Bootstrap

Native Android source tree, build configuration, CI, source documentation, tests and truthful Development status.

**Current state:** CI validates JVM unit tests, Android lint, debug APK assembly, release-variant assembly with R8/minification, and instrumentation-test APK compilation. These are build-level evidence only; emulator, physical-device, carrier, signing/distribution, human validation, and Stable acceptance remain separate. The source manifest/provenance inventory exists and CI uses branch-scoped concurrency. Remaining repository-governance items, including a canonical license decision, still need completion before Phase 0 is closed.

## Phase 1 — Core Telephony

Default-dialer role, outgoing/incoming calls, call controls, DTMF, Recents, contacts, Favorites, SIM routing, conferencing and emergency-safe boundaries.

**Current state:** runtime telephony/`ROLE_DIALER` detection, lifecycle-resume capability refresh, `ACTION_DIAL` intake, a local keypad, a content-minimized `InCallService` lifecycle boundary, explicit extended Android lifecycle mapping, API-aware call-state reads with `Call.Details.state` on API 31+ and an isolated API 29-30 fallback, transient `INCOMING`/`OUTGOING`/`UNKNOWN` direction evidence, generic disconnected-call terminal outcomes, state-aware answer/decline/end/hold/resume/DTMF contracts, a user-driven in-call DTMF keypad, live hold/mute/conference capability evidence, content-minimized conference parent/child/conferenceable-session state, guarded pairwise conference/merge/swap/separate Development controls, observable process-local call snapshots, Development incoming-to-ongoing `CallStyle` presentation, a dormant policy-gated `TelecomManager.placeCall` boundary, a fail-closed role-request preparation policy plus Development role-consent surface, Telecom-evidenced mute/unmute control, API 34+ endpoint discovery/routing, `READ_PHONE_STATE`-guarded call-capable phone-account discovery, anonymous explicit phone-account selection, permission-lifetime revocation of cached account handles, emergency-safe account-routing policy, and explicit Development capability evidence for outgoing calls, multi-SIM routing, Wi-Fi Calling/IMS state, and supplementary services are implemented in source.

Generic call outcomes are derived only from Telecom disconnect codes once a call is disconnected; GoreeCloud does not infer missed/rejected/remote/local outcomes from UI actions. Provider-specific disconnect reasons are not projected, and this transient classification is not yet a durable Recents/history implementation.

Emergency or indeterminate-emergency numbers force Android-managed phone-account routing, stale explicit account selections fail closed, and conference controls are derived from live Telecom relationships/capability bits instead of call-count assumptions. Carrier call placement remains unaccepted, multi-SIM routing remains a Development boundary rather than a production-accepted capability, and precise Wi-Fi Calling/IMS or supplementary-service state is not inferred from connectivity or carrier heuristics. The current manifest does not request `READ_PRECISE_PHONE_STATE`; those states remain unavailable until Android exposes an authorized signal through a path GoreeCloud explicitly accepts. Conference requests are not production-accepted, caller identity, endpoint device names, phone-account labels/numbers/carrier or SIM identifiers remain absent from Development presentation, ringtone ownership is not claimed, production incoming/ongoing UI acceptance is incomplete, and pre-API-34 audio routing remains unavailable. The default-dialer role surface can launch Android's consent request only when the capability model reaches `RoleRequired` and the independent preparer returns `Prepared`; current acceptance keeps it blocked. Phone-account routing, DTMF, conferencing, direction, terminal-outcome presentation, and role-request wiring are therefore Development boundaries, not proof of production-accepted calling or durable history.

Remaining Phase 1 work includes production/device acceptance of outgoing and incoming calling, satisfying the acceptance gate that will make the role-consent surface legitimately requestable, ringtone ownership where appropriate, durable Recents plus Contacts/Favorites surfaces, broader carrier/device validation, and supplementary-service or IMS state only where Android legitimately exposes an authorized signal that GoreeCloud has a reason to consume.

## Phase 2 — Privacy, Security and Persistence

Privacy Shield operation authorization, Wardveil storage boundaries, call metadata persistence, retention controls, permission explanations, Private Call and minimized diagnostics.

**Current state:** Android-managed automatic backup is fail-closed: the manifest disables backup and explicit Android 12+ extraction rules exclude app-managed data from both cloud backup and device-to-device transfer, with equivalent legacy full-backup exclusions. This is a platform privacy boundary only; it is not Everkeep backup/recovery implementation or acceptance.

## Phase 3 — Caller Safety and Screening

Identity provenance, spam/scam signals, block/allow rules, configurable protection levels, Call Screen, automation rules and Safety Center explanations.

## Phase 4 — Voicemail and Call Knowledge

Visual/direct voicemail adapters, unified timeline, notes, search, exports and Everkeep-controlled backup boundaries.

## Phase 5 — Call Assistant

Local-first transcription, Text Call, Live Translate, Hold Assistant, interactive phone menus, suggested responses, summaries and user-approved action extraction.

## Phase 6 — Advanced and Cross-Device Calling

Trusted-device calling/handoff, multiple phone identities, advanced conferencing and provider-neutral video transitions.

## Phase 7 — Release Candidate

All implemented capabilities require test coverage, accessibility verification, explicit platform/carrier support handling, truthful status evidence, privacy/security acceptance, migration/backup validation where applicable, and human release validation.
