package com.example.util

import java.util.Locale

object PromptPayGenerator {

    /**
     * Generates standard EMVCo PromptPay QR payload string for Thai banking apps.
     * @param target Mobile phone (e.g. 0812345678) or National ID (13 digits)
     * @param amount Optional transaction amount in THB
     */
    fun generatePayload(target: String, amount: Double?): String {
        val cleanTarget = target.replace("-", "").replace(" ", "").trim()

        val subtag: String = if (cleanTarget.length == 13) {
            // National ID or Tax ID (Subtag 02, length 13)
            val len = String.format(Locale.US, "%02d", cleanTarget.length)
            "02${len}${cleanTarget}"
        } else {
            // Mobile Phone (Subtag 01, length 13)
            // e.g. 0812345678 -> 0066812345678
            val formattedPhone = when {
                cleanTarget.startsWith("+66") -> "0066" + cleanTarget.substring(3)
                cleanTarget.startsWith("66") -> "00" + cleanTarget
                cleanTarget.startsWith("0") -> "0066" + cleanTarget.substring(1)
                else -> "0066$cleanTarget"
            }
            val len = String.format(Locale.US, "%02d", formattedPhone.length)
            "01${len}${formattedPhone}"
        }

        // Tag 29 PromptPay Merchant Account Information
        val aid = "0016A000000677010111"
        val tag29Content = "$aid$subtag"
        val tag29Len = String.format(Locale.US, "%02d", tag29Content.length)
        val tag29 = "29$tag29Len$tag29Content"

        val sb = StringBuilder()
        // Tag 00: Format Indicator
        sb.append("000201")
        // Tag 01: Initiation method (11 = static, 12 = dynamic with amount)
        if (amount != null && amount > 0) {
            sb.append("010212")
        } else {
            sb.append("010211")
        }
        // Tag 29: Merchant Account Info
        sb.append(tag29)
        // Tag 53: Transaction Currency (764 = THB)
        sb.append("5303764")

        // Tag 54: Transaction Amount
        if (amount != null && amount > 0) {
            val amountStr = String.format(Locale.US, "%.2f", amount)
            val amountLen = String.format(Locale.US, "%02d", amountStr.length)
            sb.append("54").append(amountLen).append(amountStr)
        }

        // Tag 58: Country Code (TH)
        sb.append("5802TH")

        // Tag 63: CRC Checksum tag + placeholder
        val rawBeforeCrc = sb.toString() + "6304"
        val crc = crc16Ccitt(rawBeforeCrc)

        return "$rawBeforeCrc$crc"
    }

    /**
     * Calculates CRC-16/CCITT-FALSE (polynomial 0x1021, initial 0xFFFF)
     */
    fun crc16Ccitt(data: String): String {
        var crc = 0xFFFF
        val bytes = data.toByteArray(Charsets.ISO_8859_1)
        for (b in bytes) {
            crc = crc xor ((b.toInt() and 0xFF) shl 8)
            for (i in 0 until 8) {
                crc = if ((crc and 0x8000) != 0) {
                    ((crc shl 1) xor 0x1021) and 0xFFFF
                } else {
                    (crc shl 1) and 0xFFFF
                }
            }
        }
        return String.format(Locale.US, "%04X", crc).uppercase(Locale.US)
    }

    /**
     * Verifies if an EMVCo QR string has valid CRC16-CCITT checksum at tag 63.
     */
    fun verifyCrc(qrData: String): Boolean {
        val tag63Idx = qrData.indexOf("6304")
        if (tag63Idx == -1 || qrData.length < tag63Idx + 8) return false
        val expectedCrc = qrData.substring(tag63Idx + 4, tag63Idx + 8)
        val dataToHash = qrData.substring(0, tag63Idx + 4)
        val calculatedCrc = crc16Ccitt(dataToHash)
        return expectedCrc.equals(calculatedCrc, ignoreCase = true)
    }

    /**
     * Parses a scanned PromptPay EMVCo QR code string.
     * Returns a Pair of (Target ID formatted, Amount).
     */
    fun parsePromptPay(qrData: String): Pair<String, Double?>? {
        if (!qrData.contains("A000000677010111")) {
            return null
        }
        try {
            // Check CRC if tag 63 exists
            if (qrData.contains("6304") && !verifyCrc(qrData)) {
                // CRC mismatch
                return null
            }

            var targetId = ""
            var amount: Double? = null

            // Find Tag 29
            val tag29Index = qrData.indexOf("29")
            if (tag29Index != -1 && qrData.length >= tag29Index + 4) {
                val tag29Len = qrData.substring(tag29Index + 2, tag29Index + 4).toIntOrNull() ?: 0
                if (qrData.length >= tag29Index + 4 + tag29Len) {
                    val tag29Content = qrData.substring(tag29Index + 4, tag29Index + 4 + tag29Len)
                    if (tag29Content.contains("01130066")) {
                        // Phone number
                        val pIdx = tag29Content.indexOf("01130066")
                        val rawPhone = tag29Content.substring(pIdx + 8, minOf(pIdx + 8 + 9, tag29Content.length))
                        targetId = "0$rawPhone"
                    } else if (tag29Content.contains("0213")) {
                        // National ID
                        val idIdx = tag29Content.indexOf("0213")
                        targetId = tag29Content.substring(idIdx + 4, minOf(idIdx + 4 + 13, tag29Content.length))
                    }
                }
            }

            // Find Tag 54 (Amount)
            val tag54Index = qrData.indexOf("54")
            if (tag54Index != -1 && qrData.length >= tag54Index + 4) {
                val len = qrData.substring(tag54Index + 2, tag54Index + 4).toIntOrNull() ?: 0
                if (qrData.length >= tag54Index + 4 + len) {
                    val amtStr = qrData.substring(tag54Index + 4, tag54Index + 4 + len)
                    amount = amtStr.toDoubleOrNull()
                }
            }

            return Pair(targetId, amount)
        } catch (_: Exception) {
            return null
        }
    }
}
