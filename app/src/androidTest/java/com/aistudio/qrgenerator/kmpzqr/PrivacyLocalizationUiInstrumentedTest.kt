package com.aistudio.qrgenerator.kmpzqr

import android.app.Application
import androidx.activity.compose.setContent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aistudio.qrgenerator.kmpzqr.data.AppDatabase
import com.aistudio.qrgenerator.kmpzqr.data.QrItemEntity
import com.aistudio.qrgenerator.kmpzqr.model.ParsedQrResult
import com.aistudio.qrgenerator.kmpzqr.model.ParsedQrType
import com.aistudio.qrgenerator.kmpzqr.ui.components.ScanResultBottomSheet
import com.aistudio.qrgenerator.kmpzqr.ui.screens.HistoryScreen
import com.aistudio.qrgenerator.kmpzqr.ui.theme.MyApplicationTheme
import com.aistudio.qrgenerator.kmpzqr.util.HistoryPrivacyUtil
import com.aistudio.qrgenerator.kmpzqr.util.LocalizationManager
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrivacyLocalizationUiInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private lateinit var application: Application
    private lateinit var database: AppDatabase
    private var originalLanguage: String = "en"

    @Before
    fun setUp() {
        application = InstrumentationRegistry.getInstrumentation()
            .targetContext.applicationContext as Application
        database = AppDatabase.getDatabase(application)
        originalLanguage = LocalizationManager.currentLanguage.value.code
        runBlocking {
            database.qrDao().clearHistory(isScan = false)
            database.qrDao().clearHistory(isScan = true)
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            database.qrDao().clearHistory(isScan = false)
            database.qrDao().clearHistory(isScan = true)
        }
        LocalizationManager.setLanguageByCode(originalLanguage)
    }

    @Test
    fun wifiHistory_runtimeUiDoesNotExposeRawPassword() {
        val secret = "synthetic-secret"
        composeRule.runOnIdle {
            LocalizationManager.setLanguageByCode("en")
        }

        runBlocking {
            database.qrDao().insertQrItem(
                QrItemEntity(
                    type = "WIFI",
                    title = "QA Wi-Fi",
                    subtitle = HistoryPrivacyUtil.wifiHistorySubtitle(
                        hasPassword = true,
                        languageCode = "en"
                    ),
                    rawContent = "WIFI:T:WPA;S:QA-NET;P:" + secret + ";;",
                    isScan = false
                )
            )
        }

        composeRule.runOnUiThread {
            val historyViewModel = MainViewModel(application)
            composeRule.activity.setContent {
                MyApplicationTheme(darkTheme = false) {
                    HistoryScreen(viewModel = historyViewModel)
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("QA Wi-Fi")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeRule.onNodeWithText("Password hidden in history").assertExists()
        assertTrue(
            "History UI must not expose the raw Wi-Fi password",
            composeRule.onAllNodesWithText(secret, substring = true)
                .fetchSemanticsNodes()
                .isEmpty()
        )
    }

    @Test
    fun scanResultActions_followSelectedLanguageAtRuntime() {
        val promptPayFixture = ParsedQrResult(
            rawText = "test-fixture",
            type = ParsedQrType.PROMPTPAY,
            title = "QA",
            subtitle = "fixture",
            promptPayId = "0812345678",
            amount = 10.0
        )

        renderScanResult(promptPayFixture, "en")
        composeRule.onNodeWithText("Copy PromptPay ID").assertExists()
        composeRule.onNodeWithText("Copy all").assertExists()
        composeRule.onNodeWithText("Share text").assertExists()

        renderScanResult(promptPayFixture, "th")
        composeRule.onNodeWithText("คัดลอกเลขบัญชีพร้อมเพย์").assertExists()
        composeRule.onNodeWithText("คัดลอกทั้งหมด").assertExists()
        composeRule.onNodeWithText("แชร์ข้อความ").assertExists()

        val wifiFixture = ParsedQrResult(
            rawText = "test-fixture",
            type = ParsedQrType.WIFI,
            title = "QA",
            subtitle = "fixture",
            wifiSsid = "QA-NET",
            wifiPass = "synthetic-secret",
            wifiSecurity = "WPA"
        )

        renderScanResult(wifiFixture, "en")
        composeRule.onNodeWithText("Wi-Fi password:").assertExists()
        composeRule.onNodeWithText("Copy").assertExists()
    }

    private fun renderScanResult(result: ParsedQrResult, languageCode: String) {
        composeRule.runOnUiThread {
            LocalizationManager.setLanguageByCode(languageCode)
            composeRule.activity.setContent {
                MyApplicationTheme(darkTheme = false) {
                    ScanResultBottomSheet(
                        result = result,
                        onDismiss = {}
                    )
                }
            }
        }
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("scan_result_sheet")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.waitForIdle()
    }
}
