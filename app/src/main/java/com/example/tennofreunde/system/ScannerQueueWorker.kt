package com.example.tennofreunde.system

import android.content.Context
import android.net.Uri
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.tennofreunde.prepareOcrImage
import com.example.tennofreunde.recognizeText
import com.example.tennofreunde.data.ScannerQueueState
import com.example.tennofreunde.data.ScannerQueueStore
import com.example.tennofreunde.data.recoverInterruptedScannerQueue
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class ScannerQueueWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        if (ScannerQueueStore.isPaused(applicationContext)) return Result.success()
        if (ScannerQueueStore.isForegroundActive(applicationContext)) return Result.success()

        ScannerQueueStore.update(applicationContext, ::recoverInterruptedScannerQueue)

        val pending = ScannerQueueStore.load(applicationContext)
            .filter { it.state == ScannerQueueState.PENDING }
            .take(MAX_BACKGROUND_IMAGES)
        if (pending.isEmpty()) return Result.success()

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        var readyCount = 0
        var failedCount = 0
        try {
            pending.forEachIndexed { index, entry ->
                if (ScannerQueueStore.isForegroundActive(applicationContext)) {
                    return Result.success()
                }
                ScannerQueueStore.update(applicationContext) { entries ->
                    entries.map {
                        if (it.id == entry.id) it.copy(
                            state = ScannerQueueState.PROCESSING,
                            attempts = it.attempts + 1,
                            lastError = ""
                        ) else it
                    }
                }
                try {
                    val prepared = withContext(Dispatchers.IO) {
                        prepareOcrImage(applicationContext, Uri.parse(entry.uri))
                    }
                    val text = recognizeText(recognizer, prepared).trim()
                    if (text.isBlank()) error("Auf dem Bild wurde kein lesbarer Text gefunden.")
                    val textFile = ScannerQueueStore.saveRecognizedText(applicationContext, entry.id, text)
                    ScannerQueueStore.update(applicationContext) { entries ->
                        entries.map {
                            if (it.id == entry.id) it.copy(
                                state = ScannerQueueState.OCR_READY,
                                recognizedTextFile = textFile,
                                lastError = ""
                            ) else it
                        }
                    }
                    readyCount++
                } catch (cancelled: CancellationException) {
                    ScannerQueueStore.update(applicationContext) { entries ->
                        entries.map { if (it.id == entry.id) it.copy(state = ScannerQueueState.PENDING) else it }
                    }
                    throw cancelled
                } catch (error: Exception) {
                    ScannerQueueStore.update(applicationContext) { entries ->
                        entries.map {
                            if (it.id == entry.id) it.copy(
                                state = ScannerQueueState.FAILED,
                                lastError = error.localizedMessage.orEmpty().take(300)
                            ) else it
                        }
                    }
                    failedCount++
                }
                TennoSystem.showScannerNotification(
                    applicationContext,
                    completed = index + 1,
                    total = pending.size,
                    ready = false
                )
            }
        } finally {
            recognizer.close()
        }

        if (readyCount > 0 || failedCount > 0) {
            TennoSystem.showScannerNotification(
                applicationContext,
                completed = readyCount,
                total = readyCount + failedCount,
                ready = true,
                failed = failedCount
            )
        }
        val morePending = ScannerQueueStore.load(applicationContext)
            .any { it.state == ScannerQueueState.PENDING }
        return if (morePending) Result.retry() else Result.success()
    }

    companion object {
        private const val MAX_BACKGROUND_IMAGES = 20
    }
}

object ScannerQueueWork {
    private const val UNIQUE_WORK = "tennofreunde_scanner_queue"

    fun schedule(context: Context) {
        if (ScannerQueueStore.isPaused(context)) return
        val request = OneTimeWorkRequestBuilder<ScannerQueueWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
            .addTag(UNIQUE_WORK)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_WORK,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun scheduleIfNeeded(context: Context) {
        if (ScannerQueueStore.load(context).any { it.state == ScannerQueueState.PENDING }) schedule(context)
    }
}
