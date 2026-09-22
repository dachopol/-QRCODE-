package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.WalletManager
import com.example.model.StoreLinkModel
import com.example.model.StorePlatform
import com.example.model.WifiSecurity
import com.example.ui.components.QrColorCustomizerCard
import com.example.ui.theme.appTextFieldColors
import com.example.util.CurrencyManager
import com.example.util.localizedText
import com.example.util.LocalizationManager
import com.example.util.localizedString

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val category by viewModel.generatorCategory.collectAsState()
    val scrollState = rememberScrollState()

    val qrDarkColor by viewModel.qrForegroundColor.collectAsState()
    val qrLightColor by viewModel.qrBackgroundColor.collectAsState()
    val includeCenterLogo by viewModel.includeCenterLogo.collectAsState()
    val walletState by WalletManager.walletState.collectAsState()

    val catPromptpay = localizedString("cat_promptpay")
    val catWifi = localizedString("cat_wifi")
    val catStore = localizedString("cat_store")
    val catText = localizedString("cat_text")

    val categories = listOf(
        Pair(catPromptpay, Icons.Default.Payments),
        Pair(catWifi, Icons.Default.Wifi),
        Pair(catStore, Icons.Default.Store),
        Pair(catText, Icons.Default.TextFields)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Category Selector TabRow
        ScrollableTabRow(
            selectedTabIndex = category,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[category]),
                    height = 3.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            categories.forEachIndexed { index, pair ->
                Tab(
                    selected = category == index,
                    onClick = { viewModel.setGeneratorCategory(index) },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .defaultMinSize(minWidth = 104.dp)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = pair.second,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = pair.first,
                                fontWeight = if (category == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    },
                    modifier = Modifier.testTag("generator_tab_$index")
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Color customization controls for QR code
            QrColorCustomizerCard(
                darkColor = qrDarkColor,
                lightColor = qrLightColor,
                onDarkColorChange = { viewModel.setQrForegroundColor(it) },
                onLightColorChange = { viewModel.setQrBackgroundColor(it) },
                includeCenterLogo = includeCenterLogo,
                onIncludeCenterLogoChange = { viewModel.setIncludeCenterLogo(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (category) {
                0 -> PromptPayForm(viewModel)
                1 -> WifiForm(viewModel)
                2 -> StoreLinkForm(viewModel)
                3 -> TextForm(viewModel)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PromptPayForm(viewModel: MainViewModel) {
    val target by viewModel.promptPayTarget.collectAsState()
    val amount by viewModel.promptPayAmount.collectAsState()
    val shopName by viewModel.promptPayShopName.collectAsState()
    val selectedCurrency by CurrencyManager.selectedCurrency.collectAsState()

    val presetAmounts = listOf("20", "50", "100", "150", "200", "300", "500", "1000")

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // PromptPay Branding Banner
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0B2853))
                    .padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = localizedText("THAI QR PAYMENT • พร้อมเพย์", "THAI QR PAYMENT • PromptPay"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = localizedText("มาตรฐานธนาคารแห่งประเทศไทย (BOT EMVCo)", "Bank of Thailand standard (BOT EMVCo)"),
                        color = Color(0xFF93C5FD),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Phone / ID Card Input
            Text(
                text = localizedText("เบอร์มือถือ หรือ เลขบัตรประชาชน 13 หลัก", "Phone number or 13-digit ID"),
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = target,
                onValueChange = { viewModel.setPromptPayTarget(it) },
                placeholder = { Text(localizedText("เช่น 0812345678 หรือ 1234567890123", "e.g. 0812345678 or 1234567890123"), color = Color(0xFF94A3B8)) },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF0284C7))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("promptpay_target_input"),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Medium),
                colors = appTextFieldColors(
                    focusedBorderColor = Color(0xFF0B2853),
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Amount Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = localizedText("ระบุยอดเงิน (บาท) *ไม่กรอกก็ได้", "Amount (THB) — optional"),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color(0xFF1E293B)
                )
                if (amount.isNotBlank()) {
                    Text(
                        text = localizedText("ล้างยอดเงิน", "Clear amount"),
                        fontSize = 12.sp,
                        color = Color(0xFFEF4444),
                        modifier = Modifier
                            .clickable { viewModel.setPromptPayAmount("") }
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { viewModel.setPromptPayAmount(it) },
                placeholder = { Text(localizedText("0.00 (เว้นว่างไว้หากให้ลูกค้ากรอกเอง)", "0.00 (leave blank for payer to enter amount)"), color = Color(0xFF94A3B8)) },
                leadingIcon = {
                    Text("฿", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF059669))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("promptpay_amount_input"),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                colors = appTextFieldColors(
                    focusedBorderColor = Color(0xFF059669),
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            // Live Currency Conversion Hint
            val parsedThb = amount.toDoubleOrNull()
            if (parsedThb != null && parsedThb > 0 && selectedCurrency.code != "THB") {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFECFDF5),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Text(
                        text = "≈ ${selectedCurrency.format(parsedThb)} ${selectedCurrency.code} (${localizedText(selectedCurrency.nameTh, selectedCurrency.nameEn)})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF065F46),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Preset Amount Chips
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presetAmounts.forEach { preset ->
                    FilterChip(
                        selected = amount == preset,
                        onClick = { viewModel.setPromptPayAmount(preset) },
                        label = {
                            Text(
                                text = "฿$preset",
                                fontSize = 12.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        modifier = Modifier.wrapContentWidth(),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF059669),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Shop / Contractor Name for Standee
            Text(
                text = localizedText("ชื่อร้านค้า / ช่างรับเหมา / ฟรีแลนซ์", "Shop / Contractor / Freelancer"),
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = shopName,
                onValueChange = { viewModel.setPromptPayShopName(it) },
                placeholder = { Text(localizedText("เช่น ร้านส้มตำเจ๊น้อย, ช่างกานต์การช่าง", "e.g. My Shop, Somchai Service"), color = Color(0xFF94A3B8)) },
                leadingIcon = {
                    Icon(Icons.Default.Store, contentDescription = null, tint = Color(0xFF64748B))
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("promptpay_shop_input"),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(color = Color.Black, fontSize = 15.sp),
                colors = appTextFieldColors()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { viewModel.generatePromptPay() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("generate_promptpay_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B2853))
            ) {
                Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = localizedText("สร้าง QR พร้อมเพย์ทันที", "Generate PromptPay QR"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun WifiForm(viewModel: MainViewModel) {
    val ssid by viewModel.wifiSsid.collectAsState()
    val password by viewModel.wifiPassword.collectAsState()
    val security by viewModel.wifiSecurity.collectAsState()
    val isHidden by viewModel.wifiHidden.collectAsState()
    var showPassword by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = localizedText("สร้าง QR เข้า Wi-Fi อัตโนมัติ", "Generate Wi-Fi QR"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = localizedText("ลูกค้าและผู้มาติดต่อสแกนแล้วเชื่อมต่อเน็ตได้ทันที ไม่ต้องบอกรหัสผ่าน", "Let guests scan to connect without typing the password"),
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // SSID
            Text(localizedText("ชื่อเครือข่าย Wi-Fi (SSID)", "Wi-Fi network name (SSID)"), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = ssid,
                onValueChange = { viewModel.setWifiSsid(it) },
                placeholder = { Text("เช่น Shop-Guest-WiFi", color = Color(0xFF94A3B8)) },
                leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null, tint = Color(0xFF0284C7)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wifi_ssid_input"),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(color = Color.Black, fontSize = 15.sp),
                colors = appTextFieldColors()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Password
            if (security != WifiSecurity.OPEN) {
                Text(localizedText("รหัสผ่าน Wi-Fi", "Wi-Fi password"), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.setWifiPassword(it) },
                    placeholder = { Text("รหัสผ่าน Wi-Fi", color = Color(0xFF94A3B8)) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF0284C7)) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wifi_pass_input"),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = TextStyle(color = Color.Black, fontSize = 15.sp),
                    colors = appTextFieldColors()
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Security Type Chips
            Text(localizedText("ประเภทความปลอดภัย", "Security type"), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WifiSecurity.values().forEach { sec ->
                    FilterChip(
                        selected = security == sec,
                        onClick = { viewModel.setWifiSecurity(sec) },
                        label = {
                            Text(
                                text = sec.name,
                                fontSize = 12.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        modifier = Modifier.wrapContentWidth(),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0284C7),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hidden network switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(localizedText("เครือข่ายที่ซ่อนอยู่", "Hidden SSID"), fontSize = 13.sp, color = Color(0xFF334155))
                Switch(
                    checked = isHidden,
                    onCheckedChange = { viewModel.setWifiHidden(it) }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = { viewModel.generateWifi() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("generate_wifi_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
            ) {
                Icon(Icons.Default.Wifi, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(localizedText("สร้าง QR Wi-Fi ทันที", "Generate Wi-Fi QR"), fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StoreLinkForm(viewModel: MainViewModel) {
    val platform by viewModel.storePlatform.collectAsState()
    val storeValue by viewModel.storeValue.collectAsState()

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = localizedText("สร้าง QR ลิงก์ร้านค้าและโซเชียลมีเดีย", "Generate store and social link QR"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = localizedText("ส่งให้ลูกค้าเข้าสู่หน้าร้าน Shopee, TikTok, LINE OA หรือเพจได้ง่ายๆ", "Send customers directly to your store or social page"),
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(localizedText("เลือกแพลตฟอร์ม", "Choose platform"), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // Platform Buttons Grid
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    StorePlatform.LINE_OA,
                    StorePlatform.SHOPEE,
                    StorePlatform.TIKTOK,
                    StorePlatform.FACEBOOK,
                    StorePlatform.LAZADA,
                    StorePlatform.WEBSITE
                ).forEach { p ->
                    val isSel = platform == p
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) Color(0xFF0B2853) else Color(0xFFF1F5F9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) Color(0xFF0B2853) else Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .wrapContentWidth()
                            .clickable { viewModel.setStorePlatform(p) }
                    ) {
                        Text(
                            text = p.title,
                            color = if (isSel) Color.White else Color(0xFF334155),
                            fontSize = 12.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(localizedText("ระบุข้อมูล / ลิงก์", "Enter ID / link"), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = storeValue,
                onValueChange = { viewModel.setStoreValue(it) },
                placeholder = { Text(platform.placeholder, color = Color(0xFF94A3B8)) },
                leadingIcon = { Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFF0284C7)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("store_link_input"),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(color = Color.Black, fontSize = 15.sp),
                colors = appTextFieldColors()
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = localizedText("ลิงก์ที่จะสร้าง: ", "Link to generate: ") + StoreLinkModel(platform, storeValue).fullUrl,
                fontSize = 11.sp,
                color = Color(0xFF0369A1)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = { viewModel.generateStoreLink() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("generate_store_link_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B2853))
            ) {
                Icon(Icons.Default.QrCode, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(localizedText("สร้าง QR ลิงก์ร้านค้า", "Generate store link QR"), fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
            }
        }
    }
}

@Composable
private fun TextForm(viewModel: MainViewModel) {
    val text by viewModel.rawText.collectAsState()

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = localizedText("สร้าง QR ข้อความทั่วไป", "Generate text QR"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = localizedText("กรอกข้อความ บันทึก หรือรายละเอียดงานเพื่อแปลงเป็นคิวอาร์โค้ด", "Enter text or notes to convert into a QR code"),
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = text,
                onValueChange = { viewModel.setRawText(it) },
                placeholder = { Text(localizedText("พิมพ์ข้อความที่ต้องการสร้าง QR...", "Enter text for the QR code..."), color = Color(0xFF94A3B8)) },
                minLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("generic_text_input"),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(color = Color.Black, fontSize = 15.sp),
                colors = appTextFieldColors()
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = { viewModel.generateText() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("generate_text_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B2853))
            ) {
                Icon(Icons.Default.QrCode, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(localizedText("สร้าง QR ข้อความ", "Generate text QR"), fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
            }
        }
    }
}
