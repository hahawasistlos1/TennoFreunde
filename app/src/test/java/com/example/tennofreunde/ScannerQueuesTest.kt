package com.example.tennofreunde

import com.example.tennofreunde.data.PendingCatalogSuggestion
import com.example.tennofreunde.data.ScannerAddedComponent
import com.example.tennofreunde.data.ScannerAddedItem
import com.example.tennofreunde.data.ScannerQueueEntry
import com.example.tennofreunde.data.ScannerQueueState
import com.example.tennofreunde.data.mergePendingCatalogSuggestions
import com.example.tennofreunde.data.recoverInterruptedScannerQueue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScannerQueuesTest {
    @Test
    fun interruptedAndUncommittedImagesReturnToPendingAfterRestart() {
        val recovered = recoverInterruptedScannerQueue(
            listOf(
                ScannerQueueEntry("one", "content://one", state = ScannerQueueState.PROCESSING),
                ScannerQueueEntry("two", "content://two", state = ScannerQueueState.COMPLETED),
                ScannerQueueEntry("three", "content://three", state = ScannerQueueState.FAILED),
                ScannerQueueEntry("four", "content://four", state = ScannerQueueState.OCR_READY, recognizedTextFile = "ash.txt")
            )
        )

        assertEquals(ScannerQueueState.PENDING, recovered[0].state)
        assertEquals(ScannerQueueState.PENDING, recovered[1].state)
        assertEquals(ScannerQueueState.FAILED, recovered[2].state)
        assertEquals(ScannerQueueState.OCR_READY, recovered[3].state)
        assertEquals("ash.txt", recovered[3].recognizedTextFile)
    }

    @Test
    fun repeatedUnknownItemBecomesOneReviewEntryAndKeepsEveryDetectedComponent() {
        fun suggestion(component: String) = PendingCatalogSuggestion(
            id = "tau prime",
            item = ScannerAddedItem(
                name = "Tau Prime",
                type = "warframe",
                tabName = "Tenno",
                subTabName = "Prime Warframe",
                components = listOf(ScannerAddedComponent(component, true, "", "", ""))
            ),
            recognizedText = "Tau Prime $component"
        )

        val merged = mergePendingCatalogSuggestions(
            listOf(suggestion("Chassis"), suggestion("Systems"))
        )

        assertEquals(1, merged.size)
        assertEquals(setOf("Chassis", "Systems"), merged.single().item.components.map { it.name }.toSet())
        assertTrue(merged.single().item.components.all { it.checked })
    }
}
