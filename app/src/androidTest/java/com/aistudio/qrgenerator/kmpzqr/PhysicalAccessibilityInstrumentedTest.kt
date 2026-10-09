package com.aistudio.qrgenerator.kmpzqr

import android.os.Build
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aistudio.qrgenerator.kmpzqr.util.LocalizationManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhysicalAccessibilityInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun rmx3241_largeFontAndSemanticFocusSmoke() {
        assertEquals("RMX3241", Build.MODEL)

        val requireLargeFont =
            InstrumentationRegistry.getArguments().getString("largeFont") == "true"
        if (requireLargeFont) {
            assertTrue(
                "Physical large-font gate requires fontScale >= 1.25; actual=" +
                    composeRule.activity.resources.configuration.fontScale,
                composeRule.activity.resources.configuration.fontScale >= 1.25f
            )
        }

        composeRule.runOnIdle {
            LocalizationManager.setLanguageByCode("th")
        }
        waitForTag("nav_generate", 15_000)
        dismissRootWarningIfPresent()

        listOf("nav_generate", "nav_card", "nav_scanner", "nav_history").forEach { tag ->
            composeRule.onNodeWithTag(tag)
                .assertIsDisplayed()
                .assertHasClickAction()
            assertMinTouchTarget(tag, 44f)
        }
        composeRule.onNodeWithTag("open_language_currency_button")
            .assertIsDisplayed()
            .assertHasClickAction()
        assertMinTouchTarget("open_language_currency_button", 44f)

        composeRule.onNodeWithTag("nav_history").performClick()
        waitForTag("history_screen", 8_000)

        composeRule.onNodeWithTag("open_language_currency_button").performClick()
        waitForTag("language_currency_dialog", 5_000)
        composeRule.onNodeWithTag("close_lang_currency_dialog_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("nav_generate").performClick()
        waitForTag("generator_tab_0", 8_000)

        val expectedContentTags = listOf(
            "promptpay_target_input",
            "wifi_ssid_input",
            "store_link_input",
            "generic_text_input",
            "location_permission_or_refresh_button"
        )
        expectedContentTags.forEachIndexed { index, expectedTag ->
            composeRule.onNodeWithTag("generator_tab_" + index)
                .performScrollTo()
                .performClick()
            waitForTag(expectedTag, 8_000)
            assertMinTouchTarget("generator_tab_" + index, 44f)
        }

        // Return to the first, densest generator page for host-side large-font capture.
        composeRule.onNodeWithTag("generator_tab_0")
            .performScrollTo()
            .performClick()
        waitForTag("promptpay_target_input", 8_000)
    }

    private fun assertMinTouchTarget(tag: String, minimumDp: Float) {
        val bounds = composeRule.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot
        val density = composeRule.activity.resources.displayMetrics.density
        val minPx = minimumDp * density
        assertTrue(
            "$tag touch target height is below ${minimumDp}dp",
            bounds.height >= minPx
        )
    }

    private fun waitForTag(tag: String, timeoutMillis: Long) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            try {
                composeRule.onAllNodesWithTag(tag)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            } catch (_: IllegalStateException) {
                false
            }
        }
    }

    private fun dismissRootWarningIfPresent() {
        val visible = composeRule.onAllNodesWithTag("root_security_warning_dialog")
            .fetchSemanticsNodes()
            .isNotEmpty()
        if (visible) {
            composeRule.onNodeWithTag("continue_security_button").performClick()
            composeRule.waitForIdle()
        }
    }
}
