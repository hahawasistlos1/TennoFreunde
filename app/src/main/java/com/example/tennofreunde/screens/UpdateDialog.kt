package com.example.tennofreunde.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun UpdateDialog(
    showDialog: Boolean,
    updateTitle: String,
    updateMessage: String,
    updateUrl: String,
    onDismiss: () -> Unit
) {

    if (!showDialog) return

    val context = LocalContext.current

    Dialog(
        onDismissRequest = { }
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),

            shape = MaterialTheme.shapes.extraLarge
        ) {

            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                Text(
                    text = "Update $updateTitle",

                    style =
                        MaterialTheme.typography.headlineSmall
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(updateMessage)

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp),

                    modifier = Modifier.fillMaxWidth()
                ) {

                    Button(
                        onClick = {
                            onDismiss()
                        },

                        modifier = Modifier.weight(1f)
                    ) {

                        Text("Später")
                    }

                    Button(
                        onClick = {

                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(updateUrl)
                            )

                            context.startActivity(intent)
                        },

                        modifier = Modifier.weight(1f)
                    ) {

                        Text("Update")
                    }
                }
            }
        }
    }
}