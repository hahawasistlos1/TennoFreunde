package com.example.tennofreunde.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tennofreunde.models.WarframeItem
import com.example.tennofreunde.ui.theme.AppColors
import com.example.tennofreunde.ui.theme.AppShapes

private enum class FinishedSortMode(val label: String) {
    NAME("Name"),
    SUB_TAB("Untertab"),
    COMPONENTS("Teile")
}

@Composable
fun FinishedScreen(
    items: List<WarframeItem>,
    onBack: () -> Unit,
    onResetItem: (WarframeItem) -> Unit,
    onResetComponent: (WarframeItem, Int) -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf("") }
    var selectedSubTab by rememberSaveable { mutableStateOf("Alle") }
    var searchText by rememberSaveable { mutableStateOf("") }
    var sortMode by rememberSaveable { mutableStateOf(FinishedSortMode.NAME) }
    var sortMenuOpen by remember { mutableStateOf(false) }

    val finishedItems = items.filter { item ->
        item.components.isNotEmpty() && item.components.all { it.checked }
    }
    val tabs = finishedItems.map { it.tabName }.distinct().sortedBy { it.lowercase() }

    LaunchedEffect(tabs) {
        if (selectedTab !in tabs) selectedTab = tabs.firstOrNull().orEmpty()
    }

    val tabItems = finishedItems.filter { it.tabName == selectedTab }
    val subTabs = listOf("Alle") + tabItems.map { it.subTabName.ifBlank { "Ohne Untertab" } }
        .distinct()
        .sortedBy { it.lowercase() }

    LaunchedEffect(selectedTab, subTabs) {
        if (selectedSubTab !in subTabs) selectedSubTab = "Alle"
    }

    val visibleItems = tabItems
        .filter { item ->
            val itemSubTab = item.subTabName.ifBlank { "Ohne Untertab" }
            val matchesSubTab = selectedSubTab == "Alle" || itemSubTab == selectedSubTab
            val matchesSearch = searchText.isBlank() || item.name.contains(searchText, ignoreCase = true)
            matchesSubTab && matchesSearch
        }
        .let { filtered ->
            when (sortMode) {
                FinishedSortMode.NAME -> filtered.sortedBy { it.name.lowercase() }
                FinishedSortMode.SUB_TAB -> filtered.sortedWith(
                    compareBy({ it.subTabName.lowercase() }, { it.name.lowercase() })
                )
                FinishedSortMode.COMPONENTS -> filtered.sortedWith(
                    compareByDescending<WarframeItem> { it.components.size }.thenBy { it.name.lowercase() }
                )
            }
        }

    val totalFinishedComponents = finishedItems.sumOf { item -> item.components.size }
    val tabFinishedComponents = tabItems.sumOf { item -> item.components.size }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück", tint = AppColors.EnergyCyan)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Fertige Sachen", style = MaterialTheme.typography.headlineSmall, color = AppColors.OrokinGold)
                Text(
                    "${finishedItems.size} Einträge · $totalFinishedComponents Teile fertig",
                    color = AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        SummaryPanel(
            selectedTab = selectedTab.ifBlank { "Keine fertigen Einträge" },
            finishedInTab = tabItems.size,
            componentsInTab = tabFinishedComponents,
            visibleCount = visibleItems.size
        )

        if (tabs.isNotEmpty()) {
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOf(selectedTab).coerceAtLeast(0),
                containerColor = Color.Transparent,
                edgePadding = 0.dp,
                divider = {},
                indicator = {}
            ) {
                tabs.forEach { tab ->
                    val selected = selectedTab == tab
                    Tab(
                        selected = selected,
                        onClick = {
                            selectedTab = tab
                            selectedSubTab = "Alle"
                        },
                        text = { TabPill(tab, selected) }
                    )
                }
            }
        }

        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.TextSecondary) },
            label = { Text("Fertige Sachen suchen") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Accent,
                unfocusedBorderColor = AppColors.CardBorder,
                focusedTextColor = AppColors.TextPrimary,
                unfocusedTextColor = AppColors.TextPrimary,
                cursorColor = AppColors.Accent
            ),
            shape = AppShapes.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                OutlinedButton(onClick = { sortMenuOpen = true }, shape = AppShapes.Small) {
                    Text("Sortierung: ${sortMode.label}", color = AppColors.TextPrimary)
                }
                DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                    FinishedSortMode.values().forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.label) },
                            onClick = {
                                sortMode = mode
                                sortMenuOpen = false
                            }
                        )
                    }
                }
            }

            Text(
                "${visibleItems.size} sichtbar",
                color = AppColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (subTabs.size > 1) {
            ScrollableTabRow(
                selectedTabIndex = subTabs.indexOf(selectedSubTab).coerceAtLeast(0),
                containerColor = Color.Transparent,
                edgePadding = 0.dp,
                divider = {},
                indicator = {}
            ) {
                subTabs.forEach { subTab ->
                    val selected = selectedSubTab == subTab
                    Tab(
                        selected = selected,
                        onClick = { selectedSubTab = subTab },
                        text = { SubTabPill(subTab, selected) }
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (visibleItems.isEmpty()) {
                item {
                    EmptyFinishedState(hasFinishedItems = finishedItems.isNotEmpty())
                }
            }

            items(visibleItems, key = { it.name }) { item ->
                FinishedItemCard(
                    item = item,
                    onResetItem = onResetItem,
                    onResetComponent = onResetComponent
                )
            }
        }
    }
}

@Composable
private fun SummaryPanel(
    selectedTab: String,
    finishedInTab: Int,
    componentsInTab: Int,
    visibleCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.HudPanel),
        shape = AppShapes.Large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(selectedTab, color = Color.White, style = MaterialTheme.typography.titleLarge)
                Text("$finishedInTab fertig · $componentsInTab Teile", color = Color(0xFFC8A8FF), style = MaterialTheme.typography.bodyMedium)
            }
            AssistChip(
                onClick = {},
                label = { Text("$visibleCount sichtbar") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = AppColors.EnergyCyan.copy(alpha = 0.16f),
                    labelColor = AppColors.EnergyCyan
                )
            )
        }
    }
}

