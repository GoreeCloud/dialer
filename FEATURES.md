# GoreeCloud Dialer Features

## Source-present Development foundation

The following capabilities exist in source as Development foundations. Their presence does not imply production acceptance on every Android device, carrier, subscription, or call type.

- Native Kotlin + Jetpack Compose Android application shell.
- Explicit Active Development / pre-Stable status.
- Capability-state model that avoids a misleading global supported/ready state.
- Runtime Android telephony and `ROLE_DIALER` availability/ownership detection.
- `ACTION_DIAL` and `tel:` intake into a local keypad without automatic call-placement side effects.
- Content-minimized `InCallService` lifecycle tracking with generated process-local session IDs.
- State-aware Answer, Decline, End, Hold, Resume, and DTMF execution boundaries.
- Live Telecom capability evidence for Hold, Mute, and conference manage/merge/swap/separate behavior.
- User-driven DTMF keypad with bounded tone pulses and cleanup.
- Development incoming and ongoing call presentation with explicit control results.
- API 34+ Telecom `CallEndpoint` discovery/routing without exposing endpoint device names.
- Permission-aware call-capable phone-account discovery and anonymous explicit account selection.
- Emergency-safe account-routing policy that delegates emergency or indeterminate-emergency routing to Android Telecom.
- Content-minimized conference relationship tracking and guarded pairwise conference/merge/swap/separate Development controls.
- Dormant `TelecomManager.placeCall` and default-dialer role-request boundaries that remain fail-closed until acceptance requirements pass.
- CI workflow for unit tests and debug assembly with branch-scoped cancellation of superseded runs.

## Not production-accepted yet

Carrier call placement, default-dialer role acquisition, production incoming/ongoing UI replacement, ringtone ownership, production multi-SIM acceptance, conference operation acceptance across target carriers/devices, pre-Android-14 audio-route switching, Recents/contacts/Favorites integration, and release validation remain incomplete or unverified.

## Planned product capabilities

- Core dialer, T9 contacts, Recents, Favorites, multi-SIM routing and production in-call controls.
- Verified caller identity and explainable caller-information provenance.
- Spam, robocall and scam protection.
- Call Screen and configurable automatic screening.
- Text Call, live transcription and Live Translate.
- Call recording where technically and legally permitted.
- AI summaries and user-approved action extraction.
- Hold Assistant and interactive phone-menu navigation.
- Visual/direct voicemail and voicemail summaries.
- Unified call timeline and searchable call knowledge.
- Caller relationship statistics that remain informational.
- Contact-centric and business calling surfaces.
- Caller announcements, voice controls, video transition and advanced conferencing.
- Private Call, Incognito Dialing and detailed retention controls.
- Modular downloadable local models.
- Export, Everkeep-controlled backup/restore, cross-device calling and handoff.
- Multiple phone identities.
- Glaze UI and first-class accessibility acceptance.

Planned items above are not implementation claims.
