package com.example.tennofreunde.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tennofreunde.models.WarframeItem
import androidx.compose.foundation.border


@Composable
fun FinishedScreen(
    items: List<WarframeItem>,
    onBack: () -> Unit,
    onResetItem: (WarframeItem) -> Unit
) {

    var selectedTab by remember {
        mutableStateOf("")
    }

    val finishedItems = items.filter { item ->

        item.components.isNotEmpty() &&
                item.components.all { it.checked }
    }

    val tabs = finishedItems
        .map { it.tabName }
        .distinct()

    LaunchedEffect(tabs) {

        if (
            selectedTab.isEmpty() &&
            tabs.isNotEmpty()
        ) {
            selectedTab = tabs.first()
        }
    }

    val totalItemsInTab =
        items.count {
            it.tabName == selectedTab
        }

    val finishedItemsInTab =
        finishedItems.count {
            it.tabName == selectedTab
        }

    val totalComponentsInTab =
        items.filter {
            it.tabName == selectedTab
        }.sumOf {
            it.components.size
        }

    val finishedComponentsInTab =
        items.filter {
            it.tabName == selectedTab
        }.sumOf { item ->

            item.components.count {
                it.checked
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF181825))
            .padding(16.dp)
    ) {

        Button(
            onClick = {
                onBack()
            }
        ) {
            Text("Zurück")
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "Fertige Sachen",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )

        Card(

            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),

            colors = CardDefaults.cardColors(
                containerColor = Color(0xDD232136)
            ),

            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            ),

            shape = RoundedCornerShape(24.dp)
        ) {

            Column(

                modifier = Modifier.padding(18.dp)
            ) {

                Text(

                    text = selectedTab,

                    color = Color.White,

                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Text(

                    text = "$finishedItemsInTab / $totalItemsInTab fertig",

                    color = Color(0xFFC8A8FF),

                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(

                    text = "Teile $finishedComponentsInTab / $totalComponentsInTab",

                    color = Color.LightGray
                )
            }
        }


                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                ScrollableTabRow(
                    selectedTabIndex =
                        tabs.indexOf(selectedTab),

                    containerColor = Color.Transparent,

                    divider = { },

                    indicator = { }
                ) {

                    tabs.forEach { tab ->

                        Tab(

                            selected =
                                selectedTab == tab,

                            onClick = {
                                selectedTab = tab
                            },

                            text = {

                                val selected =
                                    selectedTab == tab

                                Box(

                                    modifier = Modifier

                                        .background(

                                            if (selected)
                                                Color(0xFFC8A8FF)
                                            else
                                                Color(0x33FFFFFF),

                                            RoundedCornerShape(50)
                                        )

                                        .padding(
                                            horizontal = 18.dp,
                                            vertical = 10.dp
                                        )
                                ) {

                                    Text(

                                        text = tab,

                                        color =

                                            if (selected)
                                                Color.Black
                                            else
                                                Color.White
                                    )
                                }
                            }
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                val filteredItems =
                    finishedItems.filter {
                        it.tabName == selectedTab
                    }

                LazyColumn {

                    items(filteredItems) { item ->

                        Card(

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)

                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(28.dp)
                                ),

                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xDD232136)
                            ),

                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 10.dp
                            ),

                            shape = RoundedCornerShape(28.dp)
                        ) {

                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {

                                Text(
                                    text = item.name,
                                    style =
                                        MaterialTheme.typography.titleLarge,

                                    color = Color.White
                                )

                                Spacer(
                                    modifier = Modifier.height(6.dp)
                                )

                                Text(
                                    text = item.subTabName,
                                    color = Color.LightGray
                                )

                                Spacer(
                                    modifier = Modifier.height(16.dp)
                                )

                                Button(

                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFC8A8FF),
                                        contentColor = Color.Black
                                    ),

                                    shape = RoundedCornerShape(18.dp),

                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 6.dp
                                    ),

                                    onClick = {

                                        onResetItem(item)
                                    }

                                ) {

                                    Text("Reset")
                                }
                            }
                        }
                    }
                }
            }
        }

