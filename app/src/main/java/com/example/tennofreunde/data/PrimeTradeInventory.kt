package com.example.tennofreunde.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Locale

data class PrimeTradeEntry(
    val id: String,
    val name: String,
    val quantity: Int,
    val ducatsEach: Int,
    val platinumEach: Int,
    val keepOne: Boolean = true
) {
    val sellableQuantity: Int get() = (quantity - if (keepOne) 1 else 0).coerceAtLeast(0)
    val sellableDucats: Int get() = sellableQuantity * ducatsEach
    val sellablePlatinum: Int get() = sellableQuantity * platinumEach
}

enum class TradeRecommendation { KEEP, BARO, MARKET }

object PrimeTradeInventoryStore {
    private const val KEY = "prime_trade_inventory_v1"

    fun load(context: Context): List<PrimeTradeEntry> {
        val raw = context.getSharedPreferences("tenno_hub", Context.MODE_PRIVATE).getString(KEY, null)
        if (raw.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<PrimeTradeEntry>>() {}.type
        return normalizeTrades(runCatching { Gson().fromJson<List<PrimeTradeEntry>>(raw, type) }.getOrDefault(emptyList()))
    }

    fun save(context: Context, entries: List<PrimeTradeEntry>) {
        context.getSharedPreferences("tenno_hub", Context.MODE_PRIVATE).edit()
            .putString(KEY, Gson().toJson(normalizeTrades(entries)))
            .apply()
    }
}

internal fun addPrimeTradeEntry(
    entries: List<PrimeTradeEntry>,
    name: String,
    quantity: Int,
    ducatsEach: Int,
    platinumEach: Int
): List<PrimeTradeEntry> {
    val clean = name.trim().replace(Regex("\\s+"), " ")
    if (clean.isBlank() || quantity <= 0) return normalizeTrades(entries)
    val id = tradeKey(clean)
    val old = entries.firstOrNull { it.id == id }
    val merged = PrimeTradeEntry(
        id = id,
        name = clean,
        quantity = (old?.quantity ?: 0) + quantity,
        ducatsEach = ducatsEach.coerceIn(0, 1_000),
        platinumEach = platinumEach.coerceIn(0, 100_000),
        keepOne = old?.keepOne ?: true
    )
    return normalizeTrades(entries.filterNot { it.id == id } + merged)
}

internal fun updatePrimeTradeQuantity(entries: List<PrimeTradeEntry>, id: String, delta: Int): List<PrimeTradeEntry> {
    return normalizeTrades(entries.mapNotNull { entry ->
        if (entry.id != id) entry else entry.copy(quantity = entry.quantity + delta).takeIf { it.quantity > 0 }
    })
}

internal fun tradeRecommendation(entry: PrimeTradeEntry): TradeRecommendation = when {
    entry.sellableQuantity == 0 -> TradeRecommendation.KEEP
    entry.platinumEach <= 0 -> TradeRecommendation.BARO
    entry.ducatsEach.toDouble() / entry.platinumEach >= 4.0 -> TradeRecommendation.BARO
    else -> TradeRecommendation.MARKET
}

private fun normalizeTrades(entries: List<PrimeTradeEntry>): List<PrimeTradeEntry> = entries
    .filter { it.id.isNotBlank() && it.name.isNotBlank() && it.quantity > 0 }
    .distinctBy { it.id }
    .sortedBy { it.name.lowercase(Locale.ROOT) }

private fun tradeKey(value: String): String = value.trim().lowercase(Locale.ROOT).replace(Regex("\\s+"), " ")
