# GoreeCloud Dialer Architecture

## Status

Development architecture for `GoreeCloud/goreecloud-dialer`. Planned modules are not runtime claims.

## Primary rule

Authorization, capability, activation, and operation success are separate facts. No global state may imply that all Dialer protections or telecom features are accepted.

## Capability domains

1. **Telephony** — Android Telecom role and call integration, SIM routing, DTMF, audio routes, emergency-safe boundaries.
2. **Contacts** — local contact lookup, T9, favorites, contact-specific call policy.
3. **History** — Recents, unified timeline, notes, retention and search metadata.
4. **Screening** — Call Screen prompts, live response transcription, screening rules.
5. **Safety** — caller identity provenance, spam/scam signals, block/allow decisions and evidence.
6. **Voicemail** — visual/direct voicemail adapters and content references.
7. **Call Assistant** — Text Call, transcription, translation, Hold Assistant, menu navigation, summaries.
8. **Intelligence** — local model/runtime adapters and explicitly approved remote processors.
9. **Privacy** — Privacy Shield authorization and purpose/locality/retention/revocation state.
10. **Security** — Wardveil trust, storage and integrity evidence.
11. **Continuity** — Everkeep backup/restore authority.
12. **UI** — Glaze UI presentation and accessibility acceptance.

## Runtime state

Every capability should distinguish unsupported, unavailable, permission/role required, available, active, failed, and succeeded states. User interfaces should surface why a capability is unavailable when that information helps the user act.

## Emergency boundary

Emergency calling is outside experimental automation. Screening, AI, routing suggestions, recording defaults, and Call Assistant must never interfere with emergency call initiation or platform emergency behavior.

## Local-first boundary

Core telephony must not depend on GoreeCloud servers. Intelligent features select among a local provider, explicitly authorized remote processing, or unavailable. Local-to-cloud fallback must never be silent.
