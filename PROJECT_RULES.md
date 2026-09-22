# PROJECT_RULES.md — QuickQR Business

## หลักบังคับ
- ห้าม Fake/Random/Hardcode ข้อมูลที่อ้างว่าเป็นข้อมูลจริง; ไม่มีข้อมูลให้แสดงว่าง/ไม่ทราบ/ผิดพลาด
- package ต้องคง `com.aistudio.qrgenerator.kmpzqr` เว้นแต่ผู้ใช้สั่งเปลี่ยนโดยชัดเจน
- ห้ามฝังอีเมลส่วนตัวเดิมใน source code; developer identity ใช้ `215334638+AnakinYoo@users.noreply.github.com` ตามกฎล่าสุด
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
- versionCode ใหม่ต้องสูงกว่า versionCode ที่มีใน Play Console
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
- Version badge ใน UI ต้องอ่านจาก BuildConfig.VERSION_NAME ห้าม hardcode v8/v11/v12
