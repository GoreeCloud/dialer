# GoreeCloud Dialer Feature Roadmap

## Phase 0 — Repository Bootstrap

Native Android source tree, build configuration, CI, source documentation, tests and truthful Development status.

**Current state:** Android unit tests and debug assembly are validated in CI. The source manifest/provenance inventory exists; remaining repository-governance items still need completion before Phase 0 is closed.

## Phase 1 — Core Telephony

Default-dialer role, outgoing/incoming calls, call controls, DTMF, Recents, contacts, Favorites, SIM routing and emergency-safe boundaries.

**Current state:** runtime telephony/`ROLE_DIALER` detection, `ACTION_DIAL` intent intake, a local keypad surface, and a content-free `InCallService` lifecycle boundary are implemented. Carrier call placement, incoming/ongoing call UI, controls, and role requests remain unavailable. The role-request gate stays closed until Android's default-phone requirements are implemented and validated together.

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

Verified builds/tests, telecom validation, Privacy Shield/Wardveil acceptance, accessibility validation, deletion/retention verification, offline-core validation, carrier limitation documentation and no known critical/high release blockers.
