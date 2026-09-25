# QuickQR Business v17 — Compatibility Check

## Identity
- App: QuickQR Business
- applicationId: `com.aistudio.qrgenerator.kmpzqr`
- namespace: `com.aistudio.qrgenerator.kmpzqr`
- versionCode: `17`
- versionName: `17.0`
- compileSdk / targetSdk: `36`
- JDK source/target: `17`

## Build system
- Single Android `:app` module.
- Kotlin + Jetpack Compose.
- Room + KSP retained because local QR/history/profile storage uses Room.
- No forbidden direct `androidx.compose.foundation.layout.weight` import.
- Release signing is read from environment variables; secrets are not hardcoded.

## Current functionality checks
- Generate QR: present.
- PromptPay: present and THB-only.
- Wi-Fi / Store Link / Text: present.
- Digital Business Card: present.
- Scanner + gallery decode: present.
- History: present.
- Save/share: present.
- TH/EN framework: present.
- Region-linked currency display: present.
- Real foreground Location QR/map flow: present.
- Background location: not declared.
- Simulated VIP/Wallet/TopUp/Ad revenue: not active.

## Source cleanup
- Current Git tree has no active source under `com/example`.
- Source package, Gradle namespace and applicationId are aligned to `com.aistudio.qrgenerator.kmpzqr`.
- UI version display uses `BuildConfig.VERSION_NAME`.

## CI evidence
Code commit `93745ffe6b44c2497791846e19b6f3683a3a9345`:
- Android CI run `36090386196`: PASS
- Compile / assembleDebug: PASS
- Unit tests: PASS
- Debug APK artifact: PASS

Location hardening commit `ae5b97f02555db51fefc90e2f45b8d87d8c12e4a`:
- Android CI run `36090047663`: PASS

## Still required before production
- Physical Android camera verification.
- Physical-device current-location/map verification.
- TH/EN and responsive-layout checks on small screen / landscape / font scaling.
- End-to-end QR generation, scan-back, history, save and share on device.
- Release signing and Play Console upload verification.

Do not describe the app as production-ready until the remaining device and Play Console checks have evidence.


## Lint / bundle gate
Verified on code commit `d44490917c66ae6e39b8e0cc167569112597fa3e`, Android CI run `36094198127`:
- Source policy check: PASS
- Debug build: PASS
- Unit tests: PASS
- Android lint: PASS
- Release bundle compile: PASS
- Debug APK artifact: PASS
- Unsigned release AAB artifact: PASS

### Signing boundary
The CI release bundle is unsigned because no release keystore/signing secrets are injected into the workflow. This proves release-bundle compilation, not Play Console upload readiness. Signing and Play upload remain **TO VERIFY**.
