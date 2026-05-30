
package com.example.tennofreunde.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tennofreunde.api.FissureResponse
import com.example.tennofreunde.models.WarframeItem

@Composable
fun WarframeList(

    items: MutableList<WarframeItem>,

    fissuresData: List<FissureResponse>,

    currentTab: String,

    currentSubTab: String,

    saveItems: () -> Unit,

    saveLocalProgress: () -> Unit,

    sortAZ: Boolean,

    searchText: String
) {

    LazyColumn(

        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        items
            .filter { it.tabName == "Waffen" }
            .take(10)
            .forEach {

                println(
                    "WAFFE TEST -> " +
                            it.name +
                            " | TAB='" +
                            it.tabName +
                            "' | SUBTAB='" +
                            it.subTabName +
                            "'"
                )
                println("TYPE = '${it.type}'")
            }

        println("CURRENT TAB = '$currentTab'")
        println("CURRENT SUBTAB = '$currentSubTab'")
        println("CURRENT TAB = '$currentTab'")
        println("CURRENT SUBTAB = '$currentSubTab'")
        println(
            "LÄNGE ITEM='${"Primär Prime".length}'"
        )
        println(
            "LÄNGE CURRENT='${currentSubTab.length}'"
        )

        println("ITEMS SIZE = ${items.size}")
        println(
            "GEFILTERTE ITEMS = ${
                items.count {
                    it.tabName == "Waffen"
                }
            }"
        )
            val filteredItems = items.filter { item ->

                item.tabName.trim() ==
                        currentTab.trim() &&

                        item.name.contains(
                            searchText,
                            ignoreCase = true
                        ) &&

                        (
                                currentSubTab.isEmpty()
                                        ||
                                        item.subTabName.trim() ==
                                        currentSubTab.trim()
                                ) &&

                        (
                                item.type == "weapon"
                                        ||
                                        item.components.isEmpty()
                                        ||
                                        !item.components.all { component ->
                                            component.checked
                                        }
                                )
            }
                .let { filtered ->

                    if (sortAZ) {

                        filtered.sortedBy {
                            it.name.lowercase()
                        }

                    } else {

                        filtered
                    }
                }

            itemsIndexed(filteredItems) { _, item ->

                WarframeCard(

                    item = item,

                    fissuresData = fissuresData,

                    saveItems = {
                        saveItems()
                    },

                    saveLocalProgress = {
                        saveLocalProgress()
                    },

                    onDelete = {

                        items.remove(item)

                        saveItems()
                    }
                )
            }
        }
    }


