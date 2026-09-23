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
Source/version/docs and cleanup state are synchronized. GitHub Actions on main passed for the current v16 source after cleanup. AI Studio snapshot/service errors remain external-service evidence unless a compile/runtime error proves otherwise. Physical-device camera/scanner verification and Play Console testing are still required before claiming release readiness.
