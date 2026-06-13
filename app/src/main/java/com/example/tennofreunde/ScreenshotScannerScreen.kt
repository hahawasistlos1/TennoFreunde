package com.example.tennofreunde

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.content.Context
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll



@Composable
fun ScreenshotScannerScreen() {

    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var recognizedText by remember {
        mutableStateOf("")
    }
    val context = LocalContext.current

    val scrollState = rememberScrollState()

    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->

            imageUri = uri

            if (uri != null) {

                val image =
                    InputImage.fromFilePath(
                        context,
                        uri
                    )

                val recognizer =
                    TextRecognition.getClient(
                        TextRecognizerOptions.DEFAULT_OPTIONS
                    )

                recognizer.process(image)
                    .addOnSuccessListener { result ->

                        recognizedText =
                            result.text

                    }
                    .addOnFailureListener {

                        recognizedText =
                            "OCR Fehler: ${it.message}"

                    }
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Button(
            onClick = {
                launcher.launch("image/*")
            }
        ) {
            Text("Screenshot auswählen")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text =
                if (imageUri != null)
                    "Bild ausgewählt"
                else
                    "Noch kein Bild ausgewählt"
        )
        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {

            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                Text(
                    text = recognizedText
                )
            }
        }
    }
}
