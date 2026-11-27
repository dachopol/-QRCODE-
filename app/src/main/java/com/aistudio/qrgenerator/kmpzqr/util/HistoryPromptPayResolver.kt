package com.aistudio.qrgenerator.kmpzqr.util

import com.aistudio.qrgenerator.kmpzqr.data.QrItemEntity

data class ResolvedPromptPayHistory(
    val payload: String,
    val target: String,
    val amount: Double?
)

object HistoryPromptPayResolver {
    fun resolve(item: QrItemEntity): ResolvedPromptPayHistory? {
        if (item.type != "PROMPTPAY") return null
        val parsed = PromptPayGenerator.parsePromptPay(item.rawContent) ?: return null
        return ResolvedPromptPayHistory(
            payload = item.rawContent,
            target = parsed.first,
            amount = parsed.second
        )
    }
}
