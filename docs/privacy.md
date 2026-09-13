# GoreeCloud Dialer Privacy Architecture

## Principle

Call content is among the most sensitive data handled by GoreeCloud. Privacy Shield authorization travels with each operation rather than being inferred from identity or a global app switch.

## Independently authorized operations

- transcription
- translation
- recording
- scam analysis
- call screening
- voicemail transcription
- summarization
- searchable call knowledge
- export
- backup
- cross-device handoff
- optional cloud processing

Each operation should carry purpose, data categories, locality, retention, authorization state and revocation behavior.

## Processing indicators

Recording, Transcribing, Translating, AI Assistant Active and Cloud Processing Active must be visible while active. The UI must not imply Privacy Shield acceptance without runtime evidence.

## Private Call

Private Call suppresses optional intelligence workflows without terminating the underlying carrier call. Incognito Dialing may minimize local history but must explain that carriers and external telecommunications providers can retain records outside GoreeCloud's control.

## Retention

Call history, recordings, transcripts, voicemail, summaries and notes have independent retention controls. A deletion preference is not proof of deletion; storage completion must be verified before success is shown.
