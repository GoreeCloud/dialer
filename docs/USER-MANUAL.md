# GoreeCloud Dialer User Manual

## Current status

GoreeCloud Dialer is in **Active Development / pre-Stable** status. The current source contains real Android Telecom integration boundaries, but it is **not yet approved as a replacement for the device's system phone application**.

## What the Development build currently exposes

Depending on Android version, device capability, permissions, Telecom state and whether the app is participating in a live call, the Development build can expose:

- a local keypad and `ACTION_DIAL` intake
- truthful default-dialer role state without automatically requesting the role
- generated local call-session IDs and lifecycle state
- Answer, Decline/End, Hold/Resume controls when permitted by lifecycle and live Telecom capability evidence
- a user-driven DTMF keypad
- mute state/control when Telecom reports mute support
- Android 14+ audio endpoint categories and route requests
- anonymous call-capable phone-account choices after `READ_PHONE_STATE` is granted
- emergency-safe phone-account routing rules that leave emergency choice to Android Telecom
- conference relationships represented only as generated session IDs
- Development pairwise conference, merge, swap and separate controls only when Telecom reports the corresponding relationship/capability evidence

These surfaces intentionally avoid showing caller identity, phone-account labels, phone numbers, carrier/SIM identifiers or endpoint device names until dedicated product/privacy boundaries are designed and accepted.

## Calling limitations

The visible **Call** action remains disabled because GoreeCloud's outgoing-call acceptance gate is still closed. The source includes a guarded `TelecomManager.placeCall` adapter, but carrier call placement is not yet accepted as a user-facing capability.

The app also does not currently launch Android's default-dialer role-consent request. Production incoming/ongoing UI acceptance, outgoing placement acceptance and required device validation must pass before that gate can open.

## Conference limitations

Conference controls in the Development UI are evidence-driven experiments, not a production guarantee. A second tracked call is not treated as conferenceable merely because it exists. GoreeCloud requires Android Telecom to identify the target call as conferenceable or to expose the specific merge/swap/separate capability before the corresponding button is offered. A submitted request is reported as submitted; it is not described as completed without later platform evidence.

## What remains incomplete or unverified

Production carrier dialing, system-phone replacement, ringtone ownership, production multi-SIM behavior, broad carrier/device conference validation, Recents/Contacts/Favorites integration, call screening, voicemail, recording, transcription, translation, spam/scam protection, AI Call Assistant, Privacy Shield runtime integration, Wardveil runtime integration, Everkeep backup/recovery, cross-device calling and release acceptance remain incomplete or unverified.

## Privacy

The current Development runtime intentionally minimizes projected state. Generated session/route IDs, lifecycle categories and narrow capability booleans are preferred over phone numbers, caller names, account identifiers or device identities. Call audio, recordings, transcripts and voicemail are not intentionally persisted by the current core telephony foundation.

## Emergency safety

Do not rely on the current Development build as the device's primary emergency-calling application. Emergency and indeterminate-emergency numbers are specifically designed to preserve Android Telecom's routing authority rather than applying GoreeCloud's experimental phone-account selection.
