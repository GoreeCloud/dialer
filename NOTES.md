# GoreeCloud Dialer — Development Notes

## Current stabilization context

- Repository lifecycle remains Development and is not Stable or production accepted.
- Verified current stabilization base: `main` at `a2a1144227f521224c4ef78b34527fede497c32b`, including the Platform Contract 0.4 manifest and exact-source Android CI hardening from PR #23.
- Android Telecom capability, default-dialer role behavior, multi-SIM routing, call controls, emergency-call safety, Glaze UI acceptance, and physical-device/carrier validation remain active stabilization areas.
- GitHub issue #14 is the physical-device/carrier acceptance gate. Emulator or source evidence must not be represented as carrier/PSTN/device acceptance.
- Draft PR #20 is historical Glaze UI 1.4.1 / Platform Contract 0.2 provenance only. The current repository target is Official Stable Glaze UI 1.5.1 and Platform Contract 0.4 through the fail-closed root platform manifest; conformance and visual acceptance remain unestablished.

## Active stabilization observations

- Capabilities must remain explicit and fail closed: unsupported, unavailable, role/permission required, available, active, attempted/failed, and succeeded are not interchangeable states.
- Multi-SIM selection must not silently override an explicit user choice.
- Emergency-call behavior must preserve Android/platform safety boundaries; automated acceptance must never place a real emergency call.
- Real-device acceptance evidence must avoid phone numbers, subscriber identifiers, call audio/transcripts, Bluetooth identities, and other sensitive call data.

## Maintenance notes

Do not promote Dialer based on compilation, emulator success, or an outdated Glaze migration branch. Reconcile source to current governed contracts on a clean base, then repeat exact-head CI and the required physical-device/carrier matrix before changing release status.

- Android CI is integrated with immutable third-party action revisions, Ubuntu 24.04, exact-source checkout verification, and non-persisted checkout credentials. PR #23 exact-head CI passed unit/lint/build/instrumentation plus API 29/30/34 managed-device acceptance before merge. These are Development controls and do not substitute for issue #14 physical-device/carrier acceptance.
