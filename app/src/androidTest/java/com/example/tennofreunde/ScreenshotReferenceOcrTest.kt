package com.example.tennofreunde

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.tennofreunde.data.PrimeCatalog
import com.example.tennofreunde.models.ComponentItem
import com.example.tennofreunde.models.WarframeItem
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreenshotReferenceOcrTest {
    @Test
    fun androidPs4AndPs5InventoryReferencesProduceComponentMatches() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val catalog = PrimeCatalog.load(instrumentation.targetContext)
        val scanner = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val minimumMatches = mapOf("android.jpg" to 8, "ps4_1.jpg" to 8, "ps4_2.jpg" to 8, "ps5.jpg" to 8)
        val requiredMatches = mapOf(
            "android.jpg" to setOf("Nautilus Prime Cerebrum", "Nami Skyla Prime Blueprint", "Magnus Prime Receiver"),
            "ps4_1.jpg" to setOf("Helios Prime Cerebrum", "Shade Prime Cerebrum", "Hildryn Prime Chassis"),
            "ps4_2.jpg" to setOf("Shade Prime Cerebrum", "Garuda Prime Chassis", "Velox Prime Receiver"),
            "ps5.jpg" to setOf("Odonata Prime Blueprint", "Banshee Prime Chassis", "Akarius Prime Link")
        )
        val forbiddenMatches = mapOf(
            "android.jpg" to setOf("Tiberon Prime Blueprint", "Akmagnus Prime Blueprint"),
            "ps4_2.jpg" to setOf("Gara Prime Blueprint", "Gara Prime Chassis"),
            "ps5.jpg" to setOf("Afuris Prime Link", "Akarius Prime Blueprint", "Akbolto Prime Blueprint")
        )

        try {
            minimumMatches.forEach { (asset, minimum) ->
                val bitmap = decodePrepared(asset)
                val text = Tasks.await(scanner.process(InputImage.fromBitmap(bitmap, 0))).text
                bitmap.recycle()
                text.lineSequence().filter { it.isNotBlank() }.forEach { line ->
                    Log.i("ScannerOcr", "$asset|$line")
                }
                val collection = catalog.map { entry ->
                    entry.item.copy(
                        infoFields = entry.item.infoFields.toMutableList(),
                        components = mutableStateListOf<ComponentItem>().also { list ->
                            list.addAll(entry.item.components.map { it.copy(checked = false) })
                        }
                    )
                }
                val matches = reconcileOcrComponents(text, collection)
                val labels = matches.mapTo(mutableSetOf()) { "${it.itemName} ${it.componentName}" }
                Log.i("ScannerReference", "$asset matches=${matches.size}: ${matches.joinToString { "${it.itemName} ${it.componentName}" }}")
                assertTrue("$asset erkannte nur ${matches.size} Teile. OCR: $text", matches.size >= minimum)
                assertTrue("$asset fehlt: ${requiredMatches[asset].orEmpty() - labels}", labels.containsAll(requiredMatches[asset].orEmpty()))
                assertTrue("$asset enthält falsche Treffer: ${labels intersect forbiddenMatches[asset].orEmpty()}", (labels intersect forbiddenMatches[asset].orEmpty()).isEmpty())
            }
        } finally {
            scanner.close()
        }
    }

    private fun decodePrepared(asset: String): Bitmap {
        val assets = InstrumentationRegistry.getInstrumentation().context.assets
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        assets.open(asset).use { BitmapFactory.decodeStream(it, null, bounds) }
        val options = BitmapFactory.Options().apply {
            inSampleSize = ocrSampleSize(bounds.outWidth, bounds.outHeight, 2400)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = assets.open(asset).use { BitmapFactory.decodeStream(it, null, options) }
            ?: error("Referenzbild $asset konnte nicht geladen werden")
        val crop = inventoryOcrCrop(decoded.width, decoded.height) ?: return decoded
        return Bitmap.createBitmap(decoded, crop.left, crop.top, crop.width, crop.height).also {
            if (it !== decoded) decoded.recycle()
        }
    }
}
