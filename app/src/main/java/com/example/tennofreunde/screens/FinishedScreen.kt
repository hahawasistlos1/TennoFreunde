package com.example.tennofreunde.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tennofreunde.models.WarframeItem

@Composable
fun FinishedScreen(
    items: List<WarframeItem>,
    onBack: () -> Unit
) {


    val finishedItems = items.filter { item ->

        item.components.isNotEmpty() &&

                item.components.all { component ->

                    component.checked
                }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = {
                onBack()
            }
        ) {
            Text("Zurück")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Fertige Sachen",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {

            items(finishedItems) { item ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {

                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Tab: ${item.tabName}"
                        )
                    }
                }
            }
        }
    }
}