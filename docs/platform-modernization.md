# Android Platform Modernization

## Status

Planned build-platform upgrade. This document is not a runtime capability claim.

## Current verified baseline

- compile SDK 36
- target SDK 36
- minimum SDK 29
- Android Gradle Plugin 8.10.1
- Kotlin 2.1.21
- Gradle 8.11.1 in CI
- Java/JVM 17

## Why this upgrade is now required

Current stable AndroidX and Compose releases have begun requiring a newer Android build baseline. A direct dependency-only update was tested on 2026-09-13 and failed AAR metadata validation because current stable Core, Lifecycle and Compose artifacts require compile SDK 37 and Android Gradle Plugin 9.1 or later.

GoreeCloud Dialer therefore must not update these dependencies piecemeal while keeping the older toolchain. The platform upgrade should be treated as one atomic, separately validated migration.

## Planned migration boundary

The modernization should evaluate and migrate together:

- compile SDK 37
- Android Gradle Plugin 9.1 or a later compatible stable release
- a Gradle version supported by that AGP release
- Kotlin and Compose compiler/plugin compatibility
- current stable AndroidX Core, Activity, Lifecycle, Compose UI/Foundation and Material 3
- CI unit tests
- Android lint
- debug APK assembly
- instrumentation-test APK compilation

Changing `targetSdk` is a separate runtime-behavior decision. The build-platform migration must not silently change target SDK merely because compile SDK advances.

## Acceptance requirements

The migration is accepted only when:

1. the complete dependency/toolchain combination is supported by official Android/Kotlin compatibility guidance;
2. the project resolves without AAR metadata violations;
3. unit tests pass;
4. Android lint passes without introducing a suppression baseline for new blockers;
5. the debug APK assembles;
6. the instrumentation-test APK compiles;
7. source documentation and the source manifest are updated together;
8. no telecom, role, privacy, security or carrier capability is promoted solely because the build toolchain changed.

Physical-device and carrier validation remain separate from build-platform acceptance.
