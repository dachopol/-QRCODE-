package com.aistudio.qrgenerator.kmpzqr.util

import java.util.concurrent.atomic.AtomicLong

/**
 * Monotonic request tokens prevent an older QR render from replacing a newer user request.
 */
internal class QrGenerationRequestGuard {
    private val current = AtomicLong(0L)

    fun begin(): Long = current.incrementAndGet()

    fun invalidate(): Long = current.incrementAndGet()

    fun isCurrent(token: Long): Boolean = current.get() == token
}
