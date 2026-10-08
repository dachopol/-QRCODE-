package com.aistudio.qrgenerator.kmpzqr

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.fetchSemanticsNodes
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aistudio.qrgenerator.kmpzqr.util.LocalizationManager
import java.io.File
import java.io.FileOutputStream
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VisualMatrixInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val originalLanguage by lazy {
        LocalizationManager.currentLanguage.value.code
    }

    @After
    fun restoreLanguage() {
        composeRule.runOnIdle {
            LocalizationManager.setLanguageByCode(originalLanguage)
        }
    }

    @Test
    fun captureThaiEnglishDeterministicPageMatrix() {
        waitForMainNavigation()
        val outputDir = requireNotNull(
            InstrumentationRegistry.getInstrumentation()
                .targetContext
                .getExternalFilesDir("visual")
        )
        outputDir.deleteRecursively()
        assertTrue(outputDir.mkdirs() || outputDir.isDirectory)

        listOf("th", "en").forEach { locale ->
            composeRule.runOnIdle {
                LocalizationManager.setLanguageByCode(locale)
            }
            composeRule.waitForIdle()
            dismissRootWarningIfPresent()

            open("nav_generate")
            open("generator_tab_0")
            capture(outputDir, locale + "_01_generate_promptpay")

            (1..4).forEach { index ->
                dismissRootWarningIfPresent()
                open("generator_tab_" + index)
                val name = when (index) {
                    1 -> "wifi"
                    2 -> "store"
                    3 -> "text"
                    else -> "location"
                }
                capture(outputDir, locale + "_0" + (index + 1) + "_generate_" + name)
            }

            dismissRootWarningIfPresent()
            open("nav_card")
            capture(outputDir, locale + "_06_business_card")

            dismissRootWarningIfPresent()
            open("nav_history")
            capture(outputDir, locale + "_07_history")

            dismissRootWarningIfPresent()
            open("open_language_currency_button")
            composeRule.onNodeWithTag("language_currency_dialog").assertExists()
            capture(outputDir, locale + "_08_language_currency_dialog")
            open("close_lang_currency_dialog_button")

            dismissRootWarningIfPresent()
            open("nav_scanner")
            composeRule.onNodeWithTag("scanner_screen").assertExists()
            capture(outputDir, locale + "_09_scanner")
        }

        val pngs = outputDir.listFiles { file -> file.extension == "png" }.orEmpty()
        assertTrue("Expected 18 visual evidence PNGs, found " + pngs.size, pngs.size >= 18)
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

    private fun capture(outputDir: File, name: String) {
        dismissRootWarningIfPresent()
        composeRule.waitForIdle()
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        val file = File(outputDir, name + ".png")
        FileOutputStream(file).use { stream ->
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream))
        }
        assertTrue(file.isFile && file.length() > 0L)
    }
}
