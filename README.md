# QuickQR Business — Play testing build

Android app for scanning and generating QR codes for business use.

## Package / version
- Application ID: `com.aistudio.qrgenerator.kmpzqr`
- Version name: `12.0`
- Version code: `12`
- Min SDK: 24
- Target SDK: 36

## Testing configuration
- Monetization UI is disabled for Play testing.
- No real AdMob SDK is enabled in this build.
- No real Google Play Billing flow is enabled in this build.
- Simulated slip approval is disabled.
- Core QR features can be tested without payment or ads.
- Camera permission is used for QR scanning.
- QR history is stored locally on the device.

## Build / import
This archive is prepared for **Google AI Studio Android source import**. Import the project with `settings.gradle.kts`, `build.gradle.kts`, `gradle/`, and `app/` at the project root.

The archive intentionally does **not** contain a partial Gradle wrapper. For local command-line/Android Studio wrapper builds, generate a complete trusted Gradle wrapper first.

> Important: Keep the existing application ID if this app already exists in Google Play Console. Changing it creates a different app.

## UI language rules
- Thai/complex-script typography uses zero positive letter spacing.
- Thai labels may wrap to 2 lines where a single line would overflow.
- Main action buttons use minimum height rather than a rigid height when labels can wrap.
- The top bar uses compact actions so the app title is not squeezed on small phones.
- See `UI_RULES_TH.md` for the project rule set.

## Signing
- Debug builds use Android's standard debug signing configuration.
- Release signing is enabled only when `KEYSTORE_PATH`, `STORE_PASSWORD`, and `KEY_PASSWORD` are present and the keystore file exists.
- Optional: set `KEY_ALIAS`; otherwise the alias defaults to `upload`.


## AI Studio compatibility package
See `AI_STUDIO_IMPORT.md`. This v12 package keeps the Play Console applicationId unchanged and removes optional build/test plugins that are not needed for app runtime.

## Developer email rule
- Developer identity: `215334638+AnakinYoo@users.noreply.github.com`
- This GitHub noreply address is display-only and is not used as a support mailbox or `mailto:` target.

## Device UI / language follow-up
The latest source includes the real-device fixes documented in `DEVICE_UI_FIX_REPORT.md` and the permanent rules in `PROJECT_RULES.md`.

## v12 preserve-content rule
- Do not remove existing menus, options, or core QR features just to make a screen fit.
- Action/menu/status/warning labels must remain readable in full; use wrapping, minimum height, scrolling, or adaptive layout instead of truncating them.
- Long user-generated history values may be shortened only when the full value remains accessible for viewing/copying.
- See `PROJECT_RULES.md` and `RELEASE_NOTES_V12.md`.
