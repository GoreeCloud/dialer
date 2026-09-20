#!/usr/bin/env python3
"""Fail-closed validation for GoreeCloud Dialer physical-device/carrier acceptance.

This validator checks only the integrity and truthfulness of the repository acceptance
record. It cannot create physical-device or carrier evidence and must never be used to
reinterpret emulator/source evidence as PSTN, SIM/eSIM, OEM, Bluetooth, or carrier acceptance.
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RECORD = ROOT / "acceptance" / "physical-device-carrier.json"

REQUIRED_SCENARIOS = {
    "role_request_and_persistence",
    "outgoing_pstn_placement",
    "incoming_unlocked",
    "incoming_locked_or_screen_off",
    "call_controls",
    "ongoing_call_notification",
    "ringtone_ownership",
    "audio_endpoint_routing",
    "sim_and_esim_route_selection",
    "emergency_safety_boundary",
    "conference_operations",
    "disconnect_outcomes",
    "process_recreation_and_resume",
}

SENSITIVE_KEYS = {
    "phone_number",
    "telephone_number",
    "iccid",
    "imsi",
    "subscriber_id",
    "subscriber_identifier",
    "account_handle",
    "bluetooth_identity",
    "bluetooth_address",
    "call_audio",
    "call_recording",
    "transcript",
}

SHA40 = re.compile(r"^[0-9a-f]{40}$")
STATUS_VALUES = {"blocked", "in_progress", "accepted"}

errors: list[str] = []

try:
    data = json.loads(RECORD.read_text(encoding="utf-8"))
except Exception as exc:
    print(f"Dialer physical-device acceptance gate FAILED: cannot read {RECORD}: {exc}", file=sys.stderr)
    raise SystemExit(1)

if data.get("schema_version") != 1:
    errors.append("schema_version must be 1")
if data.get("product") != "GoreeCloud Dialer":
    errors.append("product must be GoreeCloud Dialer")
if data.get("lifecycle") != "development":
    errors.append("repository acceptance record must remain lifecycle=development until separately promoted")
if data.get("issue") != 14:
    errors.append("acceptance record must remain bound to GitHub issue #14")

status = data.get("status")
if status not in STATUS_VALUES:
    errors.append(f"status must be one of {sorted(STATUS_VALUES)}")

required = data.get("required_scenarios")
if not isinstance(required, list) or set(required) != REQUIRED_SCENARIOS or len(required) != len(REQUIRED_SCENARIOS):
    errors.append("required_scenarios must exactly match the governed physical-device/carrier matrix")

production_claim = data.get("production_claim")
if not isinstance(production_claim, bool):
    errors.append("production_claim must be a boolean")
elif production_claim and status != "accepted":
    errors.append("production_claim cannot be true while physical-device/carrier status is not accepted")

verified = data.get("verified_scenarios")
if not isinstance(verified, list):
    errors.append("verified_scenarios must be a list")
    verified = []

seen: set[str] = set()
for entry in verified:
    if not isinstance(entry, dict):
        errors.append("every verified_scenarios entry must be an object")
        continue

    lowered_keys = {str(key).lower() for key in entry}
    leaked = sorted(lowered_keys & SENSITIVE_KEYS)
    if leaked:
        errors.append(f"acceptance evidence contains prohibited sensitive keys: {', '.join(leaked)}")

    scenario = entry.get("scenario_id")
    if scenario not in REQUIRED_SCENARIOS:
        errors.append(f"unknown or missing scenario_id: {scenario!r}")
        continue
    if scenario in seen:
        errors.append(f"duplicate scenario evidence: {scenario}")
    seen.add(scenario)

    if entry.get("result") != "pass":
        errors.append(f"{scenario}: result must be pass before it counts as verified")
    if entry.get("evidence_level") != "physical-device-carrier-validated":
        errors.append(f"{scenario}: evidence_level must be physical-device-carrier-validated")
    revision = entry.get("source_revision")
    if not isinstance(revision, str) or not SHA40.fullmatch(revision):
        errors.append(f"{scenario}: source_revision must be an exact 40-character Git commit SHA")

    for field in ("device_model", "android_version", "carrier_context_class", "observed_at"):
        value = entry.get(field)
        if not isinstance(value, str) or not value.strip():
            errors.append(f"{scenario}: {field} is required for physical-device evidence")

if status == "accepted":
    missing = sorted(REQUIRED_SCENARIOS - seen)
    if missing:
        errors.append("accepted status is invalid while required scenarios remain missing: " + ", ".join(missing))
    if production_claim is not True:
        errors.append("accepted physical-device/carrier status must explicitly set production_claim=true")
else:
    if production_claim:
        errors.append("non-accepted status must keep production_claim=false")

if errors:
    print("Dialer physical-device/carrier acceptance gate FAILED:", file=sys.stderr)
    for error in errors:
        print(f"- {error}", file=sys.stderr)
    raise SystemExit(1)

print(
    "Dialer physical-device/carrier acceptance record passed integrity checks "
    f"(status={status}, verified={len(seen)}/{len(REQUIRED_SCENARIOS)})."
)
