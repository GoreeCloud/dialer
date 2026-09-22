# GoreeCloud Dialer — Changelogs

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