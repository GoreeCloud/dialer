# Testing Strategy

## Development layers

GoreeCloud Dialer separates test evidence by layer so one successful test type cannot imply a stronger acceptance state.

### JVM unit tests

Used for pure policy and mapping contracts such as capability truth, lifecycle mapping, routing decisions, emergency boundaries, call disposition mapping, control rejection, and operation-result semantics.

### Instrumentation test source

`app/src/androidTest` contains Android-instrumentation test code. CI currently compiles the instrumentation-test APK. Compilation proves that the Android test source remains compatible with the application/toolchain; it does not prove the test executed on an emulator or physical device.

### Future emulator/device tests

Emulator or physical-device execution should be added as a distinct CI or release-validation layer where Android platform behavior must be exercised. Telecom behavior that depends on the default-dialer role, carrier, SIM, emergency routing, incoming calls, audio devices, or conference support still requires appropriate real-device/carrier validation.

## Truth rule

Test source presence, successful test compilation, JVM-test success, emulator success, physical-device success, carrier acceptance, and human validation are independent evidence states. Documentation and UI must state the strongest evidence actually obtained and no stronger.
