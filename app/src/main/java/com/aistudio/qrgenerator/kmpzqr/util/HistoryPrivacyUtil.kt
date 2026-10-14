package com.aistudio.qrgenerator.kmpzqr.util

object HistoryPrivacyUtil {
    fun wifiHistorySubtitle(hasPassword: Boolean, languageCode: String): String {
        val isThai = languageCode.equals("th", ignoreCase = true)
        return when {
            hasPassword && isThai -> "มีรหัสผ่าน (ซ่อนในประวัติ)"
            hasPassword -> "Password hidden in history"
            isThai -> "ไม่มีรหัสผ่าน"
            else -> "No password"
        }
    }
}
