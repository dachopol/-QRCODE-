# UNIVERSAL AUTO LAYOUT RULE

## GOAL
ทุกหน้าต้อง Responsive / Adaptive, อ่านครบ, กดได้ครบ, ไม่มี overflow และไม่ซ่อนฟังก์ชันสำคัญบนจอเล็ก

## 1. MOBILE FIRST
- เริ่มออกแบบจากจอเล็ก
- หลีกเลี่ยง fixed width/height สำหรับโครงสร้างหลัก
- ใช้ fluid/flexible sizing ตาม framework

## 2. WEB
- Main layout: CSS Grid
- Internal layout: Flexbox
- ใช้ %, fr, minmax(), clamp(), max-width
- ห้ามใช้ absolute/float เป็นระบบ layout หลัก
- Container Query ใช้เมื่อเหมาะสม

## 3. FLUTTER
- ใช้ LayoutBuilder / MediaQuery อย่างมีเหตุผล
- ใช้ Expanded / Flexible / Wrap
- ใช้ FractionallySizedBox หรือ constraints เมื่อเหมาะสม
- หลีกเลี่ยง SizedBox width/height ตายตัวสำหรับ layout หลัก
- ค่า touch target ใช้หน่วย dp/logical pixels

## 4. FIGMA
- Auto Layout
- Fill Container
- Hug Contents ตามหน้าที่
- Min width สำหรับมือถือประมาณ 320
- ใช้ spacing token เดียวกับระบบจริง

## 5. BREAKPOINT BASELINE
Web baseline: sm 640 / md 768 / lg 1024
เพิ่มเติม breakpoint ได้เฉพาะเมื่อ layout จริงต้องการ ห้ามสร้าง breakpoint แบบสุ่มเพื่อแก้เฉพาะจุด

## 6. SPACING TOKEN
ใช้ชุดหลัก: 4 / 8 / 12 / 16 / 24 / 32 / 48 / 64

## 7. MEDIA
- max-width: 100%
- ป้องกัน media ล้น container
- รักษา aspect ratio ต้นฉบับ
- ใช้ object-fit ตามประเภทเนื้อหา
- ห้ามบังคับทุกภาพเป็น 16:9 หากทำให้ภาพผิดสัดส่วน

## 8. NO HORIZONTAL OVERFLOW
- ต้องไม่มี horizontal scroll ที่เกิดจาก layout ผิด
- ยกเว้น component ที่ตั้งใจให้เลื่อนแนวนอน เช่น carousel/table
- ทดสอบขั้นต่ำ 320 / 640 / 768 / 1024 / 1440

## 9. TOUCH + ACCESSIBILITY
- Touch target อย่างน้อยประมาณ 44×44 logical px
- ปุ่มไม่ชิดกันเกินไป
- รองรับ font scaling
- contrast อ่านได้
- focus/keyboard support สำหรับ Web
- semantic/accessibility label เมื่อจำเป็น

## 10. TEXT COMPLETENESS
- ห้าม clip/crop/overlap
- ไทย/อังกฤษต้องอ่านครบ
- ใช้ wrap ก่อน truncate
- truncate เฉพาะข้อมูลที่อนุญาตให้ย่อ
- ห้ามลด font ทั้งแอปเพื่อแก้ overflow

## 11. SAFE AREA
- Android/iOS ต้องคำนึง status/navigation bars
- Web mobile ต้องคำนึง viewport/safe-area
- ปุ่มสำคัญห้ามถูก system UI บัง

## 12. NAVIGATION
- ฟังก์ชันสำคัญต้องเข้าถึงได้ทุกขนาดจอ
- พื้นที่ไม่พอ → Wrap / Scroll / Drawer / Adaptive Navigation
- ห้ามซ่อน feature เพียงเพื่อให้หน้าดูพอดี

## 13. PERFORMANCE
- Web: CSS layout ก่อน JS
- Flutter: หลีกเลี่ยง rebuild/measurement ที่ไม่จำเป็น
- ห้ามเพิ่ม dependency เพื่อแก้ layout หาก framework ทำได้เอง

## 14. TEST ORIENTATION
ทดสอบ Portrait / Landscape / Small phone / Tablet / Desktop-Web

## 15. FINAL GATE
/critique → /hierarchy → /consistency → /contrast → /a11y → /readability → /typeset → /font-scale → /line-height → /thai-fix → /wrap → /truncate → /layout → /grid → /spacing → /align → /stack → /safe-area → /states → /motion → /empty → /polish → /final
