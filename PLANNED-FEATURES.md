# GoreeCloud Dialer — Planned Features and Open Obligations

**Record type:** Repository planned/incomplete-feature inventory  
**Repository:** `GoreeCloud/dialer`  
**Lifecycle:** Development / non-Stable  
**Authority:** Current `main` source, accepted repository evidence, and active GoreeCloud Tasks Management obligations  
**Governing standard:** Standard — Repository Feature Tracking and Changelog Governance v1.0, effective September 22, 2026.

## Interpretation

Items here are planned, incomplete, blocked, or acceptance-gated. Their presence does not imply implementation or release readiness. Partial foundations that already exist are also described in `IMPLEMENTED-FEATURES.md` for the verified portion only.

## Current stabilization obligations

- Execute the privacy-safe physical-device/carrier matrix for supported Android versions, OEMs, carriers/SIM classes, role state, incoming/outgoing PSTN behavior, call controls, lock-screen presentation, notifications/full-screen behavior, endpoint routing, multi-SIM, conferencing, disconnect evidence, and emergency-safe boundaries.
- Establish a non-empty verified minimum supported device/carrier release set before production call claims.
- Validate default-dialer role acquisition and replacement behavior on representative devices.
- Complete production incoming/ongoing UI acceptance and ringtone ownership where appropriate.
- Complete pre-Android-14 audio routing through an approved supported path or document an explicit unsupported boundary.
- Complete durable Recents/history plus Contacts and Favorites integration.
- Complete GLAZE UI V1.6 rendered, accessibility, representative-device, performance, rollback, and Human Visual Excellence acceptance.
- Complete applicable Privacy Shield, Wardveil Security, Everkeep, GoreeCloud Identity, GoreeCloud Mesh, GoreeCloud Manager, GoreeCloud Policy, and GoreeCloud Observability integration/evaluation with evidence-backed dispositions.
- Complete production signing/distribution, recovery/rollback, Release Candidate, production, and Stable qualification.

## Planned product capabilities

### Core calling and organization

- Production-accepted core dialer, T9 contacts, Recents, Favorites, multi-SIM routing, and in-call controls.
- Verified caller identity with explainable caller-information provenance.
- Advanced conferencing and provider-neutral video transitions where supported.

### Caller safety

- Spam, robocall, and scam protection.
- Call Screen and configurable automatic screening.
- Block/allow rules and explainable protection levels.

### Voicemail and call knowledge

- Visual/direct voicemail adapters and voicemail summaries.
- Unified call timeline, notes, search, exports, and Everkeep-governed recovery boundaries.

### Call Assistant

- Text Call, local-first transcription, and Live Translate.
- Hold Assistant and interactive phone-menu navigation.
- Suggested responses, summaries, and user-approved action extraction.
- Call recording only where technically supported, policy-approved, and legally permitted.

### Advanced and cross-device calling

- Trusted-device calling/handoff.
- Multiple phone identities.
- Cross-device continuity under accepted identity/security/privacy boundaries.

### Privacy and security

- Privacy Shield operation authorization.
- Wardveil-governed storage/security boundaries.
- Retention controls, minimized diagnostics, and Private Call/Incognito Dialing where accepted.
- Everkeep-controlled export/backup/restore.

## Explicit non-claims

Until accepted evidence exists, this file does not claim production carrier calling, production default-phone replacement, production emergency behavior, complete supported-device/carrier coverage, Release Candidate, Production Acceptance, or Stable status.

## Maintenance rule

Move an item to `IMPLEMENTED-FEATURES.md` only after the authoritative implementation and required verification are integrated. Record material lifecycle changes in `CHANGELOGS.md`. Keep actionable execution work in GoreeCloud Tasks Management without creating duplicate task authority.