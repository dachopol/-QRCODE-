# PROJECT_RULES.md — QuickQR Business — Current

## หลักบังคับ
- ห้าม Fake/Random/Hardcode ข้อมูลที่อ้างว่าเป็นข้อมูลจริง; ไม่มีข้อมูลให้แสดงว่าง/ไม่ทราบ/ผิดพลาด
- package ต้องคง `com.aistudio.qrgenerator.kmpzqr` เว้นแต่ผู้ใช้สั่งเปลี่ยนโดยชัดเจน
- ห้ามฝังอีเมลส่วนตัวเดิมใน source code; developer identity ใช้ `215334638+AnakinYoo@users.noreply.github.com` ตามกฎล่าสุด
- ปุ่มติดต่อแอดมินใช้ developer identity เป็น hidden mail target ตามคำสั่งเจ้าของโปรเจกต์; ห้ามแสดงที่อยู่อีเมลดิบบนหน้าจอ และห้ามนำอีเมลนี้ไปอ้างเป็น Play Console support mailbox ที่รับข้อความจริง
- ห้ามเปิด VIP/Wallet/TopUp/รายได้/โฆษณาจำลองเป็นข้อมูลจริง

## BUILD / KOTLIN
- ห้าม import `androidx.compose.foundation.layout.weight` โดยตรง
- ใช้ `Modifier.weight(...)` เฉพาะ RowScope/ColumnScope ที่ถูกต้อง
- ถ้า `compileDebugKotlin FAILED` ต้องอ่านชื่อไฟล์/บรรทัด/error จริงก่อนแก้ ห้ามเปลี่ยน Gradle/dependency แบบเดา
- หลังแก้ compile error ต้องยืนยัน build ผ่านก่อนถือว่างานเสร็จ

## PREVIEW / EMULATOR
- ถ้า build ผ่านแต่ Preview ค้าง `Connecting to device...` ห้ามแก้ source แบบสุ่ม
- แยก Build / Runtime / Emulator-Renderer ก่อนเสมอ
- EGL/emulation warnings ไม่ใช่หลักฐานว่า source เสีย หากไม่มี FATAL EXCEPTION/compile failure
- ทดสอบ Install บนอุปกรณ์จริงก่อนกด Fix ซ้ำเมื่อ build ผ่าน

## UI / LANGUAGE COMPLETENESS
- ภาษาไทยต้อง `letterSpacing = 0.sp` และมี lineHeight พอสำหรับสระ/วรรณยุกต์
- ข้อความทุกจุดต้องอ่านครบ: ห้าม clip/crop/overlap/ถูกแถบระบบบัง
- ห้ามแก้ด้วยการย่อ font ทั้งแอปแบบเหมารวม
- ปุ่ม/การ์ดที่มีข้อความยาวให้ใช้ minimum height แทน fixed height เมื่อจำเป็น
- Language switch ต้องทำให้ข้อความ UI ที่รองรับเปลี่ยนทันที; ข้อความที่ยังไม่มีคำแปลให้ fallback เป็นอังกฤษ ไม่ค้างภาษาไทยเมื่อเลือก EN
- ห้าม hardcode ข้อความที่ทำให้ TH/EN เปลี่ยนไม่ได้ในเส้นทางหลัก
- Top tabs และ bottom navigation ต้องเห็นฟังก์ชันครบทุกตัวบนจอมือถือ; ถ้าพื้นที่ไม่พอให้ wrap/scroll/adaptive layout อย่างชัดเจน
- ต้องเคารพ status/navigation bar ด้วย systemBars/navigationBars padding หรือ Scaffold insets
- ก่อนส่ง ZIP ต้องตรวจ TH + EN และจอแนวตั้งขนาดเล็กอย่างน้อยหนึ่งขนาด

