# QuickQR Business v14 — Final compatibility check

## Identity
- App: QuickQR Business
- applicationId: `com.aistudio.qrgenerator.kmpzqr`
- versionCode: `14`
- versionName: `14.0`
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
- Previous removed support email is absent.
- Developer identity remains `215334638+AnakinYoo@users.noreply.github.com`.
- Legacy unreferenced VIP/Wallet/TopUp/AdMob simulation stubs removed from source.
- Core QR features remain in runtime source.

## Build verification status
Current `main` has received additional v13 refactoring after the earlier successful AI Studio build, including scanner, PromptPay, privacy, localization, and build-file cleanup. Static checks are recorded here, but a fresh Google AI Studio Build/Preview is still required before Play Console submission. Do not treat this document as proof of a successful current compile.
