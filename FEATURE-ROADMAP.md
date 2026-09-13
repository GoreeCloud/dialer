# GoreeCloud Dialer Feature Roadmap

## Phase 0 — Repository Bootstrap

Native Android source tree, build configuration, CI, source documentation, tests and truthful Development status.

**Current state:** Android unit tests and debug assembly are validated in CI. The source manifest/provenance inventory exists; remaining repository-governance items still need completion before Phase 0 is closed.

## Phase 1 — Core Telephony

Default-dialer role, outgoing/incoming calls, call controls, DTMF, Recents, contacts, Favorites, SIM routing and emergency-safe boundaries.

**Current state:** runtime telephony/`ROLE_DIALER` detection, `ACTION_DIAL` intake, a local keypad, a content-minimized `InCallService` lifecycle boundary, state-aware essential call-control contracts, observable process-local call snapshots, a Development ongoing-call control surface, a dormant policy-gated `TelecomManager.placeCall` boundary, and Development incoming-to-ongoing `CallStyle` notification continuity are implemented. Carrier call placement is not accepted, caller identity projection and ringtone ownership are intentionally absent from call presentation, production incoming/ongoing UI acceptance is incomplete, SIM/audio routing is not implemented, and role requests remain unavailable. The role-request gate stays closed until Android's default-phone requirements are implemented and validated together.

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
