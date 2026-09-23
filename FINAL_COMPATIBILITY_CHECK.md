# QuickQR Business v15 — Final compatibility check

## Identity
- App: QuickQR Business
- applicationId: `com.aistudio.qrgenerator.kmpzqr`
- versionCode: `16`
- versionName: `16.0`
- JDK source/target: `17`

## AI Studio / Kotlin rules checked
- Single Android `:app` module.
- Kotlin + Jetpack Compose.
- No `import androidx.compose.foundation.layout.weight`.
- No Java 25/Foojay daemon pinning.
- No `.idea/` metadata in the ZIP.
- No incomplete Gradle wrapper in the ZIP.
- Room + KSP retained because the app database uses them.

## Project rules checked
- Thai typography rule retained (`letterSpacing = 0.sp` where defined by project typography).
- Admin contact address is hidden behind the contact button; the Security status page is removed while Anti-Root checks remain active.
- Developer identity remains `215334638+AnakinYoo@users.noreply.github.com`.
- Legacy unreferenced VIP/Wallet/TopUp/AdMob simulation stubs removed from source.
- Core QR features remain in runtime source.

## Build verification status
- The v15 source passed the GitHub Actions Android gate after the Support/Security UI changes: JDK 17, Android SDK 36, Gradle 9.3.1, `:app:assembleDebug`, and `:app:testDebugUnitTest`.
- The debug APK artifact is uploaded only after build/test success.
- Real CameraX preview and QR scanning still require verification on a physical Android device before Play Console submission.
