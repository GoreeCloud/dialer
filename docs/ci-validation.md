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

## Managed-emulator acceptance gates

After the build-level gate passes, CI executes two independent Gradle Managed Device lanes:

- Android 11 / API 30 on the `pixel2Api30` AOSP Automated Test Device.
- Android 14 / API 34 on the `pixel2Api34` AOSP managed device.

Both lanes execute the shared deterministic Android instrumentation suite. The API 34 lane additionally executes Android-14-only checks for the modern `CallEndpoint`/`InCallService` API surface, full-screen-intent capability probing, and the fail-closed default-dialer role-consent boundary.

The managed-emulator gates exercise deterministic Android runtime contracts such as Activity startup, `ACTION_DIAL` resolution, the `InCallService` manifest contract, fail-closed role-request behavior, Development control gating, the disabled Android automatic-backup flag, and supported platform API presence. GitHub Actions runs the managed devices with software rendering and explicit KVM access. Emulator success is recorded separately from build-only evidence.

## What a fully green run proves

A fully green run proves that the checked source revision passed the five build-level validations plus the configured API 30 and API 34 managed-emulator suites in GitHub Actions.

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
- real Bluetooth/wired/earpiece/speaker endpoint behavior
- successful `CallEndpoint` switching on a live Telecom call
- conference behavior across real devices/carriers
- Privacy Shield, Wardveil or Everkeep runtime acceptance
- Glaze UI human validation
- accessibility human validation
- Stable release readiness

These require independent evidence and must not be inferred from CI compilation, packaging, minification, JVM tests, or managed-emulator success.
