# CI Validation Contract

## Current Development gate

Every push to `main` and pull request is expected to pass the following build-level checks:

1. JVM unit tests: `:app:testDebugUnitTest`
2. Android lint: `:app:lintDebug`
3. Debug APK assembly: `:app:assembleDebug`
4. Release-variant assembly: `:app:assembleRelease`
5. Instrumentation-test APK compilation: `:app:assembleDebugAndroidTest`

The lint step prints the text report when it fails. New lint blockers should be repaired rather than hidden behind a broad baseline or suppression.

The release-variant gate exercises release resources, R8/minification, and release packaging configuration. The resulting CI artifact is not described as a signed production release and is not release acceptance.

## What this proves

A green run proves that the checked source revision passed these build-level validations in GitHub Actions.

## What this does not prove

A green run does not prove:

- signed production release readiness
- emulator execution
- physical-device execution
- carrier interoperability
- emergency-call acceptance
- default-dialer role acceptance
- production incoming/ongoing UI acceptance
- ringtone ownership
- multi-SIM production acceptance
- conference behavior across real devices/carriers
- Privacy Shield, Wardveil or Everkeep runtime acceptance
- Glaze UI human validation
- accessibility human validation
- Stable release readiness

These require independent evidence and must not be inferred from CI compilation, packaging, minification, or JVM-test success.
