# GoreeCloud Dialer Architecture

## Status

Development architecture for `GoreeCloud/goreecloud-dialer`. Planned modules and source-present Development boundaries are not production runtime claims.

## Primary rule

Authorization, platform capability, carrier capability, permission/role state, GoreeCloud acceptance, request submission and verified operation success are separate facts. No global state may imply that all Dialer protections or telecom features are accepted.

## Runtime state

Every capability should distinguish unsupported, unavailable, permission/role required, available, active, submitted, failed and succeeded states where those states are meaningful. User interfaces should surface why a capability is unavailable when that information helps the user act.

`Call.Details` is treated as transient Telecom evidence, not durable application state. The public call snapshot projects only narrow booleans needed to explain controls. If call details are missing, capability-derived controls fail closed.

## ACTION_DIAL boundary

`MainActivity` handles Android `ACTION_DIAL` with and without a `tel:` URI. The address is transferred to a local keypad without automatically placing a carrier call, normalizing the number, persisting it, or sending it to GoreeCloud services. Unsupported URI schemes are ignored.

## InCallService lifecycle boundary

`GoreeCloudInCallService` is registered with `BIND_INCALL_SERVICE` and tracks call additions, removals, lifecycle transitions, whether another call can be added, narrow control capabilities, content-minimized conference relationships, and whether Android has paused a post-dial sequence for user confirmation. Live Android `Call` references remain process-local only while Telecom owns the call.

Public runtime snapshots project generated session IDs, lifecycle categories, aggregate state, narrow capability booleans, endpoint categories, conference relationships expressed only as generated session IDs, and minimized post-dial wait metadata. They do not expose phone numbers, caller names, phone-account identifiers, endpoint device names, `Call.Details`, post-dial sequence content, transcripts, audio, recordings or voicemail content.

## Essential call controls

The call-control layer accepts an explicit process-local session ID and a user-driven action. A state- and capability-aware policy rejects actions that do not match the current lifecycle state or live Telecom capability before invoking Android Telecom. Source contracts cover answering an audio call, declining/end, hold/resume, and starting/stopping DTMF tones.

Hold and Resume distinguish feature support from current availability. The UI can therefore explain that Hold is unsupported versus supported but temporarily unavailable. DTMF remains user-driven and uses bounded tone pulses in the Development surface.

A non-throwing invocation of Answer, Decline, End, Hold, Resume, DTMF, or Mute is reported as **submitted**, not succeeded. Later Telecom callbacks/state are the authority for whether the requested state actually changed.

## Mute and audio routing

Mute state comes from `InCallService` Telecom callbacks. The Development mute control is shown only while an active/held call reports mute capability. Android 14+ endpoint discovery uses `CallEndpoint` callbacks and `requestCallEndpointChange`; endpoint display names/device identities are not projected. Pre-Android-14 route switching remains unavailable rather than falling back to deprecated route-control APIs.

## Conference boundary

Conference authority is derived from Android Telecom evidence rather than from the number of simultaneous calls.

- `Call.getConferenceableCalls()` determines which tracked generated session IDs may be offered for pairwise conferencing.
- `CAPABILITY_MANAGE_CONFERENCE` is retained as explanatory support evidence.
- `CAPABILITY_MERGE_CONFERENCE`, `CAPABILITY_SWAP_CONFERENCE`, and `CAPABILITY_SEPARATE_FROM_CONFERENCE` independently gate their corresponding controls.
- Parent and child relationships are projected only as generated session IDs.
- Pairwise conference, merge, swap and separate requests are state-gated to active/held sessions.
- A request that reaches Android Telecom is reported as **submitted**, not as completed. Later runtime evidence is required before GoreeCloud may claim completion.
- Stale or untracked target sessions fail closed.

Conference controls remain Development boundaries until device/carrier validation and production in-call UI acceptance are complete.

## Post-dial wait boundary

Android may pause an outgoing post-dial sequence and require the in-call application to ask whether to continue. GoreeCloud tracks only that a wait is pending plus the number of remaining characters; the actual remaining sequence is not projected into the public runtime snapshot.

The Development in-call surface presents explicit **Continue** and **Cancel** choices. Either choice is accepted only for a tracked connected/outgoing call with a currently pending wait, then forwarded through `Call.postDialContinue(...)`. A non-throwing invocation is reported as submitted. Disconnected/stale sessions and duplicate actions fail closed.

This boundary is a conventional telephony requirement and does not enable Smart DTMF, Call Assistant, automatic phone-menu navigation, or secret entry.

## Phone-account and emergency boundary

Call-capable `PhoneAccountHandle` values are retained only process-locally behind `READ_PHONE_STATE`. Public state uses anonymous generated route IDs and whether Android reports a system-default route; account labels, numbers, carriers, SIM identifiers and subscription identifiers are not projected.

Loss of telephony, Telecom availability, or `READ_PHONE_STATE` revokes cached phone-account authority. For outgoing placement, a stale explicit selection is rejected rather than silently replaced.

Emergency-number classification is a higher-priority routing boundary. Emergency and indeterminate-emergency numbers ignore GoreeCloud's experimental account selection and delegate phone-account routing to Android Telecom.

## Default-dialer eligibility gate

GoreeCloud Dialer handles `Intent.ACTION_DIAL` and contains an `InCallService` lifecycle/control foundation, Development incoming/ongoing presentation, and a dormant outgoing-call adapter. Production incoming/ongoing UI acceptance, outgoing-call acceptance and device validation remain incomplete. The application keeps role-request eligibility closed until those requirements are implemented and validated together. Detecting or even holding `ROLE_DIALER` remains a separate platform fact from GoreeCloud acceptance.

## Local-first boundary

Core telephony must not depend on GoreeCloud servers. Intelligent features select among a local provider, explicitly authorized remote processing, or unavailable. Local-to-cloud fallback must never be silent.
