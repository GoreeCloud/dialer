# GoreeCloud Dialer Release Gates

## Development is not Stable

GoreeCloud Dialer remains Active Development / pre-Stable. Passing source checks does not promote the application to Stable.

## Build gate

The build gate requires the current `main` revision to pass JVM unit tests, Android lint, debug APK assembly, release-variant assembly with R8/minification enabled, and instrumentation-test APK compilation.

Release-variant assembly is build-integrity evidence only. It exercises release resources, minification, R8 and release packaging configuration, but does not prove signing, distribution readiness, device behavior, production acceptance, or Stable status.

## Managed-emulator gate

After the build gate passes, independent managed-emulator lanes execute the deterministic instrumentation suite on Android 11 / API 30 and Android 14 / API 34.

The API 30 lane protects the older supported runtime contract. The API 34 lane verifies the newer Android Telecom/notification platform surface, including modern `CallEndpoint` API availability and full-screen-intent capability probing, while keeping default-dialer consent fail closed.

Managed-emulator success is not physical-device, carrier, SIM/eSIM, emergency-network, OEM, ringtone, Bluetooth/wired-audio, real-call, or live endpoint-routing acceptance.

## Physical-device and carrier gate

Production telephony capabilities must be promoted only from recorded evidence in `docs/physical-device-acceptance.md` and Issue #14. Device, Android version, OEM behavior, carrier context, SIM/eSIM class, role/permission state, expected result, observed result, and evidence level are recorded independently.

No automated acceptance test may place a real emergency call. Sensitive subscriber, device, call-content, or endpoint identity data must not be published as acceptance evidence.

## Platform gate

The Android platform baseline and dependency set must be mutually compatible. Major build-platform migrations are accepted only as complete, tested combinations rather than piecemeal version changes.

## Telecom gate

Production phone-app acceptance requires independent evidence for Android default-dialer requirements, incoming and ongoing call UI, outgoing call placement, essential controls, emergency-safe behavior, supported audio/SIM/conference paths, and relevant physical-device/carrier behavior.

## Privacy and security gate

Sensitive persistence, recording, transcription, voicemail content, AI processing, backup, export, and cross-device behavior require the appropriate Privacy Shield, Wardveil, retention, and other runtime authorization evidence before being promoted.

## Design and accessibility gate

Stable release acceptance also requires the current mandated Glaze UI version plus human validation of accessibility and critical call workflows. Build or emulator success does not substitute for these checks.

## Evidence rule

Every release claim must state the strongest evidence actually obtained. Planned, source-present, compiled, linted, unit-tested, debug-assembled, release-variant-assembled, instrumentation-test-compiled, API-specific managed-emulator-tested, physical-device-tested, carrier-validated, human-validated, signed/distribution-ready, and Stable are separate evidence levels.
