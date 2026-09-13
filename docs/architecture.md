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

The Android probe reads whether the device advertises telephony, whether the Android dialer role exists, and whether GoreeCloud Dialer already holds that role. It does not request the role.

## ACTION_DIAL boundary

`MainActivity` now handles Android `ACTION_DIAL` with and without a `tel:` URI. The address is transferred to a local keypad without automatically placing a carrier call, normalizing the number, persisting it, or sending it to GoreeCloud services. Unsupported URI schemes are ignored.

## InCallService lifecycle boundary

`GoreeCloudInCallService` is registered with `BIND_INCALL_SERVICE` and tracks call additions, removals, state transitions, and whether another call can be added. The process-local runtime store contains only call counts and lifecycle-state categories. It does not retain phone numbers, caller names, account identifiers, transcripts, audio, or `Call.Details`.

The service intentionally does not advertise in-call UI or ringing ownership metadata yet. Incoming-call UI, ongoing-call UI, ringtone responsibility, call controls, and durable history remain incomplete.

## Default-dialer eligibility gate

Android requires a default-phone candidate to handle `Intent.ACTION_DIAL` and fully implement `InCallService`, including incoming and ongoing call UI. GoreeCloud Dialer now satisfies only the intent-handling and service-lifecycle portions. The application therefore keeps role-request eligibility closed until incoming and ongoing UI plus essential controls are implemented and validated together. Detecting or even holding `ROLE_DIALER` remains a separate platform fact from GoreeCloud acceptance.

## Emergency boundary

Emergency calling is outside experimental automation. Screening, AI, routing suggestions, recording defaults, and Call Assistant must never interfere with emergency call initiation or platform emergency behavior. Android's preloaded dialer remains the emergency-call UI authority even when another application holds the dialer role; future outgoing-call integration must use the platform Telecom call path rather than attempting to bypass it.

## Local-first boundary

Core telephony must not depend on GoreeCloud servers. Intelligent features select among a local provider, explicitly authorized remote processing, or unavailable. Local-to-cloud fallback must never be silent.
