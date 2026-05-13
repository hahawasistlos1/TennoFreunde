
package com.example.tennofreunde.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tennofreunde.components.WarframeList
import com.example.tennofreunde.models.TabItem
import com.example.tennofreunde.models.WarframeItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler
import com.example.tennofreunde.screens.FinishedScreen

@Composable
fun TennoScreen(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {

    val context = LocalContext.current

    val gson = Gson()

    val sharedPreferences = context.getSharedPreferences(
        "tenno_data",
        Context.MODE_PRIVATE
    )

    var selectedTab by remember {
        mutableStateOf(0)
    }

    var sortAZ by remember {
        mutableStateOf(false)
    }

    var fabExpanded by remember {
        mutableStateOf(false)
    }

    var showAddTabDialog by remember {

        mutableStateOf(false)
    }

    var newTabName by remember {

        mutableStateOf("")
    }
    var showFinishedScreen by remember {
        mutableStateOf(false)
    }

    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scope = rememberCoroutineScope()

    val tabs = remember {
        mutableStateListOf<TabItem>()
    }

    val items = remember {
        mutableStateListOf<WarframeItem>()
    }

    fun saveItems() {

        val json = gson.toJson(items)

        sharedPreferences
            .edit()
            .putString("warframe_items", json)
            .apply()
    }

    fun saveTabs() {

        val json = gson.toJson(tabs)

        sharedPreferences
            .edit()
            .putString("tab_data", json)
            .apply()
    }


    LaunchedEffect(Unit) {

        val tabsJson = sharedPreferences.getString(
            "tab_data",
            null
        )

        if (tabsJson != null) {

            val tabType =
                object : TypeToken<List<TabItem>>() {}.type

            val loadedTabs: List<TabItem> =
                gson.fromJson(tabsJson, tabType)

            tabs.clear()
            tabs.addAll(loadedTabs)
        }

        val itemsJson = sharedPreferences.getString(
            "warframe_items",
            null
        )

        if (itemsJson != null) {

            val itemType =
                object : TypeToken<List<WarframeItem>>() {}.type

            val loadedItems: List<WarframeItem> =
                gson.fromJson(itemsJson, itemType)

            items.clear()
            items.addAll(loadedItems)
        }
    }


    BackHandler {

        when {

            drawerState.isOpen -> {

                scope.launch {
                    drawerState.close()
                }
            }

            fabExpanded -> {

                fabExpanded = false
            }

            showAddTabDialog -> {

                showAddTabDialog = false
            }
        }
    }


    ModalNavigationDrawer(



            drawerState = drawerState,

            drawerContent = {

                ModalDrawerSheet {

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text = "Menü",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge
                    )

                    NavigationDrawerItem(
                        label = {

                            Text(
                                if (darkMode)
                                    "Darkmode AN"
                                else
                                    "Darkmode AUS"
                            )
                        },

                        selected = darkMode,

                        onClick = {
                            onDarkModeChange(!darkMode)
                        }
                    )
                    NavigationDrawerItem(

                        label = {
                            Text("Fertige Sachen")
                        },

                        selected = false,

                        onClick = {

                            showFinishedScreen = true

                            scope.launch {
                                drawerState.close()
                            }
                        }
                    )


                    NavigationDrawerItem(
                        label = {

                            Text(
                                if (sortAZ)
                                    "A-Z Sortierung AN"
                                else
                                    "A-Z Sortierung AUS"
                            )
                        },

                        selected = sortAZ,

                        onClick = {
                            sortAZ = !sortAZ
                        }
                    )
                }
            }
        ) {

            Scaffold(

                floatingActionButton = {

                    Box {

                        FloatingActionButton(
                            onClick = {
                                fabExpanded = true
                            }
                        ) {

                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add"
                            )
                        }

                        DropdownMenu(

                            expanded = fabExpanded,

                            onDismissRequest = {
                                fabExpanded = false
                            }
                        ) {

                            DropdownMenuItem(

                                text = {
                                    Text("+ Eintrag")
                                },

                                onClick = {

                                    if (tabs.isNotEmpty()) {

                                        items.add(
                                            0,

                                            WarframeItem(
                                                name = "Neuer Eintrag",
                                                tabName = tabs[selectedTab].name,
                                                infoFields = mutableListOf(),
                                                components = mutableListOf(),
                                                isNew = true
                                            )
                                        )

                                        saveItems()
                                    }

                                    fabExpanded = false
                                }
                            )

                            DropdownMenuItem(

                                text = {
                                    Text("Tab hinzufügen")
                                },

                                onClick = {


                                    newTabName = ""

                                    showAddTabDialog = true

                                    fabExpanded = false


                                }
                            )

                            DropdownMenuItem(

                                text = {
                                    Text("Tab entfernen")
                                },

                                onClick = {

                                    if (tabs.isNotEmpty()) {

                                        val currentTabName =
                                            tabs[selectedTab].name

                                        items.removeAll {
                                            it.tabName == currentTabName
                                        }

                                        tabs.removeAt(selectedTab)

                                        if (selectedTab > 0) {
                                            selectedTab--
                                        }

                                        saveItems()
                                        saveTabs()
                                    }

                                    fabExpanded = false
                                }
                            )
                        }
                    }
                }

            ) { paddingValues ->

                if (showFinishedScreen) {

                    FinishedScreen(

                        items = items,

                        onBack = {
                            showFinishedScreen = false
                        }
                    )

                    return@Scaffold
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .statusBarsPadding()
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {

                        IconButton(
                            onClick = {

                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {

                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menü"
                            )
                        }
                    }

                    if (tabs.isNotEmpty()) {

                        TabRow(
                            selectedTabIndex = selectedTab
                        ) {

                            tabs.forEachIndexed { index, tab ->

                                Tab(
                                    selected = selectedTab == index,

                                    onClick = {
                                        selectedTab = index
                                    },

                                    text = {
                                        Text(tab.name)
                                    }
                                )
                            }
                        }
                    }


                    val totalComponents = items.sumOf {
                        it.components.size
                    }

                    val checkedComponents = items.sumOf {
                        it.components.count { component ->
                            component.checked
                        }
                    }

                    val progressPercent =
                        if (totalComponents > 0)
                            (checkedComponents * 100) / totalComponents
                        else
                            0

                    Text(
                        text = "$progressPercent%",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(
                            start = 16.dp,
                            top = 12.dp
                        )
                    )

                    LinearProgressIndicator(
                        progress = { progressPercent / 100f },

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    if (tabs.isNotEmpty()) {


                        WarframeList(

                            items = items,

                            currentTab = tabs[selectedTab].name,

                            saveItems = {
                                saveItems()
                            }
                        )

                    }

                    if (showAddTabDialog) {

                        AlertDialog(

                            onDismissRequest = {

                                showAddTabDialog = false
                            },

                            confirmButton = {

                                Button(

                                    onClick = {

                                        if (newTabName.isNotEmpty()) {

                                            tabs.add(
                                                TabItem(newTabName)
                                            )

                                            selectedTab = tabs.lastIndex

                                            saveTabs()
                                        }

                                        showAddTabDialog = false
                                    }
                                ) {

                                    Text("Erstellen")
                                }
                            },

                            dismissButton = {

                                Button(

                                    onClick = {

                                        showAddTabDialog = false
                                    }
                                ) {

                                    Text("Abbrechen")
                                }
                            },

                            title = {

                                Text("Neuer Tab")
                            },

                            text = {

                                OutlinedTextField(

                                    value = newTabName,

                                    onValueChange = {

                                        newTabName = it
                                    },

                                    singleLine = true,

                                    label = {

                                        Text("Tab Name")
                                    }
                                )
                            }
                        )
                    }

                }
            }
        }
    }


