# QuickQR Business v16.0

## Release identity
- applicationId: `com.aistudio.qrgenerator.kmpzqr`
- versionCode: `16`
- versionName: `16.0`
- UI version badge must use `BuildConfig.VERSION_NAME`.

## Project-wide consistency
- Updated Gradle, metadata, README, AI Studio import guide, Play Console testing guide, compatibility checklist, and project rules to one release version.
- Kept the package name unchanged.
- Preserved the permanent Anti-Fake / Anti-Random / no-hardcoded-real-data rule.
- Preserved Card-only UI, adaptive layout, TH/EN consistency, camera lifecycle, scanner, history, Generate QR, PromptPay, Wi-Fi, Store Link, Text, Business Card, Save and Share rules.
- Monetization simulation remains disabled until real AdMob / Play Billing integration exists.

## Cleanup
- Removed superseded release-note files for v13, v14 and v15 from the active project tree.
- Updated references so the current documentation points to v16.
- Runtime source files were not deleted merely for cleanup; project rules require reference checks before deleting classes/routes.

## Verification gate
This update synchronizes source/version/docs and cleanup state. A fresh CI build, AI Studio preview/device test, and Play Console test gate are still required before claiming v16 is release-ready.
