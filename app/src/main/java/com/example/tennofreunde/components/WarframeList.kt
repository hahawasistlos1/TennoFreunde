
package com.example.tennofreunde.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.tennofreunde.api.FissureResponse
import com.example.tennofreunde.models.WarframeItem
import com.example.tennofreunde.ui.theme.AppColors
import com.example.tennofreunde.ui.theme.LocalCompactMode

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

    pinFavorites: Boolean = true,

    archivedNames: Set<String> = emptySet(),

    german: Boolean = true,

    modifier: Modifier = Modifier
) {
    val compact = LocalCompactMode.current
    val dragThreshold = with(LocalDensity.current) { 72.dp.toPx() }

    LazyColumn(

        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (compact) 10.dp else 16.dp),
        contentPadding = PaddingValues(top = if (compact) 6.dp else 10.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 16.dp)
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
                    when {
                        pinFavorites && sortAZ -> filtered.sortedWith(
                            compareBy<WarframeItem> { it.name !in favoriteNames }
                                .thenBy { it.name.lowercase() }
                        )
                        sortAZ -> filtered.sortedBy { it.name.lowercase() }
                        else -> filtered
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
                            text = if (german) "Nichts gefunden" else "Nothing found",
                            color = AppColors.TextPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = if (german) {
                                "Passe die Suche an oder prüfe den ausgewählten Tab."
                            } else {
                                "Adjust the search or check the selected tab."
                            },
                            color = AppColors.TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (!sortAZ && filteredItems.isNotEmpty()) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text("↕", color = AppColors.EnergyCyan, style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (german) {
                                "Zum Sortieren Karte lange drücken und nach oben oder unten ziehen."
                            } else {
                                "Long-press a card and drag it up or down to reorder."
                            },
                            color = AppColors.TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            itemsIndexed(filteredItems) { _, item ->
                var draggedDistance by remember(item.name) { mutableFloatStateOf(0f) }
                var orderChanged by remember(item.name) { mutableStateOf(false) }
                Box(
                    Modifier.pointerInput(sortAZ, item.name, dragThreshold) {
                        if (!sortAZ) detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggedDistance = 0f
                                orderChanged = false
                            },
                            onDragCancel = {
                                draggedDistance = 0f
                                if (orderChanged) saveItems()
                                orderChanged = false
                            },
                            onDragEnd = {
                                draggedDistance = 0f
                                if (orderChanged) saveItems()
                                orderChanged = false
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                draggedDistance += dragAmount.y
                                val direction = when {
                                    draggedDistance > dragThreshold -> 1
                                    draggedDistance < -dragThreshold -> -1
                                    else -> 0
                                }
                                if (direction != 0) {
                                    val visibleItemsInCurrentOrder = items.filter { candidate ->
                                        filteredItems.any { visible -> visible === candidate }
                                    }
                                    val visibleIndex = visibleItemsInCurrentOrder.indexOfFirst { it === item }
                                    val neighbor = visibleItemsInCurrentOrder.getOrNull(visibleIndex + direction)
                                    if (neighbor != null && swapCollectionItems(items, item, neighbor)) {
                                        draggedDistance = 0f
                                        orderChanged = true
                                    }
                                }
                            }
                        )
                    }
                ) {
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
}

internal fun swapCollectionItems(items: MutableList<WarframeItem>, first: WarframeItem, second: WarframeItem): Boolean {
    val firstIndex = items.indexOfFirst { it === first }
    val secondIndex = items.indexOfFirst { it === second }
    if (firstIndex < 0 || secondIndex < 0 || firstIndex == secondIndex) return false
    items[firstIndex] = second
    items[secondIndex] = first
    return true
}


