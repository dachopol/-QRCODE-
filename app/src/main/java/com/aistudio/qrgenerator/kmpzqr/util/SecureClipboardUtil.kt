package com.aistudio.qrgenerator.kmpzqr.util

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import android.widget.Toast

object SecureClipboardUtil {
    /**
     * QR-derived text can contain passwords, payment identifiers, contact data, or locations.
     * Mark it sensitive so Android 13+ can redact clipboard previews.
     */
    fun copyQrContent(
        context: Context,
        label: String,
        text: String,
        confirmation: String
    ) {
        val clip = ClipData.newPlainText(label, text).apply {
            description.extras = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
        }
        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(clip)

        // Android 13+ already presents clipboard feedback. Avoid duplicate notifications.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            Toast.makeText(context, confirmation, Toast.LENGTH_SHORT).show()
        }
    }
}
