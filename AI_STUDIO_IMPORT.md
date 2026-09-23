# QuickQR Business v17 — Google AI Studio import

## Recommended (official) route
Google AI Studio Build mode officially supports **Import from GitHub**. Import the repository as an **Android** project (Kotlin + Jetpack Compose), not as a Web app.

## ZIP upload route
ZIP upload behavior can change between AI Studio releases. If using ZIP upload:
1. Create/open an **Android** app workspace first.
2. Open Code / Add files.
3. Remove placeholder source files generated for the blank app if they duplicate this project.
4. Upload this ZIP so `settings.gradle.kts`, `build.gradle.kts`, `gradle/`, and `app/` are at the project root.
5. Save, then ask the agent to sync/build without changing the package ID.

## Project identity
- App: QuickQR Business
- applicationId: `com.aistudio.qrgenerator.kmpzqr`
- versionCode: `17`
- versionName: `17.0`

## Compatibility cleanup in this package
- Single Android `:app` module
- Kotlin + Jetpack Compose
- Removed Java 25/Foojay daemon pinning
- Removed optional Foojay toolchain plugin
- Removed Roborazzi/screenshot/Robolectric template tests from this lean import package
- Kept Room + KSP because app history/database code uses them
- Removed unused dependency declarations
- Removed unreferenced legacy AdMob/Wallet/TopUp stubs
- No Gemini/Firebase/Retrofit/OkHttp requirement
- Thai UI rules are preserved

## Important
Do not change `applicationId` if this app already exists in Play Console.

## Build tool baseline
- Android Gradle Plugin: `9.1.1`
- Gradle: `9.3.1` minimum/default for AGP 9.1.1
- Kotlin Gradle Plugin: `2.2.10`
- JDK runtime/source target: `17`

## Gradle wrapper note
This package is optimized for **Google AI Studio source import**. The previous archive contained only `gradle-wrapper.properties` without `gradlew`, `gradlew.bat`, or `gradle-wrapper.jar`; that incomplete wrapper set has been removed so AI Studio will use its hosted build environment instead of a partial local wrapper.

If you later need command-line/Android Studio wrapper builds, generate a complete wrapper from a trusted Gradle installation and keep all wrapper files together.

This ZIP does not embed generated build outputs, private signing files, or an incomplete Gradle wrapper.


## Camera preview
- `metadata.json` must include `requestFramePermissions: ["camera"]` for AI Studio frame camera access.
- Android runtime still uses `android.permission.CAMERA` from `AndroidManifest.xml`.
- AI Studio / Android Emulator camera imagery can be synthetic and is not evidence of physical-device camera quality.
- The Scanner screen now avoids binding CameraX on recognized emulator environments, shows a clear preview notice instead of synthetic imagery, and keeps gallery QR import available.
- Real CameraX preview + QR scanning must be verified on an Android device before release.

## v17 support/security UI
- The Support sheet has only Report bug and Contact admin tabs; the Security status page is removed.
- Anti-Root verification still runs from app lifecycle code in the background and may show the root warning when risk is detected.
- The admin email address is not rendered as text; the contact button opens the configured mail target.
