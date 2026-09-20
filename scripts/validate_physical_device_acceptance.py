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
    "matrix_target_id",
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
    "limitations",
    "reproduction_notes",
    "observed_at",
}

REQUIRED_MATRIX_TARGET_FIELDS = {
    "matrix_target_id",
    "device_model_class",
    "oem_family",
    "android_version",
    "api_level",
    "carrier_context_class",
    "sim_configuration_class",
    "required_scenarios",
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
MATRIX_TARGET_ID = re.compile(r"^[a-z0-9][a-z0-9._-]{0,63}$")
STATUS_VALUES = {"blocked", "in_progress", "accepted"}
EVIDENCE_LEVELS = {"physical-device-tested", "carrier-validated", "human-validated"}
CARRIER_REQUIRED_SCENARIOS = {
    "outgoing_pstn_placement",
    "incoming_unlocked",
    "incoming_locked_or_screen_off",
    "call_controls",
    "ongoing_call_notification",
    "ringtone_ownership",
    "audio_endpoint_routing",
    "sim_and_esim_route_selection",
    "conference_operations",
    "disconnect_outcomes",
}

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

if data.get("schema_version") != 3:
    errors.append("schema_version must be 3")
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

required_matrix_target_fields = data.get("required_matrix_target_fields")
if (
    not isinstance(required_matrix_target_fields, list)
    or set(required_matrix_target_fields) != REQUIRED_MATRIX_TARGET_FIELDS
    or len(required_matrix_target_fields) != len(REQUIRED_MATRIX_TARGET_FIELDS)
):
    errors.append("required_matrix_target_fields must exactly match the governed issue #14 matrix-target contract")

minimum_release_set = data.get("minimum_supported_release_set")
if not isinstance(minimum_release_set, list):
    errors.append("minimum_supported_release_set must be a list")
    minimum_release_set = []

matrix_targets: dict[str, dict[str, object]] = {}
for target in minimum_release_set:
    if not isinstance(target, dict):
        errors.append("every minimum_supported_release_set entry must be an object")
        continue

    leaked = sorted(sensitive_keys_in(target))
    if leaked:
        errors.append(
            "minimum supported release-set target contains prohibited sensitive keys: "
            + ", ".join(leaked)
        )

    missing_fields = sorted(REQUIRED_MATRIX_TARGET_FIELDS - set(target))
    if missing_fields:
        errors.append(
            "minimum supported release-set target is missing required fields: "
            + ", ".join(missing_fields)
        )

    target_id = target.get("matrix_target_id")
    if not isinstance(target_id, str) or not MATRIX_TARGET_ID.fullmatch(target_id):
        errors.append(
            "minimum supported release-set matrix_target_id must use 1-64 lowercase "
            "letters, digits, dots, underscores, or hyphens"
        )
        continue
    if target_id in matrix_targets:
        errors.append(f"duplicate minimum supported release-set matrix_target_id: {target_id}")
        continue

    for field in (
        "device_model_class",
        "oem_family",
        "android_version",
        "carrier_context_class",
        "sim_configuration_class",
    ):
        value = target.get(field)
        if not isinstance(value, str) or not value.strip():
            errors.append(f"{target_id}: {field} is required for minimum supported release-set scope")

    api_level = target.get("api_level")
    if not isinstance(api_level, int) or isinstance(api_level, bool) or not (29 <= api_level <= 99):
        errors.append(f"{target_id}: api_level must be an integer in the supported Android API range")

    target_scenarios = target.get("required_scenarios")
    if (
        not isinstance(target_scenarios, list)
        or not target_scenarios
        or len(set(target_scenarios)) != len(target_scenarios)
        or not set(target_scenarios).issubset(REQUIRED_SCENARIOS)
    ):
        errors.append(
            f"{target_id}: required_scenarios must be a non-empty unique subset of the governed matrix"
        )

    matrix_targets[target_id] = target

physical_device_carrier_claim = data.get("physical_device_carrier_claim")
if not isinstance(physical_device_carrier_claim, bool):
    errors.append("physical_device_carrier_claim must be a boolean")
elif physical_device_carrier_claim and status != "accepted":
    errors.append("physical_device_carrier_claim cannot be true while physical-device/carrier status is not accepted")

verified = data.get("verified_scenarios")
if not isinstance(verified, list):
    errors.append("verified_scenarios must be a list")
    verified = []

seen: set[tuple[str, str]] = set()
seen_scenarios: set[str] = set()
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

    matrix_target_id = entry.get("matrix_target_id")
    if (
        not isinstance(matrix_target_id, str)
        or not MATRIX_TARGET_ID.fullmatch(matrix_target_id)
    ):
        errors.append("verified evidence matrix_target_id is missing or invalid")
        continue
    if matrix_target_id not in matrix_targets:
        errors.append(
            f"verified evidence references undefined minimum supported release-set target: "
            f"{matrix_target_id}"
        )

    scenario = entry.get("scenario_id")
    if scenario not in REQUIRED_SCENARIOS:
        errors.append(f"unknown or missing scenario_id: {scenario!r}")
        continue

    target = matrix_targets.get(matrix_target_id)
    target_scenarios = target.get("required_scenarios") if target else None
    if isinstance(target_scenarios, list) and scenario not in target_scenarios:
        errors.append(
            f"{matrix_target_id}/{scenario}: scenario is not required by that matrix target"
        )

    evidence_key = (matrix_target_id, scenario)
    if evidence_key in seen:
        errors.append(f"duplicate scenario evidence: {matrix_target_id}/{scenario}")
    seen.add(evidence_key)
    seen_scenarios.add(scenario)

    if entry.get("result") != "pass":
        errors.append(f"{scenario}: result must be pass before it counts as verified")
    evidence_level = entry.get("evidence_level")
    if evidence_level not in EVIDENCE_LEVELS:
        errors.append(
            f"{scenario}: evidence_level must be one of {sorted(EVIDENCE_LEVELS)}"
        )
    elif scenario in CARRIER_REQUIRED_SCENARIOS and evidence_level != "carrier-validated":
        errors.append(f"{scenario}: carrier-dependent scenario requires carrier-validated evidence")

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
        "limitations",
        "reproduction_notes",
    ):
        require_nonempty_string(entry, scenario, field)

    api_level = entry.get("api_level")
    if not isinstance(api_level, int) or isinstance(api_level, bool) or not (29 <= api_level <= 99):
        errors.append(f"{scenario}: api_level must be an integer in the supported Android API range")

    require_offset_timestamp(entry, scenario)

if status == "accepted":
    if not matrix_targets:
        errors.append(
            "accepted status requires a non-empty minimum_supported_release_set"
        )

    scoped_scenarios: set[str] = set()
    missing_pairs: list[str] = []
    for target_id, target in matrix_targets.items():
        target_scenarios = target.get("required_scenarios")
        if not isinstance(target_scenarios, list):
            continue
        scoped_scenarios.update(target_scenarios)
        for scenario in target_scenarios:
            if (target_id, scenario) not in seen:
                missing_pairs.append(f"{target_id}/{scenario}")

    missing_scope = sorted(REQUIRED_SCENARIOS - scoped_scenarios)
    if missing_scope:
        errors.append(
            "accepted minimum supported release set does not scope every governed scenario: "
            + ", ".join(missing_scope)
        )
    if missing_pairs:
        errors.append(
            "accepted status is invalid while matrix-target/scenario evidence remains missing: "
            + ", ".join(sorted(missing_pairs))
        )
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
    f"(schema=3, status={status}, matrix_targets={len(matrix_targets)}, "
    f"verified_entries={len(seen)}, covered_scenarios={len(seen_scenarios)}/{len(REQUIRED_SCENARIOS)})."
)