## กฎการตัด / PRESERVE-CONTENT RULE
- ห้ามตัดฟังก์ชัน เมนู ปุ่ม ตัวเลือก เส้นทางใช้งาน หรือข้อมูลสำคัญออกเพื่อแก้ UI/Build โดยไม่ได้รับคำสั่งชัดเจนจากเจ้าของโปรเจกต์
- การแก้บั๊กให้แก้เฉพาะจุดที่ผิดและรักษาพฤติกรรมเดิมที่ใช้งานได้ไว้ก่อน (minimal change)
- ข้อความที่เป็นชื่อเมนู ปุ่ม คำสั่ง สถานะ คำเตือน และข้อมูลที่จำเป็นต่อการตัดสินใจ ต้องแสดงครบ ห้ามใช้ Ellipsis/Clip จนความหมายขาด
- ถ้าพื้นที่ไม่พอ ให้ใช้ wrap 2+ บรรทัด, minimum height, adaptive layout, horizontal/vertical scroll หรือย่อเฉพาะองค์ประกอบตกแต่งแทนการตัดข้อความ
- ข้อมูลผู้ใช้ที่ยาวมากในรายการประวัติ อนุญาตให้ย่อด้วย Ellipsis ได้เฉพาะเมื่อแตะดู/คัดลอกข้อมูลเต็มได้ และต้องไม่ใช้กับชื่อ action หรือคำเตือน
- ห้ามตัดภาษาใดภาษาหนึ่งทิ้งเพื่อแก้ layout; TH/EN ที่รองรับต้องคงเส้นทางใช้งานเท่ากัน
- การลบระบบจำลอง VIP/Wallet/TopUp/Ad revenue ตามกฎเดิมถือเป็นข้อยกเว้น เพราะเป็นการตัดข้อมูลจำลอง ไม่ใช่ฟังก์ชัน QR หลัก
- ก่อนลบไฟล์/คลาส/route ต้องตรวจ reference และผลกระทบต่อฟังก์ชันที่เหลือก่อนเสมอ

## PLAY / VERSION
- Source of Truth ของเลขเวอร์ชันคือ `app/build.gradle.kts`; เอกสารและ UI ต้องตามค่านี้
- ห้ามล็อกเลขเวอร์ชันถาวรไว้ในกฎโปรเจกต์; เวอร์ชัน active ต้องอ่านจาก source ปัจจุบัน
- ก่อนอัปโหลด Play Console ต้องตรวจว่า versionCode สูงกว่ารุ่นที่ Play Console เคยรับจริง
- Publish ล้มเหลวแต่ build ผ่าน: ตรวจ versionCode, package, signing, permissions/API และ publish log ก่อนกด AI Fix

## RULE PRIORITY — 2026-09-22
กฎล่าสุดต่อไปนี้มีลำดับความสำคัญเหนือกฎ UI เดิมเมื่อมีความขัดแย้ง:
1. `UI_CARD_ONLY_RULE.md`
2. `UNIVERSAL_AUTO_LAYOUT_RULE.md`

### การตีความร่วมกับ PRESERVE-CONTENT RULE
- “ตัด” ตาม CARD-ONLY หมายถึงตัดคำอธิบาย/metadata/sub-text ที่ไม่จำเป็นออกจาก UI หลัก
- ห้ามตีความว่าอนุญาตให้ลบฟังก์ชัน เมนู route ปุ่ม หรือความสามารถหลัก
- technical detail ที่ยังจำเป็นต้องมีให้ย้ายไป detail/debug/log screen
- UI หลักใช้สถานะมาตรฐานเท่านั้น: ผ่าน / พร้อมใช้งาน / ไม่ผ่าน / Error


## LOCALE / REGION / CURRENCY RULE — 2026-09-22
- ภาษา ประเทศ/ภูมิภาค และสกุลเงินต้องสอดคล้องกันเป็นโปรไฟล์เดียว ห้ามเลือกแยกจนเกิดชุดค่าที่ขัดกัน
- ค่าเริ่มต้นต้องตรวจ locale/region ของเครื่อง แล้วตั้ง language + region + currency พร้อมกัน
- เมื่อผู้ใช้เปลี่ยนประเทศ/ภูมิภาค ให้เปลี่ยนสกุลเงินหลักของประเทศนั้นอัตโนมัติ
- ห้ามแสดงรายการทุกสกุลเงินให้ประเทศเดียวเลือกได้แบบอิสระใน UI หลัก
- PromptPay/Thai QR Payment เป็นการชำระเงินสกุล THB เท่านั้น: ช่องจำนวนเงิน, preset, QR payload และผลลัพธ์ต้องใช้บาทเสมอ
- ห้ามนำสกุลเงินที่เลือกของประเทศอื่นมาแทนยอด PromptPay หรือแสดง conversion ปะปนในหน้าหลัก PromptPay
- ถ้าต้องการ conversion ให้แยกไป detail screen และต้องใช้ rate จากแหล่งจริงพร้อมเวลาอัปเดต; ห้าม hardcode rate แล้วอ้างว่าเป็นค่าปัจจุบัน
- ห้าม UI ปนหลายภาษาในหน้าจอเดียวจาก translation coverage ไม่ครบ
- ภาษาใดที่ยังแปลไม่ครบทั้ง flow หลัก ห้ามเปิดเป็นตัวเลือก “พร้อมใช้งาน”; ให้ fallback ทั้งหน้าจอเป็นภาษาเดียว ไม่ใช่ fallback รายข้อความ
- Version badge ใน UI ต้องอ่านจาก BuildConfig.VERSION_NAME ห้าม hardcode หมายเลขเวอร์ชันใน UI


