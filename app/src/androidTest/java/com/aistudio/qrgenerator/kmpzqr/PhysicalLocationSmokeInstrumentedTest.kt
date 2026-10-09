package com.aistudio.qrgenerator.kmpzqr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aistudio.qrgenerator.kmpzqr.util.LocalizationManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhysicalLocationSmokeInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun rmx3241_realLocationGeneratesQrAndOpensMapHandler() {
        assertEquals("RMX3241", Build.MODEL)

        composeRule.runOnIdle {
            LocalizationManager.setLanguageByCode("en")
        }
        waitForMainNavigation()
        dismissRootWarningIfPresent()

        composeRule.onNodeWithTag("nav_generate").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("generator_tab_4")
            .performScrollTo()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 8_000) {
            nodeExists("location_permission_or_refresh_button")
        }

        if (!hasLocationPermission()) {
            composeRule.onNodeWithTag("location_permission_or_refresh_button").performClick()
            composeRule.waitUntil(timeoutMillis = 30_000) {
                hasLocationPermission()
            }
            composeRule.waitForIdle()
        }

        // Permission result callback requests a real foreground location.
        // The app's CurrentLocationProvider rejects invalid coordinates and 0,0.
        composeRule.waitUntil(timeoutMillis = 25_000) {
            composeRule.onAllNodesWithText("Ready")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeRule.onNodeWithTag("open_current_location_map_button")
            .assertExists()
            .assertIsEnabled()
        composeRule.onNodeWithTag("generate_location_qr_button")
            .assertExists()
            .assertIsEnabled()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 8_000) {
            nodeExists("qr_preview_dialog")
        }
        composeRule.onNodeWithTag("open_generated_location_map_button")
            .assertExists()
            .assertIsEnabled()

        val mapIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("geo:1,1?q=1,1")
        )
        assertNotNull(
            "No map/browser handler is available on the physical device",
            composeRule.activity.packageManager.resolveActivity(
                mapIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
        )

        val requireMapLaunch =
            InstrumentationRegistry.getArguments().getString("physicalMap") == "true"
        if (requireMapLaunch) {
            composeRule.onNodeWithTag("open_generated_location_map_button").performClick()
            Thread.sleep(2_000)
            val activities = shell("dumpsys activity activities")
            val appStillResumed = Regex(
                """mResumedActivity=.*com\.aistudio\.qrgenerator\.kmpzqr/"""
            ).containsMatchIn(activities)
            assertFalse("Map handler did not take foreground", appStillResumed)
        }
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            composeRule.activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                composeRule.activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
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
        val visible = composeRule.onAllNodesWithTag("root_security_warning_dialog")
            .fetchSemanticsNodes()
            .isNotEmpty()
        if (visible) {
            composeRule.onNodeWithTag("continue_security_button").performClick()
            composeRule.waitForIdle()
        }
    }

    private fun shell(command: String): String {
        val descriptor =
            InstrumentationRegistry.getInstrumentation()
                .uiAutomation
                .executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(descriptor)
            .bufferedReader()
            .use { it.readText() }
    }
}
