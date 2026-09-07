package com.example.tennofreunde

import android.net.Uri
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
import com.example.tennofreunde.models.ComponentItem
import com.example.tennofreunde.models.WarframeItem
import com.example.tennofreunde.data.PrimeCatalog
import com.example.tennofreunde.data.PrimeCatalogItem
import com.example.tennofreunde.ui.AppLanguage
import com.example.tennofreunde.ui.theme.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.Normalizer
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScreenshotScannerScreen(
    items: MutableList<WarframeItem>,
    language: AppLanguage,
    onProgressChanged: () -> Unit,
    onCatalogItemAdded: (WarframeItem) -> Unit = {}
) {
    val german = language == AppLanguage.GERMAN
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
    var scannerHistory by remember { mutableStateOf(loadScannerHistory(context)) }
    var learnedRules by remember { mutableStateOf(loadOcrLearningRules(context)) }
    var correctionFrom by remember { mutableStateOf("") }
    var correctionTo by remember { mutableStateOf("") }
    val catalog = remember { PrimeCatalog.load(context) }
    val selectedImageCount = selectedImageUris.size

    val scanner = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    DisposableEffect(scanner) { onDispose { scanner.close() } }

    fun previewMatchesForText(text: String): List<OcrComponentMatch> {
        return when (scannerMode) {
            ScannerMode.ALL -> previewAllMatches(text, items, catalog)
            ScannerMode.COMPONENTS -> previewCatalogItems(text, items, catalog) +
                previewOcrComponents(text, items) +
                previewVisibleCollectionCardComponents(text, items)
            ScannerMode.RELICS -> previewRelicInventory(text)
            ScannerMode.PRIME_PARTS -> previewPrimeParts(text, catalog)
            ScannerMode.FOUNDRY -> previewKeywordLines(text, "Foundry", listOf("building", "claim", "complete", "hour", "minute", "day", "bau", "fertig"))
            ScannerMode.MODS -> previewKeywordLines(text, "Mods", listOf("mod", "rank", "rang", "fusion", "capacity", "drain"))
            ScannerMode.MISSION_END -> previewKeywordLines(text, "Mission", listOf("reward", "belohnung", "credits", "endo", "xp", "affinity", "ressource"))
        }
    }

    fun previewText(text: String) {
        val correctedText = applyLearnedCorrections(text, learnedRules)
        recognizedText = correctedText
        pendingText = correctedText
        pendingMatches = previewMatchesForText(correctedText)
        matches = emptyList()
    }

    fun applyScanText(text: String, previewMatches: List<OcrComponentMatch>) {
        if (text.isBlank()) return
        val appliesToCollection = scannerMode == ScannerMode.ALL ||
            scannerMode == ScannerMode.COMPONENTS ||
            scannerMode == ScannerMode.PRIME_PARTS
        if (!appliesToCollection) {
            val result = previewMatches.map { it.copy(state = OcrMatchState.DETECTED_ONLY) }
            matches = result
            lastAppliedMatches = emptyList()
            pendingText = ""
            pendingMatches = emptyList()
            scannerHistory = saveScannerHistory(
                context = context,
                matches = result,
                german = german
            )
            return
        }
        val added = addMissingCatalogItems(text, items, catalog)
        val addedItems = added.mapNotNull { match ->
            items.firstOrNull { it.name == match.itemName }
        }.distinctBy { it.name }
        val addedKeys = added.map { "${it.itemName}\u0000${it.componentName}" }.toSet()
        val previewApplied = applyOcrMatchesToCollection(previewMatches, items)
        val previewAppliedKeys = previewApplied.map { "${it.itemName}\u0000${it.componentName}" }.toSet()
        val componentResult = added +
            previewApplied.filterNot { "${it.itemName}\u0000${it.componentName}" in addedKeys } +
            reconcileOcrComponents(text, items).filterNot {
            "${it.itemName}\u0000${it.componentName}" in addedKeys
                || "${it.itemName}\u0000${it.componentName}" in previewAppliedKeys
        }
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
        if (componentResult.any { it.state != OcrMatchState.ALREADY_CHECKED }) {
            onProgressChanged()
            addedItems.forEach(onCatalogItemAdded)
        }
        scannerHistory = saveScannerHistory(
            context = context,
            matches = result,
            german = german
        )
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

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        selectedImageUris = uris
        errorMessage = null
        recognizedText = ""
        matches = emptyList()
        pendingText = ""
        pendingMatches = emptyList()
        manualCorrection = ""
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        isScanning = true
        val scannedTexts = MutableList(uris.size) { "" }
        var finishedCount = 0

        fun finishImage(index: Int, text: String = "", error: Throwable? = null) {
            scannedTexts[index] = text
            if (error != null && errorMessage == null) {
                errorMessage = error.localizedMessage
                    ?: if (german) "Mindestens ein Bild konnte nicht gelesen werden." else "At least one image could not be read."
            }
            finishedCount += 1
            if (finishedCount == uris.size) {
                val mergedText = scannedTexts.filter { it.isNotBlank() }.joinToString("\n\n")
                isScanning = false
                if (mergedText.isBlank()) {
                    errorMessage = errorMessage ?: if (german) "Aus den ausgewählten Bildern konnte kein Text gelesen werden." else "No text could be read from the selected images."
                } else {
                    errorMessage = null
                    applyRecognizedText(mergedText)
                }
            }
        }

        uris.forEachIndexed { index, uri ->
            runCatching { InputImage.fromFilePath(context, uri) }
                .onSuccess { image ->
                    scanner.process(image)
                        .addOnSuccessListener { result -> finishImage(index, result.text) }
                        .addOnFailureListener { error -> finishImage(index, error = error) }
                }
                .onFailure { error -> finishImage(index, error = error) }
        }
    }

    val screenScrollState = rememberScrollState()

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(screenScrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                    onClick = { launcher.launch("image/*") },
                    enabled = !isScanning,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.EnergyCyan, contentColor = Color(0xFF07131C)),
                    shape = AppShapes.Small
                ) {
                    Icon(Icons.Default.ImageSearch, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (german) "Inventarbilder auswählen" else "Choose inventory images")
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
                    isScanning -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = AppColors.EnergyCyan)
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
                        TextButton(onClick = { launcher.launch("image/*") }) {
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
                            model = uri,
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
    val existingNames = items.map { it.name.lowercase() }.toMutableSet()
    val added = mutableListOf<OcrComponentMatch>()

    catalog.forEach { catalogItem ->
        if (catalogItem.item.name.lowercase() in existingNames) return@forEach
        val matchingIndex = catalogItem.item.components.indexOfFirst { component ->
            val candidate = componentCandidate(catalogItem.item.name, component.name)
            normalizedText.contains(candidate) || fuzzyContains(normalizedText, candidate)
        }
        if (matchingIndex < 0) return@forEach

        val addedItem = catalogItem.item.copy(
            infoFields = catalogItem.item.infoFields.toMutableList(),
            components = mutableStateListOf(
                *catalogItem.item.components.mapIndexed { index, component ->
                    component.copy(checked = index == matchingIndex)
                }.toTypedArray()
            )
        )
        val matchingComponent = addedItem.components[matchingIndex]
        items.add(addedItem)
        existingNames.add(catalogItem.item.name.lowercase())
        added += OcrComponentMatch(addedItem.name, matchingComponent.name, OcrMatchState.ADDED_TO_COLLECTION)
    }
    return added
}

