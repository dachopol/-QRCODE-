package com.aistudio.qrgenerator.kmpzqr

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhysicalDeviceSmokeInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun physicalCameraAndLocationSmoke() {
        assumeFalse("Physical-device smoke must not run on an emulator", isProbablyEmulator())
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val packageName = context.packageName
        val permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val missingPermissions = permissions.filter { permission ->
            context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED
        }
        assertTrue(
            "Physical QA requires CAMERA and location permissions to be pre-granted by the QA install; missing=$missingPermissions",
            missingPermissions.isEmpty()
        )

        waitForMainNavigation()
            dismissRootWarningIfPresent()

            composeRule.onNodeWithTag("nav_scanner").performClick()
        composeRule.waitUntil(timeoutMillis = 15_000) {
            val active = composeRule.onAllNodesWithTag("camera_active_state")
                .fetchSemanticsNodes().isNotEmpty()
            val error = composeRule.onAllNodesWithTag("camera_error_state")
                .fetchSemanticsNodes().isNotEmpty()
            active || error
        }
        assertTrue(
            "CameraX did not bind to a physical camera",
            composeRule.onAllNodesWithTag("camera_active_state")
                .fetchSemanticsNodes().isNotEmpty()
        )
        assertTrue(
            "Physical camera reported an error",
            composeRule.onAllNodesWithTag("camera_error_state")
                .fetchSemanticsNodes().isEmpty()
        )

        composeRule.onNodeWithTag("nav_generate").performClick()
        composeRule.onNodeWithTag("generator_tab_4")
            .performScrollTo()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 22_000) {
            val ready = composeRule.onAllNodesWithTag("current_location_ready")
                .fetchSemanticsNodes().isNotEmpty()
            val error = composeRule.onAllNodesWithTag("current_location_error")
                .fetchSemanticsNodes().isNotEmpty()
            ready || error
        }
        assertTrue(
            "The physical device did not return a valid current location",
            composeRule.onAllNodesWithTag("current_location_ready")
                .fetchSemanticsNodes().isNotEmpty()
        )
        assertTrue(
            "Current location entered an error state",
            composeRule.onAllNodesWithTag("current_location_error")
                .fetchSemanticsNodes().isEmpty()
        )

        composeRule.onNodeWithTag("generate_location_qr_button")
            .performScrollTo()
            .assertIsEnabled()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 8_000) {
            composeRule.onAllNodesWithTag("qr_preview_dialog")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("qr_preview_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("close_qr_preview_button").performClick()

        composeRule.onNodeWithTag("open_current_location_map_button")
            .performScrollTo()
            .assertIsEnabled()
    }

    @Test
    fun largeFontCoreControlsRemainOperable() {
        assumeFalse("Large-font physical smoke must not run on an emulator", isProbablyEmulator())
        val fontScale = composeRule.activity.resources.configuration.fontScale
        assertTrue("Expected system font scale >= 1.25, observed $fontScale", fontScale >= 1.25f)

        waitForMainNavigation()
        dismissRootWarningIfPresent()

        listOf("nav_generate", "nav_card", "nav_scanner", "nav_history").forEach { tag ->
            composeRule.onNodeWithTag(tag)
                .assertIsDisplayed()
                .performClick()
            composeRule.waitForIdle()
            dismissRootWarningIfPresent()
        }

        composeRule.onNodeWithTag("open_language_currency_button")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("language_currency_dialog")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("close_lang_currency_dialog_button").performClick()

        composeRule.onNodeWithTag("support_admin_button")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 8_000) {
            composeRule.onAllNodesWithTag("close_support_sheet_button")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("close_support_sheet_button").performClick()
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

    private fun isProbablyEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT.lowercase()
        val model = Build.MODEL.lowercase()
        val product = Build.PRODUCT.lowercase()
        return fingerprint.contains("generic") ||
            fingerprint.contains("emulator") ||
            model.contains("google_sdk") ||
            model.contains("emulator") ||
            product.contains("sdk")
    }
}
