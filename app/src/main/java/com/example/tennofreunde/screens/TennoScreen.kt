package com.example.tennofreunde.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.tennofreunde.components.WarframeList
import com.example.tennofreunde.models.ComponentItem
import com.example.tennofreunde.models.InfoField
import com.example.tennofreunde.models.SubTabItem
import com.example.tennofreunde.models.TabItem
import com.example.tennofreunde.models.WarframeItem
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.example.tennofreunde.screens.UpdateDialog
import com.example.tennofreunde.screens.FinishedScreen
import androidx.compose.material3.NavigationDrawerItemDefaults
import com.example.tennofreunde.ui.theme.AppBrushes
import com.example.tennofreunde.ui.theme.AppColors
import com.example.tennofreunde.ui.theme.AppShapes
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.toMutableStateList
import com.example.tennofreunde.api.FissureResponse
import com.example.tennofreunde.api.WarframeApi
import kotlinx.coroutines.delay
import com.example.tennofreunde.data.WeaponRelicLoader
import com.example.tennofreunde.data.WeaponGenerator
import com.example.tennofreunde.ScreenshotScannerScreen
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll



@Composable
fun TennoScreen(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {


    var updateTitle by remember {
        mutableStateOf("")
    }

    var updateMessage by remember {
        mutableStateOf("")
    }

    var updateUrl by remember {
        mutableStateOf("")
    }


    val context = LocalContext.current

    val gson = Gson()

    val db = Firebase.firestore

    val sharedPreferences = context.getSharedPreferences(
        "tenno_data",
        Context.MODE_PRIVATE
    )


    val items = remember {
        mutableStateListOf<WarframeItem>()
    }

    fun saveLocalProgress() {

        val progressMap = mutableMapOf<String, Boolean>()

        items.forEach { item ->

            item.components.forEach { component ->

                val key =
                    "${item.name}_${component.name}"

                progressMap[key] = component.checked


            }
        }

        val json = gson.toJson(progressMap)

        sharedPreferences
            .edit()
            .putString("local_progress", json)
            .apply()

        Firebase.auth.currentUser?.uid?.let { uid ->

            db.collection("user_progress")
                .document(uid)
                .set(progressMap)
        }
    }


    val auth = Firebase.auth

    var selectedTab by remember {
        mutableStateOf(0)
    }

    var currentUser by remember {
        mutableStateOf(auth.currentUser)
    }

    var selectedSubTab by remember {
        mutableStateOf(0)
    }


    val subTabs = remember {
        mutableStateListOf<SubTabItem>()
    }


    var sortAZ by remember {
        mutableStateOf(false)
    }

    var searchText by remember {
        mutableStateOf("")
    }

    var fissuresData by remember {
        mutableStateOf<List<FissureResponse>>(emptyList())
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


    var showAddSubTabDialog by remember {
        mutableStateOf(false)
    }

    var newSubTabName by remember {
        mutableStateOf("")
    }

    var showFinishedScreen by remember {
        mutableStateOf(false)
    }

    var showLiveScreen by remember {
        mutableStateOf(true)
    }

    var showScreenshotScanner by remember {
        mutableStateOf(false)
    }

    var showUpdateDialog by remember {
        mutableStateOf(false)
    }

    var showDeleteTabDialog by remember {
        mutableStateOf(false)
    }

    var showDeleteSubTabDialog by remember {
        mutableStateOf(false)
    }

    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scope = rememberCoroutineScope()

    val tabs = remember {
        mutableStateListOf<TabItem>()
    }
    val currentVersion = "6.4"



    val exportLauncher = rememberLauncherForActivityResult(

        contract =
            ActivityResultContracts.CreateDocument(
                "application/json"
            )

    ) { uri ->

        uri?.let {

            try {

                val json =
                    gson.toJson(items)

                context.contentResolver
                    .openOutputStream(it)
                    ?.use { output ->

                        output.write(
                            json.toByteArray()
                        )
                    }

            } catch (e: Exception) {

                e.printStackTrace()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(

        contract = OpenDocument()

    ) { uri ->

        uri?.let {

            try {

                val json =
                    context.contentResolver
                        .openInputStream(it)
                        ?.bufferedReader()
                        ?.use { reader ->
                            reader.readText()
                        }

                if (json != null) {

                    val type =
                        object : TypeToken<MutableList<WarframeItem>>() {}.type

                    val importedItems: MutableList<WarframeItem> =
                        gson.fromJson(json, type)

                    items.clear()

                    items.addAll(importedItems)


                }

            } catch (e: Exception) {

                e.printStackTrace()
            }
        }
    }


    val googleSignInClient = GoogleSignIn.getClient(

        context,

        GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(
                "661219300340-mrnjg053mltnkj0217i10iqgmv9ptefj.apps.googleusercontent.com"
            )
            .requestEmail()
            .build()
    )

    val loginLauncher =
        rememberLauncherForActivityResult(

            contract =
                ActivityResultContracts.StartActivityForResult()

        ) { result ->

            val task =
                GoogleSignIn.getSignedInAccountFromIntent(
                    result.data
                )

            try {

                val account =
                    task.getResult(ApiException::class.java)

                val credential =
                    GoogleAuthProvider.getCredential(
                        account.idToken,
                        null
                    )

                auth.signInWithCredential(credential)
                    .addOnCompleteListener { authResult ->

                        if (authResult.isSuccessful) {

                            currentUser = auth.currentUser
                            currentUser?.uid?.let { uid ->

                                db.collection("user_progress")
                                    .document(uid)
                                    .get()
                                    .addOnSuccessListener { document ->

                                        val data =
                                            document.data as? Map<String, Boolean>

                                        if (data != null) {

                                            items.forEach { item ->

                                                item.components.forEach { component ->

                                                    val key =
                                                        "${item.name}_${component.name}"

                                                    component.checked =
                                                        data[key] ?: false
                                                }
                                            }
                                        }
                                    }
                            }
                            println("Login erfolgreich")
                        } else {

                            println("Login Fehler")
                        }
                    }

            } catch (e: Exception) {

                e.printStackTrace()
            }
        }

    fun saveItems() {

        val json = gson.toJson(items)

        sharedPreferences
            .edit()
            .putString("warframe_items", json)
            .apply()


        val firebaseItems = items.map { item ->

            hashMapOf(

                "name" to item.name,

                "tabName" to item.tabName,

                "subTabName" to item.subTabName,

                "infoFields" to item.infoFields.map { info ->

                    hashMapOf(
                        "title" to info.title,
                        "value" to info.value
                    )
                },

                "components" to item.components.map { component ->

                    hashMapOf(
                        "name" to component.name,
                        "checked" to false
                    )
                }
            )
        }

        db.collection("items")
            .document("shared_items")
            .set(hashMapOf("data" to firebaseItems))

    }


    fun saveTabs() {

        val json = gson.toJson(tabs)

        sharedPreferences
            .edit()
            .putString("tab_data", json)
            .apply()

        val firebaseTabs = tabs.map { tab ->

            hashMapOf(
                "name" to tab.name
            )
        }

        db.collection("tabs")
            .document("shared_tabs")
            .set(hashMapOf("data" to firebaseTabs))
    }


    fun saveSubTabs() {

        val firebaseSubTabs = subTabs.toList().map { subTab ->

            hashMapOf(

                "name" to subTab.name,

                "parentTab" to subTab.parentTab
            )
        }

        db.collection("subtabs")
            .document("shared_subtabs")
            .set(hashMapOf("data" to firebaseSubTabs))
    }




    LaunchedEffect(Unit) {

        db.collection("subtabs")
            .document("shared_subtabs")
            .addSnapshotListener { value, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                val data =
                    value?.get("data")
                            as? List<HashMap<String, Any>>

                if (data != null) {

                    subTabs.clear()

                    data.forEach { map ->

                        subTabs.add(

                            SubTabItem(

                                name =
                                    map["name"].toString(),

                                parentTab =
                                    map["parentTab"].toString()
                            )
                        )
                    }
                }
            }
    }

    LaunchedEffect(Unit) {

        db.collection("tabs")
            .document("shared_tabs")
            .addSnapshotListener { value, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                val data =
                    value?.get("data")
                            as? List<HashMap<String, Any>>

                if (data != null) {

                    tabs.clear()

                    data.forEach { map ->

                        tabs.add(

                            TabItem(
                                name = map["name"].toString()
                            )
                        )
                    }
                }
            }
    }







    LaunchedEffect(Unit) {

        db.collection("items")
            .document("shared_items")
            .addSnapshotListener { value, error ->


                if (error != null) {
                    return@addSnapshotListener
                }

                val data =
                    value?.get("data") as? List<HashMap<String, Any>>

                if (data != null) {

                    items.clear()

                    val existingNames = mutableSetOf<String>()

                    val weaponEntries =
                        WeaponRelicLoader.loadWeapons(context)

                    val generatedWeapons =
                        WeaponGenerator.generateWeapons(
                            weaponEntries
                        )
                    println("GENERIERTE WAFFEN: ${generatedWeapons.size}")

                    generatedWeapons.take(20).forEach {
                        println("WAFFE: ${it.name}")
                    }
                    generatedWeapons.forEach { weapon ->

                        if (weapon.name.contains("soma", true)) {

                            println(
                                "SOMA GENERATOR -> ${weapon.name} | ${weapon.category}"
                            )
                        }

                        println("WAFFE: ${weapon.name}")
                        println("TAB: ${weapon.category}")
                        println("KOMPONENTEN: ${weapon.components.size}")



                        if (!existingNames.contains(weapon.name)) {

                            existingNames.add(weapon.name)

                            items.add(

                                WarframeItem(

                                    name = weapon.name,

                                    type = "weapon",

                                    tabName = "Waffen",

                                    subTabName = weapon.category,

                                    infoFields = mutableStateListOf(),

                                    components =
                                        weapon.components
                                            .toMutableStateList(),

                                    isNew = false
                                )
                            )
                        }
                    }


                    data.forEach { map ->

                        val infoFields =
                            mutableListOf<InfoField>()

                        val infoList =
                            map["infoFields"]
                                    as? List<HashMap<String, Any>>

                        infoList?.forEach { info ->

                            infoFields.add(
                                InfoField(
                                    title =
                                        info["title"].toString(),

                                    value =
                                        info["value"].toString()
                                )
                            )
                        }

                        val components =
                            mutableListOf<ComponentItem>()

                        val componentList =
                            map["components"]
                                    as? List<HashMap<String, Any>>

                        componentList?.forEach { component ->

                            val componentName =
                                component["name"].toString()

                            val progressJson =
                                sharedPreferences.getString(
                                    "local_progress",
                                    null
                                )

                            var checkedState = false

                            if (progressJson != null) {

                                val type =
                                    object : TypeToken<MutableMap<String, Boolean>>() {}.type

                                val progressMap:
                                        MutableMap<String, Boolean> =
                                    gson.fromJson(progressJson, type)

                                val key =
                                    "${map["name"]}_${componentName}"

                                checkedState =
                                    progressMap[key] ?: false
                            }

                            components.add(

                                ComponentItem(

                                    name = componentName,

                                    checked = checkedState,

                                    farmLocation =
                                        component["farmLocation"]?.toString() ?: ""
                                )
                            )
                        }


                        val itemName = map["name"].toString()

                        if (!existingNames.contains(itemName)) {

                            existingNames.add(itemName)

                            items.add(

                                WarframeItem(

                                    name = itemName,

                                    type = map["type"]?.toString() ?: "warframe",

                                    tabName =
                                        map["tabName"].toString(),

                                    subTabName =
                                        map["subTabName"]?.toString() ?: "",

                                    infoFields = infoFields,

                                    components = components.toMutableStateList(),

                                    isNew = false
                                )

                            )
                        }

                    }
                }
            }
    }

    LaunchedEffect(Unit) {

        withContext(Dispatchers.IO) {

            try {

                val client = OkHttpClient()

                val request = Request.Builder()
                    .url("https://api.github.com/repos/hahawasistlos1/TennoFreunde/releases/latest")
                    .build()

                val response =
                    client.newCall(request).execute()

                val json =
                    JSONObject(response.body?.string() ?: "")

                val latestTag =
                    json.getString("tag_name")

                updateTitle = latestTag

                updateMessage =
                    json.getString("body")

                val latestVersion =
                    latestTag
                        .replace("v", "")
                        .trim()

                if (
                    latestVersion.trim() !=
                    currentVersion.trim()
                ) {
                    println("GitHub Version: $latestVersion")
                    println("App Version: $currentVersion")

                    updateUrl =
                        json.getString("html_url")

                    updateMessage =
                        json.getString("body")

                    showUpdateDialog = true
                }

            } catch (e: Exception) {

                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(Unit) {

        while (true) {

            try {

                fissuresData =
                    WarframeApi.api.getFissures()

            } catch (e: Exception) {

                e.printStackTrace()
            }

            delay(120000)
        }
    }

    BackHandler {

        when {

            !showLiveScreen -> {

                showLiveScreen = true
            }

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

            ModalDrawerSheet(

                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .background(
                        brush = AppBrushes.MainBackground
                    )
            ){

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Text(

                    text = "TENNOFREUNDE",

                    style = MaterialTheme.typography.headlineMedium,

                    color = AppColors.TextPrimary,

                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(

                    text =

                        if (currentUser != null)
                            currentUser?.email ?: ""
                        else
                            "Nicht angemeldet",

                    color = AppColors.TextSecondary,


                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(
                    modifier = Modifier.height(28.dp)
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
                    },
                    colors = NavigationDrawerItemDefaults.colors(

                        unselectedContainerColor = Color.Transparent,

                        selectedContainerColor = AppColors.Card,

                        unselectedTextColor = AppColors.TextPrimary,

                        selectedTextColor = AppColors.TextPrimary
                    ),
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp),

                    shape = AppShapes.Large
                )

                NavigationDrawerItem(

                    label = {
                        Text("Fertige Sachen")
                    },

                    selected = false,

                    onClick = {

                        showFinishedScreen = true
                        showScreenshotScanner = false

                        scope.launch {
                            drawerState.close()
                        }
                    },

                    colors = NavigationDrawerItemDefaults.colors(

                        unselectedContainerColor = Color.Transparent,

                        selectedContainerColor = AppColors.Card,

                        unselectedTextColor = AppColors.TextPrimary,

                        selectedTextColor = AppColors.TextPrimary
                    ),

                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp),

                    shape = AppShapes.Large
                )

                NavigationDrawerItem(

                    label = {
                        Text("Screenshot Scanner (Beta)")
                    },

                    selected = false,

                    onClick = {

                        showFinishedScreen = false
                        showLiveScreen = false
                        showScreenshotScanner = true

                        scope.launch {
                            drawerState.close()
                        }
                    },

                    colors = NavigationDrawerItemDefaults.colors(

                        unselectedContainerColor = Color.Transparent,

                        selectedContainerColor = AppColors.Card,

                        unselectedTextColor = AppColors.TextPrimary,

                        selectedTextColor = AppColors.TextPrimary
                    ),

                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp),

                    shape = AppShapes.Large
                )

                NavigationDrawerItem(

                    label = {
                        Text("Sammlung")
                    },

                    selected = false,

                    onClick = {

                        showFinishedScreen = false
                        showScreenshotScanner = false
                        showLiveScreen = false

                        scope.launch {
                            drawerState.close()
                        }
                    },

                    colors = NavigationDrawerItemDefaults.colors(

                        unselectedContainerColor = Color.Transparent,

                        selectedContainerColor = AppColors.Card,

                        unselectedTextColor = AppColors.TextPrimary,

                        selectedTextColor = AppColors.TextPrimary
                    ),

                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp),

                    shape = AppShapes.Large
                )

                NavigationDrawerItem(

                    label = {

                        Text(

                            if (currentUser != null)
                                "Abmelden"
                            else
                                "Mit Google anmelden"
                        )
                    },

                    selected = false,

                    onClick = {

                        if (currentUser == null) {

                            loginLauncher.launch(
                                googleSignInClient.signInIntent
                            )

                        } else {

                            auth.signOut()

                            googleSignInClient.signOut()

                            currentUser = null
                        }
                    },
                    colors = NavigationDrawerItemDefaults.colors(

                        unselectedContainerColor = Color.Transparent,

                        selectedContainerColor = AppColors.Card,

                        unselectedTextColor = AppColors.TextPrimary,

                        selectedTextColor = AppColors.TextPrimary
                    ),
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp),

                    shape = AppShapes.Large
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
                    },
                    colors = NavigationDrawerItemDefaults.colors(

                        unselectedContainerColor = Color.Transparent,

                        selectedContainerColor = AppColors.Card,

                        unselectedTextColor = AppColors.TextPrimary,

                        selectedTextColor = AppColors.TextPrimary
                    ),
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp),

                    shape = AppShapes.Large
                )

                NavigationDrawerItem(

                    label = {
                        Text("Importieren")
                    },

                    selected = false,

                    onClick = {

                        importLauncher.launch(
                            arrayOf("application/json")
                        )
                    },
                    colors = NavigationDrawerItemDefaults.colors(

                        unselectedContainerColor = Color.Transparent,

                        selectedContainerColor = AppColors.Card,

                        unselectedTextColor = AppColors.TextPrimary,

                        selectedTextColor = AppColors.TextPrimary
                    ),
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp),

                    shape = AppShapes.Large
                )

                NavigationDrawerItem(

                    label = {
                        Text("Exportieren")
                    },

                    selected = false,

                    onClick = {

                        exportLauncher.launch(
                            "TennoFreundeBackup.json"
                        )
                    },
                    colors = NavigationDrawerItemDefaults.colors(

                        unselectedContainerColor = Color.Transparent,

                        selectedContainerColor = AppColors.Card,

                        unselectedTextColor = AppColors.TextPrimary,

                        selectedTextColor = AppColors.TextPrimary
                    ),
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp),

                    shape = AppShapes.Large
                )
            }
        }
    ) {

        Scaffold(

            floatingActionButton = {

                if (!showLiveScreen) {

                    Box {

                        FloatingActionButton(

                            onClick = {
                                fabExpanded = true
                            },

                            containerColor = AppColors.Card,

                            contentColor = AppColors.Accent,

                            shape = AppShapes.Large,

                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 10.dp
                            ),

                            modifier = Modifier

                                .padding(bottom = 8.dp)

                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.08f),
                                    shape = AppShapes.Large
                                )
                        ) {

                            Icon(

                                imageVector = Icons.Default.Add,

                                contentDescription = "Add",

                                modifier = Modifier.size(30.dp)
                            )
                        }

                        DropdownMenu(

                            expanded = fabExpanded,

                            onDismissRequest = {
                                fabExpanded = false
                            },

                            modifier = Modifier
                                .background(
                                    AppColors.Card,
                                    AppShapes.Large
                                )
                        ) {


                            DropdownMenuItem(

                                text = {
                                    Text(
                                        "Tab hinzufügen",
                                        color = AppColors.TextPrimary
                                    )
                                },

                                onClick = {

                                    newTabName = ""

                                    showAddTabDialog = true

                                    fabExpanded = false
                                }
                            )

                            DropdownMenuItem(

                                text = {
                                    Text(
                                        "+ Eintrag",
                                        color = AppColors.TextPrimary
                                    )
                                },

                                onClick = {

                                    if (tabs.isNotEmpty()) {


                                        val newItem = WarframeItem(

                                            name = "Neuer Eintrag",

                                            type = "warframe",

                                            tabName = tabs[selectedTab].name,

                                            subTabName =
                                                subTabs
                                                    .filter {
                                                        it.parentTab == tabs[selectedTab].name
                                                    }
                                                    .getOrNull(selectedSubTab)
                                                    ?.name ?: "",

                                            infoFields = mutableListOf(),

                                            components = mutableStateListOf(),

                                            isNew = true
                                        )


                                        items.add(0, newItem)

                                        val firebaseItems = items.toMutableList()

                                        db.collection("items")
                                            .document("shared_items")
                                            .set(
                                                hashMapOf(
                                                    "data" to firebaseItems.map { item ->

                                                        hashMapOf(
                                                            "name" to item.name,
                                                            "tabName" to item.tabName,
                                                            "subTabName" to item.subTabName,
                                                            "type" to item.type,

                                                            "infoFields" to item.infoFields.map { info ->

                                                                hashMapOf(
                                                                    "title" to info.title,
                                                                    "value" to info.value
                                                                )
                                                            },


                                                            "components" to item.components.map { component ->

                                                                hashMapOf(

                                                                    "name" to component.name,

                                                                    "checked" to false,

                                                                    "farmLocation" to component.farmLocation
                                                                )
                                                            }


                                                        )
                                                    }
                                                )
                                            )




                                        saveItems()
                                    }

                                    fabExpanded = false
                                }
                            )


                            DropdownMenuItem(

                                text = {
                                    Text("Untertab hinzufügen")
                                },

                                onClick = {

                                    if (tabs.isNotEmpty()) {

                                        newSubTabName = ""

                                        showAddSubTabDialog = true
                                    }

                                    fabExpanded = false
                                }
                            )
                            DropdownMenuItem(

                                text = {
                                    Text(
                                        "Untertab entfernen",
                                        color = AppColors.TextPrimary
                                    )
                                },

                                onClick = {

                                    showDeleteSubTabDialog = true

                                    fabExpanded = false
                                }
                            )

                            DropdownMenuItem(

                                text = {
                                    Text(
                                        "Tab entfernen",
                                        color = AppColors.TextPrimary
                                    )
                                },

                                onClick = {

                                    showDeleteTabDialog = true

                                    fabExpanded = false
                                }
                            )

                        }
                    }
                }
            }


        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = AppBrushes.MainBackground
                    )
                    .padding(paddingValues)
            ) {

                if (showFinishedScreen) {

                    FinishedScreen(

                        items = items,

                        onBack = {
                            showFinishedScreen = false
                        },

                        onResetItem = { item ->

                            item.components.forEach {
                                it.checked = false
                            }

                            saveLocalProgress()
                        }
                    )

                } else if (showScreenshotScanner) {

                    ScreenshotScannerScreen()

                } else if (showLiveScreen) {

                    LiveScreen()

                } else {

                    if (showAddSubTabDialog) {
                        AlertDialog(

                            containerColor = AppColors.Card,

                            shape = AppShapes.Large,

                            titleContentColor = AppColors.TextPrimary,

                            textContentColor = AppColors.TextSecondary,

                            onDismissRequest = {

                                showAddSubTabDialog = false
                            },

                            confirmButton = {

                                Button(

                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppColors.Accent,
                                        contentColor = Color.Black
                                    ),

                                    shape = AppShapes.Large,


                                    onClick = {


                                        if (
                                            newSubTabName.isNotEmpty() &&
                                            tabs.isNotEmpty()
                                        ) {

                                            subTabs.add(

                                                SubTabItem(

                                                    name = newSubTabName,

                                                    parentTab =
                                                        tabs[selectedTab].name
                                                )
                                            )

                                            saveSubTabs()
                                        }

                                        showAddSubTabDialog = false

                                    }
                                ) {

                                    Text("Erstellen")
                                }
                            },

                            dismissButton = {

                                Button(

                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppColors.Accent,
                                        contentColor = Color.Black
                                    ),

                                    shape = AppShapes.Large,

                                    onClick = {

                                        showAddSubTabDialog = false
                                    }
                                ) {

                                    Text("Abbrechen")
                                }
                            },

                            title = {

                                Text(
                                    "Neuer Untertab",
                                    color = AppColors.TextPrimary
                                )
                            },

                            text = {

                                OutlinedTextField(

                                    colors = OutlinedTextFieldDefaults.colors(

                                        focusedBorderColor = AppColors.Accent,

                                        unfocusedBorderColor =
                                            AppColors.TextPrimary.copy(alpha = 0.2f),

                                        focusedTextColor = AppColors.TextPrimary,

                                        unfocusedTextColor = AppColors.TextPrimary,

                                        cursorColor = AppColors.Accent
                                    ),

                                    shape = AppShapes.Large,

                                    value = newSubTabName,

                                    onValueChange = {

                                        newSubTabName = it
                                    },

                                    singleLine = true,

                                    label = {

                                        Text(
                                            "Untertab Name",
                                            color = AppColors.TextPrimary
                                        )
                                    }
                                )
                            }
                        )
                    }

                    if (tabs.isNotEmpty()) {

                        ScrollableTabRow(

                            selectedTabIndex = selectedTab,

                            containerColor = Color.Transparent,

                            edgePadding = 12.dp,

                            divider = { },

                            indicator = { }

                        ) {

                            tabs.forEachIndexed { index, tab ->

                                val tabItems =
                                    items.filter {
                                        it.tabName == tab.name
                                    }

                                val totalTabComponents =
                                    tabItems.sumOf {
                                        it.components.size
                                    }

                                val checkedTabComponents =
                                    tabItems.sumOf { item ->

                                        item.components.count {
                                            it.checked
                                        }
                                    }

                                val tabProgress =
                                    if (totalTabComponents > 0)
                                        checkedTabComponents * 100 / totalTabComponents
                                    else
                                        0

                                val newItemsCount =
                                    tabItems.count {
                                        it.isNew
                                    }

                                val selected =
                                    selectedTab == index

                                Tab(

                                    selected = selected,

                                    onClick = {
                                        selectedTab = index
                                    },

                                    text = {

                                        Box(

                                            modifier = Modifier

                                                .background(

                                                    if (selected)
                                                        Color(0xFFC8A8FF)
                                                    else
                                                        AppColors.TextPrimary.copy(alpha = 0.12f),

                                                    AppShapes.Large
                                                )

                                                .padding(
                                                    horizontal = 18.dp,
                                                    vertical = 10.dp
                                                )
                                        ) {

                                            Column(

                                                horizontalAlignment =
                                                    androidx.compose.ui.Alignment.CenterHorizontally
                                            ) {

                                                Text(

                                                    text =

                                                        if (newItemsCount > 0)
                                                            "${tab.name} • $newItemsCount"
                                                        else
                                                            tab.name,

                                                    color =

                                                        if (selected)
                                                            Color.Black
                                                        else
                                                            Color.White
                                                )

                                                Spacer(
                                                    modifier = Modifier.height(4.dp)
                                                )

                                                Text(

                                                    text = "$tabProgress%",

                                                    color =

                                                        if (selected)
                                                            Color.Black.copy(alpha = 0.7f)
                                                        else
                                                            Color.LightGray,

                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                                Spacer(
                                                    modifier = Modifier.height(6.dp)
                                                )

                                                LinearProgressIndicator(

                                                    progress = {
                                                        tabProgress / 100f
                                                    },

                                                    modifier = Modifier
                                                        .width(60.dp)
                                                        .height(6.dp),

                                                    color =

                                                        if (selected)
                                                            Color.Black
                                                        else
                                                            AppColors.Accent,

                                                    trackColor =
                                                        Color.White.copy(alpha = 0.15f),

                                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }


                    val currentSubTabs =

                        if (tabs.isNotEmpty()) {

                            subTabs.filter {

                                it.parentTab ==
                                        tabs[selectedTab].name
                            }

                        } else {

                            emptyList()
                        }

                    println("=== CURRENT SUBTABS ===")

                    currentSubTabs.forEach {

                        println(
                            "SUBTAB: '${it.name}'"
                        )
                    }

                    if (currentSubTabs.isNotEmpty()) {

                        ScrollableTabRow(

                            selectedTabIndex =
                                selectedSubTab.coerceAtMost(
                                    currentSubTabs.lastIndex
                                ),

                            containerColor = Color.Transparent,

                            edgePadding = 12.dp,

                            divider = { },

                            indicator = { }
                        ) {

                            currentSubTabs.forEachIndexed { index, subTab ->

                                val subTabItems =
                                    items.filter {
                                        it.subTabName == subTab.name
                                    }

                                val totalSubTabComponents =
                                    subTabItems.sumOf {
                                        it.components.size
                                    }

                                val checkedSubTabComponents =
                                    subTabItems.sumOf { item ->

                                        item.components.count {
                                            it.checked
                                        }
                                    }

                                val subTabProgress =
                                    if (totalSubTabComponents > 0)
                                        checkedSubTabComponents * 100 / totalSubTabComponents
                                    else
                                        0

                                Tab(

                                    selected = selectedSubTab == index,

                                    onClick = {
                                        selectedSubTab = index
                                    },

                                    text = {

                                        val newSubItemsCount =
                                            subTabItems.count {
                                                it.isNew
                                            }

                                        val selected =
                                            selectedSubTab == index

                                        Box(

                                            modifier = Modifier

                                                .background(

                                                    if (selected)
                                                        AppColors.Accent
                                                    else
                                                        AppColors.TextPrimary.copy(alpha = 0.08f),

                                                    RoundedCornerShape(50)
                                                )

                                                .padding(
                                                    horizontal = 16.dp,
                                                    vertical = 8.dp
                                                )
                                        ) {

                                            Column(

                                                horizontalAlignment =
                                                    androidx.compose.ui.Alignment.CenterHorizontally
                                            ) {

                                                Text(

                                                    text =

                                                        if (newSubItemsCount > 0)
                                                            "${subTab.name} • $newSubItemsCount"
                                                        else
                                                            subTab.name,

                                                    color =

                                                        if (selected)
                                                            Color.Black
                                                        else
                                                            Color.White
                                                )

                                                Spacer(
                                                    modifier = Modifier.height(4.dp)
                                                )

                                                Text(

                                                    text = "$subTabProgress%",

                                                    color =

                                                        if (selected)
                                                            Color.Black.copy(alpha = 0.7f)
                                                        else
                                                            Color.LightGray,

                                                    style = MaterialTheme.typography.labelSmall
                                                )

                                                Spacer(
                                                    modifier = Modifier.height(6.dp)
                                                )

                                                LinearProgressIndicator(

                                                    progress = {
                                                        subTabProgress / 100f
                                                    },

                                                    modifier = Modifier
                                                        .width(50.dp)
                                                        .height(5.dp),

                                                    color =

                                                        if (selected)
                                                            Color.Black
                                                        else
                                                            AppColors.Accent,

                                                    trackColor =
                                                        Color.White.copy(alpha = 0.15f),

                                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                                )
                                            }
                                        }
                                    }
                                )
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



                        OutlinedTextField(

                            colors = OutlinedTextFieldDefaults.colors(

                                focusedBorderColor = AppColors.Accent,

                                unfocusedBorderColor =
                                    AppColors.TextPrimary.copy(alpha = 0.2f),

                                focusedTextColor = AppColors.TextPrimary,

                                unfocusedTextColor = AppColors.TextPrimary,

                                cursorColor = AppColors.Accent
                            ),

                            shape = AppShapes.Large,

                            value = searchText,

                            onValueChange = {
                                searchText = it
                            },

                            singleLine = true,

                            label = {
                                Text(
                                    "Suchen",
                                    color = AppColors.TextSecondary
                                )
                            },


                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )
                        println("ITEMS GESAMT: ${items.size}")

                        println(
                            "GEWÄHLTER SUBTAB: '" +
                                    currentSubTabs
                                        .getOrNull(selectedSubTab)
                                        ?.name +
                                    "'"
                        )

                        WarframeList(

                            items = items,

                            fissuresData = fissuresData,

                            currentTab = tabs[selectedTab].name,

                            currentSubTab =
                                currentSubTabs
                                    .getOrNull(selectedSubTab)
                                    ?.name ?: "",

                            saveItems = {
                                saveItems()
                            },
                            saveLocalProgress = {
                                saveLocalProgress()
                            },

                            sortAZ = sortAZ,

                            searchText = searchText
                        )
                    }


                    if (showAddTabDialog) {

                        AlertDialog(

                            containerColor = AppColors.Card,

                            shape = AppShapes.Large,

                            titleContentColor = AppColors.TextPrimary,

                            textContentColor = AppColors.TextSecondary,

                            onDismissRequest = {
                                showAddTabDialog = false
                            },

                            confirmButton = {

                                Button(

                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppColors.Accent,
                                        contentColor = Color.Black
                                    ),

                                    shape = AppShapes.Large,

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

                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppColors.Accent,
                                        contentColor = Color.Black
                                    ),

                                    shape = AppShapes.Large,

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

                                    colors = OutlinedTextFieldDefaults.colors(

                                        focusedBorderColor = AppColors.Accent,

                                        unfocusedBorderColor =
                                            AppColors.TextPrimary.copy(alpha = 0.2f),

                                        focusedTextColor = AppColors.TextPrimary,

                                        unfocusedTextColor = AppColors.TextPrimary,

                                        cursorColor = AppColors.Accent
                                    ),

                                    shape = AppShapes.Large,

                                    value = newTabName,

                                    onValueChange = {
                                        newTabName = it
                                    },

                                    singleLine = true,

                                    label = {
                                        Text("Tab Name",
                                            color = AppColors.TextPrimary
                                        )
                                    }
                                )
                            }
                        )
                    }

                    UpdateDialog(
                        showDialog = showUpdateDialog,
                        updateTitle = updateTitle,
                        updateMessage = updateMessage,
                        updateUrl = updateUrl,
                        onDismiss = {
                            showUpdateDialog = false
                        }
                    )


                }


                if (showDeleteTabDialog) {

                    AlertDialog(

                        containerColor = AppColors.Card,

                        shape = AppShapes.Large,

                        titleContentColor = AppColors.TextPrimary,

                        textContentColor = AppColors.TextSecondary,

                        onDismissRequest = {
                            showDeleteTabDialog = false
                        },

                        confirmButton = {

                            Button(

                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.Accent,
                                    contentColor = Color.Black
                                ),

                                shape = AppShapes.Large,

                                onClick = {

                                    if (tabs.isNotEmpty()) {

                                        val currentTabName =
                                            tabs[selectedTab].name

                                        items.removeAll {
                                            it.tabName == currentTabName
                                        }

                                        subTabs.removeAll {
                                            it.parentTab == currentTabName
                                        }

                                        tabs.removeAt(selectedTab)

                                        if (tabs.isEmpty()) {

                                            selectedTab = 0

                                        } else if (selectedTab > tabs.lastIndex) {

                                            selectedTab = tabs.lastIndex
                                        }

                                        saveItems()
                                        saveTabs()
                                        saveSubTabs()
                                    }

                                    showDeleteTabDialog = false
                                }
                            ) {

                                Text("Löschen")
                            }
                        },

                        dismissButton = {

                            Button(

                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.Accent,
                                    contentColor = Color.Black
                                ),

                                shape = AppShapes.Large,

                                onClick = {
                                    showDeleteTabDialog = false
                                }
                            ) {

                                Text("Abbrechen")
                            }
                        },

                        title = {
                            Text("Tab löschen",
                                color = AppColors.TextPrimary
                            )
                        },

                        text = {

                            Text(
                                "Willst du den Tab \"${tabs[selectedTab].name}\" wirklich löschen?"
                            )
                        }
                    )
                }



                if (showDeleteSubTabDialog) {

                    val currentSubTab =
                        subTabs
                            .filter {
                                it.parentTab == tabs[selectedTab].name
                            }
                            .getOrNull(selectedSubTab)

                    AlertDialog(

                        containerColor = AppColors.Card,

                        shape = AppShapes.Large,

                        titleContentColor = AppColors.TextPrimary,

                        textContentColor = AppColors.TextSecondary,

                        onDismissRequest = {
                            showDeleteSubTabDialog = false
                        },

                        confirmButton = {

                            Button(

                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.Accent,
                                    contentColor = Color.Black
                                ),

                                shape = AppShapes.Large,

                                onClick = {

                                    if (currentSubTab != null) {

                                        items.removeAll {
                                            it.subTabName == currentSubTab.name
                                        }

                                        subTabs.remove(currentSubTab)

                                        saveItems()
                                        saveSubTabs()

                                        if (selectedSubTab > 0) {
                                            selectedSubTab--
                                        }
                                    }

                                    showDeleteSubTabDialog = false
                                }
                            ) {

                                Text("Löschen")
                            }
                        },

                        dismissButton = {

                            Button(

                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.Accent,
                                    contentColor = Color.Black
                                ),

                                shape = AppShapes.Large,

                                onClick = {
                                    showDeleteSubTabDialog = false
                                }
                            ) {

                                Text("Abbrechen")
                            }
                        },

                        title = {
                            Text("Untertab löschen",
                                color = AppColors.TextPrimary
                            )
                        },

                        text = {

                            Text(
                                "Willst du den Untertab \"${currentSubTab?.name}\" wirklich löschen?"
                            )
                        }
                    )
                }
            }
        }
    }
}
