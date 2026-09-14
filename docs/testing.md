# Testing Strategy

## Development layers

GoreeCloud Dialer separates test evidence by layer so one successful test type cannot imply a stronger acceptance state.

### JVM unit tests

Used for pure policy and mapping contracts such as capability truth, lifecycle mapping, routing decisions, emergency boundaries, call disposition mapping, control rejection, and operation-result semantics.

### Instrumentation test source

`app/src/androidTest` contains Android-instrumentation test code. CI compiles the instrumentation-test APK as a build-level compatibility check.

### Managed-emulator acceptance

CI executes two Gradle Managed Device lanes after the build gate:

- Android 11 / API 30 AOSP Automated Test Device (`pixel2Api30`)
- Android 14 / API 34 AOSP managed device (`pixel2Api34`)

The shared deterministic suite exercises Android runtime contracts that do not require a physical carrier environment, including canonical package execution, `MainActivity` launch, `ACTION_DIAL` + `tel:` resolution, the `InCallService` manifest/export/permission contract, the fail-closed default-dialer role-request gate, Development UI gating for carrier placement and role consent, and the disabled Android automatic-backup flag.

The Android 14 lane additionally verifies that the runtime exposes the modern `CallEndpoint` / `requestCallEndpointChange` API surface, that full-screen-intent capability can be probed, and that role consent remains fail closed. These checks prove platform/runtime availability only; they do not prove live-call endpoint changes.

A successful managed-emulator run is stronger evidence than test-source compilation, but it is not physical-device or carrier acceptance.

### Physical-device and carrier acceptance

Telecom behavior that depends on real telephony hardware, SIM/eSIM state, carrier provisioning, emergency routing, incoming PSTN calls, real Bluetooth/wired/earpiece/speaker endpoints, conference support, ringtone ownership, OEM lock-screen behavior, and end-to-end default-phone replacement still requires appropriate physical-device/carrier validation.

The canonical execution template is `docs/physical-device-acceptance.md` and is tracked by Issue #14.

## Truth rule

Test source presence, successful test compilation, JVM-test success, API-specific managed-emulator success, physical-device success, carrier acceptance, and human validation are independent evidence states. Documentation and UI must state the strongest evidence actually obtained and no stronger.
