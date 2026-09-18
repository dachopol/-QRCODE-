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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
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
                            text = if (selectedTab == 0) "แจ้งปัญหา / บัคการใช้งาน" else if (selectedTab == 1) "ติดต่อแอดมิน & คำถามที่พบบ่อย" else "ความปลอดภัยระบบ",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "ทีมงานพร้อมดูแลและปรับปรุงระบบตลอด 24 ชม.",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "ปิด", tint = Color(0xFF64748B))
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("แจ้งบัค", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.ContactSupport, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ติดต่อแอดมิน", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ความปลอดภัย", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal)
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
                            text = if (isRooted) "ตรวจพบความเสี่ยง (Rooted)" else "ปลอดภัย • ไม่พบการรูท",
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
                                text = "เปิดใช้งานตลอด",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ระบบ Anti-Root ตรวจสอบความปลอดภัยแบบเรียลไทม์",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "รายการตรวจสอบความสมบูรณ์ของระบบ:",
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
                            text = if (passed) "✓ ผ่าน" else "✕ เสี่ยง",
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
            Text("ตรวจสอบความปลอดภัยอุปกรณ์ใหม่เดี๋ยวนี้", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

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
                text = "ส่งรายงานบัคเรียบร้อยแล้ว!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF16A34A)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "ขอบคุณที่แจ้งปัญหาเข้ามา ทีมงานแอดมินจะรีบตรวจสอบและแก้ไขในทันทีครับ",
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onSuccess,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B2853))
            ) {
                Text("ตกลงและปิดหน้านี้")
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "เลือกหมวดหมู่ปัญหาที่พบ:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                categories.chunked(2).forEach { rowList ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowList.forEach { cat ->
                            val isSelected = category == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0284C7).copy(alpha = 0.15f),
                                    selectedLabelColor = Color(0xFF0284C7)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowList.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("รายละเอียดปัญหาที่เกิดขึ้น") },
                placeholder = { Text("เช่น ไม่สามารถสแกน QR โค้ดที่บันทึกลงเครื่องได้...") },
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
                label = { Text("ข้อมูลติดต่อกลับ (อีเมล / Line ID / เบอร์โทร)") },
                placeholder = { Text("เพื่อให้ทีมงานแอดมินแจ้งผลการแก้ไข") },
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
                Text("ส่งรายงานแจ้งบัคให้แอดมิน", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("ฝ่ายดูแลลูกค้า & แอดมิน", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text("อีเมล: chenkung12@gmail.com", fontSize = 12.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "หากพบปัญหาการสร้าง QR, การสแกน, นามบัตรดิจิทัล หรือต้องการความช่วยเหลือ สามารถส่งอีเมลหาทีมงานได้โดยตรง เราพร้อมให้บริการตรวจสอบและแก้ไขตลอด 24 ชั่วโมง",
                    fontSize = 12.sp,
                    color = Color(0xFF334155),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:chenkung12@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "[QR PromptPay] สอบถามปัญหา / ติดต่อแอดมิน")
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "กรุณาส่งอีเมลไปที่: chenkung12@gmail.com", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ส่งอีเมลหาทีมงานโดยตรง", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // FAQ Section
        Text("คำถามและวิธีใช้งานเบื้องต้น (FAQ)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
        Spacer(modifier = Modifier.height(8.dp))

        val faqs = listOf(
            Pair("วิธีสร้าง QR พร้อมเพย์", "เลือกแท็บ 'พร้อมเพย์' กรอกเบอร์มือถือ 10 หลักหรือเลขบัตรประชาชน 13 หลัก พร้อมใส่จำนวนเงินที่ต้องการรับเงิน จากนั้นกดสร้าง"),
            Pair("การบันทึกภาพและแชร์", "เมื่อสร้าง QR แล้ว ให้กดปุ่ม 'บันทึกภาพ' หรือ 'แชร์ภาพ' เพื่อส่งต่อให้ลูกค้าหรือเพื่อนได้ทันที"),
            Pair("สิทธิ์ VIP และโหมดทดสอบ", "ขณะนี้แอปเปิดใช้งานสิทธิ์ VIP ถาวรฟรี (isTestMode = true) สามารถใช้งานได้ทุกฟีเจอร์โดยไม่ต้องชำระเงิน"),
            Pair("ระบบสแกนคิวอาร์โค้ด", "สามารถใช้กล้องสแกน หรือเลือกรูปภาพจากเครื่องเพื่ออ่านค่า QR Code ได้อย่างรวดเร็วและปลอดภัย")
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
            text = "ZipQR เวอร์ชั่น 3.0 (Build 3)",
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}
