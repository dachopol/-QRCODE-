# QuickQR Business — Product Brief

## ROLE
Senior Product Designer + Engineer

## PROJECT
QuickQR Business คือแอป Android สำหรับธุรกิจและผู้ใช้ทั่วไป ที่สร้าง/สแกน QR, PromptPay, Wi‑Fi, Store Link, Text, Digital Business Card และเก็บ History ในเครื่อง

## MARKET BENCHMARKS
คำว่า Benchmark ด้านล่างหมายถึงคู่เทียบเพื่อกำหนดมาตรฐานสินค้า ไม่ใช่อันดับทางการของ Google Play

1. QR & Barcode Scanner — Gamma Play
   - จุดแข็งที่ต้องเทียบ: ความเร็วในการสแกน, flow สั้น, auto-detect, ความแพร่หลายสูง
   - Play Store footprint ที่ตรวจสอบล่าสุด: 500M+ downloads, 4.8★, 4M+ reviews

2. QR & Barcode Scanner — TeaCapps
   - จุดแข็งที่ต้องเทียบ: scan actions, Wi‑Fi/vCard, scan from image, history, security-oriented link handling
   - Play Store footprint ที่ตรวจสอบล่าสุด: 100M+ downloads, 4.6★, 3M+ reviews

3. QR TIGER QR Code Generator
   - จุดแข็งที่ต้องเทียบ: QR generator, customization, logo/branding, business use cases
   - Play Store footprint ที่ตรวจสอบล่าสุด: 1M+ downloads, 4.7★

## GOAL
สร้าง QuickQR Business ให้เหนือ Benchmark ทั้ง 3 ในประสบการณ์ใช้งานสำหรับผู้ใช้ธุรกิจไทย โดยเน้น:
- สร้าง QR ได้เร็วและเข้าใจง่าย
- PromptPay ถูกต้องและใช้ THB เท่านั้น
- สแกนเร็วและผลลัพธ์มี action ที่เกี่ยวข้อง
- Business Card ใช้ง่ายและพร้อมแชร์
- UI ภาษาเดียวทั้งหน้า ไม่ปนภาษา
- Region / Currency สอดคล้องกัน
- ไม่มี technical metadata รกหน้าหลัก
- Responsive / Adaptive ทุกขนาดจอ
- Privacy-first และ local-first เท่าที่ฟังก์ชันรองรับ

## CATEGORY
Primary: Tools / Productivity
Positioning: Small-business QR utility

## MUST HAVE
- Generate QR: PromptPay / Wi‑Fi / Store Link / Text
- Digital Business Card / vCard
- Scanner: camera + gallery
- History
- QR color / logo customization
- TH/EN complete screen-level localization
- Locale / Region / Currency consistency
- PromptPay = THB only
- Build version from BuildConfig.VERSION_NAME
- UI CARD-ONLY RULE
- UNIVERSAL AUTO LAYOUT RULE
- Safe-area / font scaling / accessibility
- No horizontal overflow
- Core features reachable on small phones

## PROHIBITED
- ห้าม fake/random/hardcode ข้อมูลที่อ้างว่าเป็นข้อมูลจริง
- ห้าม hardcode version label
- ห้ามภาษาไทย/อังกฤษ/เกาหลี/จีนปนกันในหน้าจอเดียว
- ห้ามให้ Region และ Currency ขัดกัน
- ห้ามแปลงยอด PromptPay ไปเป็นสกุลอื่นใน flow หลัก
- ห้าม hardcode exchange rate แล้วแสดงเหมือนเป็นอัตราปัจจุบัน
- ห้ามตัด feature เพื่อให้ layout พอดี
- ห้าม clip/crop/overlap ข้อความหรือปุ่ม
- ห้าม technical metadata ใน UI หลัก
- ห้าม VIP/Wallet/Ad revenue จำลองถูกนำเสนอเป็นข้อมูลจริง
- ห้ามลอก UI/asset/branding ของคู่แข่ง

## VISUAL SYSTEM
- Glassmorphism: ใช้เป็น accent ประมาณ 5% เท่านั้น
- Blur target: 40px / logical equivalent เมื่อ framework รองรับและไม่กระทบ performance
- Radius: 28px สำหรับ surface หลักที่เหมาะสม
- Primary actions: Pill button
- Background: light, airy, high-contrast; หลีกเลี่ยง solid block หนักเกินจำเป็น
- Typography: ไทยอ่านครบ, letterSpacing 0.sp สำหรับข้อความไทย
- Card hierarchy: Header → Status Card (ถ้ามี) → Vertical Card List
- PASS/READY card: ไม่มี sub-text
- FAIL/ERROR card: แสดงเฉพาะสาเหตุ/จุดแก้/error ที่จำเป็น

## PRODUCT DIFFERENTIATION
- Thailand-first PromptPay flow ที่ไม่ปนสกุลเงิน
- Business Card + PromptPay + Wi‑Fi + Store Link ในแอปเดียว
- Main UI สะอาดด้วย Card-only rule
- Screen-wide localization fallback เพื่อไม่ให้ภาษาปน
- Local-first history
- QR customization แบบไม่เปิด technical implementation details ใน UI หลัก

## DELIVERY
- Android/Kotlin/Jetpack Compose source
- GitHub-ready project
- AI Studio import compatible
- Play Console testing metadata
- Versioned release notes
- PROJECT_RULES.md + UI_CARD_ONLY_RULE.md + UNIVERSAL_AUTO_LAYOUT_RULE.md
- Build must be verified before release submission

## SOURCES CHECKED
- Google Play: Gamma Play — QR & Barcode Scanner
- Google Play: TeaCapps — QR & Barcode Scanner
- Google Play: QR TIGER — QR Code Generator

Last benchmark research: 2026-09-23


## FUTURE ROADMAP — CRYPTO QR
- วางแผนเพิ่ม Crypto QR ในอนาคต โดยแยกจาก PromptPay และ Region/Currency flow เดิม
- สถานะปัจจุบัน: **Planned / Not enabled in runtime**
- รายละเอียดและ Gate อยู่ใน `ROADMAP_CRYPTO_QR.md`
