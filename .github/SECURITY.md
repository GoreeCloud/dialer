# GoreeCloud Dialer Security Architecture

## Wardveil boundary

Wardveil is the applicable GoreeCloud security authority for sensitive-storage protection, trusted-device evidence, protected key/material handling, integrity/provenance of security-sensitive configuration and minimized security diagnostics.

Configured protection and verified runtime protection are separate states.

## Sensitive storage

Recordings, transcripts, voicemail content, summaries and notes must be treated as high-sensitivity data. The Development shell currently stores none of these artifacts and therefore must not present encrypted-storage claims.

## Diagnostics

Operational diagnostics must not disclose phone numbers, call content, transcript text, voicemail content, credentials, cryptographic secrets or sensitive storage paths.

## Cross-device trust

Future cross-device calling and handoff require explicit device trust and strong authentication. Device discovery alone is not authorization to receive call content.

## Failure behavior

When security acceptance is missing or fails, sensitive optional processing must fail closed while ordinary carrier calling remains as independent as the platform permits.
