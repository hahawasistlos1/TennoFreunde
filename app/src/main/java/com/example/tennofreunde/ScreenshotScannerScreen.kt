package com.example.tennofreunde

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RemoveDone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.tennofreunde.models.ComponentItem
import com.example.tennofreunde.models.WarframeItem
import com.example.tennofreunde.data.PrimeCatalog
import com.example.tennofreunde.data.PrimeCatalogItem
import com.example.tennofreunde.data.LatestPrimeCatalog
import com.example.tennofreunde.data.discoverUnknownPrimeItems
import com.example.tennofreunde.ui.AppLanguage
import com.example.tennofreunde.ui.theme.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.text.Normalizer
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max

enum class OcrMatchState { ADDED_TO_COLLECTION, NEWLY_CHECKED, ALREADY_CHECKED, DETECTED_ONLY }
data class OcrComponentMatch(val itemName: String, val componentName: String, val state: OcrMatchState)
private data class ScannerHistoryEntry(val title: String, val detail: String)
private data class OcrLearningRule(val wrong: String, val correct: String)
private enum class ScannerMode { ALL, COMPONENTS, RELICS, PRIME_PARTS, FOUNDRY, MODS, MISSION_END }
private data class ComponentCandidate(
    val item: WarframeItem,
    val componentIndex: Int,
    val component: ComponentItem,
    val candidate: String
)
private data class PreparedOcrImage(val input: InputImage, val bitmap: Bitmap)
private data class ScannerFolderImage(val uri: Uri, val key: String)
internal data class OcrCrop(val left: Int, val top: Int, val width: Int, val height: Int)

private const val MAX_SCANNER_IMAGE_BYTES = 32L * 1024L * 1024L
private const val MAX_FOLDER_IMAGES_PER_RUN = 100
private const val MAX_RESULT_MATCHES = 200
private const val MAX_RECOGNIZED_TEXT_CHARS = 50_000

private fun scannerFolderImages(context: Context, treeUri: Uri): List<ScannerFolderImage> {
    val resolver = context.contentResolver
    val rootId = DocumentsContract.getTreeDocumentId(treeUri)
    val pendingFolders = ArrayDeque<String>().apply { add(rootId) }
    val images = mutableListOf<Pair<ScannerFolderImage, Long>>()
    var visited = 0
    while (pendingFolders.isNotEmpty() && visited < 250) {
        val parentId = pendingFolders.removeFirst()
        visited++
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentId)
        resolver.query(
            childrenUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                DocumentsContract.Document.COLUMN_SIZE
            ),
            null,
            null,
            null
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val mimeIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
            val modifiedIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
            val sizeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)
            while (cursor.moveToNext()) {
                val documentId = cursor.getString(idIndex)
                val mime = cursor.getString(mimeIndex).orEmpty()
                if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                    pendingFolders.add(documentId)
                } else if (mime.startsWith("image/")) {
                    val modified = if (modifiedIndex >= 0 && !cursor.isNull(modifiedIndex)) cursor.getLong(modifiedIndex) else 0L
                    val size = if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) cursor.getLong(sizeIndex) else 0L
                    val documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
                    images += ScannerFolderImage(documentUri, "$documentId|$modified|$size") to modified
                }
            }
        }
    }
    return images.sortedBy { it.second }.map { it.first }
}

private fun copyScannerImageToCache(context: Context, uri: Uri): File {
    val cached = File.createTempFile("tenno_scan_", ".image", context.cacheDir)
    try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            cached.outputStream().buffered().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var total = 0L
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    total += read
                    if (total > MAX_SCANNER_IMAGE_BYTES) {
                        error("Das Bild ist größer als 32 MB.")
                    }
                    output.write(buffer, 0, read)
                }
            }
        } ?: error("Bilddatei konnte nicht geöffnet werden.")
        if (cached.length() == 0L) error("Die Bilddatei ist leer.")
        return cached
    } catch (error: Throwable) {
        cached.delete()
        throw error
    }
}

private fun prepareOcrImage(context: Context, uri: Uri, maxDimension: Int = 2400): PreparedOcrImage {
    // Some gallery and cloud providers expose a URI as a one-shot stream. Copying it once
    // also keeps decoding off the provider and prevents the second open from failing.
    val cached = copyScannerImageToCache(context, uri)
    try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(cached.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) error("Ungültige Bilddatei.")

        val sampleSize = ocrSampleSize(bounds.outWidth, bounds.outHeight, maxDimension)
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = BitmapFactory.decodeFile(cached.absolutePath, options)
            ?: error("Bilddatei konnte nicht decodiert werden.")
        val crop = inventoryOcrCrop(decoded.width, decoded.height)
        val bitmap = if (crop != null) {
            Bitmap.createBitmap(decoded, crop.left, crop.top, crop.width, crop.height).also {
                if (it !== decoded) decoded.recycle()
            }
        } else {
            decoded
        }
        return PreparedOcrImage(InputImage.fromBitmap(bitmap, 0), bitmap)
    } finally {
        cached.delete()
    }
}

internal fun inventoryOcrCrop(width: Int, height: Int): OcrCrop? {
    if (width <= 0 || height <= 0 || width.toFloat() / height < 1.4f) return null
    val left = 0
    val top = (height * 0.08f).toInt()
    val right = (width * 0.76f).toInt().coerceAtMost(width)
    val bottom = (height * 0.96f).toInt().coerceAtMost(height)
    return OcrCrop(left, top, (right - left).coerceAtLeast(1), (bottom - top).coerceAtLeast(1))
}

internal fun ocrSampleSize(width: Int, height: Int, maxDimension: Int = 2048): Int {
    var sampleSize = 1
    while (maxOf(width / sampleSize, height / sampleSize) > maxDimension) sampleSize *= 2
    return sampleSize
}

