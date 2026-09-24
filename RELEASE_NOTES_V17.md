# QuickQR Business v17.0

## Release identity
- applicationId: `com.aistudio.qrgenerator.kmpzqr`
- versionCode: `17`
- versionName: `17.0`
- UI version badge must use `BuildConfig.VERSION_NAME`.

## Source of Truth
- Google AI Studio workspace version 17 was confirmed by the project owner.
- GitHub `main` is synchronized to the same release identity in this commit.
- `app/build.gradle.kts` is the version source; metadata/docs must follow it.

## Project-wide consistency
- Updated Gradle, metadata, README, AI Studio import guide, Play Console testing guide, and compatibility checklist to v17.
- Kept package `com.aistudio.qrgenerator.kmpzqr` unchanged.
- Preserved Anti-Fake / Anti-Random / no-hardcoded-real-data rules.
- Preserved Card-only UI, adaptive layout, TH/EN consistency, camera lifecycle, scanner, history, Generate QR, PromptPay, Wi-Fi, Store Link, Text, Business Card, Save and Share.
- Monetization simulation remains disabled until real AdMob / Play Billing integration exists.

## Cleanup
- Removed superseded release-note files from the active project tree.
- Updated all active documentation references to v17.
- No runtime source/class/route was deleted unless it was confirmed superseded/unreferenced.

## Startup splash
- Added a lightweight startup screen using the QuickQR app icon.
- Shows **QuickQR Business** and **by AnakinYoo** before entering the main app.
- Uses existing project resources and no new dependency; core QR navigation is unchanged.

## Verification gate
- This commit must pass fresh GitHub Actions build + unit-test gates.
- AI Studio snapshot/service errors are treated as external-service evidence unless compile/runtime logs prove a source failure.
- Physical-device camera/scanner verification and Play Console testing are still required before release readiness.
