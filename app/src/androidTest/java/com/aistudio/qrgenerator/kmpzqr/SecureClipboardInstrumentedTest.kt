package com.aistudio.qrgenerator.kmpzqr

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aistudio.qrgenerator.kmpzqr.util.SecureClipboardUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecureClipboardInstrumentedTest {
    @Test
    fun copiedQrContent_isMarkedSensitiveAndPreservesPayload() {
        val payload = "WIFI:T:WPA;S:QA-NET;P:synthetic-secret;;"

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val clipboard =
                    activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                SecureClipboardUtil.copyQrContent(
                    context = activity,
                    label = "QR Code",
                    text = payload,
                    confirmation = "Copied"
                )

                val clip = clipboard.primaryClip
                requireNotNull(clip)
                assertEquals(payload, clip.getItemAt(0).text.toString())
                assertTrue(
                    clip.description.extras
                        ?.getBoolean(ClipDescription.EXTRA_IS_SENSITIVE, false) == true
                )

                clipboard.clearPrimaryClip()
            }
        }
    }
}
