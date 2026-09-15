package com.example.tennofreunde

import com.example.tennofreunde.data.TradeRecommendation
import com.example.tennofreunde.data.addPrimeTradeEntry
import com.example.tennofreunde.data.tradeRecommendation
import com.example.tennofreunde.data.updatePrimeTradeQuantity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrimeTradeInventoryTest {
    @Test
    fun duplicateNamesMergeAndKeepOneProtectsLastCopy() {
        var entries = addPrimeTradeEntry(emptyList(), "Wisp Prime Chassis", 2, 45, 20)
        entries = addPrimeTradeEntry(entries, " wisp   prime chassis ", 1, 45, 20)
        val entry = entries.single()

        assertEquals(3, entry.quantity)
        assertEquals(2, entry.sellableQuantity)
        assertEquals(90, entry.sellableDucats)
        assertEquals(40, entry.sellablePlatinum)
    }

    @Test
    fun recommendationUsesRealDucatToPlatinumRatio() {
        val baro = addPrimeTradeEntry(emptyList(), "Low price", 2, 100, 10).single()
        val market = addPrimeTradeEntry(emptyList(), "High price", 2, 15, 30).single()
        val keep = addPrimeTradeEntry(emptyList(), "Only one", 1, 100, 1).single()

        assertEquals(TradeRecommendation.BARO, tradeRecommendation(baro))
        assertEquals(TradeRecommendation.MARKET, tradeRecommendation(market))
        assertEquals(TradeRecommendation.KEEP, tradeRecommendation(keep))
        assertTrue(updatePrimeTradeQuantity(listOf(keep), keep.id, -1).isEmpty())
    }
}
