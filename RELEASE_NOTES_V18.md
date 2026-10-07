# QuickQR Business v18.0

## Release identity
- applicationId: `com.aistudio.qrgenerator.kmpzqr`
- versionCode: `19`
- versionName: `18.0`
- targetSdk: `36`

## What changed
- Current Play release identity is v18.0 / versionCode 19; code18 is superseded and must not be uploaded.
- Kept the existing QuickQR Business package ID unchanged for Play Console continuity.
- Preserved the current QR generator/scanner, PromptPay, Wi-Fi, Store Link, Text/URL, Business Card, Location QR/map, local history, TH/EN, and region/currency flows.
- Preserved Anti-Fake / Anti-Random rules: unavailable real data must not be replaced with fabricated values.
- Added optional custom center image/logo for generated QR codes. Users can choose PNG/JPEG/WebP with the Android system picker; the app preserves aspect ratio, stores the selected image privately, and keeps an automatic-logo fallback.

## Verification boundary
- Current Gradle identity: PASS for v18.0 / versionCode 19.
- Active code19 build source commit `c7304af1dc3040c13aff69b7e2ee6574a1192276`: build/unit tests/signing PASS; Play Internal Testing acceptance recorded PASS.
- Active signed AAB SHA-256: `2B38514B4E24D151BC2AA4E15CD4C21C296918C445E3227CC1C4ACDD1F292D02`.
- Physical-device QA records for v17 remain historical evidence and must not be relabeled as v18.
- v18 physical-device debug install/cold-launch smoke on realme RMX3241: PASS.
- Exact code19 real-device regression smoke: BLOCKED pending authorized realme RMX3241 reconnect/unlock.
- Direct Android Publisher exact-package read and tester continuity: TO VERIFY until authorized evidence exists.

## Security
- Do not commit keystores or signing passwords.
- Keep the existing application ID unless the owner explicitly changes the Play app identity.
