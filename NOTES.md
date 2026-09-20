# GoreeCloud Dialer — Development Notes

## Current stabilization context

- Repository lifecycle remains Development and is not Stable or production accepted.
- GitHub repository identity is now `GoreeCloud/dialer` after a repository rename. Repository ID, product identity, Android package namespaces, evidence schema identifiers, and telephony acceptance boundaries remain unchanged; the rename does not create physical-device/carrier evidence or release authority.
- Authoritative main before this candidate is `ae9cbadd97723d4d465efb99871c51e733bdd44a`, including the Platform Contract 0.4 manifest, exact-source Android CI hardening, the fail-closed physical-device/carrier acceptance gate through schema v4, and the repository rename to `GoreeCloud/dialer`. This V1.6 branch is not current integration authority until exact-head CI succeeds and governed merge/readback occurs.
- Android Telecom capability, default-dialer role behavior, multi-SIM routing, call controls, emergency-call safety, Glaze UI acceptance, and physical-device/carrier validation remain active stabilization areas.
- GitHub issue #14 is the physical-device/carrier acceptance gate. Emulator or source evidence must not be represented as carrier/PSTN/device acceptance.
- Draft PR #20 is historical Glaze UI 1.4.1 / Platform Contract 0.2 provenance only. The current repository target is Official Stable Glaze UI 1.6.0 and Platform Contract 0.4 through the fail-closed root platform manifest; conformance and visual acceptance remain unestablished.

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
- A future accepted scenario must identify an exact source commit, real device model, Android version, privacy-safe carrier context class, observation time, and the explicit `carrier-validated` evidence level.
- Acceptance records reject prohibited sensitive fields such as phone numbers, ICCIDs, IMSIs, subscriber identifiers, Bluetooth identities, call audio, recordings, and transcripts.
- This validation hardens the release boundary only. It does not perform carrier testing and does not satisfy issue #14.


## Physical-device/carrier evidence schema v2 — September 19, 2026

- PR #26 is integrated on `main` and strengthens the machine-readable issue #14 evidence contract before any real-device result can be counted.
- Accepted entries must now carry exact source/build identity, device model and OEM, Android/API version, privacy-safe carrier and SIM/eSIM classes, role/capability preconditions, expected behavior, observed result, limitations, reproduction notes, and a timezone-qualified observation timestamp.
- The validator recursively rejects prohibited sensitive evidence keys, including nested phone/subscriber/account/Bluetooth/call-content fields, and distinguishes issue #14 physical-device-tested, carrier-validated, and human-validated evidence. Carrier-dependent scenarios must use carrier-validated evidence before they can count.
- The repository record remains status `blocked` with zero verified scenarios. This work improves evidence integrity only and does not create physical-device or carrier acceptance.

## Physical-device/carrier matrix-scope hardening — September 19, 2026

- PR #29 is integrated on `main` and advances the issue #14 machine-readable acceptance contract to schema version 3.
- Counted evidence must now bind to an explicitly declared privacy-safe `matrix_target_id` from the top-level minimum supported release set.
- An accepted claim requires a non-empty minimum supported release set, complete qualifying evidence for every scenario required by every declared matrix target, and complete coverage of the governed issue #14 scenario set across that release scope.
- The current record intentionally leaves the minimum supported release set empty because supported real-hardware/carrier scope has not yet been established; physical-device/carrier status therefore remains blocked with zero verified entries.
- This improves evidence integrity only. It does not invent supported devices, execute PSTN/carrier testing, create SIM/eSIM evidence, contact emergency services, or establish production, Release Candidate, or Stable qualification.


## Physical-device/carrier evidence schema v4 — September 19, 2026

- PR #31 is integrated on `main` and advances the issue #14 acceptance record from schema version 3 to schema version 4 without creating any physical-device or carrier result.
- Any future counted evidence must bind to acceptance procedure version `1.0`, an approved privacy-safe `test_method`, and an explicit `safety_boundary`.
- Carrier-dependent scenarios require both `carrier-validated` evidence and `carrier-call-validation`; the emergency-safety scenario may count only through `safe-platform-emergency-validation` with `no-emergency-services-contact`.
- The minimum supported release set remains empty and verified scenarios remain zero, so physical-device/carrier acceptance remains blocked.
- The root Platform Contract now identifies GLAZE UI V1.6 / 1.6.0 as the current required shared target; this does not establish Dialer-local Glaze implementation or acceptance.

- Accepted exact-head evidence for PR #31: Android CI run `35490210001` succeeded, including the schema-v4 integrity gate, build/test/lint, and API 29/30/34 managed-device acceptance, before squash merge to authoritative main commit `5fe2f94141a3ddd07e7545477fba801037f000ac`.


## GLAZE UI V1.6 source-mapping candidate — September 20, 2026

- This Development branch maps native Dialer presentation source to exact Official Stable GLAZE UI V1.6 / 1.6.0 release source `a7180679ea851389e0f3004515f9a25f420e716d`.
- The Dialer shell remains certainty-first and requests solid presentation. A first-party V1.6 policy adds bounded Reduced Transparency, Reduced Motion, constrained-performance, large-text/reflow, visible-focus, and interaction-target semantics without creating any telephony authority.
- The inherited V1.6 coarse interaction floor is recorded as 44dp and the pointer-compact floor as 32dp. Dialer deliberately retains a stricter 48dp ordinary target and 56dp Touch Assistance target.
- Phone numbers, call/carrier/SIM state, permissions, default-dialer role state, Telecom capability evidence, emergency classification, and issue #14 acceptance evidence remain outside presentation authority.
- The existing disabled Carrier Call control remains disabled; the source mapping does not activate placement, role consent, PSTN behavior, or carrier/device acceptance.
- Platform Contract Glaze moves from `applicable-migration-required` to `applicable-blocked` while overall conformance remains `nonconformant`. Rendered/accessibility/adaptive/device/performance/rollback/Human Visual Excellence/platform/signing/release acceptance remains open.
- Issue #14 remains independently blocked with an empty minimum supported release set and zero verified physical-device/carrier entries.
- Superseded candidate head `6e734bdc5e8b00258dd6e66d130f1e9cfbb7f6a5` failed Android CI run `35493619281` after the issue #14 record passed because the new Glaze source guard required a one-line Kotlin `Text(...)` spelling for the unchanged carrier-blocked message. The guard was narrowed to the stable message literal without weakening the telephony or carrier acceptance boundary; that failed head remains audit evidence and is not accepted.
