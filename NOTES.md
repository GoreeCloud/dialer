# GoreeCloud Dialer — Development Notes

## Current stabilization context

- Repository lifecycle remains Development and is not Stable or production accepted.
- Verified baseline for this notes change: `main` at `81f1658f1f16c1baeb88bf16c54ed71506849972`.
- Android Telecom capability, default-dialer role behavior, multi-SIM routing, call controls, emergency-call safety, Glaze UI acceptance, and physical-device/carrier validation remain active stabilization areas.
- GitHub issue #14 is the physical-device/carrier acceptance gate. Emulator or source evidence must not be represented as carrier/PSTN/device acceptance.
- Draft PR #20 is a historically green Glaze UI 1.4.1 / Platform Contract 0.2 migration branch; it is stacked and is no longer the current Glaze consumer target now that the governed Stable line is 1.5.1.

## Active stabilization observations

- Capabilities must remain explicit and fail closed: unsupported, unavailable, role/permission required, available, active, attempted/failed, and succeeded are not interchangeable states.
- Multi-SIM selection must not silently override an explicit user choice.
- Emergency-call behavior must preserve Android/platform safety boundaries; automated acceptance must never place a real emergency call.
- Real-device acceptance evidence must avoid phone numbers, subscriber identifiers, call audio/transcripts, Bluetooth identities, and other sensitive call data.

## Maintenance notes

Do not promote Dialer based on compilation, emulator success, or an outdated Glaze migration branch. Reconcile source to current governed contracts on a clean base, then repeat exact-head CI and the required physical-device/carrier matrix before changing release status.
