package com.aistudio.qrgenerator.kmpzqr

import android.os.Build
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.fetchSemanticsNodes
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Android16CompatibilityInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun api36Runtime_navigationAndSystemBackRemainFunctional() {
        assertTrue("Expected Android 16 / API 36 runtime", Build.VERSION.SDK_INT >= 36)
        waitForMainNavigation()
        dismissRootWarningIfPresent()

        composeRule.onNodeWithTag("open_language_currency_button").performClick()
        composeRule.onNodeWithTag("language_currency_dialog").assertExists()
        Espresso.pressBack()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("language_currency_dialog").assertDoesNotExist()

        composeRule.onNodeWithTag("support_admin_button").performClick()
        composeRule.onNodeWithTag("support_bottom_sheet").assertExists()
        Espresso.pressBack()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("support_bottom_sheet").assertDoesNotExist()

        listOf("nav_generate", "nav_card", "nav_scanner", "nav_history").forEach { tag ->
            composeRule.onNodeWithTag(tag).performClick()
            composeRule.waitForIdle()
            dismissRootWarningIfPresent()
        }
    }

    private fun waitForMainNavigation() {
        composeRule.waitUntil(timeoutMillis = 8_000) {
            composeRule.onAllNodesWithTag("nav_generate")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
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
}
