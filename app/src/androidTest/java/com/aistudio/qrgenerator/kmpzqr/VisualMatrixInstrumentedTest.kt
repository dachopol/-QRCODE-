package com.aistudio.qrgenerator.kmpzqr

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import android.view.inputmethod.InputMethodManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aistudio.qrgenerator.kmpzqr.data.AppDatabase
import com.aistudio.qrgenerator.kmpzqr.data.QrItemEntity
import com.aistudio.qrgenerator.kmpzqr.util.LocalizationManager
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VisualMatrixInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val screenshotDigestOwners = mutableMapOf<String, String>()

    private val originalLanguage by lazy {
        LocalizationManager.currentLanguage.value.code
    }

    private val appContext by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
    }

    @After
    fun restoreState() {
        resetVisualHistory(populated = false)
        composeRule.runOnIdle {
            LocalizationManager.setLanguageByCode(originalLanguage)
        }
    }

    @Test
    fun captureThaiEnglishDeterministicFifteenScreenMatrix() {
        waitForMainNavigation()
        val outputDir = requireNotNull(
            InstrumentationRegistry.getInstrumentation()
                .targetContext
                .getExternalFilesDir("visual")
        )
        outputDir.deleteRecursively()
        assertTrue(outputDir.mkdirs() || outputDir.isDirectory)

        listOf("th", "en").forEach { locale ->
            resetVisualHistory(populated = false)
            composeRule.runOnIdle {
                LocalizationManager.setLanguageByCode(locale)
            }
            composeRule.waitForIdle()
            dismissRootWarningIfPresent()

            // 01-05: generator categories.
            open("nav_generate")
            openGeneratorTab(0)
            capture(outputDir, locale + "_01_generate_promptpay")

            (1..4).forEach { index ->
                dismissRootWarningIfPresent()
                openGeneratorTab(index)
                val name = when (index) {
                    1 -> "wifi"
                    2 -> "store"
                    3 -> "text"
                    else -> "location"
                }
                if (index == 3) {
                    clearTextField("generic_text_input")
                    hideKeyboard()
                }
                capture(outputDir, locale + "_0" + (index + 1) + "_generate_" + name)
            }

            // 06: business card baseline. Clear values populated by the previous locale.
            dismissRootWarningIfPresent()
            open("nav_card")
            listOf(
                "card_business_input",
                "card_name_input",
                "card_profession_input",
                "card_services_input"
            ).forEach(::clearTextField)
            hideKeyboard()
            // Re-enter so the baseline capture starts at the top after scrolling through the fields.
            open("nav_history")
            open("nav_card")
            waitUntilDisplayed("digital_card_preview")
            waitForScreenshotFrame()
            capture(outputDir, locale + "_06_business_card")

            // 07: deterministic empty history.
            dismissRootWarningIfPresent()
            open("nav_history")
            capture(outputDir, locale + "_07_history_empty")

            // 08: language/currency dialog.
            dismissRootWarningIfPresent()
            open("open_language_currency_button")
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onAllNodesWithTag("language_currency_dialog")
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
            capture(outputDir, locale + "_08_language_currency_dialog")
            open("close_lang_currency_dialog_button")

            // 09: scanner baseline.
            dismissRootWarningIfPresent()
            open("nav_scanner")
            waitUntilDisplayed("scanner_screen", timeoutMillis = 8_000)
            waitForScreenshotFrame()
            capture(outputDir, locale + "_09_scanner")

            // 10: support/report sheet through the real app flow.
            dismissRootWarningIfPresent()
            open("nav_generate")
            open("support_admin_button")
            waitUntilDisplayed("close_support_sheet_button", timeoutMillis = 8_000)
            settleAnimations()
            waitUntilDisplayed("close_support_sheet_button")
            waitForScreenshotFrame()
            capture(outputDir, locale + "_10_support_sheet")
            open("close_support_sheet_button")

            // 11: QR preview through the real generator flow.
            dismissRootWarningIfPresent()
            open("nav_generate")
            openGeneratorTab(3)
            val textInput = composeRule.onNodeWithTag("generic_text_input")
            textInput.performTextClearance()
            textInput.performTextInput("QuickQR Visual QA")
            open("generate_text_button")
            composeRule.waitUntil(timeoutMillis = 8_000) {
                composeRule.onAllNodesWithTag("qr_preview_dialog")
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
            capture(outputDir, locale + "_11_qr_preview")
            open("close_qr_preview_button")
            hideKeyboard()

            // 12: populated history backed by the real Room database.
            resetVisualHistory(populated = true)
            open("nav_history")
            waitUntilDisplayed("history_delete_button", timeoutMillis = 8_000)
            waitForScreenshotFrame()
            capture(outputDir, locale + "_12_history_populated")

            // 13: business-card populated state using real form interactions.
            open("nav_card")
            fillTextField("card_business_input", "QuickQR QA Shop")
            fillTextField("card_name_input", "QA User")
            fillTextField("card_profession_input", "QR workflow verification")
            fillTextField("card_services_input", "Deterministic visual QA fixture")
            hideKeyboard()
            // Re-enter the real Business Card screen so its scroll state starts at the populated preview.
            open("nav_history")
            open("nav_card")
            waitUntilDisplayed("digital_card_preview")
            composeRule.onNodeWithTag("digital_card_preview").performScrollTo()
            composeRule.waitForIdle()
            waitForScreenshotFrame()
            capture(outputDir, locale + "_13_business_card_populated")

            // 14: real QR appearance/customizer state.
            open("nav_generate")
            openGeneratorTab(0)
            val appearanceAlreadyOpen = composeRule.onAllNodesWithTag("qr_color_customizer_card")
                .fetchSemanticsNodes()
                .isNotEmpty()
            if (!appearanceAlreadyOpen) {
                composeRule.onNodeWithTag("qr_appearance_toggle")
                    .performScrollTo()
                    .performClick()
                settleAnimations()
            }
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onAllNodesWithTag("qr_color_customizer_card")
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
            composeRule.onNodeWithTag("reset_qr_colors_button").performScrollTo()
            waitUntilDisplayed("reset_qr_colors_button")
            settleAnimations()
            waitForScreenshotFrame()
            capture(outputDir, locale + "_14_qr_appearance")

            // 15: real History delete-confirmation state backed by Room data.
            resetVisualHistory(populated = true)
            open("nav_history")
            waitUntilDisplayed("history_delete_button", timeoutMillis = 8_000)
            composeRule.onAllNodesWithTag("history_delete_button")[0].performClick()
            waitUntilDisplayed("history_delete_cancel_button", timeoutMillis = 5_000)
            settleAnimations()
            waitForScreenshotFrame()
            capture(outputDir, locale + "_15_history_delete_dialog")
            open("history_delete_cancel_button")
        }

        val pngs = outputDir.listFiles { file -> file.extension == "png" }.orEmpty()
        assertTrue("Expected 30 visual evidence PNGs, found " + pngs.size, pngs.size >= 30)
    }

    private fun waitForMainNavigation() {
        composeRule.waitUntil(timeoutMillis = 8_000) {
            composeRule.onAllNodesWithTag("nav_generate")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        dismissRootWarningIfPresent()
    }

    private fun dismissRootWarningIfPresent() {
        val warningVisible = composeRule.onAllNodesWithTag("root_security_warning_dialog")
            .fetchSemanticsNodes()
            .isNotEmpty()
        if (warningVisible) {
            composeRule.onNodeWithTag("continue_security_button").performClick()
            composeRule.waitForIdle()
        }
    }

    private fun open(tag: String) {
        composeRule.onNodeWithTag(tag).performClick()
        composeRule.waitForIdle()
    }

    private fun waitUntilDisplayed(tag: String, timeoutMillis: Long = 5_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            runCatching {
                composeRule.onNodeWithTag(tag).assertIsDisplayed()
            }.isSuccess
        }
    }

    private fun waitForScreenshotFrame() {
        // Compose semantics can become visible before the emulator framebuffer is committed.
        // Wait for the UI thread to drain, then allow the compositor to present the modal frame
        // before UiAutomation.takeScreenshot() reads the display.
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        android.os.SystemClock.sleep(350L)
    }

    private fun clearTextField(tag: String) {
        val field = composeRule.onNodeWithTag(tag)
        field.performScrollTo()
        field.performTextClearance()
        composeRule.waitForIdle()
    }

    private fun fillTextField(tag: String, text: String) {
        val field = composeRule.onNodeWithTag(tag)
        field.performScrollTo()
        field.performTextClearance()
        field.performTextInput(text)
        composeRule.waitForIdle()
    }

    private fun openGeneratorTab(index: Int) {
        val expectedContentTag = when (index) {
            0 -> "promptpay_target_input"
            1 -> "wifi_ssid_input"
            2 -> "store_link_input"
            3 -> "generic_text_input"
            4 -> "location_permission_or_refresh_button"
            else -> error("Unsupported generator tab index: " + index)
        }

        composeRule.onNodeWithTag("generator_tab_" + index)
            .performScrollTo()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(expectedContentTag)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.waitForIdle()
    }

    private fun resetVisualHistory(populated: Boolean) {
        val dao = AppDatabase.getDatabase(appContext).qrDao()
        runBlocking {
            dao.clearHistory(isScan = false)
            dao.clearHistory(isScan = true)
            if (populated) {
                dao.insertQrItem(
                    QrItemEntity(
                        type = "TEXT",
                        title = "Visual QA item",
                        subtitle = "Runtime Room fixture",
                        rawContent = "QuickQR visual QA payload",
                        timestamp = 1_000L,
                        isScan = false
                    )
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun settleAnimations() {
        composeRule.mainClock.advanceTimeBy(800L)
        composeRule.waitForIdle()
    }

    private fun hideKeyboard() {
        composeRule.runOnUiThread {
            val focus = composeRule.activity.currentFocus ?: return@runOnUiThread
            val inputMethodManager = composeRule.activity
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(focus.windowToken, 0)
            focus.clearFocus()
        }
        composeRule.waitForIdle()
    }

    private fun assertScreenshotHasVisibleContent(bitmap: Bitmap, name: String) {
        val stepX = maxOf(1, bitmap.width / 32)
        val stepY = maxOf(1, bitmap.height / 32)
        var sampled = 0
        var visible = 0
        var y = 0
        while (y < bitmap.height) {
            var x = 0
            while (x < bitmap.width) {
                val pixel = bitmap.getPixel(x, y)
                val brightness =
                    android.graphics.Color.red(pixel) +
                    android.graphics.Color.green(pixel) +
                    android.graphics.Color.blue(pixel)
                sampled += 1
                if (brightness > 45) {
                    visible += 1
                }
                x += stepX
            }
            y += stepY
        }
        val visibleRatio = if (sampled == 0) 0.0 else visible.toDouble() / sampled.toDouble()
        assertTrue(
            "$name screenshot appears blank/black: visibleRatio=$visibleRatio",
            visibleRatio >= 0.05
        )
    }

    private fun capture(outputDir: File, name: String) {
        dismissRootWarningIfPresent()
        composeRule.waitForIdle()
        val bitmap = requireNotNull(
            InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        ) { "UiAutomation screenshot unavailable" }
        assertScreenshotHasVisibleContent(bitmap, name)
        val file = File(outputDir, name + ".png")
        FileOutputStream(file).use { stream ->
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream))
        }
        assertTrue(file.isFile && file.length() > 0L)

        val digest = MessageDigest.getInstance("SHA-256")
            .digest(file.readBytes())
            .joinToString("") { byte -> "%02x".format(byte) }
        val duplicateOwner = screenshotDigestOwners.putIfAbsent(digest, name)
        assertTrue(
            "$name duplicates visual evidence screenshot $duplicateOwner",
            duplicateOwner == null
        )

        // Persist the emulator's actual UI capture beyond test-app cleanup.
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/QuickQR-Visual-QA")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
        val uri = requireNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
        try {
            requireNotNull(resolver.openOutputStream(uri)).use { stream ->
                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream))
            }
            resolver.update(uri, ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }, null, null)
        } catch (error: Exception) {
            resolver.delete(uri, null, null)
            throw error
        }
    }
}
