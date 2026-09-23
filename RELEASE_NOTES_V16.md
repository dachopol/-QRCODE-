# QuickQR Business v16.0

## Goal
Raise real product quality against leading QR scanner/generator apps without fake data, fake reviews, or feature removal.

## User-facing improvements
- Scanner analyzes the center target area instead of the whole camera frame, reducing accidental reads from nearby QR codes.
- Duplicate camera callbacks no longer create repeated history rows while the same result sheet is open.
- History now supports local search across title, type, subtitle, and QR content.
- History filter labels follow TH/EN language selection.
- Opening a history item now reopens the saved item itself instead of using the current generator form values.
- Scan result badges, copy labels, and share chooser are localized.

## Existing strengths retained
- PromptPay / Thai QR Payment generation and parsing.
- Wi-Fi, Store Link, Text, Digital Business Card.
- Camera scan plus QR scan from image picker.
- Result preview before external URL actions.
- Local-first history.
- No fake VIP, wallet, top-up, ad revenue, or random real-data claims.

## Version
- applicationId: `com.aistudio.qrgenerator.kmpzqr`
- versionCode: `16`
- versionName: `16.0`

## Release gate
- GitHub Actions must pass `:app:assembleDebug` and `:app:testDebugUnitTest`.
- Real CameraX behavior must still be verified on a physical Android device before Play testing.
