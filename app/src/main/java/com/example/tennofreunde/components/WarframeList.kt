package com.example.tennofreunde.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tennofreunde.models.WarframeItem

@Composable
fun WarframeList(

    items: MutableList<WarframeItem>,

    currentTab: String,

    saveItems: () -> Unit
) {

    LazyColumn(

        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        val filteredItems = items.filter { item ->

            item.tabName == currentTab &&

            (
                    item.components.isEmpty()
                            ||
                            !item.components.all { component ->
                                component.checked
                            }
                    )

        }

        itemsIndexed(filteredItems) { _, item ->

            WarframeCard(

                item = item,

                saveItems = {
                    saveItems()
                },

                onDelete = {

                    items.remove(item)

                    saveItems()
                }
            )
        }
    }
}

