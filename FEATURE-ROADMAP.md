# GoreeCloud Dialer Feature Roadmap

## Phase 0 — Repository Bootstrap

Native Android source tree, build configuration, CI, source documentation, tests and truthful Development status.

**Current state:** Android unit tests and debug assembly have validated prior source checkpoints in CI. The source manifest/provenance inventory exists and CI now uses branch-scoped concurrency; remaining repository-governance items, including a canonical license decision, still need completion before Phase 0 is closed.

## Phase 1 — Core Telephony

Default-dialer role, outgoing/incoming calls, call controls, DTMF, Recents, contacts, Favorites, SIM routing, conferencing and emergency-safe boundaries.

**Current state:** runtime telephony/`ROLE_DIALER` detection, `ACTION_DIAL` intake, a local keypad, a content-minimized `InCallService` lifecycle boundary, state-aware answer/decline/end/hold/resume/DTMF contracts, a user-driven in-call DTMF keypad, live hold/mute/conference capability evidence, content-minimized conference parent/child/conferenceable-session state, guarded pairwise conference/merge/swap/separate Development controls, observable process-local call snapshots, Development incoming-to-ongoing `CallStyle` presentation, a dormant policy-gated `TelecomManager.placeCall` boundary, a fail-closed role-request preparation policy, Telecom-evidenced mute/unmute control, API 34+ endpoint discovery/routing, `READ_PHONE_STATE`-guarded call-capable phone-account discovery, anonymous explicit phone-account selection, permission-lifetime revocation of cached account handles, and emergency-safe account-routing policy are implemented in source.

Emergency or indeterminate-emergency numbers force Android-managed phone-account routing, stale explicit account selections fail closed, and conference controls are derived from live Telecom relationships/capability bits instead of call-count assumptions. Carrier call placement remains unaccepted, conference requests are not production-accepted, caller identity, endpoint device names, phone-account labels/numbers/carrier or SIM identifiers remain absent from Development presentation, ringtone ownership is not claimed, production incoming/ongoing UI acceptance is incomplete, pre-API-34 audio routing remains unavailable, and no UI currently launches the default-dialer role request. Phone-account routing, DTMF, and conferencing are therefore Development boundaries, not proof of production-accepted calling. The role-request gate stays closed until Android's default-phone requirements are implemented and validated together.

Remaining Phase 1 work includes production/device acceptance of outgoing and incoming calling, default-dialer role-request enablement after all acceptance gates pass, ringtone ownership where appropriate, Recents/contacts/Favorites surfaces, broader carrier/device validation, and any supplementary-service state that Android legitimately exposes.

## Phase 2 — Privacy, Security and Persistence

Privacy Shield operation authorization, Wardveil storage boundaries, call metadata persistence, retention controls, permission explanations, Private Call and minimized diagnostics.

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
