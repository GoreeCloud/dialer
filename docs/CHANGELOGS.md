# GoreeCloud Dialer — Changelogs

## 2026-09-30 — Repository baseline documentation completion

- Added `docs/BENEFITS.md`, `docs/COMPETITIVE-OBJECTIVES.md`, `docs/BRANDING.md`, and `.editorconfig` to complete the current repository baseline.
- Branding documentation records that the current Android manifest has the GoreeCloud Dialer label but no accepted repository-specific launcher-icon provenance yet.
- Updated README and documentation navigation to the canonical records while preserving all existing telephony, carrier, privacy, emergency, release, and Stable boundaries.
- Added `scripts/validate_repository_structure.py` and CI enforcement so root cleanliness and mandatory repository records fail closed on future changes.

## 2026-09-30 — Repository root-cleanliness migration

- Moved canonical human-readable repository records from root into `docs/` and promoted repository security guidance to `.github/SECURITY.md`.
- Renamed the existing privacy record to canonical `docs/PRIVACY.md` and updated README, Platform Contract, and Glaze validation paths.
- Removed stale `FEATURE-ROADMAP.md` references in favor of `docs/IMPLEMENTED-FEATURES.md`, `docs/PLANNED-FEATURES.md`, and `docs/CHANGELOGS.md`.
- Preserved the telephony, carrier, emergency, privacy, and lifecycle boundaries; this is documentation/control-plane organization only.

## 2026-09-29 — direct dial-input editing candidate
- The Development dial surface now exposes the local dial string as an editable phone field in addition to the on-screen keypad.
- Keyboard and paste input keeps digits, `*`, `#`, and a leading international `+`; common formatting characters are ignored and the existing 128-character bound remains enforced.
- Managed-Android acceptance now exercises direct text replacement and confirms that the carrier **Call** action remains disabled.
- No Telecom placement, carrier routing, permission, account, Recents, Contacts, or network authority was added.

**Acceptance boundary:** this remains unmerged candidate work on PR #40. Fresh exact-head CI plus representative-device accessibility/form-factor and carrier/device acceptance remain required.

## 2026-09-29 — bounded local dial editing candidate

- Added a local dial-string editor for digits, `*`, `#`, a start-only international `+`, one-character Delete, and explicit Clear.
- Bounded the local entry state to 128 characters and kept the carrier Call action disabled; no Telecom placement, account routing, permission, network, or carrier authority is added.
- Added JVM policy coverage and managed-Android interaction coverage for international-prefix entry, clearing, and continued fail-closed call placement.
- Kept the capability explicitly candidate-only in the repository feature authority until integration. Every changed candidate head requires fresh exact-head CI, while representative-device/carrier/accessibility acceptance remains separate.

## 2026-09-29 — first-use replay-state correction candidate

- The active onboarding candidate now keeps first-use completion durable when the user voluntarily replays setup.
- Replay uses a separate persisted state, can be explicitly closed, resumes its own step after interruption, and preserves the global contextual-tip preference.
- Guidance schema v2 reads prior schema-v1 state without forcing an unnecessary setup reset.
- This remains unmerged Development candidate work. Every changed candidate head requires fresh exact-head CI; representative-device/accessibility acceptance remains separate.


**Record type:** Repository change history  
**Repository:** `GoreeCloud/dialer`  
**Lifecycle:** Development / non-Stable  
**Governing standard:** Standard — Repository Feature Tracking and Changelog Governance v1.0, effective September 22, 2026.

## 2026-09-22 — Repository feature/changelog governance migration

### Added

- `IMPLEMENTED-FEATURES.md` as the authoritative implemented-feature inventory.
- `PLANNED-FEATURES.md` as the authoritative planned/incomplete-feature inventory.
- `CHANGELOGS.md` as the authoritative repository change-history record.

### Changed

- Retired the repository `FEATURE-ROADMAP.md` control model.
- Removed the obsolete requirement to maintain a synchronized Google Drive feature-roadmap authority.
- Preserved `FEATURES.md` as a product-facing feature description rather than lifecycle authority.

### Lifecycle boundary

This migration changes documentation/control-plane authority only. GoreeCloud Dialer remains Development/non-Stable. Carrier/device acceptance, production default-phone behavior, signing/distribution, Release Candidate, Production Acceptance, and Stable qualification remain open.

## Historical change evidence

Historical implementation and validation evidence remains preserved by Git history, merged pull requests, repository `NOTES.md`, workflow evidence, physical-device/carrier acceptance records, and product-specific governed evidence. Future material integrated feature/change entries must be recorded here.

## Maintenance rule

Record material integrated changes here with enough exact repository evidence to distinguish authoritative `main` state from draft/unmerged work. Do not convert source presence, emulator evidence, or green CI into a production carrier/device or Stable claim.