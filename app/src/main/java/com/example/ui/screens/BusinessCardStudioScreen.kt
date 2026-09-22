package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.model.CardColorTheme
import com.example.model.DigitalBusinessCard
import com.example.ui.theme.appTextFieldColors
import com.example.util.ImageExporter
import com.example.util.QrCodeUtil
import com.example.util.localizedText
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BusinessCardStudioScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val card by viewModel.businessCard.collectAsState()
    val scrollState = rememberScrollState()

    // Live QR preview for business card
    val liveQrBitmap = remember(card) {
        val payload = QrCodeUtil.buildVCardPayload(
            fullName = card.fullName,
            org = card.businessName,
            title = card.profession,
            phone = card.phoneNumber,
            email = card.email,
            url = if (card.facebook.isNotBlank()) "https://facebook.com/${card.facebook}" else "",
            note = "พร้อมเพย์: ${card.promptPayId} | ${card.services}"
        )
        QrCodeUtil.generateQrBitmap(
            content = payload,
            size = 400,
            darkColor = card.cardTheme.primaryColorHex.toInt()
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = localizedText("นามบัตรดิจิทัล", "Digital Business Card"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = localizedText("ส่งให้ลูกค้าทาง LINE / Facebook หรือพิมพ์ติดหน้าร้าน", "Share via LINE / Facebook or print for your shop"),
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // LIVE DIGITAL CARD PREVIEW (High Fidelity Canvas Card)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("digital_card_preview"),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Card Top Banner with Theme Gradient
                val primary = Color(card.cardTheme.primaryColorHex)
                val accent = Color(card.cardTheme.accentColorHex)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(Brush.horizontalGradient(listOf(primary, accent)))
                        .padding(16.dp)
                ) {
                    Column {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = localizedText("นามบัตรดิจิทัล", "DIGITAL BUSINESS CARD"),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = card.businessName.ifBlank { localizedText("ชื่อร้าน / กิจการ", "Shop / Business") },
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = card.profession.ifBlank { localizedText("อาชีพ / บริการที่รับทำ", "Profession / Services") },
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                }

                // Card Body
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = card.fullName.ifBlank { localizedText("ชื่อผู้ติดต่อ", "Contact name") },
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Contact info items
                            if (card.phoneNumber.isNotBlank()) {
                                ContactItem(icon = Icons.Default.Phone, text = card.phoneNumber, color = primary)
                            }
                            if (card.promptPayId.isNotBlank()) {
                                ContactItem(icon = Icons.Default.Payment, text = "${localizedText("พร้อมเพย์", "PromptPay")}: ${card.promptPayId}", color = Color(0xFF059669))
                            }
                            if (card.lineId.isNotBlank()) {
                                ContactItem(icon = Icons.Default.Store, text = "LINE: ${card.lineId}", color = Color(0xFF059669))
                            }
                            if (card.facebook.isNotBlank()) {
                                ContactItem(icon = Icons.Default.Business, text = "FB: ${card.facebook}", color = primary)
                            }
                        }

                        // Embedded QR Code
                        liveQrBitmap?.let { bitmap ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(start = 12.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(card.cardTheme.surfaceColorHex),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, primary.copy(alpha = 0.2f)),
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = localizedText("QR Code บนนามบัตร", "Business card QR code"),
                                        modifier = Modifier
                                            .size(96.dp)
                                            .padding(6.dp)
                                    )
                                }
                                Text(
                                    text = localizedText("สแกนเพื่อบันทึก", "Scan to save"),
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    // Services section if available
                    if (card.services.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(card.cardTheme.surfaceColorHex),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = localizedText("บริการและรายละเอียดงาน:", "Services and details:"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primary
                                )
                                Text(
                                    text = card.services,
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.generateBusinessCardPreview() },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("create_card_qr_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(card.cardTheme.primaryColorHex))
            ) {
                Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(localizedText("ดู QR นามบัตร", "View card QR"), fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
            }

            Button(
                onClick = { viewModel.saveBusinessCardProfile() },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("save_profile_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(localizedText("บันทึกเป็นค่าหลัก", "Save as default"), fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // THEME COLOR SELECTOR
        Text(
            text = localizedText("เลือกโทนสีนามบัตร", "Choose card color theme"),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CardColorTheme.values().forEach { theme ->
                val isSel = card.cardTheme == theme
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(theme.primaryColorHex))
                        .clickable { viewModel.updateBusinessCard(card.copy(cardTheme = theme)) }
                        .border(
                            width = if (isSel) 3.dp else 0.dp,
                            color = if (isSel) Color(0xFF0284C7) else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    color = Color(theme.primaryColorHex)
                ) {
                    if (isSel) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // FORM FIELDS TO CUSTOMIZE
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = localizedText("ข้อมูลบนนามบัตรดิจิทัล", "Digital business card details"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Business Name
                Text(localizedText("ชื่อร้านค้า / กิจการ / ธุรกิจ", "Shop / Business name"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = card.businessName,
                    onValueChange = { viewModel.updateBusinessCard(card.copy(businessName = it)) },
                    placeholder = { Text(localizedText("เช่น สมชาย การช่าง รับเหมา", "e.g. Somchai Service"), color = Color(0xFF94A3B8)) },
                    leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = Color(0xFF0284C7)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("card_business_input"),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = TextStyle(color = Color.Black, fontSize = 14.sp),
                    colors = appTextFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Full Name
                Text(localizedText("ชื่อ-นามสกุล / ชื่อเล่น", "Full name / Nickname"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = card.fullName,
                    onValueChange = { viewModel.updateBusinessCard(card.copy(fullName = it)) },
                    placeholder = { Text(localizedText("เช่น ช่างสมชาย, คุณแนน", "e.g. Somchai, Nan"), color = Color(0xFF94A3B8)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF0284C7)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("card_name_input"),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = TextStyle(color = Color.Black, fontSize = 14.sp),
                    colors = appTextFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Profession / Role
                Text(localizedText("อาชีพ / ประเภทบริการ", "Profession / Services"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = card.profession,
                    onValueChange = { viewModel.updateBusinessCard(card.copy(profession = it)) },
                    placeholder = { Text(localizedText("เช่น ช่างรับเหมาต่อเติม, แม่ค้าออนไลน์, ฟรีแลนซ์", "e.g. Contractor, Online seller, Freelancer"), color = Color(0xFF94A3B8)) },
                    leadingIcon = { Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF0284C7)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("card_profession_input"),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = TextStyle(color = Color.Black, fontSize = 14.sp),
                    colors = appTextFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Phone & PromptPay ID
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(localizedText("เบอร์โทรศัพท์", "Phone number"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = card.phoneNumber,
                            onValueChange = { viewModel.updateBusinessCard(card.copy(phoneNumber = it)) },
                            placeholder = { Text("0812345678", color = Color(0xFF94A3B8)) },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF0284C7)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            textStyle = TextStyle(color = Color.Black, fontSize = 14.sp),
                            colors = appTextFieldColors()
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(localizedText("พร้อมเพย์รับเงิน", "PromptPay"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = card.promptPayId,
                            onValueChange = { viewModel.updateBusinessCard(card.copy(promptPayId = it)) },
                            placeholder = { Text(localizedText("เบอร์หรือเลขบัตร", "Phone or ID"), color = Color(0xFF94A3B8)) },
                            leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null, tint = Color(0xFF059669)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            textStyle = TextStyle(color = Color.Black, fontSize = 14.sp),
                            colors = appTextFieldColors()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // LINE & Facebook
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("LINE ID", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = card.lineId,
                            onValueChange = { viewModel.updateBusinessCard(card.copy(lineId = it)) },
                            placeholder = { Text("@line_id", color = Color(0xFF94A3B8)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            textStyle = TextStyle(color = Color.Black, fontSize = 14.sp),
                            colors = appTextFieldColors()
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(localizedText("Facebook เพจ", "Facebook page"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = card.facebook,
                            onValueChange = { viewModel.updateBusinessCard(card.copy(facebook = it)) },
                            placeholder = { Text(localizedText("ชื่อเพจ", "Page name"), color = Color(0xFF94A3B8)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            textStyle = TextStyle(color = Color.Black, fontSize = 14.sp),
                            colors = appTextFieldColors()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Services / About
                Text(localizedText("ขอบเขตงาน / สินค้าแนะนำ", "Services / Recommended products"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = card.services,
                    onValueChange = { viewModel.updateBusinessCard(card.copy(services = it)) },
                    placeholder = { Text(localizedText("เช่น รับงานทั่วกทม.และปริมณฑล ประเมินราคาฟรี...", "e.g. Service area, free estimate..."), color = Color(0xFF94A3B8)) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("card_services_input"),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = TextStyle(color = Color.Black, fontSize = 14.sp),
                    colors = appTextFieldColors()
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun ContactItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFF334155),
            fontWeight = FontWeight.Medium
        )
    }
}
