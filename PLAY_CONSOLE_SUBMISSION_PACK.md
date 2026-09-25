# QuickQR Business — Google Play Console Submission Pack

Last verified against source: v17.0 / versionCode 17
Package: `com.aistudio.qrgenerator.kmpzqr`
Target SDK: 36
Min SDK: 24

## 1. App identity
- App name: QuickQR Business
- Developer / brand: AnakinYoo
- Package: `com.aistudio.qrgenerator.kmpzqr`
- Version name: `17.0`
- Version code: `17`
- Category: **Business** (recommended for current positioning)
- App type: App
- Pricing: Free for current testing build

## 2. Main store listing
Use the copy in `STORE_LISTING_TH_EN.md`.

Required graphics/assets:
- App icon: 512×512 PNG
- Feature graphic: 1024×500
- Phone screenshots: use screenshots from the current v17 build only
- Do not use screenshots from v13/v14/v16 or AI Studio stale preview

## 3. App access
Recommended Play Console answer for current v17:
- Does all or part of the app require login, membership, location restriction, or special credentials? **No**
- Reviewer instructions: **No login required. All core QR functions are available immediately after install. Camera permission is requested only when scanning; foreground location permission is requested only when the user opens the Location QR/map feature.**

## 4. Ads
Current v17:
- Contains ads: **No**
- Google Mobile Ads SDK: **Not present**
- Simulated ad / VIP / Wallet / TopUp flows: **Disabled / not in runtime**

If AdMob is added later, this declaration and Privacy/Data Safety must be updated before release.

## 5. In-app purchases / subscriptions
Current v17:
- Google Play Billing SDK: **Not present**
- Purchases / subscriptions: **No**
- Paid unlock / ad-free entitlement: **No in current runtime**

## 6. Permissions
Declared Android permissions:
- `android.permission.CAMERA` — used to scan QR codes.
- `android.permission.ACCESS_COARSE_LOCATION` — approximate foreground location for Location QR/map.
- `android.permission.ACCESS_FINE_LOCATION` — precise foreground location when the user grants it.

The app does not declare `android.permission.ACCESS_BACKGROUND_LOCATION`.

Other source facts:
- Camera hardware is optional.
- No `INTERNET` permission in current v17.
- Android backup is disabled.

## 7. Data safety
Use `DATA_SAFETY_V17.md`.

Current source-based assessment:
- App-controlled off-device collection: **None identified**
- App-controlled data sharing: **None identified**
- Camera/photo data is processed on-device for QR scanning.
- Location coordinates are processed on-device for the Location QR/map feature and are passed to an external map/share app only after explicit user action.
- QR history and business card profile are stored locally on-device.
- User-triggered Android Share Sheet / email actions can transfer content to a third-party app selected by the user.

Important: verify the final uploaded AAB and every active Play track before submitting Data Safety. If an older active build has different SDKs or networking behavior, update the declaration.

## 8. Privacy policy
Use:
- `PRIVACY_POLICY_TH_EN.md`
- `docs/privacy-policy.html`

Before review you still need a public HTTPS URL that anyone can open without login.

Required owner input:
- Support/contact email that can actually receive mail: **chenkung12@gmail.com**
- Public Privacy Policy URL: **https://dachopol.github.io/-QRCODE-/privacy-policy.html** — verified HTTPS 200 on 26 September 2026

Do not use the GitHub noreply address as the Play support mailbox.

## 9. Target audience and content
Current product positioning is a general/business QR utility, not a children-focused app.

Recommended selection, subject to owner confirmation:
- Target audience: **18 and over**
- Designed for children: **No**
- Families program: **No**

If you intend to market the app to children or teens, re-answer this section and re-review Families policies.

## 10. Content rating preparation
Based on current v17 source, expected questionnaire answers are:
- Violence: No
- Sexual content / nudity: No
- Strong language: No
- Drugs / alcohol / tobacco: No
- Gambling: No
- In-app purchases: No
- User-to-user communication: No
- User-generated social feed: No
- In-app unrestricted web browsing: No
- Location sharing: **TO VERIFY against the exact Console question** — the app can encode the current location in a QR and the user can explicitly open/share that QR through Android.

The final rating is assigned by IARC after the questionnaire; do not claim a rating before Play Console returns it.

## 11. Financial features
PromptPay is a QR payload generator.
- PromptPay flow uses THB only.
- The app does not process funds, hold balances, approve payments, or verify bank settlement.
- Do not describe QuickQR as a bank, wallet, payment processor, or money transfer service.

Crypto QR is roadmap-only and is **not enabled in v17 runtime**.

## 12. App category / tags
Recommended:
- Category: **Business**
- Positioning: QR scanner + QR generator for small business
- Alternative category if owner prefers utility positioning: Tools

## 13. Testing / reviewer checklist
Before production:
1. Install the exact v17 AAB/APK intended for Play.
2. Splash shows QuickQR Business / by AnakinYoo.
3. Header shows v17.0.
4. Generate PromptPay with valid 10-digit Thai mobile number.
5. PromptPay rejects invalid short number with specific error.
6. Generate Wi-Fi QR.
7. Generate Store Link QR.
8. Generate Text QR.
9. Create Business Card / vCard.
10. Scan QR with physical Android camera.
11. Scan QR from gallery.
12. Save QR image.
13. Share QR image.
14. History save/delete works.
15. TH/EN switches the whole supported screen.
16. Region/currency UI remains consistent; PromptPay stays THB.
17. Open Location QR, grant approximate/precise permission, verify real coordinates, open the map, and generate/share the location QR.
18. Small phone / portrait / landscape has no blocking overflow.

