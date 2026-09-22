# GoreeCloud Dialer — Implemented Features

**Record type:** Repository implemented-feature inventory  
**Repository:** `GoreeCloud/dialer`  
**Lifecycle:** Development / non-Stable  
**Authority:** Current `main` source and accepted repository evidence  
**Governing standard:** Standard — Repository Feature Tracking and Changelog Governance v1.0, effective September 22, 2026.

## Interpretation

This record describes source-present GoreeCloud Dialer Development foundations. Their presence does **not** establish production acceptance across Android devices, OEMs, carriers, subscriptions, call types, or emergency scenarios.

`FEATURES.md` remains the product-facing feature description. This file is the lifecycle authority for what is implemented.

## Implemented Development capabilities

### Native Android and telephony boundary

- Native Kotlin + Jetpack Compose Android application shell.
- Explicit Development/pre-Stable lifecycle presentation.
- Capability-state model that avoids a misleading global supported/ready state.
- Runtime Android telephony and `ROLE_DIALER` availability/ownership detection.
- `ACTION_DIAL` and `tel:` intake into a local keypad without automatic call placement.
- Content-minimized `InCallService` lifecycle tracking with generated process-local session IDs.
- API-aware call-state reads, including the Android 12+ path and an isolated Android 10/11 fallback.
- Transient incoming/outgoing/unknown direction evidence and generic Telecom-derived disconnected-call outcomes.

### In-call Development controls

- State-aware Answer, Decline, End, Hold, Resume, and DTMF execution boundaries.
- User-driven DTMF keypad with bounded tone pulses and cleanup.
- Live Telecom capability evidence for Hold, Mute, and conference manage/merge/swap/separate behavior.
- Content-minimized conference relationship tracking and guarded pairwise conference controls.
- Development incoming/ongoing call presentation with explicit control results.
- API 34+ Telecom `CallEndpoint` discovery/routing without exposing endpoint device names.
- Telecom-evidenced mute/unmute control.

### Role, account, and routing safety

- Fail-closed default-dialer role-request preparation policy and Development role-consent surface.
- Dormant policy-gated `TelecomManager.placeCall` boundary.
- Permission-aware call-capable phone-account discovery.
- Anonymous explicit phone-account selection without presenting sensitive account labels/numbers/carrier/SIM identifiers.
- Permission-lifetime revocation of cached account handles.
- Emergency-safe account-routing policy that delegates emergency or indeterminate-emergency routing to Android Telecom.
- Explicit Development capability evidence for outgoing calls, multi-SIM routing, Wi-Fi Calling/IMS state, and supplementary services without inferring unavailable signals.

### Privacy, backup, and validation boundary

- Android-managed automatic backup disabled.
- Android 12+ extraction rules and legacy backup exclusions keep app-managed data out of Android cloud/device-to-device backup paths.
- Managed-emulator acceptance lanes cover supported Android API baselines as repository Development evidence.
- Physical-device/carrier acceptance template and fail-closed evidence model exist separately from emulator/source evidence.

## Implemented-but-not-accepted boundaries

The following source foundations exist but remain acceptance-gated and therefore also appear in `PLANNED-FEATURES.md`:

- Call placement and default-dialer role preparation without accepted real carrier/device execution.
- Incoming/ongoing call presentation without accepted OEM lock-screen/notification behavior.
- Multi-SIM, conference, audio-route, and supplementary-service foundations without complete supported-device/carrier acceptance.
- GLAZE UI source/presentation work without complete Dialer rendered/accessibility/device/performance acceptance.

## Explicitly not established

Current authoritative source does not establish production carrier calling, production default-phone replacement, durable Recents/history, complete Contacts/Favorites integration, production ringtone ownership, accepted emergency/call-flow coverage, complete Integral Platform System acceptance, production signing/distribution, Release Candidate, or Stable qualification.

## Maintenance rule

When an obligation in `PLANNED-FEATURES.md` becomes implemented and is verified on the authoritative integration line, reconcile it here and record the material change in `CHANGELOGS.md`. Draft or unmerged pull requests are not implementation authority.