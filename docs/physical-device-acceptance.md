# GoreeCloud Dialer Physical-Device and Carrier Acceptance

**Status:** Execution template — no physical-device or carrier result is accepted unless recorded with evidence.

**Tracking issue:** #14 — Establish physical-device and carrier telephony acceptance matrix.

## Purpose

This document is the canonical repository template for validating GoreeCloud Dialer behavior that cannot be accepted from source presence, JVM tests, build gates, or managed emulators.

It must not be used to infer a global "Dialer ready" state. Every scenario is accepted independently.

## Evidence states

Use exactly one state for each executed scenario:

- `NOT_RUN` — no qualifying execution evidence exists.
- `BLOCKED` — the scenario could not be executed because a prerequisite was unavailable.
- `PLATFORM_UNSUPPORTED` — Android/device platform does not expose the capability.
- `CARRIER_UNSUPPORTED` — the carrier/network does not support the capability under test.
- `FAILED` — observed behavior violated the expected contract.
- `INCONCLUSIVE` — execution occurred but evidence was insufficient or ambiguous.
- `PHYSICAL_DEVICE_PASSED` — the expected device behavior was observed on real hardware.
- `CARRIER_VALIDATED` — carrier-dependent behavior was observed with real service and passed.
- `HUMAN_VALIDATED` — a required human interaction/accessibility/design check passed.

A stronger evidence state must never be inferred from a weaker one.

## Minimum platform coverage

The supported matrix should include, where hardware is available:

| Platform band | Purpose | Status |
| --- | --- | --- |
| Android 10 / API 29 | minimum-SDK compatibility | `NOT_RUN` |
| Android 11 / API 30 | legacy supported runtime and comparison with CI emulator lane | `NOT_RUN` |
| Android 12–13 / API 31–33 | `Call.Details.state` era and OEM notification/role behavior | `NOT_RUN` |
| Android 14 / API 34 | modern `CallEndpoint` and full-screen-intent behavior | `NOT_RUN` |
| Android 15 / API 35 | newer Telecom/notification compatibility | `NOT_RUN` |
| targetSdk 36 platform | target-era behavior and release acceptance | `NOT_RUN` |

## Minimum OEM coverage

The initial physical-device program should cover at least:

| OEM family | Purpose | Status |
| --- | --- | --- |
| Pixel / AOSP-near | baseline Android behavior | `NOT_RUN` |
| Samsung | major customized Telecom/lock-screen behavior | `NOT_RUN` |
| Motorola | additional broadly deployed Android implementation | `NOT_RUN` |
| Additional supported OEM | chosen from actual GoreeCloud-supported user/device coverage | `NOT_RUN` |

## Required scenario matrix

Record each scenario independently for every qualifying device/carrier combination.

| Scenario | Expected contract | Evidence state |
| --- | --- | --- |
| ROLE_DIALER request | Android user-consent flow appears only after GoreeCloud acceptance allows preparation | `NOT_RUN` |
| ROLE_DIALER persistence/revocation | role state refreshes truthfully after settings/system changes | `NOT_RUN` |
| Outgoing PSTN placement | accepted non-emergency calls use `TelecomManager.placeCall` through the selected safe route | `NOT_RUN` |
| Incoming PSTN — unlocked | incoming presentation appears and controls operate correctly | `NOT_RUN` |
| Incoming PSTN — locked | lock-screen/full-screen-or-notification behavior follows Android/OEM policy | `NOT_RUN` |
| Incoming PSTN — screen off | call presentation remains usable without overstating full-screen access | `NOT_RUN` |
| Full-screen access unavailable | presentation degrades to notification rather than claiming full-screen success | `NOT_RUN` |
| Answer / Decline / End | actions are state-valid and observed Telecom state follows the request where supported | `NOT_RUN` |
| Hold / Resume | controls appear only when capability evidence permits them | `NOT_RUN` |
| Mute / Unmute | UI and Telecom mute evidence stay synchronized | `NOT_RUN` |
| DTMF | user-driven tones operate and stop correctly on real calls | `NOT_RUN` |
| Post-dial wait | user confirmation is required and remaining digits are not leaked into public diagnostics | `NOT_RUN` |
| Ongoing `CallStyle` continuity | incoming presentation transitions correctly to ongoing/end controls | `NOT_RUN` |
| Ringtone interaction/ownership | behavior is validated before any `IN_CALL_SERVICE_RINGING` claim | `NOT_RUN` |
| Earpiece / speaker | supported output transitions work on hardware | `NOT_RUN` |
| Wired headset | supported endpoint behavior is correct | `NOT_RUN` |
| Bluetooth | supported endpoint behavior is correct without publishing device identity | `NOT_RUN` |
| API 34+ `CallEndpoint` change | requests use Telecom-supplied endpoints and observed result matches callback evidence | `NOT_RUN` |
| Single-SIM discovery | available calling account evidence is truthful and privacy-minimized | `NOT_RUN` |
| Multi-SIM/eSIM discovery | multiple accounts are discovered only with required authority | `NOT_RUN` |
| Explicit account selection | chosen non-emergency account is preserved where Android/carrier permits | `NOT_RUN` |
| Stale route | unavailable saved route fails closed rather than silently switching accounts | `NOT_RUN` |
| Emergency-safe route boundary | GoreeCloud does not force experimental account routing or automation for emergency/indeterminate numbers | `NOT_RUN` |
| Conference / merge / swap / separate | controls appear and execute only where live capability/carrier evidence supports them | `NOT_RUN` |
| Disconnect outcome mapping | missed/rejected/busy/local/remote/etc. classification matches Android generic disconnect evidence | `NOT_RUN` |
| Activity resume refresh | role/permission/account evidence refreshes after returning from system settings | `NOT_RUN` |
| Process death/recreation | no stale process-local Telecom authority is reused after recreation | `NOT_RUN` |

