package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ui.theme.appTextFieldColors
import com.example.util.PrivacyProtection
import com.example.util.localizedText
import com.example.util.localizedNow
import com.example.util.RootSecurityManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportAndBugReportBottomSheet(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableStateOf(0) } // 0=แจ้งบัค/ปัญหา, 1=AI ผู้ช่วยตอบคำถาม/ติดต่อแอดมิน

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("support_bottom_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.BugReport else if (selectedTab == 1) Icons.AutoMirrored.Filled.ContactSupport else Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (selectedTab == 0) localizedText("แจ้งปัญหา", "Report a problem") else if (selectedTab == 1) localizedText("ติดต่อและช่วยเหลือ", "Support") else localizedText("ความปลอดภัยระบบ", "Security"),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = localizedText("ข้อมูลช่วยเหลือ ระบบ และการตรวจสอบความปลอดภัย", "Help, system information and security checks"),
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = localizedText("ปิด", "Close"), tint = Color(0xFF64748B))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs: Tab 0 = แจ้งบัค, Tab 1 = ติดต่อแอดมิน / FAQ, Tab 2 = ความปลอดภัย (Anti-Root)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFFF1F5F9),
                contentColor = Color(0xFF0B2853),
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = localizedText("แจ้งบัค", "Report bug"),
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ContactSupport, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = localizedText("ติดต่อแอดมิน", "Support"),
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = localizedText("ความปลอดภัย", "Security"),
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> BugReportForm(viewModel, onSuccess = onDismiss)
                1 -> AdminContactSupportView()
                2 -> SecurityStatusView()
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun SecurityStatusView() {
    val context = LocalContext.current
    val rootResult by RootSecurityManager.rootState.collectAsState()
    val isRooted = rootResult?.isRooted ?: false

    Column(modifier = Modifier.fillMaxWidth()) {
        // Status Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isRooted) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isRooted) Color(0xFFFECACA) else Color(0xFFBBF7D0)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isRooted) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = if (isRooted) Color(0xFFDC2626) else Color(0xFF16A34A),
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isRooted) localizedText("ตรวจพบความเสี่ยง", "Risk detected") else localizedText("ปลอดภัย • ไม่พบการรูท", "Safe • No root detected"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRooted) Color(0xFF991B1B) else Color(0xFF166534)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isRooted) Color(0xFFEF4444) else Color(0xFF10B981)
                        ) {
                            Text(
                                text = localizedText("เปิดใช้งานตลอด", "Always on"),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = localizedText("ระบบ Anti-Root ตรวจสอบความปลอดภัยแบบเรียลไทม์", "Anti-Root security checks run in real time"),
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = localizedText("รายการตรวจสอบความสมบูรณ์ของระบบ:", "System integrity checks:"),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF334155)
        )

        Spacer(modifier = Modifier.height(8.dp))

        val checks = listOf(
            Triple("Root Binary Check (su, busybox)", !(rootResult?.suBinaryFound ?: false), "ตรวจหาไฟล์คำสั่งระดับผู้ดูแลระบบ"),
            Triple("Root Management Apps (Magisk, SuperSU)", !(rootResult?.rootAppFound ?: false), "ตรวจหาแอปพลิเคชันจัดการสิทธิ์รูท"),
            Triple("Process Execution Integrity", !(rootResult?.suExecutionSucceeded ?: false), "ป้องกันการเรียกคำสั่ง su ในเบื้องหลัง"),
            Triple("System Partition Read-Only", !(rootResult?.rwMountsFound ?: false), "ตรวจสอบการดัดแปลงไฟล์ระบบ system rw"),
            Triple("Official Build Signature", !(rootResult?.testKeysFound ?: false), "ตรวจสอบลายเซ็น OS Release-keys")
        )

        checks.forEach { (title, passed, subtitle) ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = subtitle,
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (passed) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                    ) {
                        Text(
                            text = if (passed) localizedText("✓ ผ่าน", "✓ Pass") else localizedText("✕ เสี่ยง", "✕ Risk"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (passed) Color(0xFF16A34A) else Color(0xFFDC2626),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Re-scan button
        Button(
            onClick = {
                RootSecurityManager.verifyDeviceIntegrity(context)
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B2853)),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("rescan_root_security_button")
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(localizedText("ตรวจสอบความปลอดภัยอุปกรณ์ใหม่", "Run security check again"), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BugReportForm(
    viewModel: MainViewModel,
    onSuccess: () -> Unit
) {
    var category by remember { mutableStateOf("QR พร้อมเพย์สแกนไม่ติด") }
    var description by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }

    val categories = listOf(
        "QR พร้อมเพย์สแกนไม่ติด",
        "Wi-Fi เชื่อมต่อไม่ได้",
        "นามบัตรดิจิทัลมีปัญหา",
        "สแกนเนอร์เปิดกล้องไม่ได้",
        "ข้อเสนอแนะอื่นๆ"
    )

    if (isSubmitted) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDCFCE7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = localizedText("ส่งรายงานบัคเรียบร้อยแล้ว!", "Bug report submitted!"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF16A34A)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = localizedText("ขอบคุณที่แจ้งปัญหา", "Thank you for reporting the issue"),
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onSuccess,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B2853))
            ) {
                Text(localizedText("ตกลงและปิด", "OK and close"))
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = localizedText("เลือกหมวดหมู่ปัญหาที่พบ:", "Choose issue category:"),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { cat ->
                    val isSelected = category == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { category = cat },
                        label = {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0284C7).copy(alpha = 0.15f),
                            selectedLabelColor = Color(0xFF0284C7)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.wrapContentWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(localizedText("รายละเอียดปัญหาที่เกิดขึ้น", "Issue details")) },
                placeholder = { Text(localizedText("เช่น ไม่สามารถสแกน QR โค้ดที่บันทึกลงเครื่องได้...", "Describe what went wrong...")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("bug_description_input"),
                shape = RoundedCornerShape(12.dp),
                colors = appTextFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = contactInfo,
                onValueChange = { contactInfo = it },
                label = { Text(localizedText("ข้อมูลติดต่อกลับ", "Contact information")) },
                placeholder = { Text(localizedText("อีเมล / Line ID / เบอร์โทร", "Email / Line ID / phone")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bug_contact_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = appTextFieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (description.isNotBlank()) {
                        contactInfo = PrivacyProtection.sanitizeEmail(contactInfo)
                        isSubmitted = true
                    }
                },
                enabled = description.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_bug_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(localizedText("ส่งรายงานปัญหา", "Submit report"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun AdminContactSupportView() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_contact_view")
    ) {
        // Direct Contact Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(localizedText("ฝ่ายดูแลลูกค้า", "Support"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF0284C7).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = localizedText("ฝ่ายช่วยเหลือ", "Official Support"),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0284C7),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(localizedText("อีเมล: ", "Email: ") + PrivacyProtection.OFFICIAL_ADMIN_EMAIL, fontSize = 12.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = localizedText("หากพบปัญหาการสร้าง QR การสแกน หรือนามบัตรดิจิทัล สามารถติดต่อฝ่ายช่วยเหลือได้", "Contact support for QR generation, scanning, or digital business card issues"),
                    fontSize = 12.sp,
                    color = Color(0xFF334155),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:${PrivacyProtection.OFFICIAL_ADMIN_EMAIL}")
                                    putExtra(Intent.EXTRA_SUBJECT, "[ZipQR v9.0] แจ้งปัญหา / ติดต่อแอดมิน")
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, localizedNow("กรุณาส่งอีเมลไปที่: ", "Email: ") + PrivacyProtection.OFFICIAL_ADMIN_EMAIL, Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(localizedText("ติดต่อฝ่ายช่วยเหลือ", "Contact support"), fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Admin Email", PrivacyProtection.OFFICIAL_ADMIN_EMAIL))
                            Toast.makeText(context, localizedNow("คัดลอกอีเมลแล้ว", "Email copied"), Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(localizedText("คัดลอก", "Copy"), fontSize = 12.sp, maxLines = 1, softWrap = false)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // FAQ Section
        Text(localizedText("คำถามและวิธีใช้งานเบื้องต้น", "FAQ"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
        Spacer(modifier = Modifier.height(8.dp))

        val faqs = listOf(
            Pair(localizedText("วิธีสร้าง QR พร้อมเพย์", "How to create a PromptPay QR"), localizedText("เลือกพร้อมเพย์ กรอกหมายเลขและจำนวนเงิน แล้วกดสร้าง", "Choose PromptPay, enter the ID and amount, then generate")),
            Pair(localizedText("การบันทึกภาพและแชร์", "Save and share"), localizedText("เมื่อสร้าง QR แล้ว เลือกบันทึกหรือแชร์", "After generating a QR, choose Save or Share")),
            Pair(localizedText("ระบบสแกนคิวอาร์โค้ด", "QR scanner"), localizedText("ใช้กล้องหรือเลือกรูปจากเครื่องเพื่ออ่าน QR Code", "Use the camera or an image from your device to scan a QR code"))
        )

        faqs.forEach { (question, answer) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "• $question",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = answer,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "ZipQR เวอร์ชั่น 8.0 (Build 8) • GEN_QR v8 (qrcode1.5.4)",
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}
