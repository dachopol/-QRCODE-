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


## Physical Android QA — 2026-09-25
Verified on a real **realme RMX3241** (Android API 33), physical display 1080×2400, density 480, 3-button system navigation, font scale 1.1.

### PASS
- Installed and launched QuickQR Business v17.0 / versionCode 17 using package `com.aistudio.qrgenerator.kmpzqr`.
- Main navigation reaches Generate, Business Card, Scanner and History without crash.
- Generate options verified on-device: PromptPay, Wi-Fi, Store Link, Text/URL and Location.
- TH/EN switching works on the reviewed main flows.
- Region selection is coupled to currency; US→USD was verified while PromptPay stayed THB/฿.
- Business Card preview/actions render; History filters/empty state render.
- Support sheet opens; issue categories now switch fully between TH/EN.
- Camera permission dialog identifies QuickQR Business correctly.
- CameraX opened the physical rear camera as an active camera client; flash/gallery controls rendered.
- Leaving Scanner disconnected camera 0 and closed the camera client.
- Foreground Location permission flow is correct; no background-location permission is requested.
- With Location enabled but no device fix available, the app timed out to “Current location is unavailable” instead of inventing coordinates.
- Real-device 3-button safe-area regression was found and fixed. Final code commit `accba8b239ecc39330c1335a49c95f777f09e22d` uses an adaptive custom bottom bar with the navigation-bar inset on the outer Surface. EN and TH labels are visibly above the system navigation controls.
- Temporary QA permissions were cleared by the final clean install; Camera/Fine/Coarse Location are all denied after testing.

### CI evidence for the safe-area fix
- Android CI run `36151591187`: PASS
- Project policy: PASS
- Compile / unit test / lint / release bundle: PASS
- Built-artifact verification: PASS
- Debug APK and unsigned release AAB artifact upload: PASS

### Still TO VERIFY
- Successful real-coordinate acquisition and map opening on a device that has an actual GPS/network location fix.
- Landscape runtime. ADB could read rotation settings but the device denied shell `WRITE_SETTINGS`, so rotation was not forced.
- Additional large-font accessibility matrix beyond the device's current font scale 1.1.
- Release keystore signing and Play Console upload/acceptance.


## Physical Android runtime evidence — 2026-09-25
Test device: realme RMX3241 (physical Android device). Precise location and device identifiers are intentionally not stored here.

Verified against source commit `5b78529405808cbd221871c68bea36377d8ec4dc`:
- Physical-device app launch: PASS
- TH → EN language switch across main navigation: PASS
- Region/currency pairing UI (for example Thailand/THB, United States/USD, Japan/JPY): PASS
- Camera permission flow: PASS
- CameraX live preview from physical camera: PASS
- Foreground coarse/fine location permission flow: PASS
- Real current location acquisition after network-assisted provider availability: PASS
- Invalid `0,0` placeholder not used: PASS
- Open current location in external map app: PASS
- Generate Location QR from the acquired real point: PASS
- Save generated QR image to `Pictures/QuickQR_Business`: PASS
- Scanner gallery picker reads the saved Location QR back as Map location: PASS
- History stores both Created and Scanned location entries: PASS

Important runtime finding and fix:
- An older installed v17 build could remain on “Reading the device location…” because it did not contain the current timeout implementation.
- The latest source correctly exits unavailable requests rather than spinning forever.
- Physical testing also exposed that choosing GPS exclusively when it was enabled could miss a usable indoor network fix.
- Commit `5b78529405808cbd221871c68bea36377d8ec4dc` now races enabled GPS and Network providers and accepts the first valid real location.
- Android CI run `36154569928`: PASS for policy, compile, tests, lint, bundle, artifact verification, debug APK and unsigned release AAB.

Still to verify before production:
- Small-screen / landscape / enlarged-font layout runtime.
- Light-mode runtime check.
- Release keystore signing.
- Play Console upload / signing / versionCode acceptance.


## Physical layout/theme QA — 2026-09-26
Test device: realme RMX3241 physical Android device.

Verified:
- Landscape runtime (2400×1080 effective display, rotation 90°): PASS
- Header / generator tabs / input area / bottom navigation remain reachable in landscape: PASS
- Light mode runtime: PASS
- Dark/Auto mode restored after test: PASS
- Device font scale during testing: 1.15; primary screens remained readable without critical clipping: PASS for 115% scaling

Small-screen simulation:
- TO VERIFY on a separate smaller physical device or emulator.
- This realme build blocks ADB shell changes to `wm size`, `user_rotation`, and `font_scale` with system permission restrictions, so no fake/simulated PASS is claimed.
- No source change was made solely to force a synthetic small-screen result.


## Small-screen physical-device closure — 2026-09-26
The physical test device itself reports Android configuration `sw360dp` at 480 dpi (1080×2400 physical panel), which is a standard small-phone width class.

Evidence:
- Portrait configuration: `sw360dp w360dp`: PASS
- Device font scale during runtime QA: `1.15`: PASS
- Primary generator flow remained readable/clickable without critical clipping: PASS
- Landscape configuration on the same device: PASS
- Light mode and automatic night mode: PASS

This supersedes the earlier synthetic-small-screen TO VERIFY note. A separate 720×1600 emulator simulation is not required to claim small-phone-width coverage because the physical device already provides 360dp width runtime evidence.

Emulator note:
- Existing AVDs were present, but their referenced Android 37.1 system image was not installed.
- A temporary system-image install attempt was stopped once physical `sw360dp` evidence was confirmed; no fake emulator PASS is claimed and no persistent AVD configuration was changed.


## Small-screen + large-font emulator QA — 2026-09-26
Android Emulator evidence:
- Effective display: 720×1280
- Font scale: 1.30
- Source fix commits: `124e3ce470056b1599abbacd15ed46f8bdc712b7`, `efc89d4d1f2f69b1842f16e489667a73d38a8e92`
- Android CI run `36184920392`: SUCCESS

Verified:
- Main screen renders on 720×1280 without critical overflow: PASS
- Header shows full `QuickQR Business` title at 130% font: PASS
- Version `v17.0` remains visible: PASS
- Compact locale control remains accessible and is shortened to `LANG/CURRENCY` form such as `EN/USD`: PASS
- Bottom navigation labels remain readable at 130% font: PASS
- Horizontal generator tab strip scrolls and exposes Text/URL and Location: PASS
- Location screen remains reachable on small screen: PASS
- Vertical scrolling exposes Allow, Map, and Generate Location QR actions above the fixed bottom navigation: PASS
- No core feature was removed to satisfy the compact layout: PASS

Cleanup:
- Emulator font scale restored to 1.0 after testing.
- Emulator stopped after QA.