## SENIOR PRODUCT DESIGNER + ENGINEER RULE — 2026-09-23
ใช้เป็นกฎระดับระบบสำหรับงานออกแบบและพัฒนาแอปทุกครั้ง โดยต้องกรอกค่าจากโปรเจกต์จริงก่อนใช้งาน และห้ามเดาคู่แข่ง/หมวดหมู่/ข้อห้าม/คุณลักษณะที่ผู้ใช้ยังไม่ได้ระบุ

### ROLE
คุณคือ Senior Product Designer + Engineer ระดับโลก

### PROJECT BRIEF TEMPLATE
- โปรเจค: [ชื่อ] คือ [อธิบาย 1 ประโยค]
- เป้าหมาย: ชนะ [Top1], [Top2], [Top3] ในหมวด [หมวดหมู่]
- ข้อห้าม: [ระบุข้อห้ามของโปรเจกต์]
- ต้องมี: [ระบุองค์ประกอบ/ฟีเจอร์บังคับ]
- ความสวย: Glassmorphism 5% + blur 40px + radius 28px + Pill button
- ส่งมอบ: [ไฟล์/โค้ด/แพ็กเกจที่ต้องการ]

### EXECUTION RULES
- ห้ามเติม Top1/Top2/Top3 เอง หากยังไม่มีข้อมูลยืนยัน; ให้ทำเครื่องหมาย To verify หรือเว้นไว้จนกว่าจะกำหนด
- เป้าหมาย “ชนะ” หมายถึงยกระดับความชัดเจน การใช้งาน ความเร็ว ความน่าเชื่อถือ accessibility และคุณภาพงานโดยรวม ห้ามลอก UI/แบรนด์/ทรัพย์สินทางปัญญาของคู่แข่ง
- ต้องเริ่มจากข้อห้ามและข้อบังคับก่อนออกแบบ ห้ามตัดฟังก์ชันหลักเพื่อให้หน้าสวย
- งาน UI ต้องสอดคล้องกับ UI_CARD_ONLY_RULE และ UNIVERSAL_AUTO_LAYOUT_RULE เมื่อใช้กับโปรเจกต์นี้
- Glassmorphism ใช้เป็น accent ประมาณ 5% เท่านั้น ไม่ใช้กระจก/blur ทับทั้งหน้าจอจนอ่านยากหรือกระทบ performance
- blur 40px / radius 28px / Pill button เป็น design token เป้าหมาย ไม่ใช่เหตุผลให้ฝืน layout; บนจอเล็กต้อง adaptive และรักษา touch target/accessibility
- ห้ามใช้ fixed dimension เพื่อบังคับหน้าหลักจนเกิด overflow
- ห้ามแสดง metadata ทางเทคนิคใน UI หลักตาม CARD-ONLY RULE
- ส่งมอบต้องเป็นไฟล์/โค้ดที่ใช้งานต่อได้จริงตามที่ระบุ ไม่ส่ง mockup แทน implementation เว้นแต่ผู้ใช้ขอ mockup
- ก่อนส่งมอบต้องผ่าน final gate ของ UNIVERSAL AUTO LAYOUT RULE และตรวจข้อความไทย/อังกฤษไม่ปนกันตาม locale rule


## PRODUCT BRIEF REFERENCE — 2026-09-23
- ใช้ `PRODUCT_BRIEF.md` เป็น Product brief หลักของ QuickQR Business
- Benchmark ที่ใช้: QR & Barcode Scanner (Gamma Play), QR & Barcode Scanner (TeaCapps), QR TIGER QR Code Generator
- Benchmark ใช้เพื่อกำหนดมาตรฐานด้าน speed, simplicity, scan actions, QR generation/customization และ business utility; ไม่ถือเป็นอันดับทางการของ Google Play
- ก่อน redesign หรือเพิ่ม feature ให้ตรวจ PROJECT_RULES + PRODUCT_BRIEF + UI_CARD_ONLY_RULE + UNIVERSAL_AUTO_LAYOUT_RULE ร่วมกัน


