
package com.example.tennofreunde.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tennofreunde.api.FissureResponse
import com.example.tennofreunde.models.WarframeItem
import com.example.tennofreunde.ui.theme.AppColors

@Composable
fun WarframeList(

    items: MutableList<WarframeItem>,

    fissuresData: List<FissureResponse>,

    currentTab: String,

    currentSubTab: String,

    saveItems: () -> Unit,

    saveLocalProgress: () -> Unit,

    sortAZ: Boolean,

    searchText: String,

    categoryFilter: String = "all",

    onlyMissing: Boolean = true,

    onlyAlmostDone: Boolean = false,

    onlyFavorites: Boolean = false,

    favoriteNames: Set<String> = emptySet(),

    archivedNames: Set<String> = emptySet(),

    modifier: Modifier = Modifier
) {

    LazyColumn(

        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
            val filteredItems = items.filter { item ->
                val checkedCount = item.components.count { component -> component.checked }
                val categoryMatches = categoryFilter == "all" ||
                        item.type.equals(categoryFilter, ignoreCase = true) ||
                        item.tabName.contains(categoryFilter, ignoreCase = true) ||
                        item.subTabName.contains(categoryFilter, ignoreCase = true)

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

                        categoryMatches &&

                        item.name !in archivedNames &&

                        (!onlyFavorites || item.name in favoriteNames) &&

                        (!onlyAlmostDone || (item.components.size > 1 && checkedCount > 0 && checkedCount < item.components.size)) &&

                        (!onlyMissing || item.components.isEmpty() || !item.components.all { component -> component.checked })
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

            if (filteredItems.isEmpty()) {

                item {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Text(
                            text = "Nichts gefunden",
                            color = AppColors.TextPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "Passe die Suche an oder prüfe den ausgewählten Tab.",
                            color = AppColors.TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
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


