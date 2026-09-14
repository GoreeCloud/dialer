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
- Android Gradle Plugin 8.10.1
- JVM 17
- Development version `0.1.0-dev`

The repository intentionally remains on its last verified SDK-36-compatible AndroidX/Compose dependency set. Current stable AndroidX/Compose releases that require compile SDK 37 / AGP 9.1+ are tracked behind a separate atomic platform-modernization gate rather than being adopted piecemeal.

## Source-present Development foundation

The current source contains:

- Android project/build shell and Compose Development surfaces.
- A capability-state contract that keeps support, availability, permissions/roles, activation, request submission, failure and success separate.
- Android telephony and `ROLE_DIALER` detection without automatically requesting the role.
- A Development default-dialer role-consent surface that is enabled only when the capability model reaches `RoleRequired`; the independent preparer re-checks GoreeCloud acceptance before Android's user-consent intent can be launched.
- Runtime telephony capability and phone-account evidence re-probed when the Activity resumes and after phone-state permission results.
- `ACTION_DIAL`/`tel:` intake and a local keypad without automatic carrier-call placement.
- A truthful outgoing-call capability state that acknowledges the existing `TelecomManager.placeCall` boundary while keeping carrier-call runtime acceptance incomplete.
- `READ_PHONE_STATE`-guarded call-capable phone-account discovery and anonymous explicit routing with production multi-SIM acceptance still incomplete.
- Explicit Wi-Fi Calling/IMS and supplementary-service capability states that remain unavailable rather than being inferred from connectivity, carrier branding, or other heuristics.
- No `READ_PRECISE_PHONE_STATE` request in the current manifest; precise IMS state must not be claimed without an authorized Android signal and an accepted observer.
- An `InCallService` lifecycle boundary with generated process-local call session IDs.
- Explicit mapping for current Android call lifecycle states rather than collapsing valid special states into `UNKNOWN`.
- API-aware lifecycle-state reads using `Call.Details.state` on API 31+ with the deprecated `Call.state` path isolated to the API 29-30 compatibility range.
- Privacy-minimized direction evidence normalized to `INCOMING`, `OUTGOING`, or `UNKNOWN` from Android Telecom.
- Generic terminal outcomes derived only for disconnected calls from `android.telecom.DisconnectCause.code`; localized/provider-specific reason text remains outside public runtime state.
- State-aware Answer, Decline, End, Hold, Resume and DTMF execution boundaries.
- Live `Call.Details` evidence for hold support/current availability, mute support, and conference manage/merge/swap/separate capabilities; missing details fail closed.
- User-driven Development DTMF presentation with bounded pulses and explicit cleanup.
- Development incoming/ongoing `CallStyle` presentation and explicit operation-result messaging.
- Content-free incoming-call access evidence for app-level notification enablement, Android 13+ `POST_NOTIFICATIONS`, and Android 14+ full-screen intent access.
- Explicit user-triggered Development actions for notification permission and Android notification/full-screen settings; these surfaces are never launched automatically.
- Incoming-call access evidence refreshes after permission results and Activity resume, while the final notification post path re-checks authorization immediately before posting.
- Telecom mute-state tracking and API 34+ `CallEndpoint` discovery/routing.
- Explicit system-default or anonymous phone-account route selection, with stale selections rejected rather than silently changed.
- Emergency classification and routing policy that delegates emergency or indeterminate-emergency phone-account choice to Android Telecom.
- Content-minimized conference parent/child/conferenceable-call relationships represented only by generated session IDs.
- Guarded pairwise conference, merge, swap and separate Development requests derived from Telecom's live relationship/capability evidence.
- Dormant `TelecomManager.placeCall` boundary that stays rejected until GoreeCloud acceptance gates pass.
- Android-managed automatic backup disabled plus explicit cloud-backup, device-transfer, and legacy full-backup exclusions for app-managed storage; this is not Everkeep implementation or recovery acceptance.
- JVM unit-test source plus Android instrumentation-test source.
- CI validation for JVM unit tests, Android lint, debug APK assembly, minified release-variant assembly, and instrumentation-test APK compilation.
- Managed-emulator execution on Android 11 / API 30 and Android 14 / API 34 after the build gate passes.
- Android-14-specific runtime checks for modern `CallEndpoint`/`InCallService` API presence, full-screen-intent capability probing, incoming-call access settings construction, and fail-closed role-consent behavior.
- Separate short-retention managed-device report artifacts for API 30 and API 34, including failed runs, so emulator evidence is inspectable rather than log-only.
- A repository physical-device/carrier acceptance template for the remaining real telephony evidence.
- Architecture, privacy, security, feature, roadmap, platform-modernization, CI-validation, testing, release-gate, and physical-device-acceptance documentation.

The direction/terminal-outcome layer is transient runtime evidence only. It is not a durable Recents implementation and does not persist call history.

Release-variant assembly is build evidence only. Managed-emulator success is Android runtime evidence only. Neither proves signing, production distribution readiness, carrier behavior, physical-device behavior, live endpoint changes, real incoming-call delivery, or Stable acceptance.

## Explicitly not production-accepted

The source above must not be interpreted as proof that GoreeCloud Dialer is ready to replace the system phone application. The following remain incomplete or unverified for production acceptance:

- carrier call placement and physical-device/carrier validation
- end-to-end default-dialer role acceptance; the role-consent surface remains blocked by current GoreeCloud acceptance state
- production incoming and ongoing call UI acceptance on real calls/OEM devices
- real-world notification/full-screen incoming-call delivery across supported OEM devices and lock-screen states
- ringtone ownership
- durable Recents/call-history persistence and retention
- production multi-SIM routing acceptance
- precise Wi-Fi Calling/IMS state observation
- supplementary-service integration beyond legitimately authorized Android platform signals
- production conference operation validation across supported devices/carriers
- real earpiece/speaker/wired/Bluetooth endpoint behavior and live API 34+ endpoint changes
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
- production signing/distribution acceptance
- current Glaze UI release acceptance and human accessibility/release validation

## Runtime truth requirements

A source file, UI control, permission declaration, settings intent, policy object, role availability signal, submitted Telecom request, transient direction/outcome classification, compiled instrumentation test, release-variant build, emulator pass, or test double does not by itself prove that a user-visible production capability succeeded. Device capability, carrier support, runtime state, permissions, role ownership, GoreeCloud acceptance, request submission, callback/result evidence, privacy authorization, security acceptance, physical-device validation, carrier validation, signing/distribution evidence, and human release validation remain independent facts.

## Product scope

The planned product scope is defined by the canonical GoreeCloud Dialer project specification and repository roadmap. Source documentation must continue to distinguish planned behavior, source-present Development boundaries, build/CI validation, release-variant build evidence, API-specific emulator validation, physical-device validation, carrier validation, signing/distribution evidence, human validation, and production acceptance.