private fun previewCatalogItems(
    text: String,
    items: List<WarframeItem>,
    catalog: List<PrimeCatalogItem>
): List<OcrComponentMatch> {
    val normalizedText = normalizeOcr(text)
    if (normalizedText.length < 5) return emptyList()
    val existingNames = items.map { it.name.lowercase() }.toSet()

    return catalog.mapNotNull { catalogItem ->
        if (catalogItem.item.name.lowercase() in existingNames) return@mapNotNull null
        val matchingComponent = catalogItem.item.components.firstOrNull { component ->
            val candidate = componentCandidate(catalogItem.item.name, component.name)
            normalizedText.contains(candidate) || fuzzyContains(normalizedText, candidate)
        } ?: return@mapNotNull null
        OcrComponentMatch(catalogItem.item.name, matchingComponent.name, OcrMatchState.ADDED_TO_COLLECTION)
    }
}

private fun previewOcrComponents(text: String, items: List<WarframeItem>): List<OcrComponentMatch> {
    val normalizedText = normalizeOcr(text)
    if (normalizedText.length < 4) return emptyList()
    val candidates = items.flatMap { item -> item.components.map { component -> Triple(item, component, componentCandidate(item.name, component.name)) } }
    val matches = candidates.filter { (_, _, candidate) ->
        candidate.length >= 5 && (normalizedText.contains(candidate) || fuzzyContains(normalizedText, candidate))
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
            val candidate = componentCandidate(catalogItem.item.name, component.name)
            if (normalizedText.contains(candidate) || fuzzyContains(normalizedText, candidate)) {
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
        previewVisibleCollectionCardComponents(text, items)
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
    val matches = candidates.filter { (_, _, _, candidate) ->
        candidate.length >= 5 && (normalizedText.contains(candidate) || fuzzyContains(normalizedText, candidate))
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

fun applyOcrMatchesToCollection(
    matches: List<OcrComponentMatch>,
    items: List<WarframeItem>
): List<OcrComponentMatch> {
    return matches.flatMap { match ->
        if (match.state == OcrMatchState.DETECTED_ONLY) return@flatMap emptyList()
        matchingCollectionItems(items, match.itemName).mapNotNull { item ->
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
        "system", "systems" -> aliases += setOf("system", "systems")
        "chassis" -> aliases += "chassis"
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

private fun normalizeOcr(value: String): String {
    val ascii = Normalizer.normalize(value.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")
    return ascii
        .replace(Regex("\\bbp\\b"), "blueprint")
        .replace("hauptblaupause", "blueprint")
        .replace("haupt blaupause", "blueprint")
        .replace("blaupause", "blueprint")
        .replace("neuroptik", "neuroptics")
        .replace("systeme", "systems")
        .replace("gehause", "chassis")
        .replace(Regex("[^a-z0-9]+"), " ")
        .trim()
        .replace(Regex("\\s+"), " ")
}

private fun fuzzyContains(text: String, candidate: String): Boolean {
    val words = text.split(" ")
    val targetWords = candidate.split(" ")
    if (targetWords.size > words.size) return false
    return words.windowed(targetWords.size).any { window ->
        similarity(window.joinToString(" "), candidate) >= 0.88
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
