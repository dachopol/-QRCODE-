package com.example.util

import java.util.Locale

object PromptPayGenerator {

    private const val PROMPTPAY_AID = "A000000677010111"
    private const val THB_CURRENCY_CODE = "764"

    /**
     * Generates an EMVCo PromptPay payload for a validated Thai mobile number
     * or 13-digit Thai ID. Amount is optional and always expressed in THB.
     */
    fun generatePayload(target: String, amount: Double?): String {
        val cleanTarget = target.replace("-", "").replace(" ", "").trim()

        val subtag = if (cleanTarget.length == 13) {
            tlv("02", cleanTarget)
        } else {
            val formattedPhone = when {
                cleanTarget.startsWith("+66") -> "0066" + cleanTarget.substring(3)
                cleanTarget.startsWith("66") -> "00" + cleanTarget
                cleanTarget.startsWith("0") -> "0066" + cleanTarget.substring(1)
                else -> "0066$cleanTarget"
            }
            tlv("01", formattedPhone)
        }

        val merchantAccountInfo = tlv("00", PROMPTPAY_AID) + subtag

        val body = buildString {
            append(tlv("00", "01"))
            append(tlv("01", if (amount != null && amount > 0.0) "12" else "11"))
            append(tlv("29", merchantAccountInfo))
            append(tlv("53", THB_CURRENCY_CODE))

            if (amount != null && amount > 0.0) {
                append(tlv("54", String.format(Locale.US, "%.2f", amount)))
            }

            append(tlv("58", "TH"))
        }

        val beforeCrc = body + "6304"
        return beforeCrc + crc16Ccitt(beforeCrc)
    }

    private fun tlv(tag: String, value: String): String =
        tag + String.format(Locale.US, "%02d", value.length) + value

    /**
     * CRC-16/CCITT-FALSE, polynomial 0x1021 and initial value 0xFFFF.
     */
    fun crc16Ccitt(data: String): String {
        var crc = 0xFFFF
        val bytes = data.toByteArray(Charsets.ISO_8859_1)
        for (b in bytes) {
            crc = crc xor ((b.toInt() and 0xFF) shl 8)
            repeat(8) {
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
     * Verifies a terminal EMVCo CRC tag.
     */
    fun verifyCrc(qrData: String): Boolean {
        val tag63Index = qrData.lastIndexOf("6304")
        if (tag63Index < 0 || tag63Index + 8 != qrData.length) return false

        val expected = qrData.substring(tag63Index + 4)
        val dataToHash = qrData.substring(0, tag63Index + 4)
        return expected.equals(crc16Ccitt(dataToHash), ignoreCase = true)
    }

    /**
     * Parses a PromptPay EMVCo payload.
     * Returns Pair(targetId, amount) only when the key PromptPay fields are valid.
     */
    fun parsePromptPay(qrData: String): Pair<String, Double?>? {
        return try {
            if (!verifyCrc(qrData)) return null

            val topLevel = parseTlv(qrData) ?: return null
            if (topLevel["00"] != "01") return null
            if (topLevel["53"] != THB_CURRENCY_CODE) return null
            if (topLevel["58"] != "TH") return null

            val merchantInfo = topLevel["29"] ?: return null
            val merchantFields = parseTlv(merchantInfo) ?: return null
            if (merchantFields["00"] != PROMPTPAY_AID) return null

            val target = when {
                !merchantFields["01"].isNullOrBlank() -> {
                    val phone = merchantFields.getValue("01")
                    if (!phone.startsWith("0066") || phone.length != 13) return null
                    "0" + phone.substring(4)
                }
                !merchantFields["02"].isNullOrBlank() -> {
                    val id = merchantFields.getValue("02")
                    if (id.length != 13 || !id.all(Char::isDigit)) return null
                    id
                }
                else -> return null
            }

            val amount = topLevel["54"]?.toDoubleOrNull()
            Pair(target, amount)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseTlv(data: String): Map<String, String>? {
        val result = linkedMapOf<String, String>()
        var index = 0

        while (index < data.length) {
            if (index + 4 > data.length) return null

            val tag = data.substring(index, index + 2)
            val length = data.substring(index + 2, index + 4).toIntOrNull() ?: return null
            val valueStart = index + 4
            val valueEnd = valueStart + length

            if (valueEnd > data.length) return null
            result[tag] = data.substring(valueStart, valueEnd)
            index = valueEnd
        }

        return result
    }
}
