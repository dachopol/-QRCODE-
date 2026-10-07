# QuickQR Business — ข้อมูลสำหรับกรอก Google Play Console (Testing)

## ข้อมูลแอป
- ชื่อแอป: QuickQR Business
- Application ID / Package: `com.aistudio.qrgenerator.kmpzqr`
- Version name: `18.0`
- Version code: `19`
- ประเภท: แอปสแกนและสร้าง QR Code สำหรับธุรกิจ

## คำอธิบายสั้น
สแกนและสร้าง QR Code สำหรับธุรกิจ รองรับ PromptPay, Wi‑Fi, ลิงก์ร้านค้า, ข้อความ และนามบัตรดิจิทัล

## คำอธิบายสำหรับหน้าร้าน (ร่าง)
QuickQR Business ช่วยสร้างและสแกน QR Code ที่ใช้บ่อยในงานธุรกิจและร้านค้า เช่น PromptPay, Wi‑Fi, ลิงก์ร้านค้า, ข้อความ และนามบัตรดิจิทัล พร้อมบันทึกประวัติการใช้งานไว้ภายในอุปกรณ์เพื่อเรียกดูภายหลัง

## App access
- ต้องล็อกอินหรือไม่: ไม่ต้อง
- ฟังก์ชันหลักเข้าถึงได้โดยไม่ต้องมีบัญชี

## Ads
- Contains ads: **No** สำหรับ build ทดสอบนี้
- เหตุผล: ไม่มี Google Mobile Ads SDK ที่ทำงานจริง และเส้นทางโฆษณาจำลองถูกปิดใน build นี้

## In-app purchases / subscriptions
- ไม่มีการซื้อจริงใน build ทดสอบนี้
- ไม่มี Google Play Billing flow ที่เปิดใช้งาน

## Permissions
- Camera: ใช้เพื่อสแกน QR Code

## Data safety — จุดที่ยืนยันได้จากโค้ดชุดนี้
- ประวัติ QR ถูกจัดเก็บภายในเครื่องด้วยฐานข้อมูลภายในแอป
- Android backup ถูกปิดเพื่อไม่ส่งประวัติ QR/โปรไฟล์ธุรกิจไปยัง cloud backup
- ไม่มีระบบล็อกอิน
- ไม่มี AdMob SDK จริงใน build ทดสอบนี้
- ไม่มี Play Billing SDK/การชำระเงินจริงใน build ทดสอบนี้
- ไม่มี `INTERNET` permission ใน build นี้; การเปิดลิงก์ใช้แอปภายนอกของระบบ

## สิ่งที่ต้องกรอกเองก่อนส่ง
- Project developer identity email: 215334638+AnakinYoo@users.noreply.github.com
- Play Console support email: [private configuration — do not publish a personal address in this repository]
- Privacy policy URL: https://dachopol.github.io/-QRCODE-/privacy-policy.html (verified HTTPS 200)
- App category: แนะนำเลือก Tools หรือ Business ตามตำแหน่งตลาดที่ต้องการ
- Store icon 512×512: [ต้องอัปโหลด]
- Feature graphic 1024×500: [ต้องอัปโหลด]
- Phone screenshots: [ต้องอัปโหลดภาพจาก build นี้]
- ประเทศ/พื้นที่สำหรับการทดสอบ: [เลือกตามที่ต้องการ]

## Release notes สำหรับ Internal/Closed testing
QuickQR Business v18.0 testing build: ปรับชื่อแอปและข้อมูลเวอร์ชันให้ตรงกัน ปิดระบบโฆษณา/กระเป๋าเงิน/การชำระเงินจำลองสำหรับการทดสอบ และเปิดฟังก์ชัน QR หลักให้ผู้ทดสอบใช้งานได้โดยไม่ต้องชำระเงิน

## เช็กลิสต์ก่อนอัปโหลด AAB
1. สร้าง AAB ด้วย release signing ที่ถูกต้อง
2. ตรวจว่า package ยังเป็น `com.aistudio.qrgenerator.kmpzqr`
3. ตรวจว่า versionCode เป็น 19 สำหรับ active candidate และห้ามใช้ code18 ที่ถูก supersede
4. กล้องจริงผ่าน physical-device QA แล้ว; ทดสอบซ้ำบน release-signed build ก่อนอัปโหลด
5. ทดสอบ Generator: PromptPay / Wi‑Fi / Store Link / Text / Business Card
6. ทดสอบบันทึกและแชร์ภาพ QR
7. ทดสอบ History
8. ยืนยัน Privacy Policy และ Data safety ให้ตรงกับ AAB ที่อัปโหลดจริง

## Submission pack
Use these current v18 documents together:
- `PLAY_CONSOLE_SUBMISSION_PACK.md`
- `STORE_LISTING_TH_EN.md`
- `DATA_SAFETY_V18.md`
- `PRIVACY_POLICY_TH_EN.md`
- `docs/privacy-policy.html`

Owner-only fields still required before Play review:
- Real monitored support email: [configure privately in Play Console; use a dedicated public support address rather than a personal mailbox]
- Final target-audience/category confirmation
- Store graphics/screenshots from current v18

## Historical v18/code18 verification boundary — 2026-09-27
- Current GitHub source is v18.0 / versionCode 18.
- Android CI for current v18 source is PASS.
- Current CI release AAB is unsigned and is not Play-upload-ready.
- Existing full-flow physical-device QA evidence was produced on v17 builds and remains historical/regression evidence.
- v18 debug APK clean-install + cold-launch smoke on realme RMX3241: PASS.
- Install/runtime smoke test of the exact v18 release-signed build: TO VERIFY.
- Play Console signing/upload/versionCode acceptance for v18: TO VERIFY.

- The v18 debug smoke does not replace the required smoke test of the exact release-signed AAB/APK candidate.


## Current code19 testing boundary — 2026-10-07
- Gradle identity: v18.0 / versionCode 19 / targetSdk 36.
- Active Internal Testing code19: PASS.
- Signed code19 AAB SHA-256: `2B38514B4E24D151BC2AA4E15CD4C21C296918C445E3227CC1C4ACDD1F292D02`.
- Exact code19 real-device gate: BLOCKED until authorized realme RMX3241 reconnect/unlock.
- Android Publisher read-only probe: source/static safety PASS; runtime auth BLOCKED because no approved credential/connector is connected.
- Closed Testing evidence model must keep these fields separate:
  - invited/configured
  - eligible unique testers
  - opted-in testers
  - continuous opt-in duration
  - meaningful feedback/feature coverage
  - device coverage
- Email-list or Google Group configuration is not proof of opt-in or continuity.
- For an applicable new personal developer account, current official requirement is at least 12 testers opted in continuously for at least 14 days before applying for Production access.
- Production remains untouched.
