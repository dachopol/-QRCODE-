package com.aistudio.qrgenerator.kmpzqr

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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import com.aistudio.qrgenerator.kmpzqr.ui.components.LanguageAndCurrencyDialog
import com.aistudio.qrgenerator.kmpzqr.ui.components.QrPreviewDialog
import com.aistudio.qrgenerator.kmpzqr.ui.components.RootSecurityWarningDialog
import com.aistudio.qrgenerator.kmpzqr.ui.components.ScanResultBottomSheet
import com.aistudio.qrgenerator.kmpzqr.ui.components.SupportAndBugReportBottomSheet
import com.aistudio.qrgenerator.kmpzqr.ui.screens.BusinessCardStudioScreen
import com.aistudio.qrgenerator.kmpzqr.ui.screens.GeneratorScreen
import com.aistudio.qrgenerator.kmpzqr.ui.screens.HistoryScreen
import com.aistudio.qrgenerator.kmpzqr.ui.screens.ScannerScreen
import com.aistudio.qrgenerator.kmpzqr.ui.theme.AppPillShape
import com.aistudio.qrgenerator.kmpzqr.ui.theme.GlassAccent
import com.aistudio.qrgenerator.kmpzqr.ui.theme.GlassBorder
import com.aistudio.qrgenerator.kmpzqr.ui.theme.MyApplicationTheme
import com.aistudio.qrgenerator.kmpzqr.util.CurrencyManager
import com.aistudio.qrgenerator.kmpzqr.util.LocalizationManager
import com.aistudio.qrgenerator.kmpzqr.util.RootSecurityManager
import com.aistudio.qrgenerator.kmpzqr.util.localizedString

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize persistent managers
        LocalizationManager.initialize(this)
        CurrencyManager.initialize(this)
// Anti-Root Security: Initial inspection on startup (Async to avoid black screen)
        RootSecurityManager.verifyDeviceIntegrityAsync(this, lifecycleScope)

        setContent {
            MyApplicationTheme {
                var showSplash by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    delay(900)
                    showSplash = false
                }

                if (showSplash) {
                    QuickQrSplashScreen()
                } else {
                    MainAppScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Anti-Root Security: Continuous verification whenever app returns to foreground
        RootSecurityManager.verifyDeviceIntegrityAsync(this, lifecycleScope)
    }
}

@Composable
private fun QuickQrSplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0B2853), Color(0xFF0284C7))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = "QuickQR Business icon",
                    modifier = Modifier.size(88.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "QuickQR Business",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "by AnakinYoo",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val activePreview by viewModel.activePreview.collectAsState()
    val activeScanResult by viewModel.activeScanResult.collectAsState()
    val showSupportSheet by viewModel.showSupportSheet.collectAsState()
    val showLanguageAndCurrencyDialog by viewModel.showLanguageAndCurrencyDialog.collectAsState()
    val languageCurrencyInitialTab by viewModel.languageCurrencyInitialTab.collectAsState()

    val currentLanguage by LocalizationManager.currentLanguage.collectAsState()
    val currentCurrency by CurrencyManager.selectedCurrency.collectAsState()
    val rootCheckResult by RootSecurityManager.rootState.collectAsState()
    val isRootWarningAcknowledged by RootSecurityManager.isWarningAcknowledged.collectAsState()

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
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = localizedString("app_title"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Surface(
                            shape = AppPillShape,
                            color = Color(0xFF0284C7).copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color(0xFF0284C7).copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = "v${BuildConfig.VERSION_NAME}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Surface(
                        shape = AppPillShape,
                        color = GlassAccent,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier
                            .padding(start = 6.dp, end = 4.dp)
                            .wrapContentWidth()
                            .clickable { viewModel.openLanguageAndCurrencyDialog(0) }
                            .testTag("open_language_currency_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentLanguage.code.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1),
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "•",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${currentCurrency.flagEmoji} ${currentCurrency.code}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.openSupportSheet() },
                        modifier = Modifier.testTag("support_admin_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ContactSupport,
                            contentDescription = localizedString("contact_admin"),
                            tint = Color(0xFF0284C7)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.navigationBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 72.dp)
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navItems.forEachIndexed { index, (label, icon, tag) ->
                        val isSelected = currentTab == index
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(AppPillShape)
                                .background(if (isSelected) GlassAccent else Color.Transparent)
                                .clickable { viewModel.setTab(index) }
                                .defaultMinSize(minHeight = 60.dp)
                                .padding(horizontal = 4.dp, vertical = 6.dp)
                                .testTag(tag),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) Color(0xFF0284C7) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(if (isSelected) 24.dp else 22.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                fontSize = if (isSelected) 12.sp else 11.sp,
                                lineHeight = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) Color(0xFF0369A1) else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                        }
                    }
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

    // Anti-Root Security Protection Dialog (Active at all times)
    rootCheckResult?.let { result ->
        if (result.isRooted && !isRootWarningAcknowledged) {
            RootSecurityWarningDialog(
                result = result,
                onDismiss = { RootSecurityManager.acknowledgeWarning() }
            )
        }
    }
}
