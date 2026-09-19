package com.example.util

object PrivacyProtection {
    // Specifically block and mask the private GitHub noreply email requested by user
    const val BLOCKED_NOREPLY_EMAIL = "215334638+AnakinYoo@users.noreply.github.com"
    const val OFFICIAL_ADMIN_EMAIL = "chenkung12@gmail.com"

    /**
     * Sanitizes any email or string to guarantee that private GitHub noreply addresses
     * (specifically 215334638+AnakinYoo@users.noreply.github.com) are completely hidden and replaced with the official admin email.
     */
    fun sanitizeEmail(input: String?): String {
        if (input.isNullOrBlank()) return OFFICIAL_ADMIN_EMAIL
        if (input.contains("AnakinYoo", ignoreCase = true) ||
            input.contains("users.noreply.github.com", ignoreCase = true) ||
            input.contains("215334638", ignoreCase = true)
        ) {
            return OFFICIAL_ADMIN_EMAIL
        }
        return input.trim()
    }

    /**
     * Verifies if an email address is permissible for public display.
     */
    fun isPermittedEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        return !email.contains("AnakinYoo", ignoreCase = true) &&
               !email.contains("noreply.github.com", ignoreCase = true) &&
               !email.contains("215334638", ignoreCase = true)
    }
}
