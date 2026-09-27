# QuickQR Business v18.0

## Release identity
- applicationId: `com.aistudio.qrgenerator.kmpzqr`
- versionCode: `18`
- versionName: `18.0`
- targetSdk: `36`

## What changed
- Aligned Android source and release metadata to v18.0 / versionCode 18.
- Kept the existing QuickQR Business package ID unchanged for Play Console continuity.
- Preserved the current QR generator/scanner, PromptPay, Wi-Fi, Store Link, Text/URL, Business Card, Location QR/map, local history, TH/EN, and region/currency flows.
- Preserved Anti-Fake / Anti-Random rules: unavailable real data must not be replaced with fabricated values.

## Verification boundary
- GitHub `main` source identity: PASS for v18.0 / versionCode 18.
- Android CI on commit `811a75c0d35edd3a643c58d22bbc325a60c438a4`, run `36196245061`: PASS.
- Debug APK artifact from that run: PASS.
- Release AAB artifact from that run: generated as an unsigned CI artifact only.
- Physical-device QA records for v17 remain historical evidence and must not be relabeled as v18.
- v18 physical-device install/runtime smoke test: TO VERIFY.
- v18 owner-signed AAB and Play Console upload/version acceptance: TO VERIFY.

## Security
- Do not commit keystores or signing passwords.
- Keep the existing application ID unless the owner explicitly changes the Play app identity.