## Per-execution record

Every executed scenario must record:

- GoreeCloud commit SHA and build identity
- device model and OEM
- Android version / API level
- carrier/network context at a non-sensitive level
- SIM/eSIM configuration class, without subscriber identifiers
- permission and role state before execution
- feature/capability evidence before execution
- expected Android/GoreeCloud behavior
- observed behavior
- evidence state
- failure or limitation notes
- reproduction notes where applicable

## Machine-readable accepted-entry contract

The repository machine-readable record at `acceptance/physical-device-carrier.json` uses schema version 4. Any scenario that is counted as verified must be bound to one explicitly declared minimum-release-set matrix target and include all of these fields:

- `matrix_target_id`
- `scenario_id`
- `result`
- `evidence_level`
- `procedure_version`
- `test_method`
- `safety_boundary`
- `source_revision`
- `build_identity`
- `device_model`
- `oem`
- `android_version`
- `api_level`
- `carrier_context_class`
- `sim_configuration_class`
- `role_state_before`
- `capability_state_before`
- `expected_behavior`
- `observed_result`
- `limitations`
- `reproduction_notes`
- `observed_at`

The validator requires an exact 40-character Git source revision, a declared matrix-target binding, procedure version `1.0`, an approved test method, an explicit safety boundary, and a timezone-qualified observation timestamp. Counted evidence must use one of the issue #14 evidence levels `physical-device-tested`, `carrier-validated`, or `human-validated`; carrier-dependent scenarios require both `carrier-validated` evidence and `carrier-call-validation`. The `emergency_safety_boundary` scenario may count only with `safe-platform-emergency-validation` and `no-emergency-services-contact`. Recursive evidence-key inspection rejects prohibited sensitive identifier/content fields even when they are nested.

## Minimum supported release-set binding

Schema version 4 requires the repository to define the minimum physical-device/carrier release set explicitly before physical-device/carrier status can become `accepted`. The top-level `minimum_supported_release_set` is a privacy-safe list of matrix targets. Each target must carry:

- `matrix_target_id`
- `device_model_class`
- `oem_family`
- `android_version`
- `api_level`
- `carrier_context_class`
- `sim_configuration_class`
- `required_scenarios`

Every counted scenario entry must reference one declared `matrix_target_id`. For `accepted` status, every scenario required by every declared target must have qualifying evidence, and the union of target scenario scopes must cover the complete governed issue #14 scenario set.

The current Development record intentionally leaves `minimum_supported_release_set` empty because the authoritative supported release set has not yet been established from real hardware/carrier scope. The validator therefore cannot permit an accepted claim merely because one instance of each scenario appears somewhere in the evidence record.

This schema change defines evidence integrity and safety binding only. It does not choose supported devices, OEMs, carriers, SIM/eSIM configurations, or release scope on the owner's behalf and it creates no physical-device/carrier evidence.

## Safety and privacy rules

- Never place a real emergency call as an automated test.
- Emergency-boundary validation must use safe, platform-supported procedures that do not contact emergency services unless a separately approved manual emergency-testing program exists.
- Do not publish phone numbers, ICCIDs, IMSIs, subscription IDs, `PhoneAccountHandle` identifiers, Bluetooth device identities, call audio, transcripts, voicemail content, or other sensitive call data.
- Do not flip `DevelopmentDefaultDialerAcceptance` flags from source presence, build success, or emulator success alone.
- Do not describe a carrier-dependent behavior as accepted from Wi-Fi-only/emulator testing.
- When carrier or OEM behavior conflicts with the expected contract, record the failure or limitation rather than masking it with fallback assumptions.

## Promotion rule

A production capability may be promoted only from evidence that matches its real dependency scope. Device-only behavior requires physical-device evidence; carrier behavior requires carrier validation; human interaction/design/accessibility behavior requires human validation; privacy/security-sensitive operations additionally require their own GoreeCloud framework acceptance.

Managed-emulator success remains supporting platform evidence and never substitutes for this matrix.
