# Testing Strategy

## Development layers

GoreeCloud Dialer separates test evidence by layer so one successful test type cannot imply a stronger acceptance state.

### JVM unit tests

Used for pure policy and mapping contracts such as capability truth, lifecycle mapping, routing decisions, emergency boundaries, call disposition mapping, control rejection, and operation-result semantics.

### Instrumentation test source

`app/src/androidTest` contains Android-instrumentation test code. CI compiles the instrumentation-test APK as a build-level compatibility check.

### Managed-emulator acceptance

CI also executes a separate Gradle Managed Device job on an Android 11 / API 30 AOSP Automated Test Device (`pixel2Api30`). This layer exercises deterministic Android runtime contracts that do not require a physical carrier environment, including:

- canonical package execution
- `MainActivity` launch on Android
- `ACTION_DIAL` + `tel:` resolution into GoreeCloud Dialer
- `InCallService` manifest/export/permission contract
- the current fail-closed default-dialer role-request gate
- the Development UI keeping carrier-call placement and default-role request controls blocked while acceptance is incomplete
- Android automatic-backup flag remaining disabled

A successful managed-emulator run is stronger evidence than test-source compilation, but it is not physical-device or carrier acceptance.

### Physical-device and carrier acceptance

Telecom behavior that depends on real telephony hardware, SIM/eSIM state, carrier provisioning, emergency routing, incoming PSTN calls, real Bluetooth/wired endpoints, conference support, ringtone ownership, OEM lock-screen behavior, and end-to-end default-phone replacement still requires appropriate physical-device/carrier validation.

## Truth rule

Test source presence, successful test compilation, JVM-test success, managed-emulator success, physical-device success, carrier acceptance, and human validation are independent evidence states. Documentation and UI must state the strongest evidence actually obtained and no stronger.
