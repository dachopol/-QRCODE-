package com.aistudio.qrgenerator.kmpzqr.ui.components

import com.aistudio.qrgenerator.kmpzqr.BuildConfig

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lightbulb
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import com.aistudio.qrgenerator.kmpzqr.MainViewModel
import com.aistudio.qrgenerator.kmpzqr.ui.theme.appTextFieldColors
import com.aistudio.qrgenerator.kmpzqr.util.localizedText
import com.aistudio.qrgenerator.kmpzqr.util.localizedNow

private const val ADMIN_SUPPORT_EMAIL = "215334638+AnakinYoo@users.noreply.github.com"

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
        containerColor = MaterialTheme.colorScheme.surface,
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
                            imageVector = if (selectedTab == 0) Icons.Default.BugReport else Icons.AutoMirrored.Filled.ContactSupport,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (selectedTab == 0) localizedText("แจ้งปัญหา", "Report a problem") else localizedText("ติดต่อและช่วยเหลือ", "Support"),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = localizedText("แจ้งปัญหา ติดต่อแอดมิน และวิธีใช้งาน", "Report issues, contact admin and get help"),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = localizedText("ปิด", "Close"), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs: Tab 0 = แจ้งบัค, Tab 1 = ติดต่อแอดมิน / FAQ
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
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
                                maxLines = 2
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
                                maxLines = 2
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> BugReportForm(viewModel, onSuccess = onDismiss)
                1 -> AdminContactSupportView()
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BugReportForm(
    viewModel: MainViewModel,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    var category by remember { mutableStateOf("QR พร้อมเพย์สแกนไม่ติด") }
    var description by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }
    var isShareOpened by remember { mutableStateOf(false) }

    val categories = listOf(
        "QR พร้อมเพย์สแกนไม่ติด",
        "Wi-Fi เชื่อมต่อไม่ได้",
        "นามบัตรดิจิทัลมีปัญหา",
        "สแกนเนอร์เปิดกล้องไม่ได้",
        "ข้อเสนอแนะอื่นๆ"
    )

    if (isShareOpened) {
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
                text = localizedText("เปิดเมนูแชร์รายงานแล้ว", "Report share sheet opened"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF16A34A)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = localizedText("เลือกแอปที่ต้องการใช้ส่งรายงาน", "Choose an app to send the report"),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                maxLines = 2
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
                        val reportText = buildString {
                            append("QuickQR Business v${BuildConfig.VERSION_NAME}\n")
                            append("Category: ").append(category).append("\n")
                            append("Issue: ").append(description.trim()).append("\n")
                            if (contactInfo.isNotBlank()) {
                                append("Contact: ").append(contactInfo.trim())
                            }
                        }
                        try {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "QuickQR Business issue report")
                                putExtra(Intent.EXTRA_TEXT, reportText)
                            }
                            context.startActivity(Intent.createChooser(intent, localizedNow("ส่งรายงานด้วย", "Send report with")))
                            isShareOpened = true
                        } catch (_: Exception) {
                            Toast.makeText(
                                context,
                                localizedNow("ไม่พบแอปสำหรับแชร์รายงาน", "No app available to share the report"),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                enabled = description.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("submit_bug_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(localizedText("แชร์รายงาน", "Share report"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
        // Admin email remains hidden from the UI and is opened only through the button.
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                    Text(
                        localizedText("ช่วยเหลือ", "Support"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = localizedText(
                        "แตะปุ่มด้านล่างเพื่อเขียนอีเมลถึงแอดมิน โดยไม่แสดงที่อยู่อีเมลบนหน้าจอ",
                        "Tap the button below to email the admin without showing the address on screen."
                    ),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.fromParts("mailto", ADMIN_SUPPORT_EMAIL, null)
                                putExtra(Intent.EXTRA_SUBJECT, "QuickQR Business support request")
                                putExtra(Intent.EXTRA_TEXT, "QuickQR Business v${BuildConfig.VERSION_NAME}\n")
                            }
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(
                                    context,
                                    localizedNow("ไม่พบแอปอีเมลในเครื่อง", "No email app found"),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (_: Exception) {
                            Toast.makeText(
                                context,
                                localizedNow("ไม่สามารถเปิดแอปอีเมลได้", "Unable to open the email app"),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 44.dp)
                        .testTag("email_admin_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(localizedText("ส่งอีเมลถึงแอดมิน", "Email admin"), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // FAQ Section
        Text(localizedText("คำถามและวิธีใช้งานเบื้องต้น", "FAQ"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "• $question",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = answer,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "QuickQR Business v${BuildConfig.VERSION_NAME}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}
