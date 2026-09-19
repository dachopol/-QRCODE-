package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.BillingSupportResult
import com.example.data.PricingPlan
import com.example.data.SlipVerificationResult
import com.example.data.WalletManager
import com.example.util.CurrencyManager
import com.example.util.ImageExporter
import com.example.util.PromptPayGenerator
import com.example.util.QrCodeUtil
import com.example.util.localizedString
import kotlin.random.Random

@Composable
fun TopUpDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val walletState by WalletManager.walletState.collectAsState()
    val selectedCurrency by CurrencyManager.selectedCurrency.collectAsState()

    var selectedPlan by remember { mutableStateOf<PricingPlan?>(WalletManager.PLANS.first()) }
    var showPaymentStep by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }

    // Dialog state for real billing/support checks
    var statusDialogMessage by remember { mutableStateOf<String?>(null) }
    var statusDialogTitle by remember { mutableStateOf<String?>(null) }

    // Unique reference code for transaction verification
    val refCode = remember(selectedPlan) {
        "REF-${Random.nextInt(100000, 999999)}"
    }

    if (showCurrencyDialog) {
        LanguageAndCurrencyDialog(
            initialTab = 1,
            onDismiss = { showCurrencyDialog = false }
        )
    }

    if (statusDialogMessage != null) {
        AlertDialog(
            onDismissRequest = { statusDialogMessage = null },
            icon = { Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF0284C7)) },
            title = { Text(text = statusDialogTitle ?: "แจ้งเตือนระบบ", fontWeight = FontWeight.Bold) },
            text = { Text(text = statusDialogMessage ?: "") },
            confirmButton = {
                Button(onClick = { statusDialogMessage = null }) {
                    Text("ตกลง")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(refCode))
                        Toast.makeText(context, "คัดลอกรหัสอ้างอิง $refCode แล้ว", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("คัดลอกรหัส REF")
                }
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("topup_subscription_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
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
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF0B2853), Color(0xFF0284C7))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ระบบเติมเงิน & สมาชิก",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "ทดสอบ v8.0",
                                        color = Color(0xFF047857),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "โหมดทดสอบ (Sandbox) • จำลองการชำระเงินและรับสิทธิ์ทันที",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "ปิด", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Current Wallet & Quota Status Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ยอดเงินในกระเป๋า (Wallet)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "฿${String.format("%,.2f", walletState.walletBalance)}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0B2853)
                                )
                            }

                            // Quota Chip
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (walletState.isUnlimitedVip) Color(0xFF059669).copy(alpha = 0.15f)
                                else if (walletState.totalAvailableUses > 0) Color(0xFF0284C7).copy(alpha = 0.15f)
                                else Color(0xFFEF4444).copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (walletState.isUnlimitedVip) Icons.Default.Star else Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = if (walletState.isUnlimitedVip) Color(0xFF059669)
                                        else if (walletState.totalAvailableUses > 0) Color(0xFF0284C7)
                                        else Color(0xFFEF4444),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = walletState.displayStatusText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (walletState.isUnlimitedVip) Color(0xFF059669)
                                        else if (walletState.totalAvailableUses > 0) Color(0xFF0284C7)
                                        else Color(0xFFEF4444)
                                    )
                                }
                            }
                        }

                        // Sandbox Fast Test Actions
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (walletState.isUnlimitedVip) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (walletState.isUnlimitedVip) Color(0xFF86EFAC) else Color(0xFFFDE68A)),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minWidth = 72.dp)
                                    .clickable {
                                        val newState = !walletState.isUnlimitedVip
                                        WalletManager.setPremiumMode(newState)
                                        Toast.makeText(context, if (newState) "⭐ เปิดโหมด Premium VIP แล้ว" else "🔄 สลับกลับสู่โหมดทดลองใช้ฟรี (Trial)", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 7.dp, horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (walletState.isUnlimitedVip) Color(0xFF059669) else Color(0xFFD97706),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (walletState.isUnlimitedVip) "Premium: ON" else "Premium: OFF",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (walletState.isUnlimitedVip) Color(0xFF059669) else Color(0xFFB45309),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE0F2FE),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD)),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minWidth = 72.dp)
                                    .clickable {
                                        WalletManager.addTestCredits(50)
                                        Toast.makeText(context, "⚡ เพิ่มเครดิตทดสอบ +50 ครั้ง สำเร็จแล้ว!", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 7.dp, horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "+50 เครดิต",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0369A1),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF1F5F9),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier
                                    .defaultMinSize(minWidth = 56.dp)
                                    .clickable {
                                        WalletManager.resetTestQuota()
                                        Toast.makeText(context, "🔄 รีเซ็ตสิทธิ์เริ่มต้น (ฟรี 3 ครั้ง) แล้ว", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 7.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "รีเซ็ต",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF475569),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!showPaymentStep) {
                    // STEP 1: Select Plan Header with Currency Switcher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = localizedString("topup_dialog_title"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1E293B)
                        )

                        // Currency Switcher Pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFECFDF5),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)),
                            modifier = Modifier
                                .clickable { showCurrencyDialog = true }
                                .testTag("topup_currency_selector_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${selectedCurrency.flagEmoji} ${selectedCurrency.code}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "▾",
                                    fontSize = 10.sp,
                                    color = Color(0xFF065F46)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    WalletManager.PLANS.forEach { plan ->
                        val isSelected = selectedPlan?.id == plan.id
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) Color(0xFFF0F9FF) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF0284C7) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedPlan = plan }
                                .testTag("plan_item_${plan.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .border(
                                                2.dp,
                                                if (isSelected) Color(0xFF0284C7) else Color(0xFFCBD5E1),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF0284C7))
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = plan.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF0F172A),
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            plan.badge?.let { badge ->
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color(0xFFEF4444)
                                                ) {
                                                    Text(
                                                        text = badge,
                                                        color = Color.White,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        softWrap = false,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = plan.subtitle,
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = selectedCurrency.format(plan.priceThb),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0284C7),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    if (selectedCurrency.code != "THB") {
                                        Text(
                                            text = "(~฿${plan.priceThb.toInt()})",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                    plan.originalPriceThb?.let { orig ->
                                        Text(
                                            text = selectedCurrency.format(orig),
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8),
                                            style = androidx.compose.ui.text.TextStyle(
                                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Primary Sandbox Action: Instant Payment Simulation
                    Button(
                        onClick = {
                            val plan = selectedPlan
                            if (plan != null) {
                                WalletManager.activatePlan(plan)
                                Toast.makeText(context, "🎉 [โหมดทดสอบ] เปิดใช้งาน ${plan.title} เรียบร้อยแล้ว!", Toast.LENGTH_LONG).show()
                                onDismiss()
                            }
                        },
                        enabled = selectedPlan != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("simulate_instant_payment_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⚡ จำลองการชำระเงินสำเร็จ (เปิดใช้งานทันที)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Secondary Action: Test PromptPay Flow
                    OutlinedButton(
                        onClick = { showPaymentStep = true },
                        enabled = selectedPlan != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("continue_to_payment_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0B2853))
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF0B2853))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "📱 แสดง QR พร้อมเพย์ (ทดสอบขั้นตอนสแกนจ่าย)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0B2853)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Google Play Billing Compatibility Check
                    OutlinedButton(
                        onClick = {
                            val support = WalletManager.checkGooglePlayBillingSupport(context)
                            when (support) {
                                is BillingSupportResult.Supported -> {
                                    statusDialogTitle = "Google Play Billing (Sandbox)"
                                    statusDialogMessage = "เชื่อมต่อระบบ Google Play สำเร็จ กำลังเริ่มกระบวนการชำระเงิน"
                                }
                                is BillingSupportResult.NotSupported -> {
                                    statusDialogTitle = "สถานะการรองรับ Google Play Billing"
                                    statusDialogMessage = support.reason
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("google_play_billing_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Google Play In-App Purchase (ตรวจสอบระบบ)", fontSize = 12.sp, color = Color(0xFF475569))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Option to watch 30s ad to use for free
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            com.example.admob.AdMobManager.show30sFreeAd(triggerReason = "รับสิทธิ์ใช้งานฟรี 1 ครั้ง") {
                                WalletManager.addFreeUse(1)
                                Toast.makeText(context, "🎉 คุณได้รับสิทธิ์ใช้งานฟรี 1 ครั้งเรียบร้อยแล้ว!", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("watch_30s_ad_for_free_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF10B981)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF059669))
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🎬 ดูโฆษณาเพื่อรับสิทธิ์ฟรี (+1 ครั้ง)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // STEP 2: Real PromptPay Payment Screen
                    val plan = selectedPlan!!
                    val promptPayTarget = "0812345678" // Official Merchant ID
                    val payload = remember(plan) {
                        PromptPayGenerator.generatePayload(promptPayTarget, plan.priceThb)
                    }
                    val qrBitmap = remember(payload) {
                        QrCodeUtil.generateQrBitmap(
                            content = payload,
                            size = 600,
                            darkColor = Color(0xFF0B2853).toArgb(),
                            lightColor = Color.White.toArgb()
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF0284C7).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Thai QR Payment Banner
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0B2853))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "THAI QR PAYMENT • สแกนจ่ายพร้อมเพย์ (โหมดทดสอบ)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = plan.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "ยอดชำระ: ${selectedCurrency.format(plan.priceThb)}" + if (selectedCurrency.code != "THB") " (฿${String.format("%,.2f", plan.priceThb)})" else "",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color(0xFF059669)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Reference Code Box
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "รหัสอ้างอิง: $refCode",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "คัดลอก",
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString(refCode))
                                            Toast.makeText(context, "คัดลอกรหัสอ้างอิงแล้ว", Toast.LENGTH_SHORT).show()
                                        }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Display QR
                            if (qrBitmap != null) {
                                Box(
                                    modifier = Modifier
                                        .size(200.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        bitmap = qrBitmap.asImageBitmap(),
                                        contentDescription = "PromptPay QR Code",
                                        modifier = Modifier.size(180.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "เปิดแอปธนาคารใดก็ได้ สแกน QR โค้ดนี้เพื่อชำระเงิน",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Save QR to phone
                            OutlinedButton(
                                onClick = {
                                    if (qrBitmap != null) {
                                        ImageExporter.saveBitmapToGallery(context, qrBitmap, "PromptPay_TopUp_${plan.priceThb}")
                                        Toast.makeText(context, "บันทึก QR ลงเครื่องแล้ว เปิดแอปธนาคารเพื่อสแกนได้ทันที", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("บันทึกภาพ QR สแกนจ่าย", fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Test Slip Verification / Instant Activation
                    Button(
                        onClick = {
                            val result = WalletManager.verifySlipOnline(refCode)
                            when (result) {
                                is SlipVerificationResult.Success -> {
                                    WalletManager.activatePlan(plan)
                                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                    onDismiss()
                                }
                                is SlipVerificationResult.Unsupported -> {
                                    statusDialogTitle = "ผลการตรวจสอบการชำระเงิน"
                                    statusDialogMessage = result.reason
                                }
                                is SlipVerificationResult.Failed -> {
                                    statusDialogTitle = "ข้อผิดพลาด"
                                    statusDialogMessage = result.error
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("verify_slip_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⚡ ยืนยันสลิป / อนุมัติสิทธิ์ทันที (โหมดทดสอบ)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Email Support Action
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:${WalletManager.SUPPORT_EMAIL}")
                                    putExtra(Intent.EXTRA_SUBJECT, "[ZipQR] แจ้งหลักฐานการชำระเงิน $refCode (${plan.title})")
                                    putExtra(Intent.EXTRA_TEXT, "แนบสลิปการโอนเงิน\nแพ็กเกจ: ${plan.title}\nยอดเงิน: ฿${plan.priceThb}\nรหัสอ้างอิง: $refCode")
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                clipboardManager.setText(AnnotatedString(WalletManager.SUPPORT_EMAIL))
                                Toast.makeText(context, "คัดลอกอีเมล ${WalletManager.SUPPORT_EMAIL} แล้ว", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF0B2853))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ส่งสลิปไปที่ ${WalletManager.SUPPORT_EMAIL}", fontSize = 12.sp, color = Color(0xFF0B2853))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showPaymentStep = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ย้อนกลับไปเลือกแพ็กเกจอื่น", fontSize = 13.sp, color = Color(0xFF64748B))
                    }
                }
            }
        }
    }
}