@Composable
private fun FinishedItemCard(
    item: WarframeItem,
    onResetItem: (WarframeItem) -> Unit,
    onResetComponent: (WarframeItem, Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AppColors.CardBorder, AppShapes.Medium),
        colors = CardDefaults.cardColors(containerColor = AppColors.HudPanel),
        shape = AppShapes.Medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Text(
                        listOf(item.tabName, item.subTabName).filter { it.isNotBlank() }.joinToString(" · "),
                        color = AppColors.TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    "${item.components.size}/${item.components.size}",
                    color = Color(0xFF55E6B5),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(color = AppColors.CardBorder)

            item.components.forEachIndexed { index, component ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(component.name, color = AppColors.TextPrimary, style = MaterialTheme.typography.bodyLarge)
                        Text("Abgehakt", color = Color(0xFF55E6B5), style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedButton(
                        onClick = { onResetComponent(item, index) },
                        shape = AppShapes.Small
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, tint = AppColors.Accent)
                        Spacer(Modifier.width(6.dp))
                        Text("Teil reset", color = AppColors.TextPrimary)
                    }
                }
            }

            Button(
                onClick = { onResetItem(item) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8A8FF), contentColor = Color.Black),
                shape = AppShapes.Small
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Alles resetten")
            }
        }
    }
}

@Composable
private fun TabPill(text: String, selected: Boolean) {
    Box(
        modifier = Modifier
            .background(if (selected) Color(0xFFC8A8FF) else Color(0x33FFFFFF), RoundedCornerShape(50))
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(text, color = if (selected) Color.Black else Color.White)
    }
}

@Composable
private fun SubTabPill(text: String, selected: Boolean) {
    Box(
        modifier = Modifier
            .background(if (selected) AppColors.Accent else AppColors.TextPrimary.copy(alpha = 0.08f), RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text, color = if (selected) Color.Black else Color.White, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun EmptyFinishedState(hasFinishedItems: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            if (hasFinishedItems) "Nichts in dieser Auswahl" else "Noch nichts fertig",
            color = AppColors.TextPrimary,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            if (hasFinishedItems) "Passe Suche, Tab oder Untertab an." else "Wenn alle Teile eines Eintrags abgehakt sind, erscheint er hier.",
            color = AppColors.TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
