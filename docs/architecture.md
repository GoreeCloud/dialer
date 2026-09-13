# GoreeCloud Dialer Architecture

## Status

Development architecture for `GoreeCloud/goreecloud-dialer`. Planned modules are not runtime claims.

## Primary rule

Authorization, capability, activation, and operation success are separate facts. No global state may imply that all Dialer protections or telecom features are accepted.

## Runtime state

Every capability should distinguish unsupported, unavailable, permission/role required, available, active, failed, and succeeded states. User interfaces should surface why a capability is unavailable when that information helps the user act.

## ACTION_DIAL boundary

`MainActivity` handles Android `ACTION_DIAL` with and without a `tel:` URI. The address is transferred to a local keypad without automatically placing a carrier call, normalizing the number, persisting it, or sending it to GoreeCloud services. Unsupported URI schemes are ignored.

## InCallService lifecycle boundary

`GoreeCloudInCallService` is registered with `BIND_INCALL_SERVICE` and tracks call additions, removals, state transitions, and whether another call can be added. Live Android `Call` references remain process-local only while Telecom owns the call. Public runtime snapshots project only generated session IDs, lifecycle categories, and aggregate state. They do not expose phone numbers, caller names, account identifiers, `Call.Details`, transcripts, audio, or recordings.

## Essential call controls

The call-control layer accepts an explicit process-local session ID and a user-driven action. A state-aware policy rejects actions that do not match the current lifecycle state before invoking Android Telecom. Implemented source contracts cover answering an audio call, declining/end, hold/resume, and starting/stopping DTMF tones. Platform exceptions are returned as failed operation results rather than being reported as success.

These contracts are not yet connected to accepted incoming/ongoing call UI and therefore are not claimed as user-facing call-control acceptance.

## Default-dialer eligibility gate

GoreeCloud Dialer handles `Intent.ACTION_DIAL` and now contains an `InCallService` lifecycle/control foundation, but incoming and ongoing call UI acceptance is incomplete. The application keeps role-request eligibility closed until those requirements are implemented and validated together. Detecting or even holding `ROLE_DIALER` remains a separate platform fact from GoreeCloud acceptance.

## Emergency boundary

Emergency calling is outside experimental automation. Screening, AI, routing suggestions, recording defaults, and Call Assistant must never interfere with emergency call initiation or platform emergency behavior. Future outgoing-call integration must use the Android Telecom call path and preserve the platform's emergency behavior.

## Local-first boundary

Core telephony must not depend on GoreeCloud servers. Intelligent features select among a local provider, explicitly authorized remote processing, or unavailable. Local-to-cloud fallback must never be silent.
