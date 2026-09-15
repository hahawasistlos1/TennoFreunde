package com.example.tennofreunde

import android.net.Uri
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.tennofreunde.data.ScannerQueueEntry
import com.example.tennofreunde.data.ScannerQueueState
import com.example.tennofreunde.data.ScannerQueueStore
import com.example.tennofreunde.system.ScannerQueueWorker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ScannerQueueWorkerTest {
    @Test
    fun backgroundWorkerReadsImageAndStoresOcrResultOutsidePreferences() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val source = File(context.filesDir, "worker_scanner_test.jpg")
        instrumentation.context.assets.open("android.jpg").use { input ->
            source.outputStream().use(input::copyTo)
        }
        ScannerQueueStore.setPaused(context, false)
        ScannerQueueStore.setForegroundActive(context, false)
        ScannerQueueStore.save(
            context,
            listOf(ScannerQueueEntry("worker-test", Uri.fromFile(source).toString()))
        )

        val request = OneTimeWorkRequestBuilder<ScannerQueueWorker>().build()
        val manager = WorkManager.getInstance(context)
        manager.enqueue(request)
        val deadline = SystemClock.elapsedRealtime() + TimeUnit.MINUTES.toMillis(2)
        var entry = ScannerQueueStore.load(context).single()
        while (SystemClock.elapsedRealtime() < deadline) {
            entry = ScannerQueueStore.load(context).single()
            if (entry.state == ScannerQueueState.OCR_READY || entry.state == ScannerQueueState.FAILED) break
            SystemClock.sleep(500)
        }

        assertEquals(ScannerQueueState.OCR_READY, entry.state)
        assertTrue(ScannerQueueStore.readRecognizedText(entry).length > 20)
        assertTrue(entry.recognizedTextFile.startsWith(context.filesDir.absolutePath))

        ScannerQueueStore.save(context, emptyList())
        source.delete()
    }
}
