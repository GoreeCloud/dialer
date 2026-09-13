# GoreeCloud Dialer Specifications

## Repository

Canonical repository: `GoreeCloud/goreecloud-dialer`.

## Status

Active Development / pre-Stable.

## Platform baseline

- Android native application
- Kotlin + Jetpack Compose
- namespace/application ID `com.goreecloud.dialer`
- compile/target SDK 36
- minimum SDK 29
- JVM 17
- Development version `0.1.0-dev`

## Source-present Development foundation

The current source contains:

- Android project/build shell and Compose Development surfaces.
- A capability-state contract that keeps support, availability, permissions/roles, activation, request submission, failure and success separate.
- Android telephony and `ROLE_DIALER` detection without automatically requesting the role.
- `ACTION_DIAL`/`tel:` intake and a local keypad without automatic carrier-call placement.
- An `InCallService` lifecycle boundary with generated process-local call session IDs.
- Explicit mapping for current Android call lifecycle states rather than collapsing valid special states into `UNKNOWN`.
- Privacy-minimized direction evidence normalized to `INCOMING`, `OUTGOING`, or `UNKNOWN` from Android Telecom.
- Generic terminal outcomes derived only for disconnected calls from `android.telecom.DisconnectCause.code`; localized/provider-specific reason text remains outside public runtime state.
- State-aware Answer, Decline, End, Hold, Resume and DTMF execution boundaries.
- Live `Call.Details` evidence for hold support/current availability, mute support, and conference manage/merge/swap/separate capabilities; missing details fail closed.
- User-driven Development DTMF presentation with bounded pulses and explicit cleanup.
- Development incoming/ongoing `CallStyle` presentation and explicit operation-result messaging.
- Telecom mute-state tracking and API 34+ `CallEndpoint` discovery/routing.
- `READ_PHONE_STATE`-guarded call-capable phone-account discovery using opaque process-local route IDs.
- Explicit system-default or anonymous phone-account route selection, with stale selections rejected rather than silently changed.
- Emergency classification and routing policy that delegates emergency or indeterminate-emergency phone-account choice to Android Telecom.
- Content-minimized conference parent/child/conferenceable-call relationships represented only by generated session IDs.
- Guarded pairwise conference, merge, swap and separate Development requests derived from Telecom's live relationship/capability evidence.
- Dormant `TelecomManager.placeCall` and default-dialer role-request boundaries that stay rejected until GoreeCloud acceptance gates pass.
- Unit-test and debug-assembly CI workflow with branch-scoped cancellation of superseded runs.
- Architecture, privacy, security, feature, roadmap and user documentation.

The direction/terminal-outcome layer is transient runtime evidence only. It is not a durable Recents implementation and does not persist call history.

## Explicitly not production-accepted

The source above must not be interpreted as proof that GoreeCloud Dialer is ready to replace the system phone application. The following remain incomplete or unverified for production acceptance:

- carrier call placement and device/carrier validation
- default-dialer role-request enablement and end-to-end role acceptance
- production incoming and ongoing call UI acceptance
- ringtone ownership
- durable Recents/call-history persistence and retention
- production multi-SIM routing acceptance
- production conference operation validation across supported devices/carriers
- pre-Android-14 audio-route switching
- Contacts and Favorites integration
- call screening and caller-safety runtime paths
- visual voicemail
- call recording
- durable call metadata persistence and retention
- Privacy Shield runtime authorization
- Wardveil runtime acceptance
- Everkeep runtime backup/recovery integration
- Call Assistant and local/remote AI provider paths
- Glaze UI release acceptance and human accessibility/release validation

## Runtime truth requirements

A source file, UI control, policy object, permission declaration, role availability signal, submitted Telecom request, transient direction/outcome classification, or test double does not by itself prove that a user-visible capability succeeded. Device capability, carrier support, runtime state, permissions, role ownership, GoreeCloud acceptance, request submission, callback/result evidence, privacy authorization and security acceptance remain independent facts.

## Product scope

The planned product scope is defined by the canonical GoreeCloud Dialer project specification and repository roadmap. Source documentation must continue to distinguish planned behavior, source-present Development boundaries, CI validation, device/carrier validation and production acceptance.
