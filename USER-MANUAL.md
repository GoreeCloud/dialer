# GoreeCloud Dialer User Manual

## Current status

GoreeCloud Dialer is in Active Development / pre-Stable status. The current source revision is a Development shell and capability-contract foundation; it is not yet a replacement for the system phone application.

## What currently works

The Android application shell can present the GoreeCloud Dialer Development screen and report that telecom integrations are not yet connected. The source includes capability-state contracts used to prevent UI from claiming unsupported calling functionality.

## What does not yet work

Carrier dialing, receiving calls, default-dialer role integration, call screening, voicemail, recording, transcription, translation, spam/scam protection, AI Call Assistant, Privacy Shield runtime integration, Wardveil runtime integration, Everkeep backup and cross-device calling remain unimplemented or unverified.

## Privacy

The current Development shell does not intentionally capture or persist call audio, recordings, transcripts or voicemail. Future sensitive features must use explicit authorization and visible processing indicators.

## Safety

Do not rely on the current Development build for emergency calling or as the device's primary phone application until the project reaches the relevant validated milestone.
