#!/usr/bin/env python3
"""Fail-closed validation for GoreeCloud Dialer physical-device/carrier acceptance.

This validator checks only the integrity and truthfulness of the repository acceptance
record. It cannot create physical-device or carrier evidence and must never be used to
reinterpret emulator/source evidence as PSTN, SIM/eSIM, OEM, Bluetooth, or carrier acceptance.
"""

from __future__ import annotations

from datetime import datetime
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

REQUIRED_EVIDENCE_FIELDS = {
    "scenario_id",
    "result",
    "evidence_level",
    "source_revision",
    "build_identity",
    "device_model",
    "oem",
    "android_version",
    "api_level",
    "carrier_context_class",
    "sim_configuration_class",
    "role_state_before",
    "capability_state_before",
    "expected_behavior",
    "observed_result",
    "observed_at",
}

SENSITIVE_KEYS = {
    "phone_number",
    "telephone_number",
    "msisdn",
    "iccid",
    "imsi",
    "subscriber_id",
    "subscriber_identifier",
    "subscription_id",
    "account_handle",
    "phone_account_handle",
    "bluetooth_identity",
    "bluetooth_address",
    "call_audio",
    "call_recording",
    "transcript",
    "voicemail_content",
}

SHA40 = re.compile(r"^[0-9a-f]{40}$")
STATUS_VALUES = {"blocked", "in_progress", "accepted"}

errors: list[str] = []


def normalize_key(value: object) -> str:
    text = re.sub(r"([a-z0-9])([A-Z])", r"\1_\2", str(value))
    return re.sub(r"[^a-z0-9]+", "_", text.lower()).strip("_")


def sensitive_keys_in(value: object) -> set[str]:
    found: set[str] = set()
    if isinstance(value, dict):
        for key, nested in value.items():
            normalized = normalize_key(key)
            if normalized in SENSITIVE_KEYS:
                found.add(normalized)
            found.update(sensitive_keys_in(nested))
    elif isinstance(value, list):
        for nested in value:
            found.update(sensitive_keys_in(nested))
    return found


def require_nonempty_string(entry: dict[str, object], scenario: str, field: str) -> None:
    value = entry.get(field)
    if not isinstance(value, str) or not value.strip():
        errors.append(f"{scenario}: {field} is required for physical-device evidence")


def require_offset_timestamp(entry: dict[str, object], scenario: str) -> None:
    value = entry.get("observed_at")
    if not isinstance(value, str) or not value.strip():
        errors.append(f"{scenario}: observed_at is required for physical-device evidence")
        return
    try:
        parsed = datetime.fromisoformat(value.replace("Z", "+00:00"))
    except ValueError:
        errors.append(f"{scenario}: observed_at must be an ISO-8601 timestamp")
        return
    if parsed.tzinfo is None:
        errors.append(f"{scenario}: observed_at must include a timezone offset or Z")


try:
    data = json.loads(RECORD.read_text(encoding="utf-8"))
except Exception as exc:
    print(f"Dialer physical-device acceptance gate FAILED: cannot read {RECORD}: {exc}", file=sys.stderr)
    raise SystemExit(1)

if data.get("schema_version") != 2:
    errors.append("schema_version must be 2")
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

required_fields = data.get("required_evidence_fields")
if (
    not isinstance(required_fields, list)
    or set(required_fields) != REQUIRED_EVIDENCE_FIELDS
    or len(required_fields) != len(REQUIRED_EVIDENCE_FIELDS)
):
    errors.append("required_evidence_fields must exactly match the governed issue #14 evidence contract")

physical_device_carrier_claim = data.get("physical_device_carrier_claim")
if not isinstance(physical_device_carrier_claim, bool):
    errors.append("physical_device_carrier_claim must be a boolean")
elif physical_device_carrier_claim and status != "accepted":
    errors.append("physical_device_carrier_claim cannot be true while physical-device/carrier status is not accepted")

verified = data.get("verified_scenarios")
if not isinstance(verified, list):
    errors.append("verified_scenarios must be a list")
    verified = []

seen: set[str] = set()
for entry in verified:
    if not isinstance(entry, dict):
        errors.append("every verified_scenarios entry must be an object")
        continue

    leaked = sorted(sensitive_keys_in(entry))
    if leaked:
        errors.append(f"acceptance evidence contains prohibited sensitive keys: {', '.join(leaked)}")

    missing_fields = sorted(REQUIRED_EVIDENCE_FIELDS - set(entry))
    if missing_fields:
        errors.append(
            "acceptance evidence is missing required fields: " + ", ".join(missing_fields)
        )

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

    for field in (
        "build_identity",
        "device_model",
        "oem",
        "android_version",
        "carrier_context_class",
        "sim_configuration_class",
        "role_state_before",
        "capability_state_before",
        "expected_behavior",
        "observed_result",
    ):
        require_nonempty_string(entry, scenario, field)

    api_level = entry.get("api_level")
    if not isinstance(api_level, int) or isinstance(api_level, bool) or not (29 <= api_level <= 99):
        errors.append(f"{scenario}: api_level must be an integer in the supported Android API range")

    require_offset_timestamp(entry, scenario)

if status == "accepted":
    missing = sorted(REQUIRED_SCENARIOS - seen)
    if missing:
        errors.append("accepted status is invalid while required scenarios remain missing: " + ", ".join(missing))
    if physical_device_carrier_claim is not True:
        errors.append("accepted physical-device/carrier status must explicitly set physical_device_carrier_claim=true")
else:
    if physical_device_carrier_claim:
        errors.append("non-accepted status must keep physical_device_carrier_claim=false")

if errors:
    print("Dialer physical-device/carrier acceptance gate FAILED:", file=sys.stderr)
    for error in errors:
        print(f"- {error}", file=sys.stderr)
    raise SystemExit(1)

print(
    "Dialer physical-device/carrier acceptance record passed integrity checks "
    f"(schema=2, status={status}, verified={len(seen)}/{len(REQUIRED_SCENARIOS)})."
)
