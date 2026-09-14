# GoreeCloud Dialer Release Gates

## Development is not Stable

GoreeCloud Dialer remains Active Development / pre-Stable. Passing source checks does not promote the application to Stable.

## Build gate

The build gate requires the current `main` revision to pass:

- JVM unit tests
- Android lint
- debug APK assembly
- release-variant assembly with R8/minification enabled
- instrumentation-test APK compilation

Release-variant assembly is build-integrity evidence only. It exercises release resources, minification, R8 and release packaging configuration, but does not prove signing, distribution readiness, device behavior, production acceptance, or Stable status.

## Managed-emulator gate

After the build gate passes, the managed-emulator gate executes Android instrumentation tests on the configured Android 11 / API 30 AOSP Automated Test Device. This gate may accept deterministic Android runtime contracts such as Activity launch, intent resolution, manifest binding requirements, fail-closed role-request behavior, and Development control gating.

Managed-emulator success is not physical-device, carrier, SIM/eSIM, emergency-network, OEM, ringtone, Bluetooth/wired-audio, or real-call acceptance.

## Platform gate

The Android platform baseline and dependency set must be mutually compatible. Major build-platform migrations are accepted only as complete, tested combinations rather than piecemeal version changes.

## Telecom gate

Production phone-app acceptance requires independent evidence for Android default-dialer requirements, incoming and ongoing call UI, outgoing call placement, essential controls, emergency-safe behavior, supported audio/SIM/conference paths, and relevant physical-device/carrier behavior.

## Privacy and security gate

Sensitive persistence, recording, transcription, voicemail content, AI processing, backup, export, and cross-device behavior require the appropriate Privacy Shield, Wardveil, retention, and other runtime authorization evidence before being promoted.

## Design and accessibility gate

Stable release acceptance also requires the current mandated Glaze UI version plus human validation of accessibility and critical call workflows. Build or emulator success does not substitute for these checks.

## Evidence rule

Every release claim must state the strongest evidence actually obtained. Planned, source-present, compiled, linted, unit-tested, debug-assembled, release-variant-assembled, instrumentation-test-compiled, managed-emulator-tested, physical-device-tested, carrier-validated, human-validated, signed/distribution-ready, and Stable are separate evidence levels.
