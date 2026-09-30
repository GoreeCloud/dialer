# GoreeCloud Dialer Benefits

GoreeCloud Dialer is intended to provide a privacy-focused native calling experience that keeps ordinary carrier calling usable without requiring a GoreeCloud account or cloud service.

## User benefits

- Core dialing remains local-first and does not depend on GoreeCloud servers.
- Telecom, carrier, SIM/eSIM, permission, role, and emergency authority remain separate from application presentation state.
- Sensitive phone-account, carrier, device, endpoint, subscriber, and provider-specific failure details are minimized from Development presentation surfaces.
- Call controls are capability-gated so unsupported or unavailable actions fail closed instead of appearing authoritative.
- Emergency-number handling preserves Android Telecom routing rather than attempting application-owned route selection.
- Accessibility, large-text behavior, touch assistance, and clear capability explanations are first-class acceptance requirements.

## Platform benefits

- Android Telecom remains the platform authority for call lifecycle, routing, and default-dialer behavior.
- Physical-device/carrier acceptance is kept separate from emulator/source evidence.
- Privacy-safe evidence records permit carrier/device validation without retaining call-content evidence or sensitive subscriber identifiers.
- Glaze UI presentation policy is prevented from becoming a source of telephony or emergency authority.

These benefits describe the source-supported Development direction. They do not establish representative-device/carrier acceptance, Production Acceptance, Release Candidate, or Stable status.
