# QuickQR Business — Crypto QR Roadmap

Status: **Planned / Not enabled in runtime**

## Goal
เพิ่มความสามารถสร้าง QR สำหรับคริปโตในอนาคต โดยแยกจาก PromptPay และระบบ Region/Currency ปัจจุบันอย่างชัดเจน

## Scope ที่วางแผน
- Crypto QR เป็นเมนูแยกใน Generate QR
- เหรียญเริ่มต้นที่พิจารณา: BTC, ETH, USDT, USDC
- ผู้ใช้ต้องเลือก Network ก่อนสร้าง QR เมื่อเหรียญรองรับหลายเครือข่าย
- รองรับ Wallet Address และมาตรฐาน Payment URI ที่เหมาะสมกับแต่ละเครือข่าย
- QR ที่สร้างต้อง decode กลับและตรงกับ address/network ที่ผู้ใช้กรอก

## กฎบังคับ
- PromptPay ยังคง THB เท่านั้น และห้ามผูกกับ Crypto QR
- ห้ามสร้าง wallet address, balance, transaction, exchange rate หรือสถานะสำเร็จปลอม
- ค่าเริ่มต้น wallet/address ต้องว่าง
- ถ้าแสดงราคา/Conversion ต้องดึงจากแหล่งจริง พร้อมเวลาอัปเดต
- ดึง rate ไม่ได้ให้แสดง `-- / Error` ห้ามใช้ hardcode rate แล้วอ้างเป็นราคาปัจจุบัน
- ต้องแสดง Network ชัดเจนก่อนสร้าง QR เพื่อป้องกันการส่งผิดเครือข่าย
- ห้ามเก็บ private key / seed phrase / recovery phrase
- QuickQR จะสร้าง/อ่าน QR เท่านั้น ไม่ทำหน้าที่เป็น crypto wallet เว้นแต่มีการอนุมัติ scope ใหม่ภายหลัง

## UX Plan
Flow เป้าหมาย:
Generate QR
→ Crypto QR
→ Coin
→ Network
→ Wallet Address
→ Amount (optional)
→ Validate
→ Generate QR
→ Decode self-check
→ Save / Share

## Validation ที่ต้องมี
- ตรวจรูปแบบ address ตาม coin/network จริง
- ป้องกัน address/network mismatch เท่าที่มาตรฐานรองรับ
- Amount ต้องเป็นเลขที่ถูกต้องและไม่ติดลบ
- QR หลังสร้างต้องอ่านกลับได้
- TH/EN ต้องครบทั้ง flow
- จอเล็กต้องไม่มี overflow และทุก action ต้องเข้าถึงได้

## Security / Privacy
- ไม่ส่ง wallet address ออกนอกเครื่องโดยไม่จำเป็น
- ไม่เก็บ secret credential ใด ๆ
- History ถ้าเปิดบันทึก ต้องเก็บเฉพาะข้อมูลที่จำเป็นและให้ผู้ใช้ลบได้
- ก่อนเพิ่ม API ราคา ต้องตรวจ Privacy Policy / Data Safety ใหม่

## Release Gate ก่อนเปิดจริง
Research / Standards check
→ Coin + Network scope lock
→ Address validation
→ Payment URI implementation
→ Real-rate provider decision (ถ้าจำเป็น)
→ Privacy/Data Safety review
→ Unit tests
→ QR round-trip tests
→ Device/UI test
→ TH/EN
→ Security review
→ Play Console review
→ Release

## ไม่รวมใน v17
- ไม่มี Crypto QR runtime
- ไม่มี wallet
- ไม่มี private key / seed phrase
- ไม่มีราคาเหรียญหรือ conversion
- ไม่มี transaction/balance lookup
