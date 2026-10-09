package com.aistudio.qrgenerator.kmpzqr

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
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

        composeRule.runOnIdle {
            LocalizationManager.setLanguageByCode("en")
        }
        waitForMainNavigation()
        dismissRootWarningIfPresent()

        openScannerAndEnsureCameraPermission()
        waitForPhysicalCameraBind()

        composeRule.onNodeWithTag("nav_generate").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_generate").assertExists()

        openScannerAndEnsureCameraPermission()
        waitForPhysicalCameraBind()
    }

    private fun openScannerAndEnsureCameraPermission() {
        dismissRootWarningIfPresent()
        composeRule.onNodeWithTag("nav_scanner").performClick()
        composeRule.waitForIdle()
        waitForScannerSurface()
        dismissRootWarningIfPresent()

        if (!cameraPermissionGranted()) {
            composeRule.onNodeWithText("Allow camera").performClick()
            // The host-side physical QA workflow accepts the Android runtime
            // permission dialog exactly as a user would. Keep this test alive
            // while that system dialog is handled outside the app process.
            composeRule.waitUntil(timeoutMillis = 30_000) {
                cameraPermissionGranted()
            }
            composeRule.waitForIdle()
            waitForScannerSurface()
        }
    }

    private fun waitForScannerSurface() {
        val deadline = android.os.SystemClock.uptimeMillis() + 20_000L
        while (android.os.SystemClock.uptimeMillis() < deadline) {
            dismissRootWarningIfPresent()
            if (nodeExists("scanner_screen")) {
                composeRule.waitForIdle()
                return
            }
            Thread.sleep(250L)
        }
        throw AssertionError("Scanner screen did not become available on RMX3241")
    }

    private fun waitForPhysicalCameraBind() {
        // The torch control appears only after CameraX has bound a physical
        // camera instance that reports a flash unit. RMX3241 has a rear flash,
        // making this a direct physical bind signal rather than a mock preview.
        composeRule.waitUntil(timeoutMillis = 20_000) {
            try {
                composeRule.onAllNodesWithContentDescription("Toggle flash")
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            } catch (_: IllegalStateException) {
                false
            }
        }
    }

    private fun cameraPermissionGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            composeRule.activity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    private fun waitForMainNavigation() {
        composeRule.waitUntil(timeoutMillis = 15_000) {
            nodeExists("nav_generate")
        }
    }

    private fun nodeExists(tag: String): Boolean =
        try {
            composeRule.onAllNodesWithTag(tag)
                .fetchSemanticsNodes()
                .isNotEmpty()
        } catch (_: IllegalStateException) {
            false
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
