# QuickQR Business v12 — AI Studio / Play testing changes

## Version identity
- `versionCode = 12`
- `versionName = 12.0`
- Kept application ID `com.aistudio.qrgenerator.kmpzqr`.
- Removed the previous support email from source code and support UI.

## AI Studio compatibility cleanup
- Kept a single Android `:app` module.
- Kept Kotlin + Jetpack Compose architecture.
- Removed generated Java 25/Foojay daemon toolchain pinning.
- Removed optional Foojay settings plugin.
- Removed Roborazzi screenshot-test plugin, dependencies and screenshot test assets.
- Kept Room + KSP because the app history/database code uses them.
- Removed unused dependency/plugin declarations.
- Changed `compileSdk` declaration to the standard `compileSdk = 36` form.
- Reduced Gradle properties to conservative settings for hosted IDE/build environments.
- Added `AI_STUDIO_IMPORT.md`.

## Runtime/testing rules preserved
- Visible branding is QuickQR Business.
- Simulated payment approval is disabled.
- Simulated ad/revenue paths are disabled for testing.
- Core QR generator/scanner remains available for testing.
- No Gemini/Firebase/Retrofit/OkHttp requirement in this build.

## UI / ภาษาไทย
- Main typography uses `letterSpacing = 0.sp`.
- Thai text has suitable line height for vowels/marks.
- Bottom navigation and buttons can wrap where needed instead of stretching/overflowing.
- Primary QR button uses minimum height instead of a fixed height.
- Top bar actions are compact so the app title is not squeezed on phones.

## Not changed
- Play Console package/application identity.
- Core QR data model and Room history.
- PromptPay, Wi‑Fi, store-link, text and business-card QR features.
- Release signing remains environment-variable based.
- Added the project developer identity email rule.
- Removed remaining fake VIP/quota/wallet behavior from the Play-testing path.
- Core QR features remain available while monetization is not connected.

## New-rules cleanup
- Removed legacy runtime paths for VIP, Wallet, TopUp, simulated ad stats, and simulated interstitials.
- Cleared hardcoded user/business defaults (PromptPay, Wi-Fi, store ID, business-card profile).
- Kept examples only where they are clearly labeled as examples/placeholders.

## Final compatibility cleanup
- Removed `gradle/gradle-daemon-jvm.properties` that pinned Java 25/Foojay.
- Removed the incomplete wrapper directory that contained only `gradle-wrapper.properties`.
- Removed `.idea/` IDE-local metadata.
- Removed unreferenced legacy AdMob/Wallet/TopUp source stubs.
- Removed screenshot/Robolectric/template test sources whose dependencies are not part of this lean runtime build.
- Confirmed the invalid `import androidx.compose.foundation.layout.weight` is absent.
- Kept `Modifier.weight(...)` runtime usages without importing the internal layout extension.


## Device UI follow-up
- Fixed Generate QR category menu to keep all 4 tabs visible.
- Made TH/EN language tabs reactive and localized key QR UI sections.
- Removed fixed-height clipping from security/preview actions.
- Added navigation-bar inset to bottom navigation.
- Increased digital-card banner minimum height to prevent cropped text.
- Added PROJECT_RULES.md and locked UI/language completeness rules.

## UI/language follow-up from real-device screenshots
- Keep all four Generate QR tabs visible.
- Thai/English main UI switching completed for the affected screens and generated/scan result labels.
- Removed fixed-height clipping from long security/preview/result actions.
- Added Android navigation-bar inset to bottom navigation.
- Expanded business-card header adaptively to avoid cropped text.
- Added `PROJECT_RULES.md` and `DEVICE_UI_FIX_REPORT.md`.

## v12 — กฎการตัด / Preserve Content
- ห้ามตัดฟังก์ชัน เมนู ปุ่ม หรือ option เดิมเพื่อแก้ layout/build โดยไม่ได้รับคำสั่งชัดเจน
- เมนู/action/status/warning ต้องแสดงข้อความครบ; ใช้ wrap/min-height/scroll/adaptive layout แทน Ellipsis
- ข้อมูลยาวใน History ย่อได้เฉพาะเมื่อยังเปิดดูหรือคัดลอกข้อมูลเต็มได้
- Bottom navigation เพิ่ม minimum height และไม่ใช้ Ellipsis กับชื่อเมนู
- คง TH/EN และ 4 หมวด Generate QR + 4 เมนูล่างครบ
