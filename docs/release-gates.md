# GoreeCloud Dialer Release Gates

## Development is not Stable

GoreeCloud Dialer remains Active Development / pre-Stable. Passing source checks does not promote the application to Stable.

## Build gate

The build gate requires the current `main` revision to pass:

- JVM unit tests
- Android lint
- debug APK assembly
- instrumentation-test APK compilation

## Platform gate

The Android platform baseline and dependency set must be mutually compatible. Major build-platform migrations are accepted only as complete, tested combinations rather than piecemeal version changes.

## Telecom gate

Production phone-app acceptance requires independent evidence for Android default-dialer requirements, incoming and ongoing call UI, outgoing call placement, essential controls, emergency-safe behavior, supported audio/SIM/conference paths, and relevant physical-device/carrier behavior.

## Privacy and security gate

Sensitive persistence, recording, transcription, voicemail content, AI processing, backup, export, and cross-device behavior require the appropriate Privacy Shield, Wardveil, retention, and other runtime authorization evidence before being promoted.

## Design and accessibility gate

Stable release acceptance also requires the current mandated Glaze UI version plus human validation of accessibility and critical call workflows. Build success does not substitute for these checks.

## Evidence rule

Every release claim must state the strongest evidence actually obtained. Planned, source-present, compiled, linted, unit-tested, instrumented-test-compiled, emulator-tested, device-tested, carrier-validated, human-validated, and Stable are separate evidence levels.
