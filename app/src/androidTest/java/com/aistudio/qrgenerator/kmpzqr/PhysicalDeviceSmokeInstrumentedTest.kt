package com.aistudio.qrgenerator.kmpzqr

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aistudio.qrgenerator.kmpzqr.util.LocalizationManager
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhysicalDeviceSmokeInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun rmx3241_cameraX_bindsAndRebindsOnPhysicalDevice() {
        assertEquals("RMX3241", Build.MODEL)

        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation
            .executeShellCommand(
                "pm grant ${BuildConfig.APPLICATION_ID} ${Manifest.permission.CAMERA}"
            )
            .close()

        composeRule.runOnIdle {
            LocalizationManager.setLanguageByCode("en")
        }
        waitForMainNavigation()
        dismissRootWarningIfPresent()

        openScannerAndWaitForCamera()
        composeRule.onNodeWithTag("nav_generate").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_generate").assertExists()

        openScannerAndWaitForCamera()
    }

    private fun openScannerAndWaitForCamera() {
        composeRule.onNodeWithTag("nav_scanner").performClick()
        composeRule.waitUntil(timeoutMillis = 8_000) {
            composeRule.onAllNodesWithTag("scanner_screen")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        dismissRootWarningIfPresent()

        // The torch control is rendered only after CameraX binds a physical
        // camera instance that reports a flash unit. RMX3241 has a rear flash,
        // so this is a direct bind/readiness signal rather than a preview mock.
        composeRule.waitUntil(timeoutMillis = 12_000) {
            composeRule.onAllNodesWithContentDescription("Toggle flash")
                .fetchSemanticsNodes()
                .isNotEmpty()
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
