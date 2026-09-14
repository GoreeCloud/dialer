# CI Validation Contract

## Build-level Development gate

Every push to `main` and pull request is expected to pass the following build-level checks:

1. JVM unit tests: `:app:testDebugUnitTest`
2. Android lint: `:app:lintDebug`
3. Debug APK assembly: `:app:assembleDebug`
4. Release-variant assembly: `:app:assembleRelease`
5. Instrumentation-test APK compilation: `:app:assembleDebugAndroidTest`

The lint step prints the text report when it fails. New lint blockers should be repaired rather than hidden behind a broad baseline or suppression.

The release-variant gate exercises release resources, R8/minification, and release packaging configuration. The resulting CI output is not described as a signed production release and is not release acceptance.

## Managed-emulator acceptance gate

After the build-level gate passes, CI provisions a Gradle Managed Device using an Android 11 / API 30 AOSP Automated Test Device and executes `:app:pixel2Api30DebugAndroidTest`.

The managed-emulator gate exercises deterministic Android runtime contracts such as Activity startup, `ACTION_DIAL` resolution, the `InCallService` manifest contract, fail-closed role-request behavior, Development control gating, and the disabled Android automatic-backup flag.

GitHub Actions runs the managed emulator with software rendering and explicit KVM access. Emulator success is recorded separately from build-only evidence.

## What a fully green run proves

A fully green run proves that the checked source revision passed the five build-level validations and the configured managed-emulator acceptance suite in GitHub Actions.

## What this does not prove

A green run does not prove:

- signed production release readiness
- physical-device execution
- carrier interoperability
- emergency-call acceptance on real networks
- end-to-end default-dialer role acceptance on supported OEM devices
- production incoming/ongoing call UI acceptance on real calls
- ringtone ownership
- multi-SIM production acceptance
- real Bluetooth/wired endpoint behavior
- conference behavior across real devices/carriers
- Privacy Shield, Wardveil or Everkeep runtime acceptance
- Glaze UI human validation
- accessibility human validation
- Stable release readiness

These require independent evidence and must not be inferred from CI compilation, packaging, minification, JVM tests, or managed-emulator success.
