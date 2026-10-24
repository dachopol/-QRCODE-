package com.aistudio.qrgenerator.kmpzqr

import com.aistudio.qrgenerator.kmpzqr.util.PromptPayGenerator
import com.aistudio.qrgenerator.kmpzqr.util.QrValidationUtil
import com.aistudio.qrgenerator.kmpzqr.util.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class PromptPayAmountBoundaryTest {
    @Test
    fun enteredAmount_blankIsOptionalButZeroAndInvalidNumbersFailClosed() {
        assertEquals(ValidationResult.Valid, QrValidationUtil.validateAmount(""))
        assertTrue(QrValidationUtil.validateAmount("0") is ValidationResult.Invalid)
        assertTrue(QrValidationUtil.validateAmount("-1") is ValidationResult.Invalid)
        assertTrue(QrValidationUtil.validateAmount("NaN") is ValidationResult.Invalid)
        assertTrue(QrValidationUtil.validateAmount("Infinity") is ValidationResult.Invalid)
        assertEquals(ValidationResult.Valid, QrValidationUtil.validateAmount("1.25"))
    }

    @Test
    fun legacyZeroAmountStillEncodesAsAmountUnspecified() {
        val payload = PromptPayGenerator.generatePayload("0812345678", 0.0)
        val parsed = PromptPayGenerator.parsePromptPay(payload)

        assertEquals("0812345678", parsed?.first)
        assertEquals(null, parsed?.second)
    }

    @Test
    fun generatorRejectsNegativeAndNonFiniteAmounts() {
        assertIllegalArgument { PromptPayGenerator.generatePayload("0812345678", -1.0) }
        assertIllegalArgument { PromptPayGenerator.generatePayload("0812345678", Double.NaN) }
        assertIllegalArgument { PromptPayGenerator.generatePayload("0812345678", Double.POSITIVE_INFINITY) }
    }

    @Test
    fun generatorRejectsAmountThatCannotFitTwoDigitTlvLength() {
        assertIllegalArgument { PromptPayGenerator.generatePayload("0812345678", 1.0e100) }
    }

    private fun assertIllegalArgument(block: () -> Unit) {
        try {
            block()
            fail("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // Expected.
        }
    }
}
