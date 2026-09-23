#!/usr/bin/env python3
from pathlib import Path

FILES = [
    "app/src/main/java/com/goreecloud/dialer/telephony/CallAudioControl.kt",
    "app/src/main/java/com/goreecloud/dialer/telephony/CallControl.kt",
    "app/src/main/java/com/goreecloud/dialer/telephony/OutgoingCallPlacement.kt",
    "app/src/main/java/com/goreecloud/dialer/telephony/PhoneAccountRouting.kt",
    "app/src/main/java/com/goreecloud/dialer/telephony/GoreeCloudInCallService.kt",
]

REQUIRED = [
    "TelephonyFailurePresentation.AUDIO_CONTROL",
    "TelephonyFailurePresentation.CALL_CONTROL",
    "TelephonyFailurePresentation.OUTGOING_CALL",
    "TelephonyFailurePresentation.PHONE_ACCOUNT_DISCOVERY",
    "TelephonyFailurePresentation.ENDPOINT_ROUTING",
    "TelephonyFailurePresentation.ENDPOINT_CHANGE",
]

combined = ""
for filename in FILES:
    source = Path(filename).read_text(encoding="utf-8")
    combined += source
    if ".message" in source:
        raise SystemExit(f"{filename}: platform exception message text must not reach user-visible telephony failures")

for marker in REQUIRED:
    if marker not in combined:
        raise SystemExit(f"missing privacy-safe telephony failure marker: {marker}")

helper = Path(
    "app/src/main/java/com/goreecloud/dialer/telephony/TelephonyFailurePresentation.kt"
).read_text(encoding="utf-8")
if ".message" in helper:
    raise SystemExit("failure presentation helper must not accept or expose platform exception messages")

print("telephony failure privacy boundary: PASS")