private suspend fun recognizeText(scanner: TextRecognizer, prepared: PreparedOcrImage): String =
    suspendCancellableCoroutine { continuation ->
        scanner.process(prepared.input)
            .addOnSuccessListener { result ->
                prepared.bitmap.recycle()
                if (continuation.isActive) continuation.resume(result.text)
            }
            .addOnFailureListener { error ->
                prepared.bitmap.recycle()
                if (continuation.isActive) continuation.resumeWithException(error)
            }
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScreenshotScannerScreen(
    items: MutableList<WarframeItem>,
    language: AppLanguage,
    onProgressChanged: () -> Unit,
    onCatalogItemsAdded: (List<WarframeItem>) -> Unit = {}
) {
    val german = language == AppLanguage.GERMAN
    val compact = LocalCompactMode.current
    val context = LocalContext.current
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var recognizedText by remember { mutableStateOf("") }
    var matches by remember { mutableStateOf<List<OcrComponentMatch>>(emptyList()) }
    var pendingText by remember { mutableStateOf("") }
    var pendingMatches by remember { mutableStateOf<List<OcrComponentMatch>>(emptyList()) }
    var lastAppliedMatches by remember { mutableStateOf<List<OcrComponentMatch>>(emptyList()) }
    var manualCorrection by remember { mutableStateOf("") }
    var scannerMode by remember { mutableStateOf(ScannerMode.ALL) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isScanning by remember { mutableStateOf(false) }
    var scannedImageCount by remember { mutableIntStateOf(0) }
    var lastScanImageCount by remember { mutableIntStateOf(0) }
    var scanJob by remember { mutableStateOf<Job?>(null) }
    var scannerHistory by remember { mutableStateOf(loadScannerHistory(context)) }
    var learnedRules by remember { mutableStateOf(loadOcrLearningRules(context)) }
    var correctionFrom by remember { mutableStateOf("") }
    var correctionTo by remember { mutableStateOf("") }
    val bundledCatalog = remember { PrimeCatalog.load(context) }
    var catalog by remember { mutableStateOf(bundledCatalog) }
    var latestCatalogLoading by remember { mutableStateOf(true) }
    val scannerFolderPreferences = remember {
        context.getSharedPreferences("scanner_folder", Context.MODE_PRIVATE)
    }
    var scannerFolderUri by remember {
        mutableStateOf(scannerFolderPreferences.getString("tree_uri", null)?.let(Uri::parse))
    }
    var folderPendingCount by remember { mutableIntStateOf(0) }
    var folderStatus by remember { mutableStateOf<String?>(null) }
    var folderChecking by remember { mutableStateOf(false) }
    val selectedImageCount = lastScanImageCount
    val scanScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val latest = withContext(Dispatchers.IO) { LatestPrimeCatalog.load(context) }
            catalog = (bundledCatalog + latest)
                .distinctBy { normalizeOcr(it.item.name) }
        } finally {
            latestCatalogLoading = false
        }
    }

    val scanner = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    DisposableEffect(scanner) { onDispose { scanner.close() } }
    DisposableEffect(Unit) {
        onDispose { scanJob?.cancel() }
    }

    fun previewMatchesForText(text: String): List<OcrComponentMatch> {
        return when (scannerMode) {
            ScannerMode.ALL -> previewAllMatches(text, items, catalog)
            ScannerMode.COMPONENTS -> previewCatalogItems(text, items, catalog) +
                previewOcrComponents(text, items) +
                previewVisibleCollectionCardComponents(text, items) +
                previewUnknownPrimeItems(text, items, catalog)
            ScannerMode.RELICS -> previewRelicInventory(text)
            ScannerMode.PRIME_PARTS -> previewPrimeParts(text, catalog) +
                previewUnknownPrimeItems(text, items, catalog)
            ScannerMode.FOUNDRY -> previewKeywordLines(text, "Foundry", listOf("building", "claim", "complete", "hour", "minute", "day", "bau", "fertig"))
            ScannerMode.MODS -> previewKeywordLines(text, "Mods", listOf("mod", "rank", "rang", "fusion", "capacity", "drain"))
            ScannerMode.MISSION_END -> previewKeywordLines(text, "Mission", listOf("reward", "belohnung", "credits", "endo", "xp", "affinity", "ressource"))
        }
    }

    fun previewText(text: String) {
        val correctedText = applyLearnedCorrections(text, learnedRules)
        recognizedText = correctedText
        pendingText = correctedText
        pendingMatches = emptyList()
        matches = emptyList()
        scanScope.launch {
            val preview = withContext(Dispatchers.Default) {
                previewMatchesForText(correctedText)
            }
            if (pendingText == correctedText) pendingMatches = preview
        }
    }

    fun applyScanText(
        text: String,
        previewMatches: List<OcrComponentMatch>,
        notifyChanges: Boolean = true,
        updateHistory: Boolean = true
    ): Pair<List<OcrComponentMatch>, List<WarframeItem>> {
        if (text.isBlank()) return emptyList<OcrComponentMatch>() to emptyList()
        val appliesToCollection = scannerMode == ScannerMode.ALL ||
            scannerMode == ScannerMode.COMPONENTS ||
            scannerMode == ScannerMode.PRIME_PARTS
        if (!appliesToCollection) {
            val result = previewMatches.map { it.copy(state = OcrMatchState.DETECTED_ONLY) }
            matches = result
            lastAppliedMatches = emptyList()
            pendingText = ""
            pendingMatches = emptyList()
            if (updateHistory) {
                scannerHistory = saveScannerHistory(context, result, german)
            }
            return result to emptyList()
        }
        val unknown = addUnknownPrimeItems(text, items, catalog)
        val added = addMissingCatalogItems(text, items, catalog)
        val allAddedMatches = unknown + added
        val addedItems = allAddedMatches.mapNotNull { match ->
            items.firstOrNull { it.name == match.itemName }
        }.distinctBy { it.name }
        val addedKeys = allAddedMatches.map { "${it.itemName}\u0000${it.componentName}" }.toSet()
        val previewApplied = applyOcrMatchesToCollection(previewMatches, items)
        val componentResult = allAddedMatches +
            previewApplied.filterNot { "${it.itemName}\u0000${it.componentName}" in addedKeys }
        val appliedKeys = componentResult.map { "${it.itemName}\u0000${it.componentName}" }.toSet()
        val detectedOnly = previewMatches
            .filterNot { "${it.itemName}\u0000${it.componentName}" in appliedKeys }
            .filter { it.componentName in setOf("Relikt-Inventar", "Foundry", "Mods", "Mission") }
            .map { it.copy(state = OcrMatchState.DETECTED_ONLY) }
        val result = if (scannerMode == ScannerMode.ALL) {
            (componentResult + detectedOnly).distinctBy { "${it.itemName}\u0000${it.componentName}" }
        } else {
            componentResult
        }
        matches = result
        val appliedComponentKeys = componentResult.map { "${it.itemName}\u0000${it.componentName}" }.toSet()
        lastAppliedMatches = result.filter {
            it.state != OcrMatchState.ALREADY_CHECKED && "${it.itemName}\u0000${it.componentName}" in appliedComponentKeys
        }
        pendingText = ""
        pendingMatches = emptyList()
        if (notifyChanges && componentResult.any { it.state != OcrMatchState.ALREADY_CHECKED }) {
            onProgressChanged()
            if (addedItems.isNotEmpty()) onCatalogItemsAdded(addedItems)
        }
        if (updateHistory) scannerHistory = saveScannerHistory(context, result, german)
        return result to addedItems
    }

    fun applyPendingText() {
        applyScanText(pendingText, pendingMatches)
    }

    fun applyRecognizedText(text: String) {
        val correctedText = applyLearnedCorrections(text, learnedRules)
        recognizedText = correctedText
        pendingText = ""
        pendingMatches = emptyList()
        applyScanText(correctedText, previewMatchesForText(correctedText))
    }

    fun undoLastScan() {
        if (lastAppliedMatches.isEmpty()) return
        lastAppliedMatches.forEach { match ->
            when (match.state) {
            OcrMatchState.ADDED_TO_COLLECTION -> items.removeAll { it.name == match.itemName && it.isNew }
            OcrMatchState.NEWLY_CHECKED -> items.firstOrNull { it.name == match.itemName }
                ?.components
                ?.let { components ->
                    val index = components.indexOfFirst { it.name == match.componentName }
                    if (index >= 0) {
                        components[index] = components[index].copy(checked = false)
                    }
                }
            OcrMatchState.ALREADY_CHECKED,
            OcrMatchState.DETECTED_ONLY -> Unit
        }
        }
        matches = emptyList()
        lastAppliedMatches = emptyList()
        onProgressChanged()
    }

    fun saveLearningRule() {
        val wrong = correctionFrom.trim()
        val correct = correctionTo.trim()
        if (wrong.length < 2 || correct.length < 2) return
        learnedRules = saveOcrLearningRule(context, OcrLearningRule(wrong, correct))
        correctionFrom = ""
        correctionTo = ""
        if (recognizedText.isNotBlank()) previewText(recognizedText)
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(buildScannerHistoryExport(scannerHistory, learnedRules, german).toByteArray(Charsets.UTF_8))
            } ?: error(if (german) "Exportdatei konnte nicht geöffnet werden." else "Export file could not be opened.")
        }.onFailure { error ->
            errorMessage = error.localizedMessage ?: if (german) "Scanner-Verlauf konnte nicht exportiert werden." else "Scanner history could not be exported."
        }
    }

    fun startScannerRun(requests: List<ScannerFolderImage>, fromFolder: Boolean) {
        if (isScanning || requests.isEmpty()) return
        selectedImageUris = requests.map { it.uri }.take(8)
        lastScanImageCount = requests.size
        errorMessage = null
        recognizedText = ""
        matches = emptyList()
        pendingText = ""
        pendingMatches = emptyList()
        manualCorrection = ""
        isScanning = true
        scannedImageCount = 0
        scanJob = scanScope.launch {
            val scannedTexts = mutableListOf<String>()
            val combinedMatches = mutableListOf<OcrComponentMatch>()
            val addedItems = linkedMapOf<String, WarframeItem>()
            val completedFolderKeys = mutableSetOf<String>()
            var firstError: Throwable? = null
            var collectionChanged = false
            var changesPersisted = false

            fun persistProcessedChanges() {
                if (changesPersisted) return
                if (collectionChanged) {
                    if (addedItems.isNotEmpty()) onCatalogItemsAdded(addedItems.values.toList())
                    onProgressChanged()
                }
                if (completedFolderKeys.isNotEmpty()) {
                    val processed = scannerFolderPreferences
                        .getStringSet("processed", emptySet()).orEmpty().toMutableSet()
                    processed += completedFolderKeys
                    scannerFolderPreferences.edit().putStringSet("processed", processed).apply()
                }
                changesPersisted = true
            }

            try {
                requests.forEach { request ->
                    try {
                        val prepared = withContext(Dispatchers.IO) { prepareOcrImage(context, request.uri) }
                        val rawText = recognizeText(scanner, prepared)
                        if (rawText.isNotBlank()) {
                            val correctedText = withContext(Dispatchers.Default) {
                                applyLearnedCorrections(rawText, learnedRules)
                            }
                            val preview = withContext(Dispatchers.Default) {
                                previewMatchesForText(correctedText)
                            }
                            val (imageMatches, imageAdditions) = applyScanText(
                                correctedText,
                                preview,
                                notifyChanges = false,
                                updateHistory = false
                            )
                            scannedTexts += correctedText
                            combinedMatches += imageMatches
                            imageAdditions.forEach { addedItems[normalizeOcr(it.name)] = it }
                            collectionChanged = collectionChanged || imageMatches.any {
                                it.state == OcrMatchState.NEWLY_CHECKED ||
                                    it.state == OcrMatchState.ADDED_TO_COLLECTION
                            }
                        }
                        if (fromFolder && request.key.isNotBlank()) completedFolderKeys += request.key
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (error: Exception) {
                        firstError = firstError ?: error
                    }
                    scannedImageCount += 1
                    yield()
                }
                val mergedText = scannedTexts.joinToString("\n\n").take(MAX_RECOGNIZED_TEXT_CHARS)
                val finalMatches = combinedMatches
                    .distinctBy { "${it.itemName}\u0000${it.componentName}" }
                    .take(MAX_RESULT_MATCHES)
                recognizedText = mergedText
                matches = finalMatches
                lastAppliedMatches = finalMatches.filter {
                    it.state == OcrMatchState.NEWLY_CHECKED || it.state == OcrMatchState.ADDED_TO_COLLECTION
                }
                persistProcessedChanges()
                if (finalMatches.isNotEmpty()) {
                    scannerHistory = saveScannerHistory(context, finalMatches, german)
                }
                if (mergedText.isBlank()) {
                    errorMessage = firstError?.localizedMessage
                        ?: if (german) "Aus den Bildern konnte kein Text gelesen werden." else "No text could be read from the images."
                } else {
                    errorMessage = if (firstError != null) {
                        if (german) "Mindestens ein Bild konnte nicht gelesen werden; die übrigen Ergebnisse sind verfügbar."
                        else "At least one image could not be read; the other results are available."
                    } else null
                }
                if (fromFolder) {
                    folderPendingCount = (folderPendingCount - requests.size).coerceAtLeast(0)
                    folderStatus = if (german) {
                        "${requests.size} neue Ordnerbilder verarbeitet."
                    } else "Processed ${requests.size} new folder images."
                }
            } catch (cancelled: CancellationException) {
                persistProcessedChanges()
                errorMessage = if (german) "Scan abgebrochen. Bereits verarbeitete Bilder bleiben übernommen." else "Scan cancelled. Images already processed remain applied."
            } finally {
                isScanning = false
                scanJob = null
            }
        }
    }

    fun checkScannerFolder() {
        val treeUri = scannerFolderUri ?: return
        if (isScanning || folderChecking) return
        folderChecking = true
        scanScope.launch {
            val folderResult = withContext(Dispatchers.IO) {
                runCatching { scannerFolderImages(context, treeUri) }
            }
            folderChecking = false
            val folderImages = folderResult.getOrElse {
                folderStatus = if (german) "Ordner konnte nicht gelesen werden. Bitte erneut auswählen." else "Folder could not be read. Please choose it again."
                return@launch
            }
            val processed = scannerFolderPreferences.getStringSet("processed", emptySet()).orEmpty()
            val pending = folderImages.filterNot { it.key in processed }
            folderPendingCount = pending.size
            if (pending.isNotEmpty()) {
                startScannerRun(pending.take(MAX_FOLDER_IMAGES_PER_RUN), fromFolder = true)
            } else {
                folderStatus = if (german) "Ordner ist aktuell – keine neuen Bilder." else "Folder is up to date — no new images."
            }
        }
    }

    val folderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        if (scannerFolderUri != uri) {
            scannerFolderPreferences.edit().remove("processed").apply()
        }
        scannerFolderUri = uri
        scannerFolderPreferences.edit().putString("tree_uri", uri.toString()).apply()
        folderStatus = if (german) "Inventarordner gespeichert. Neue Bilder werden automatisch gesucht." else "Inventory folder saved. New images will be detected automatically."
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        val acceptedUris = uris.take(8)
        acceptedUris.forEach { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        startScannerRun(acceptedUris.map { ScannerFolderImage(it, "") }, fromFolder = false)
        if (uris.size > acceptedUris.size) {
            errorMessage = if (german) "Zum Schutz des Speichers werden höchstens 8 direkt ausgewählte Bilder verarbeitet. Für große Mengen nutze den Inventarordner."
            else "Up to 8 directly selected images are processed. Use the inventory folder for large batches."
        }
    }

    val currentScanning by rememberUpdatedState(isScanning)
    LaunchedEffect(scannerFolderUri, scannerMode, latestCatalogLoading) {
        if (scannerFolderUri == null || latestCatalogLoading) return@LaunchedEffect
        while (true) {
            if (!currentScanning) checkScannerFolder()
            delay(15_000)
        }
    }

    val screenScrollState = rememberScrollState()

    Box(Modifier.fillMaxSize()) {
    Column(
        Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .widthIn(max = 980.dp)
            .align(Alignment.TopCenter)
            .background(Color.Transparent)
            .verticalScroll(screenScrollState)
            .padding(if (compact) 10.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 14.dp)
    ) {
        Card(Modifier.fillMaxWidth(), shape = AppShapes.Large, colors = CardDefaults.cardColors(containerColor = AppColors.HudPanel)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (german) "ARSENAL-SCAN" else "ARSENAL SCAN", color = AppColors.OrokinGold, style = MaterialTheme.typography.labelLarge)
                Text(if (german) "Screenshot-Komponenten abgleichen" else "Match screenshot components", color = AppColors.TextPrimary, style = MaterialTheme.typography.headlineSmall)
                Text(
                    if (german) "Du kannst einen oder mehrere Screenshots auswählen. Standardmäßig erkennt die App automatisch Komponenten, Prime-Teile, Relikte, Foundry-, Mod- und Missionszeilen."
                    else "You can select one or more screenshots. By default, the app detects components, prime parts, relics, Foundry, mod, and mission lines automatically.",
                    color = AppColors.TextSecondary
                )
                Text(
                    text = if (latestCatalogLoading) {
                        if (german) "Aktueller Neuheiten-Katalog wird geladen …" else "Loading the latest item catalog …"
                    } else {
                        if (german) "Neuheiten-Erkennung bereit · ${catalog.size} Gegenstände" else "New-item detection ready · ${catalog.size} items"
                    },
                    color = if (latestCatalogLoading) AppColors.TextSecondary else Color(0xFF55E6B5),
                    style = MaterialTheme.typography.bodySmall
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    scannerModes(german).forEach { (mode, label) ->
                        AssistChip(
                            onClick = { scannerMode = mode },
                            label = { Text(label) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (scannerMode == mode) AppColors.EnergyCyan.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.05f),
                                labelColor = if (scannerMode == mode) AppColors.EnergyCyan else AppColors.TextSecondary
                            )
                        )
                    }
                }
                Text(scannerModeDescription(scannerMode, german), color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                Button(
                    onClick = { launcher.launch(arrayOf("image/*")) },
                    enabled = !isScanning,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.EnergyCyan, contentColor = Color(0xFF07131C)),
                    shape = AppShapes.Small
                ) {
                    Icon(Icons.Default.ImageSearch, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (german) "Inventarbilder auswählen" else "Choose inventory images")
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.Small,
                    color = Color.White.copy(alpha = 0.05f)
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            if (german) "Automatischer Inventarordner" else "Automatic inventory folder",
                            color = AppColors.OrokinGold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            if (scannerFolderUri == null) {
                                if (german) "Wähle einmal einen Ordner. Solange der Scanner geöffnet ist, sucht die App alle 15 Sekunden nach neuen Screenshots und verarbeitet jedes Bild nur einmal."
                                else "Choose a folder once. While the scanner is open, the app checks for new screenshots every 15 seconds and processes each image once."
                            } else {
                                if (german) "Ordner aktiv · $folderPendingCount neue Bilder vorgemerkt"
                                else "Folder active · $folderPendingCount new images queued"
                            },
                            color = AppColors.TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { folderLauncher.launch(null) },
                                enabled = !isScanning && !folderChecking,
                                shape = AppShapes.Small
                            ) {
                                Icon(Icons.Default.CreateNewFolder, null)
                                Spacer(Modifier.width(8.dp))
                                Text(if (german) "Ordner auswählen" else "Choose folder")
                            }
                            if (scannerFolderUri != null) {
                                OutlinedButton(
                                    onClick = { checkScannerFolder() },
                                    enabled = !isScanning && !folderChecking,
                                    shape = AppShapes.Small
                                ) {
                                    Icon(Icons.Default.Refresh, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (german) "Jetzt prüfen" else "Check now")
                                }
                            }
                        }
                        folderStatus?.let {
                            Text(it, color = Color(0xFF55E6B5), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                if (selectedImageUris.isNotEmpty()) {
                    SelectedScreenshotStrip(selectedImageUris, german)
                }
                if (recognizedText.isNotBlank()) {
                    val previewCount = pendingMatches.size
                    val newCount = matches.count { it.state == OcrMatchState.NEWLY_CHECKED || it.state == OcrMatchState.ADDED_TO_COLLECTION }
                    val existingCount = matches.count { it.state == OcrMatchState.ALREADY_CHECKED }
                    val detectedOnlyCount = matches.count { it.state == OcrMatchState.DETECTED_ONLY }
                    val quality = ocrQualityScore(recognizedText, pendingMatches)
                    val imageCountLabel = if (german) {
                        "$selectedImageCount Bild${if (selectedImageCount == 1) "" else "er"}"
                    } else {
                        "$selectedImageCount image${if (selectedImageCount == 1) "" else "s"}"
                    }
                    Text(
                        if (pendingText.isNotBlank()) {
                            if (german) "$imageCountLabel · $previewCount mögliche Treffer · OCR-Qualität $quality %" else "$imageCountLabel · $previewCount possible matches · OCR quality $quality %"
                        } else if (german) {
                            "$imageCountLabel · $newCount neu abgehakt · $existingCount bereits vorhanden · $detectedOnlyCount nur gefunden"
                        } else {
                            "$imageCountLabel · $newCount newly checked · $existingCount already owned · $detectedOnlyCount found only"
                        },
                        color = AppColors.EnergyCyan
                    )
                }
                if (learnedRules.isNotEmpty()) {
                    Text(
                        if (german) "${learnedRules.size} gelernte OCR-Regel${if (learnedRules.size == 1) "" else "n"} aktiv"
                        else "${learnedRules.size} learned OCR rule${if (learnedRules.size == 1) "" else "s"} active",
                        color = AppColors.TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Card(Modifier.fillMaxWidth(), shape = AppShapes.Large, colors = CardDefaults.cardColors(containerColor = AppColors.HudPanel)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 360.dp, max = 720.dp)
                    .padding(16.dp)
            ) {
                when {
                    isScanning -> Column(
                        Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = AppColors.EnergyCyan)
                        LinearProgressIndicator(
                            progress = { if (selectedImageCount == 0) 0f else scannedImageCount.toFloat() / selectedImageCount },
                            modifier = Modifier.fillMaxWidth(0.65f),
                            color = AppColors.EnergyCyan,
                            trackColor = Color.White.copy(alpha = 0.08f)
                        )
                        Text(
                            if (german) "Bild ${scannedImageCount + 1} von $selectedImageCount wird verarbeitet …"
                            else "Processing image ${scannedImageCount + 1} of $selectedImageCount …",
                            color = AppColors.TextSecondary
                        )
                        OutlinedButton(onClick = { scanJob?.cancel() }, shape = AppShapes.Small) {
                            Text(if (german) "Scan abbrechen" else "Cancel scan")
                        }
                    }
                    pendingText.isNotBlank() -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (german) "Treffer prüfen" else "Review matches", color = AppColors.OrokinGold, style = MaterialTheme.typography.labelLarge)
                        if (errorMessage != null) {
                            Text(errorMessage.orEmpty(), color = Color(0xFFFF9B8F), style = MaterialTheme.typography.bodySmall)
                        }
                        BeforeAfterSummary(pendingMatches, german)
                        if (pendingMatches.isEmpty()) {
                            Text(
                                if (german) "OCR-Text wurde gelesen, aber noch keinem bekannten Eintrag sicher zugeordnet. Du kannst den Scan trotzdem bestätigen oder unten eine Korrektur ergänzen."
                                else "OCR text was read, but not safely matched to a known entry yet. You can still confirm the scan or add a correction below.",
                                color = AppColors.TextSecondary
                            )
                        } else {
                            pendingMatches.forEach { OcrMatchRow(it, items, german) }
                            val uncertainCount = pendingMatches.count { it.state == OcrMatchState.ALREADY_CHECKED }
                            if (uncertainCount > 0) {
                                Text(
                                    if (german) "$uncertainCount Treffer sind wahrscheinlich bereits vorhanden und werden separat angezeigt."
                                    else "$uncertainCount matches are probably already owned and are shown separately.",
                                    color = AppColors.TextSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Button(
                            onClick = { applyPendingText() },
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.EnergyCyan, contentColor = Color(0xFF07131C)),
                            shape = AppShapes.Small
                        ) {
                            Text(if (german) "Scan bestätigen und übernehmen" else "Confirm and apply scan")
                        }
                        OcrLearningEditor(
                            wrong = correctionFrom,
                            correct = correctionTo,
                            german = german,
                            onWrongChanged = { correctionFrom = it },
                            onCorrectChanged = { correctionTo = it },
                            onSave = { saveLearningRule() }
                        )
                        OutlinedTextField(
                            value = manualCorrection,
                            onValueChange = { manualCorrection = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(if (german) "Manuelle Korrektur / erkannter Name" else "Manual correction / detected name") },
                            minLines = 2
                        )
                        if (manualCorrection.isNotBlank()) {
                            Button(
                                onClick = { previewText("$recognizedText\n$manualCorrection") },
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.OrokinGold, contentColor = Color(0xFF17120A)),
                                shape = AppShapes.Small
                            ) {
                                Text(if (german) "Korrektur erneut prüfen" else "Review correction")
                            }
                        }
                        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = AppColors.CardBorder)
                        Text(if (german) "Erkannter Rohtext" else "Recognized raw text", color = AppColors.OrokinGold, style = MaterialTheme.typography.labelLarge)
                        Text(recognizedText, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                    errorMessage != null -> Column(
                        Modifier.align(Alignment.Center).padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(errorMessage.orEmpty(), color = Color(0xFFFF9B8F))
                        TextButton(onClick = { launcher.launch(arrayOf("image/*")) }) {
                            Text(if (german) "Bilder erneut scannen" else "Scan images again")
                        }
                    }
                    selectedImageCount == 0 -> EmptyScannerText(if (german) "Wähle einen oder mehrere Warframe-Inventar-Screenshots aus." else "Choose one or more Warframe inventory screenshots.")
                    matches.isEmpty() && scannerHistory.isEmpty() -> EmptyScannerText(
                        if (german) "Keine eindeutige Komponente gefunden. Der erkannte Text wurde nicht automatisch verändert."
                        else "No unambiguous component was found. Nothing was changed automatically."
                    )
                    else -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        matches.forEach { OcrMatchRow(it, items, german) }
                        if (lastAppliedMatches.isNotEmpty()) {
                            Button(
                                onClick = { undoLastScan() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9B8F), contentColor = Color(0xFF1C0707)),
                                shape = AppShapes.Small
                            ) {
                                Text(if (german) "Letzten Scan rückgängig" else "Undo last scan")
                            }
                        }
                        if (scannerHistory.isNotEmpty()) {
                            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = AppColors.CardBorder)
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(if (german) "Scanner-Verlauf" else "Scanner history", color = AppColors.OrokinGold, style = MaterialTheme.typography.labelLarge)
                                TextButton(onClick = { exportLauncher.launch(if (german) "tennofreunde-scanner-verlauf.txt" else "tennofreunde-scanner-history.txt") }) {
                                    Text(if (german) "Export" else "Export")
                                }
                            }
                            scannerHistory.forEach { entry ->
                                Text(entry.title, color = AppColors.TextPrimary, style = MaterialTheme.typography.bodySmall)
                                Text(entry.detail, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if (recognizedText.isNotBlank()) {
                            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = AppColors.CardBorder)
                            Text(if (german) "Erkannter Rohtext" else "Recognized raw text", color = AppColors.OrokinGold, style = MaterialTheme.typography.labelLarge)
                            Text(recognizedText, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun EmptyScannerText(text: String) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Info, null, tint = AppColors.OrokinGold)
        Spacer(Modifier.height(10.dp))
        Text(text, color = AppColors.TextSecondary)
    }
}

@Composable
private fun SelectedScreenshotStrip(uris: List<Uri>, german: Boolean) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            if (german) "${uris.size} ausgewählte Screenshot${if (uris.size == 1) "" else "s"}" else "${uris.size} selected screenshot${if (uris.size == 1) "" else "s"}",
            color = AppColors.TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uris.forEachIndexed { index, uri ->
                Surface(
                    modifier = Modifier.width(78.dp).height(104.dp),
                    shape = AppShapes.Small,
                    color = Color.White.copy(alpha = 0.06f),
                    tonalElevation = 0.dp
                ) {
                    Box {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(uri)
                                .size(234, 312)
                                .crossfade(false)
                                .build(),
                            contentDescription = if (german) "Ausgewählter Screenshot ${index + 1}" else "Selected screenshot ${index + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                            shape = AppShapes.Small,
                            color = AppColors.HudPanel.copy(alpha = 0.86f)
                        ) {
                            Text(
                                "${index + 1}",
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                color = AppColors.EnergyCyan,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BeforeAfterSummary(matches: List<OcrComponentMatch>, german: Boolean) {
    val addedItems = matches.count { it.state == OcrMatchState.ADDED_TO_COLLECTION }
    val newChecks = matches.count { it.state == OcrMatchState.NEWLY_CHECKED }
    val alreadyOwned = matches.count { it.state == OcrMatchState.ALREADY_CHECKED }
    val detectedOnly = matches.count { it.state == OcrMatchState.DETECTED_ONLY }
    Surface(shape = AppShapes.Small, color = Color.White.copy(alpha = 0.05f)) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryMetric(
                label = if (german) "Neue Items" else "New items",
                value = addedItems.toString(),
                modifier = Modifier.weight(1f)
            )
            SummaryMetric(
                label = if (german) "Neue Haken" else "New checks",
                value = newChecks.toString(),
                modifier = Modifier.weight(1f)
            )
            SummaryMetric(
                label = if (german) "Schon da" else "Owned",
                value = alreadyOwned.toString(),
                modifier = Modifier.weight(1f)
            )
            SummaryMetric(
                label = if (german) "Gefunden" else "Found",
                value = detectedOnly.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, color = AppColors.EnergyCyan, style = MaterialTheme.typography.titleMedium)
        Text(label, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun OcrLearningEditor(
    wrong: String,
    correct: String,
    german: Boolean,
    onWrongChanged: (String) -> Unit,
    onCorrectChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    Surface(shape = AppShapes.Small, color = Color.White.copy(alpha = 0.04f)) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                if (german) "OCR lernen lassen" else "Teach OCR correction",
                color = AppColors.OrokinGold,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                if (german) "Wenn ein Begriff falsch gelesen wird, speichert die App die Korrektur für spätere Scans."
                else "If a term is read incorrectly, the app saves the correction for later scans.",
                color = AppColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = wrong,
                    onValueChange = onWrongChanged,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text(if (german) "Falsch" else "Wrong") }
                )
                OutlinedTextField(
                    value = correct,
                    onValueChange = onCorrectChanged,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text(if (german) "Richtig" else "Correct") }
                )
            }
            Button(
                onClick = onSave,
                enabled = wrong.trim().length >= 2 && correct.trim().length >= 2,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.OrokinGold, contentColor = Color(0xFF17120A)),
                shape = AppShapes.Small
            ) {
                Text(if (german) "Regel speichern" else "Save rule")
            }
        }
    }
}

@Composable
private fun OcrMatchRow(match: OcrComponentMatch, items: List<WarframeItem>, german: Boolean) {
    val isDetectedOnly = match.state == OcrMatchState.DETECTED_ONLY
    val isNew = match.state != OcrMatchState.ALREADY_CHECKED && !isDetectedOnly
    val wasAdded = match.state == OcrMatchState.ADDED_TO_COLLECTION
    val collectionState = collectionComponentCheckedState(match, items)
    Surface(shape = AppShapes.Small, color = if (isNew) Color(0x332FD6A2) else Color.White.copy(alpha = 0.05f)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(if (isNew) Icons.Default.CheckCircle else Icons.Default.RemoveDone, null, tint = if (isNew) Color(0xFF55E6B5) else AppColors.OrokinGold)
            Column(Modifier.weight(1f)) {
                Text(match.itemName, color = AppColors.TextPrimary)
                Text(match.componentName, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                if (!isDetectedOnly) {
                    Text(
                        text = when (collectionState) {
                            true -> if (german) "Sammlung: Haken gesetzt" else "Collection: checked"
                            false -> if (german) "Sammlung: nicht gesetzt" else "Collection: not checked"
                            null -> if (german) "Sammlung: keine Zuordnung" else "Collection: no match"
                        },
                        color = if (collectionState == true) Color(0xFF55E6B5) else Color(0xFFFFC36E),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Text(
                if (isDetectedOnly) {
                    if (german) "GEFUNDEN" else "FOUND"
                } else if (wasAdded) {
                    if (german) "ERGÄNZT" else "ADDED"
                } else if (isNew) {
                    if (german) "ABGEHAKT" else "CHECKED"
                } else {
                    if (german) "VORHANDEN" else "OWNED"
                },
                color = if (isNew) Color(0xFF55E6B5) else AppColors.OrokinGold,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

fun addMissingCatalogItems(
    text: String,
    items: MutableList<WarframeItem>,
    catalog: List<PrimeCatalogItem>
): List<OcrComponentMatch> {
    val normalizedText = normalizeOcr(text)
    if (normalizedText.length < 5) return emptyList()
    val added = mutableListOf<OcrComponentMatch>()
    val existingByName = items.associateByTo(linkedMapOf()) { normalizeOcr(it.name) }
    val ownedWarframes = ownedWarframeNames(text)

    catalog.forEach { catalogItem ->
        val partMatches = catalogItem.item.components.filter { component ->
            componentTextMatches(normalizedText, catalogItem.item.name, component.name)
        }
        val matchingComponents = if (
            partMatches.isEmpty() &&
            catalogItem.item.type.equals("warframe", ignoreCase = true) &&
            normalizeOcr(catalogItem.item.name) in ownedWarframes
        ) catalogItem.item.components.toList() else partMatches
        if (matchingComponents.isEmpty()) return@forEach

        val normalizedItemName = normalizeOcr(catalogItem.item.name)
        val existingItem = existingByName[normalizedItemName]
        if (existingItem == null) {
            val matchingNames = matchingComponents.mapTo(mutableSetOf()) { normalizeOcr(it.name) }
            val addedItem = catalogItem.item.copy(
                infoFields = catalogItem.item.infoFields.toMutableList(),
                components = mutableStateListOf(
                    *catalogItem.item.components.map { component ->
                        component.copy(checked = normalizeOcr(component.name) in matchingNames)
                    }.toTypedArray()
                )
            )
            items.add(addedItem)
            existingByName[normalizedItemName] = addedItem
            matchingComponents.forEach { component ->
                added += OcrComponentMatch(addedItem.name, component.name, OcrMatchState.ADDED_TO_COLLECTION)
            }
        } else {
            if (existingItem.catalogSource == "scanner_discovered") {
                existingItem.type = catalogItem.item.type
                existingItem.tabName = catalogItem.item.tabName
                existingItem.subTabName = catalogItem.item.subTabName
                existingItem.imageName = catalogItem.item.imageName
                existingItem.catalogSource = catalogItem.item.catalogSource
            }
            catalogItem.item.components.forEach { catalogComponent ->
                if (matchingComponentIndex(existingItem, catalogComponent.name) < 0) {
                    existingItem.components.add(catalogComponent.copy(checked = false))
                }
            }
            matchingComponents.forEach { catalogComponent ->
                val index = matchingComponentIndex(existingItem, catalogComponent.name)
                if (index >= 0) {
                    val current = existingItem.components[index]
                    val state = if (current.checked) OcrMatchState.ALREADY_CHECKED else OcrMatchState.NEWLY_CHECKED
                    if (!current.checked) existingItem.components[index] = current.copy(checked = true)
                    added += OcrComponentMatch(existingItem.name, current.name, state)
                }
            }
        }
    }
    return added
}

fun addUnknownPrimeItems(
    text: String,
    items: MutableList<WarframeItem>,
    catalog: List<PrimeCatalogItem>
): List<OcrComponentMatch> {
    val knownNames = items.map { it.name } + catalog.map { it.item.name }
    val discovered = discoverUnknownPrimeItems(text, knownNames)
    return discovered.flatMap { saved ->
        val item = saved.toWarframeItem().apply { catalogSource = "scanner_discovered" }
        items.add(item)
        item.components.filter { it.checked }.map { component ->
            OcrComponentMatch(item.name, component.name, OcrMatchState.ADDED_TO_COLLECTION)
        }
    }
}

private fun previewUnknownPrimeItems(
    text: String,
    items: List<WarframeItem>,
    catalog: List<PrimeCatalogItem>
): List<OcrComponentMatch> {
    val knownNames = items.map { it.name } + catalog.map { it.item.name }
    return discoverUnknownPrimeItems(text, knownNames).flatMap { item ->
        item.components.filter { it.checked }.map { component ->
            OcrComponentMatch(item.name, component.name, OcrMatchState.ADDED_TO_COLLECTION)
        }
    }
}

private fun previewCatalogItems(
    text: String,
    items: List<WarframeItem>,
    catalog: List<PrimeCatalogItem>
): List<OcrComponentMatch> {
    val normalizedText = normalizeOcr(text)
    if (normalizedText.length < 5) return emptyList()
    val existingByName = items.associateBy { normalizeOcr(it.name) }
    val ownedWarframes = ownedWarframeNames(text)
    return catalog.flatMap { catalogItem ->
        val existingItem = existingByName[normalizeOcr(catalogItem.item.name)]
        val ownedWarframe = catalogItem.item.type.equals("warframe", ignoreCase = true) &&
            normalizeOcr(catalogItem.item.name) in ownedWarframes
        catalogItem.item.components.mapNotNull { component ->
            if (!ownedWarframe && !componentTextMatches(normalizedText, catalogItem.item.name, component.name)) return@mapNotNull null
            val existingIndex = existingItem?.let { matchingComponentIndex(it, component.name) } ?: -1
            val state = when {
                existingItem == null || existingIndex < 0 -> OcrMatchState.ADDED_TO_COLLECTION
                existingItem.components[existingIndex].checked -> OcrMatchState.ALREADY_CHECKED
                else -> OcrMatchState.NEWLY_CHECKED
            }
            OcrComponentMatch(catalogItem.item.name, component.name, state)
        }
    }
}

private fun previewOcrComponents(text: String, items: List<WarframeItem>): List<OcrComponentMatch> {
    val normalizedText = normalizeOcr(text)
    if (normalizedText.length < 4) return emptyList()
    val candidates = items.flatMap { item -> item.components.map { component -> item to component } }
    val matches = candidates.filter { (item, component) ->
        componentTextMatches(normalizedText, item.name, component.name)
    }

    return matches.distinctBy { (item, component) -> "${item.name}\u0000${component.name}" }
        .map { (item, component) ->
            val state = if (component.checked) OcrMatchState.ALREADY_CHECKED else OcrMatchState.NEWLY_CHECKED
            OcrComponentMatch(item.name, component.name, state)
        }
}

private fun previewRelicInventory(text: String): List<OcrComponentMatch> {
    val relicPattern = Regex("\\b(Lith|Meso|Neo|Axi|Requiem)\\s+[A-Z0-9]{1,4}\\b", RegexOption.IGNORE_CASE)
    return relicPattern.findAll(text)
        .map { match -> match.value.trim().replaceFirstChar(Char::uppercase) }
        .distinct()
        .take(30)
        .map { relic -> OcrComponentMatch(relic, "Relikt-Inventar", OcrMatchState.NEWLY_CHECKED) }
        .toList()
}

private fun previewPrimeParts(text: String, catalog: List<PrimeCatalogItem>): List<OcrComponentMatch> {
    val normalizedText = normalizeOcr(text)
    if (normalizedText.length < 5) return emptyList()
    return catalog.flatMap { catalogItem ->
        catalogItem.item.components.mapNotNull { component ->
            if (componentTextMatches(normalizedText, catalogItem.item.name, component.name)) {
                OcrComponentMatch(catalogItem.item.name, component.name, OcrMatchState.NEWLY_CHECKED)
            } else {
                null
            }
        }
    }.distinctBy { "${it.itemName}\u0000${it.componentName}" }.take(30)
}

private fun previewAllMatches(
    text: String,
    items: List<WarframeItem>,
    catalog: List<PrimeCatalogItem>
): List<OcrComponentMatch> {
    val componentMatches = previewCatalogItems(text, items, catalog) +
        previewOcrComponents(text, items) +
        previewVisibleCollectionCardComponents(text, items) +
        previewUnknownPrimeItems(text, items, catalog)
    val referenceMatches = previewRelicInventory(text) +
        previewPrimeParts(text, catalog) +
        previewKeywordLines(text, "Foundry", listOf("building", "claim", "complete", "hour", "minute", "day", "bau", "fertig")) +
        previewKeywordLines(text, "Mods", listOf("mod", "rank", "rang", "fusion", "capacity", "drain")) +
        previewKeywordLines(text, "Mission", listOf("reward", "belohnung", "credits", "endo", "xp", "affinity", "ressource"))

    return (componentMatches + referenceMatches)
        .distinctBy { "${it.itemName}\u0000${it.componentName}" }
        .take(60)
}

fun previewVisibleCollectionCardComponents(
    text: String,
    items: List<WarframeItem>
): List<OcrComponentMatch> {
    val normalizedText = normalizeOcr(text)
    if ("komponenten" !in normalizedText && "components" !in normalizedText) return emptyList()

    return items
        .filter { item -> normalizedText.contains(normalizeOcr(item.name)) }
        .flatMap { item ->
            item.components.mapNotNull { component ->
                val aliases = componentAliases(item.name, component.name)
                if (aliases.any { alias -> alias.isNotBlank() && normalizedText.contains(alias) }) {
                    val state = if (component.checked) OcrMatchState.ALREADY_CHECKED else OcrMatchState.NEWLY_CHECKED
                    OcrComponentMatch(item.name, component.name, state)
                } else {
                    null
                }
            }
        }
        .distinctBy { "${it.itemName}\u0000${it.componentName}" }
        .take(40)
}

private fun scannerModes(german: Boolean): List<Pair<ScannerMode, String>> {
    return listOf(
        ScannerMode.ALL to if (german) "Alles erkennen" else "Detect all",
        ScannerMode.COMPONENTS to if (german) "Komponenten" else "Components",
        ScannerMode.RELICS to if (german) "Relikte" else "Relics",
        ScannerMode.PRIME_PARTS to if (german) "Prime-Teile" else "Prime parts",
        ScannerMode.FOUNDRY to "Foundry",
        ScannerMode.MODS to "Mods",
        ScannerMode.MISSION_END to if (german) "Mission-Ende" else "Mission end"
    )
}

private fun scannerModeDescription(mode: ScannerMode, german: Boolean): String {
    return when (mode) {
        ScannerMode.ALL -> if (german) {
            "Automatisch: Sammlungskomponenten werden übernommen, andere gefundene Zeilen werden als Scan-Ergebnis angezeigt."
        } else {
            "Automatic: collection components are applied; other detected lines are shown as scan results."
        }
        ScannerMode.COMPONENTS -> if (german) {
            "Sucht bekannte Komponenten aus deiner Sammlung und hakt neue Treffer nach Bestätigung ab."
        } else {
            "Finds known collection components and checks new matches after confirmation."
        }
        ScannerMode.RELICS -> if (german) {
            "Sucht Reliktnamen und Relikt-Inventar im Screenshot."
        } else {
            "Finds relic names and relic inventory text in the screenshot."
        }
        ScannerMode.PRIME_PARTS -> if (german) {
            "Sucht Prime-Warframes und Prime-Waffen mit ihren Teilen."
        } else {
            "Finds Prime warframes and weapons with their parts."
        }
        ScannerMode.FOUNDRY -> if (german) {
            "Sucht Foundry-Status wie Bauzeit, fertig oder abholen."
        } else {
            "Finds Foundry status lines like build time, complete, or claim."
        }
        ScannerMode.MODS -> if (german) {
            "Sucht Mod-, Rang-, Fusion- und Kapazitätszeilen."
        } else {
            "Finds mod, rank, fusion, and capacity lines."
        }
        ScannerMode.MISSION_END -> if (german) {
            "Sucht Belohnungen, Credits, Endo, XP und Ressourcen."
        } else {
            "Finds rewards, credits, Endo, XP, and resources."
        }
    }
}

private fun previewKeywordLines(text: String, label: String, keywords: List<String>): List<OcrComponentMatch> {
    return text
        .lines()
        .map { it.trim() }
        .filter { line -> line.length >= 3 && keywords.any { keyword -> line.contains(keyword, ignoreCase = true) } }
        .distinct()
        .take(30)
        .map { line -> OcrComponentMatch(line.take(42), label, OcrMatchState.NEWLY_CHECKED) }
}

private fun ocrQualityScore(text: String, matches: List<OcrComponentMatch>): Int {
    val textScore = (text.length / 4).coerceIn(0, 45)
    val matchScore = (matches.size * 12).coerceIn(0, 45)
    val noisePenalty = if (text.count { it == '?' || it == '#' } > 5) 15 else 0
    return (10 + textScore + matchScore - noisePenalty).coerceIn(0, 100)
}

fun reconcileOcrComponents(text: String, items: List<WarframeItem>): List<OcrComponentMatch> {
    val normalizedText = normalizeOcr(text)
    if (normalizedText.length < 4) return emptyList()
    val candidates = items.flatMap { item ->
        item.components.mapIndexed { index, component ->
            ComponentCandidate(item, index, component, componentCandidate(item.name, component.name))
        }
    }
    val matches = candidates.filter { match ->
        componentTextMatches(normalizedText, match.item.name, match.component.name)
    }.toMutableList()
    items.filter { item ->
        item.type.equals("warframe", ignoreCase = true) && textHasOwnedWarframeLine(text, item.name)
    }.forEach { item ->
        item.components.forEachIndexed { index, component ->
            if (matches.none { it.item === item && it.componentIndex == index }) {
                matches += ComponentCandidate(item, index, component, componentCandidate(item.name, component.name))
            }
        }
    }

    // A component may appear through more than one OCR line; only process each stored component once.
    return matches.distinctBy { match -> "${match.item.name}\u0000${match.component.name}" }
        .map { match ->
            val current = match.item.components.getOrNull(match.componentIndex) ?: match.component
            val state = if (current.checked) OcrMatchState.ALREADY_CHECKED else {
                match.item.components[match.componentIndex] = current.copy(checked = true)
                OcrMatchState.NEWLY_CHECKED
            }
            OcrComponentMatch(match.item.name, current.name, state)
        }
}

private fun ownedWarframeNames(text: String): Set<String> {
    val lines = text.lines().map(::normalizeOcr).filter { it.isNotBlank() }
    val partWords = setOf(
        "blueprint", "chassis", "neuroptics", "systems", "system",
        "lauf", "barrel", "receiver", "gehause", "stock", "schaft"
    )
    val rankSuffix = Regex(" (rang|rank) [0-9]+$")
    return lines.indices.mapNotNullTo(linkedSetOf()) { index ->
        val line = lines[index]
        val nextLineHasPart = lines.getOrNull(index + 1)?.split(" ")?.any { it in partWords } == true
        if (nextLineHasPart) null else line.replace(rankSuffix, "").trim().takeIf { it.isNotBlank() }
    }
}

private fun textHasOwnedWarframeLine(text: String, itemName: String): Boolean =
    normalizeOcr(itemName) in ownedWarframeNames(text)

fun applyOcrMatchesToCollection(
    matches: List<OcrComponentMatch>,
    items: List<WarframeItem>
): List<OcrComponentMatch> {
    val itemsByName = items.groupBy { normalizeOcr(it.name) }
    return matches.flatMap { match ->
        if (match.state == OcrMatchState.DETECTED_ONLY) return@flatMap emptyList()
        itemsByName[normalizeOcr(match.itemName)].orEmpty().mapNotNull { item ->
            val componentIndex = matchingComponentIndex(item, match.componentName)
            if (componentIndex < 0) return@mapNotNull null
            val component = item.components[componentIndex]
            if (component.checked) {
                OcrComponentMatch(item.name, component.name, OcrMatchState.ALREADY_CHECKED)
            } else {
                item.components[componentIndex] = component.copy(checked = true)
                OcrComponentMatch(item.name, component.name, OcrMatchState.NEWLY_CHECKED)
            }
        }
    }
}

private fun matchingCollectionItems(items: List<WarframeItem>, itemName: String): List<WarframeItem> {
    val scanned = normalizeOcr(itemName)
    return items.filter { normalizeOcr(it.name) == scanned }
}

private fun collectionComponentCheckedState(match: OcrComponentMatch, items: List<WarframeItem>): Boolean? {
    val states = matchingCollectionItems(items, match.itemName).mapNotNull { item ->
        val componentIndex = matchingComponentIndex(item, match.componentName)
        item.components.getOrNull(componentIndex)?.checked
    }
    return when {
        states.isEmpty() -> null
        states.all { it } -> true
        else -> false
    }
}

private fun matchingComponentIndex(item: WarframeItem, scannedComponentName: String): Int {
    val scanned = normalizeOcr(scannedComponentName)
    val scannedWithItem = normalizeOcr("${item.name} $scannedComponentName")
    return item.components.indexOfFirst { component ->
        val stored = normalizeOcr(component.name)
        val storedWithItem = normalizeOcr("${item.name} ${component.name}")
        val aliases = componentAliases(item.name, component.name)
        val scannedNamesSpecificPart = scannedMentionsSpecificPart(scanned) || scannedMentionsSpecificPart(scannedWithItem)
        val mainBlueprint = isMainBlueprintComponent(item.name, component.name)
        if (mainBlueprint && scannedNamesSpecificPart) return@indexOfFirst false
        stored == scanned ||
            storedWithItem == scannedWithItem ||
            scanned.contains(stored) ||
            aliases.any { alias -> scanned.contains(alias) || scannedWithItem.contains(alias) } ||
            scannedWithItem.contains(storedWithItem) ||
            fuzzyContains(scannedWithItem, storedWithItem) ||
            fuzzyContains(storedWithItem, scannedWithItem)
    }
}

private fun isMainBlueprintComponent(itemName: String, componentName: String): Boolean {
    val item = normalizeOcr(itemName)
    val component = normalizeOcr(componentName)
    val withoutItem = component.removePrefix("$item ").trim()
    return withoutItem == "bp" || withoutItem == "blueprint"
}

private fun scannedMentionsSpecificPart(value: String): Boolean {
    return listOf("neuroptics", "systems", "system", "chassis").any { value.contains(it) }
}

private fun componentAliases(itemName: String, componentName: String): Set<String> {
    val item = normalizeOcr(itemName)
    val component = normalizeOcr(componentName)
    val withoutItem = component
        .removePrefix("$item ")
        .removeSuffix(" blueprint")
        .trim()
    val aliases = mutableSetOf(component, withoutItem)
    when (withoutItem) {
        "bp", "blueprint" -> aliases += setOf(
            "bp",
            "blueprint",
            "main blueprint",
            "main bp",
            "hauptblueprint",
            "haupt blueprint",
            "hauptblaupause",
            "haupt blaupause"
        )
        "neuroptik", "neuroptics" -> aliases += setOf("neuroptik", "neuroptics")
        "system", "systems" -> aliases += setOf("system", "systems", "systeme")
        "chassis" -> aliases += "chassis"
        "barrel" -> aliases += setOf("barrel", "lauf")
        "receiver" -> aliases += setOf("receiver", "gehause")
        "stock" -> aliases += setOf("stock", "schaft")
        "blade" -> aliases += setOf("blade", "klinge")
        "handle" -> aliases += setOf("handle", "griff")
        "grip" -> aliases += setOf("grip", "griff")
        "link" -> aliases += setOf("link", "verbindung")
        "string" -> aliases += setOf("string", "sehne")
        "upper limb" -> aliases += setOf("upper limb", "oberteil")
        "lower limb" -> aliases += setOf("lower limb", "unterteil")
        "pouch" -> aliases += setOf("pouch", "beutel")
        "ornament" -> aliases += setOf("ornament", "verzierung")
    }
    return aliases.filter { it.length >= 2 }.toSet()
}

private fun loadScannerHistory(context: android.content.Context): List<ScannerHistoryEntry> {
    return context.getSharedPreferences("scanner_history", android.content.Context.MODE_PRIVATE)
        .getStringSet("entries", emptySet())
        .orEmpty()
        .sortedDescending()
        .take(5)
        .mapNotNull { entry ->
            val parts = entry.split("|", limit = 3)
            if (parts.size == 3) ScannerHistoryEntry(parts[1], parts[2]) else null
        }
}

private fun saveScannerHistory(
    context: android.content.Context,
    matches: List<OcrComponentMatch>,
    german: Boolean
): List<ScannerHistoryEntry> {
    val prefs = context.getSharedPreferences("scanner_history", android.content.Context.MODE_PRIVATE)
    val current = prefs.getStringSet("entries", emptySet()).orEmpty()
    val newCount = matches.count { it.state == OcrMatchState.NEWLY_CHECKED || it.state == OcrMatchState.ADDED_TO_COLLECTION }
    val existingCount = matches.count { it.state == OcrMatchState.ALREADY_CHECKED }
    val detectedOnlyCount = matches.count { it.state == OcrMatchState.DETECTED_ONLY }
    val title = if (german) {
        "Scan: $newCount abgehakt, $existingCount vorhanden, $detectedOnlyCount gefunden"
    } else {
        "Scan: $newCount checked, $existingCount owned, $detectedOnlyCount found"
    }
    val detail = matches.take(4).joinToString(", ") { "${it.itemName} ${it.componentName}" }.ifBlank {
        if (german) "Keine Treffer übernommen" else "No matches applied"
    }
    val stored = (current + "${System.currentTimeMillis()}|$title|$detail")
        .sortedDescending()
        .take(5)
        .toSet()
    prefs.edit().putStringSet("entries", stored).apply()
    return loadScannerHistory(context)
}

private fun loadOcrLearningRules(context: android.content.Context): List<OcrLearningRule> {
    return context.getSharedPreferences("scanner_history", android.content.Context.MODE_PRIVATE)
        .getStringSet("ocr_rules", emptySet())
        .orEmpty()
        .mapNotNull { entry ->
            val parts = entry.split("|", limit = 2)
            if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
                OcrLearningRule(parts[0], parts[1])
            } else {
                null
            }
        }
        .sortedBy { it.wrong.lowercase() }
}

private fun saveOcrLearningRule(context: android.content.Context, rule: OcrLearningRule): List<OcrLearningRule> {
    val prefs = context.getSharedPreferences("scanner_history", android.content.Context.MODE_PRIVATE)
    val current = loadOcrLearningRules(context)
    val merged = (current.filterNot { it.wrong.equals(rule.wrong, ignoreCase = true) } + rule)
        .takeLast(50)
    prefs.edit()
        .putStringSet("ocr_rules", merged.map { "${it.wrong}|${it.correct}" }.toSet())
        .apply()
    return loadOcrLearningRules(context)
}

private fun applyLearnedCorrections(text: String, rules: List<OcrLearningRule>): String {
    return rules.fold(text) { current, rule ->
        current.replace(rule.wrong, rule.correct, ignoreCase = true)
    }
}

private fun buildScannerHistoryExport(
    history: List<ScannerHistoryEntry>,
    rules: List<OcrLearningRule>,
    german: Boolean
): String {
    val title = if (german) "TennoFreunde Scanner-Verlauf" else "TennoFreunde Scanner history"
    val scanTitle = if (german) "Scans" else "Scans"
    val ruleTitle = if (german) "Gelernte OCR-Regeln" else "Learned OCR rules"
    val noEntries = if (german) "Keine Einträge vorhanden." else "No entries available."
    return buildString {
        appendLine(title)
        appendLine("=".repeat(title.length))
        appendLine()
        appendLine(scanTitle)
        appendLine("-".repeat(scanTitle.length))
        if (history.isEmpty()) {
            appendLine(noEntries)
        } else {
            history.forEach { entry ->
                appendLine("- ${entry.title}: ${entry.detail}")
            }
        }
        appendLine()
        appendLine(ruleTitle)
        appendLine("-".repeat(ruleTitle.length))
        if (rules.isEmpty()) {
            appendLine(noEntries)
        } else {
            rules.forEach { rule ->
                appendLine("- ${rule.wrong} -> ${rule.correct}")
            }
        }
    }
}

private fun componentCandidate(itemName: String, componentName: String): String {
    val item = normalizeOcr(itemName)
    val component = normalizeOcr(componentName)
    return if (component.contains(item)) component else "$item $component"
}

private fun componentTextMatches(normalizedText: String, itemName: String, componentName: String): Boolean {
    val item = normalizeOcr(itemName)
    if (!fuzzyContains(normalizedText, item, 0.90)) return false
    if (normalizeOcr(componentName) in setOf("vorhanden", "gebaut")) {
        return presenceItemTextMatches(normalizedText, item)
    }
    val candidates = buildSet {
        add(componentCandidate(itemName, componentName))
        componentAliases(itemName, componentName).forEach { alias ->
            add(if (alias.startsWith("$item ")) alias else "$item $alias")
        }
    }.filter { it.length >= 5 }
    return candidates.any { candidate ->
        normalizedText.contains(candidate) || fuzzyContains(normalizedText, candidate, 0.93)
    }
}

private fun presenceItemTextMatches(normalizedText: String, item: String): Boolean {
    if (!fuzzyContains(normalizedText, item, 0.94)) return false
    val variants = setOf(
        "prime", "kuva", "tenet", "mk1", "vandal", "wraith", "prisma",
        "mutalist", "dex", "synoid", "rakta", "secura", "sancti", "telos", "vaykor"
    )
    if (item.split(" ").any { it in variants }) return true
    return variants.none { variant ->
        normalizedText.contains("$variant $item") || normalizedText.contains("$item $variant")
    }
}

private fun normalizeOcr(value: String): String {
    val ascii = Normalizer.normalize(value.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")
    return ascii
        .replace(Regex("([a-z0-9])prime\\b")) { match -> "${match.groupValues[1]} prime" }
        .replace(Regex("\\bbp\\b"), "blueprint")
        .replace("hauptblaupause", "blueprint")
        .replace("haupt blaupause", "blueprint")
        .replace("blaupause", "blueprint")
        .replace("neuroptik", "neuroptics")
        .replace("systeme", "systems")
        .replace("hildtyo", "hildryn")
        .replace(Regex("[^a-z0-9]+"), " ")
        .trim()
        .replace(Regex("\\s+"), " ")
}

private fun fuzzyContains(text: String, candidate: String, threshold: Double = 0.88): Boolean {
    val words = text.split(" ")
    val targetWords = candidate.split(" ")
    if (targetWords.size > words.size) return false
    return words.windowed(targetWords.size).any { window ->
        similarity(window.joinToString(" "), candidate) >= threshold
    }
}

private fun similarity(a: String, b: String): Double {
    if (a == b) return 1.0
    val distance = levenshtein(a, b)
    return 1.0 - distance.toDouble() / max(a.length, b.length).coerceAtLeast(1)
}

private fun levenshtein(a: String, b: String): Int {
    var previous = IntArray(b.length + 1) { it }
    a.forEachIndexed { i, ca ->
        val current = IntArray(b.length + 1)
        current[0] = i + 1
        b.forEachIndexed { j, cb ->
            current[j + 1] = minOf(current[j] + 1, previous[j + 1] + 1, previous[j] + if (ca == cb) 0 else 1)
        }
        previous = current
    }
    return previous[b.length]
}
