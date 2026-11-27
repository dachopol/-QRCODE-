package com.aistudio.qrgenerator.kmpzqr

import com.aistudio.qrgenerator.kmpzqr.util.QrGenerationRequestGuard
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QrGenerationRequestGuardTest {
    @Test
    fun newestRequestWinsAndInvalidateMakesPreviousTokenStale() {
        val guard = QrGenerationRequestGuard()

        val first = guard.begin()
        assertTrue(guard.isCurrent(first))

        val second = guard.begin()
        assertFalse(guard.isCurrent(first))
        assertTrue(guard.isCurrent(second))

        guard.invalidate()
        assertFalse(guard.isCurrent(second))
    }
}