## WORKING APP / USABLE BUILD RULE
- ทุก release ต้องเป็นแอปที่ใช้งานได้จริง ไม่ใช่ mockup, demo, placeholder หรือหน้าจอจำลอง
- ฟังก์ชันหลักที่แสดงใน UI ต้องมี implementation จริงและกดใช้งานได้: Generate QR, PromptPay, Wi‑Fi, Store Link, Text, Business Card, Scanner, History, Language/Region และ Save/Share
- ห้ามแสดงข้อมูลสุ่ม ข้อมูลตัวอย่าง หรือสถานะสำเร็จปลอมเป็นข้อมูลจริง
- ห้ามเพิ่มปุ่ม/เมนูที่ไม่มี action จริง; ถ้าฟังก์ชันยังไม่พร้อมต้องซ่อนหรือระบุว่าไม่พร้อมอย่างชัดเจน
- ก่อนถือว่า "ใช้งานได้" ต้องผ่านอย่างน้อย: compile/build สำเร็จ, เปิดแอปได้, navigation ใช้ได้, ฟังก์ชันหลักไม่ crash, input validation ทำงาน, QR ที่สร้างอ่านกลับได้, Scanner อ่าน QR ได้, History บันทึก/ลบได้, TH/EN ไม่ปน, Region/Currency ถูกต้อง, safe-area/overflow ผ่านบนจอเล็ก
- ต้องทดสอบบน Preview/Emulator หรืออุปกรณ์จริงหลังเปลี่ยนโค้ดสำคัญ; static review อย่างเดียวห้ามใช้ยืนยันว่า build ใช้งานได้
- ห้ามอ้างว่า build ผ่านหรือพร้อมเผยแพร่หากยังไม่ได้ยืนยันจาก build ล่าสุด
- เมื่อมี error ให้แก้ root cause ทีละจุด ห้ามสุ่มแก้หรือลบฟังก์ชันเพื่อให้ compile ผ่าน
- package ต้องคง `com.aistudio.qrgenerator.kmpzqr` เว้นแต่ผู้ใช้สั่งเปลี่ยนโดยตรง


## CAMERA / PREVIEW RULE
- AI Studio preview metadata must request `camera` when scanner preview is enabled.
- Android runtime camera permission remains `android.permission.CAMERA`.
- CameraX Preview and ImageAnalysis must be unbound when leaving the scanner screen.
- Prefer PreviewView COMPATIBLE mode when embedded preview/emulator SurfaceView rendering is corrupted.
- Do not treat synthetic/emulator camera imagery as proof of physical-device camera quality; verify on a real device before release.


## RELEASE SOURCE OF TRUTH / CLEANUP RULE — 2026-09-23
- `main` ของ GitHub เป็น Source of Truth หลัง resolve conflict แล้ว
- เลขเวอร์ชันหลักอ่านจาก `app/build.gradle.kts`; UI badge อ่านจาก `BuildConfig.VERSION_NAME`; metadata/docs ต้องตรงกัน
- ทุก release ใหม่ต้องเพิ่ม `versionCode` และห้ามย้อนกลับไปใช้เลขที่ Play Console เคยรับแล้ว
- หลังอัปเดตทั้งโปรเจกต์ ให้ลบเอกสาร release เก่าที่เจ้าของโปรเจกต์สั่งให้ยกเลิก และแก้ reference ให้ชี้รุ่นปัจจุบันก่อนลบ
- ห้ามลบ source/class/route ที่ยังถูกอ้างอิงหรือเป็นฟังก์ชันหลักเพื่อทำความสะอาดไฟล์
- ก่อนลบไฟล์ runtime ต้องค้น reference และยืนยันว่า build path ที่เหลือไม่พึ่งไฟล์นั้น
- ไฟล์หรือข้อมูลเก่าที่ทำให้เวอร์ชัน/กฎ/พรีวิวขัดกับ Source of Truth ต้องถูกแทนที่ ไม่เก็บซ้ำเป็น active source


## PROJECT SCOPE ISOLATION / AUTOMATION RULE FILE — 2026-09-23
- ใช้ไฟล์ `ใช้แชทสร้างแอพอัปโหลดอัตโนมัติ.txt` ใน repository นี้เป็นกฎอัตโนมัติของ **QuickQR Business** เท่านั้น
- กฎจากโปรเจกต์อื่นใช้ร่วมได้เฉพาะหลักทั่วไป เช่น Anti-Fake / Anti-Random / Root-Cause Fix / Responsive / Release Gate
- ห้ามนำ feature หรือ implementation เฉพาะโปรเจกต์อื่นเข้ามาใน QuickQR อัตโนมัติ เช่น Speed Test, EndpointHealthChecker, IP/Server status หรือ Video throughput เว้นแต่เจ้าของโปรเจกต์สั่งเพิ่มโดยชัดเจน
- เมื่อไฟล์กฎภายนอกขัดกับ GitHub `main`, ให้ยึด `main` + `PROJECT_RULES.md` + Product Brief ของ QuickQR เป็น Source of Truth
- ก่อนอัปเดตอัตโนมัติทุกครั้งต้องตรวจ package, version, core routes, references และ build gate ก่อนลบหรือแทนที่ไฟล์
