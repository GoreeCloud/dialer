# GoreeCloud Dialer — Development Notes

## Current stabilization context

- Repository lifecycle remains Development and is not Stable or production accepted.
- Latest source-bearing stabilization baseline: `599f5d49c5504d7030eff4df2404e07b994c8f89`, including the Platform Contract 0.4 manifest, exact-source Android CI hardening, reconciled stabilization notes, the fail-closed physical-device/carrier acceptance gate from PR #25, and the integrated PR #26 issue #14 evidence schema v2 hardening. Documentation-only PR #27 followed that source-bearing baseline without changing Dialer runtime behavior or carrier-acceptance state.
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


## Physical-device/carrier acceptance gate — September 19, 2026

- The repository carries an explicit machine-readable issue #14 acceptance record whose truthful current state is `blocked`.
- CI verifies that the complete governed physical-device/carrier scenario set remains present and that source, build, lint, managed-emulator, or instrumentation evidence cannot silently be relabeled as physical-device/carrier acceptance.
- A future accepted scenario must identify an exact source commit, real device model, Android version, privacy-safe carrier context class, observation time, and the explicit `physical-device-carrier-validated` evidence level.
- Acceptance records reject prohibited sensitive fields such as phone numbers, ICCIDs, IMSIs, subscriber identifiers, Bluetooth identities, call audio, recordings, and transcripts.
- This validation hardens the release boundary only. It does not perform carrier testing and does not satisfy issue #14.


## Physical-device/carrier evidence schema v2 — September 19, 2026

- PR #26 is integrated on `main` and strengthens the machine-readable issue #14 evidence contract before any real-device result can be counted.
- Accepted entries must now carry exact source/build identity, device model and OEM, Android/API version, privacy-safe carrier and SIM/eSIM classes, role/capability preconditions, expected behavior, observed result, limitations, reproduction notes, and a timezone-qualified observation timestamp.
- The validator recursively rejects prohibited sensitive evidence keys, including nested phone/subscriber/account/Bluetooth/call-content fields, and distinguishes issue #14 physical-device-tested, carrier-validated, and human-validated evidence. Carrier-dependent scenarios must use carrier-validated evidence before they can count.
- The repository record remains status `blocked` with zero verified scenarios. This work improves evidence integrity only and does not create physical-device or carrier acceptance.
