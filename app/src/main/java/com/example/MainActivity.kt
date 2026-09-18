package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admob.AdMobBannerView
import com.example.admob.AdMobInterstitialDialog
import com.example.admob.AdMobManager
import com.example.admob.AdStatsDialog
import com.example.data.WalletManager
import com.example.ui.components.LanguageAndCurrencyDialog
import com.example.ui.components.QrPreviewDialog
import com.example.ui.components.RootSecurityWarningDialog
import com.example.ui.components.ScanResultBottomSheet
import com.example.ui.components.SupportAndBugReportBottomSheet
import com.example.ui.components.TopUpDialog
import com.example.ui.screens.BusinessCardStudioScreen
import com.example.ui.screens.GeneratorScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.CurrencyManager
import com.example.util.LocalizationManager
import com.example.util.RootSecurityManager
import com.example.util.localizedString

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize persistent managers
        LocalizationManager.initialize(this)
        CurrencyManager.initialize(this)
        WalletManager.initialize(this)

        // Anti-Root Security: Initial inspection on startup
        RootSecurityManager.verifyDeviceIntegrity(this)

        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Anti-Root Security: Continuous verification whenever app returns to foreground
        RootSecurityManager.verifyDeviceIntegrity(this)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val activePreview by viewModel.activePreview.collectAsState()
    val activeScanResult by viewModel.activeScanResult.collectAsState()
    val showTopUpDialog by viewModel.showTopUpDialog.collectAsState()
    val showAdStats by viewModel.showAdStats.collectAsState()
    val showSupportSheet by viewModel.showSupportSheet.collectAsState()
    val showLanguageAndCurrencyDialog by viewModel.showLanguageAndCurrencyDialog.collectAsState()
    val languageCurrencyInitialTab by viewModel.languageCurrencyInitialTab.collectAsState()

    val walletState by WalletManager.walletState.collectAsState()
    val activeInterstitial by AdMobManager.activeInterstitial.collectAsState()
    val currentLanguage by LocalizationManager.currentLanguage.collectAsState()
    val currentCurrency by CurrencyManager.selectedCurrency.collectAsState()
    val rootCheckResult by RootSecurityManager.rootState.collectAsState()
    val isRootBypassed by RootSecurityManager.isBypassedForTesting.collectAsState()

    val navGenerateLabel = localizedString("nav_generate")
    val navCardLabel = localizedString("nav_card")
    val navScannerLabel = localizedString("nav_scanner")
    val navHistoryLabel = localizedString("nav_history")

    val navItems = listOf(
        Triple(navGenerateLabel, Icons.Default.QrCode, "nav_generate"),
        Triple(navCardLabel, Icons.Default.Badge, "nav_card"),
        Triple(navScannerLabel, Icons.Default.QrCodeScanner, "nav_scanner"),
        Triple(navHistoryLabel, Icons.Default.History, "nav_history")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                        listOf(Color(0xFF0B2853), Color(0xFF0284C7))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "App Logo",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = localizedString("app_title"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = localizedString("app_subtitle"),
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                },
                actions = {
                    // Language & Google Currency Selector Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEFF6FF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD)),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clickable { viewModel.openLanguageAndCurrencyDialog(0) }
                            .testTag("open_language_currency_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${currentLanguage.flagEmoji} ${currentLanguage.code.uppercase()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "•",
                                fontSize = 9.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${currentCurrency.symbol} ${currentCurrency.code}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669)
                            )
                        }
                    }

                    // Contact Admin & Bug Report Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clickable { viewModel.openSupportSheet() }
                            .testTag("support_admin_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ContactSupport,
                                contentDescription = localizedString("contact_admin"),
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = localizedString("contact_admin"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }

                    // Wallet & Quota Top-up Opener Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (walletState.isUnlimitedVip) Color(0xFF059669).copy(alpha = 0.12f)
                        else if (walletState.totalAvailableUses > 0) Color(0xFF0284C7).copy(alpha = 0.12f)
                        else Color(0xFFEF4444).copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (walletState.isUnlimitedVip) Color(0xFF059669).copy(alpha = 0.3f)
                            else if (walletState.totalAvailableUses > 0) Color(0xFF0284C7).copy(alpha = 0.3f)
                            else Color(0xFFEF4444).copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable { viewModel.openTopUpDialog() }
                            .testTag("wallet_topup_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (walletState.isUnlimitedVip) Icons.Default.Star else Icons.Default.AccountBalanceWallet,
                                contentDescription = "เติมเงิน/กระเป๋าเงิน",
                                tint = if (walletState.isUnlimitedVip) Color(0xFF059669)
                                else if (walletState.totalAvailableUses > 0) Color(0xFF0284C7)
                                else Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (walletState.isUnlimitedVip) "VIP ไม่จำกัด"
                                else if (walletState.paidCredits > 0) "${walletState.paidCredits} เครดิต"
                                else if (walletState.freeUsesLeft > 0) "ฟรี ${walletState.freeUsesLeft}/3"
                                else "เติมเงิน",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (walletState.isUnlimitedVip) Color(0xFF059669)
                                else if (walletState.totalAvailableUses > 0) Color(0xFF0284C7)
                                else Color(0xFFEF4444)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            // Clean Navigation Bar - No blocking ads!
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 6.dp,
                modifier = Modifier.height(72.dp)
            ) {
                    navItems.forEachIndexed { index, (label, icon, tag) ->
                        val isSelected = currentTab == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.setTab(index) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF0B2853),
                                selectedTextColor = Color(0xFF0B2853),
                                unselectedIconColor = Color(0xFF64748B),
                                unselectedTextColor = Color(0xFF64748B),
                                indicatorColor = Color(0xFF0284C7).copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag(tag)
                        )
                    }
                }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> GeneratorScreen(viewModel = viewModel)
                1 -> BusinessCardStudioScreen(viewModel = viewModel)
                2 -> ScannerScreen(viewModel = viewModel)
                3 -> HistoryScreen(viewModel = viewModel)
            }
        }
    }

    // Active QR Preview Dialog (PromptPay Standee / QR code + Save & Share)
    activePreview?.let { preview ->
        QrPreviewDialog(
            preview = preview,
            onDismiss = { viewModel.closePreview() },
            onSaveRequested = { bitmap, title ->
                viewModel.requestSaveBitmap(bitmap, title)
            }
        )
    }

    // Active Scan Result Bottom Sheet (Parsed PromptPay, Wi-Fi, URL, vCard)
    activeScanResult?.let { result ->
        ScanResultBottomSheet(
            result = result,
            onDismiss = { viewModel.closeScanResult() }
        )
    }

    // TopUp & Subscription Dialog
    if (showTopUpDialog) {
        TopUpDialog(
            onDismiss = { viewModel.closeTopUpDialog() }
        )
    }

    // AdMob Revenue & Stats Dialog
    if (showAdStats) {
        AdStatsDialog(
            onDismiss = { viewModel.closeAdStats() }
        )
    }

    // Support and Bug Report Bottom Sheet
    if (showSupportSheet) {
        SupportAndBugReportBottomSheet(
            viewModel = viewModel,
            onDismiss = { viewModel.closeSupportSheet() }
        )
    }

    // Language and Google Supported Currency Dialog
    if (showLanguageAndCurrencyDialog) {
        LanguageAndCurrencyDialog(
            initialTab = languageCurrencyInitialTab,
            onDismiss = { viewModel.closeLanguageAndCurrencyDialog() }
        )
    }

    // 30-Second Rewarded Ad for Free Tier (Skippable after 30s)
    activeInterstitial?.let { request ->
        AdMobInterstitialDialog(
            request = request,
            onDismiss = { request.onDismiss() }
        )
    }

    // Anti-Root Security Protection Dialog (Active at all times)
    rootCheckResult?.let { result ->
        if (result.isRooted && !isRootBypassed) {
            RootSecurityWarningDialog(
                result = result,
                onDismiss = { /* dismissed via restricted mode */ }
            )
        }
    }
}