## 14. Release notes — Internal / Closed testing
**Thai**
QuickQR Business v17.0 ปรับความสอดคล้องของภาษา TH/EN, แก้การตรวจสอบหมายเลข PromptPay, เพิ่มหน้าเริ่มต้น QuickQR Business by AnakinYoo, ปรับ responsive UI และทำความสะอาดไฟล์เก่า โดยยังคงฟังก์ชันสร้าง/สแกน QR, นามบัตร และประวัติในเครื่อง

**English**
QuickQR Business v17.0 improves TH/EN consistency, PromptPay number validation, responsive UI, and startup branding. It also removes superseded project files while preserving QR generation, scanning, business cards, and local history.

## 15. Submission blockers — must be completed by owner
- [x] Support email recorded in project documents: chenkung12@gmail.com
- [ ] Owner verifies this mailbox receives support requests
- [x] Public HTTPS Privacy Policy URL — verified `https://dachopol.github.io/-QRCODE-/privacy-policy.html`
- [x] Privacy policy access implemented in the Support sheet; Android ACTION_VIEW handler verified. Final post-fix foreground tap remains part of release smoke test.
- [ ] Final target audience confirmation
- [ ] Final category confirmation
- [ ] Store icon 512×512
- [ ] Feature graphic 1024×500
- [ ] Current v17 phone screenshots
- [ ] Production countries/regions
- [ ] Release signing / upload key
- [x] AAB generated from current main by CI; current CI artifact is intentionally unsigned and not Play-upload-ready
- [x] Physical-device scanner test — CameraX live preview + gallery scan-back verified on real Android device
- [ ] Data Safety rechecked against final AAB / active tracks
- [ ] IARC questionnaire submitted


## 16. ขั้นตอนดำเนินการต่อ — ตรวจเอกสาร 24 September 2026

### ตรวจแล้วจากไฟล์ main ในรอบนี้
- `app/build.gradle.kts`: versionName 17.0 / versionCode 17 / package `com.aistudio.qrgenerator.kmpzqr` / targetSdk 36
- `app/src/main/AndroidManifest.xml`: CAMERA, camera optional, allowBackup=false; ไม่พบ INTERNET ใน manifest ต้นทางนี้
- Privacy Policy ทั้ง Markdown และ HTML มี `chenkung12@gmail.com` ตรงกัน การมีอีเมลในเอกสารไม่ได้ยืนยันว่ารับเมลได้จริง
- การตรวจนี้เป็น source/document review ไม่ใช่ผลตรวจ merged manifest, final AAB, CI หรืออุปกรณ์จริง

### งานถัดไปตามลำดับ
1. เผยแพร่ `docs/privacy-policy.html` เป็นหน้า HTTPS สาธารณะ จากนั้นตรวจเปิดแบบไม่ล็อกอินและตรวจเนื้อหาที่แสดงจริง
2. ใส่ URL ที่ตรวจแล้วในหัวข้อ 8 และใน Play Console; ตรวจว่าภายในแอปมีลิงก์หรือข้อความนโยบายให้อ่านได้
3. ใช้ `STORE_LISTING_TH_EN.md` กรอกชื่อและคำอธิบาย แนบ icon, feature graphic และภาพจาก build ที่จะส่งจริง
4. ยืนยันหมวดหมู่ กลุ่มอายุ และประเทศจำหน่ายตามกลุ่มผู้ใช้จริง; Business และ 18+ ในเอกสารนี้ยังเป็นข้อเสนอ ไม่ใช่ค่าที่เจ้าของยืนยันแล้ว
5. ตรวจ App access, Ads, Data Safety, Financial features และ IARC ตามคำถามที่ Console แสดงจริง การสร้าง QR ไม่ใช่หลักฐานให้ตอบว่าไม่มี financial features โดยอัตโนมัติ
6. ตรวจ package, versionCode ที่ Play เคยรับ, upload certificate และ signing ของ AAB จริงก่อนอัปโหลด ห้ามส่ง keystore/password ในแชทหรือ commit ลง repo
7. ทดสอบผ่าน test track พร้อมตรวจกล้องจริง, QR round-trip, Save/Share, History, TH/EN, PromptPay THB และจอเล็ก บันทึกผลตาม build ที่ติดตั้ง
8. ตรวจรายงาน Console และรายการที่ยังไม่เสร็จก่อนส่ง review; ยังไม่ถือว่าเผยแพร่แล้ว

### URL และสถานะที่ยังไม่ยืนยัน
- Public Privacy Policy URL: `https://dachopol.github.io/-QRCODE-/privacy-policy.html` — HTTPS 200 verified on 26 September 2026
- Physical-device QA: camera, location, map, QR round-trip, history, TH/EN, region/currency, landscape, light mode, and 360dp small-phone width verified
- Current-main CI: policy, compile, tests, lint, bundle and artifact verification PASS
- Signed AAB / Play upload / Console declarations: still not verified
- ห้ามทำเครื่องหมายผ่านจากรายงานเก่าหรือการมีไฟล์เอกสารเพียงอย่างเดียว

Official reference: https://support.google.com/googleplay/android-developer/answer/10144311?hl=en
Google Play requires a publicly accessible privacy policy URL and a privacy policy link or text inside the app.
