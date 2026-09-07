package com.example.tennofreunde.components

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.tennofreunde.R
import com.example.tennofreunde.models.ComponentItem
import com.example.tennofreunde.models.WarframeItem
import com.example.tennofreunde.ui.theme.AppColors
import com.example.tennofreunde.ui.theme.AppShapes
import com.example.tennofreunde.api.FissureResponse
import com.example.tennofreunde.RelicLoader
import com.example.tennofreunde.utils.translateComponent
import com.example.tennofreunde.utils.warframeItemImageUrl

@Composable
fun WarframeCard(

    item: WarframeItem,

    fissuresData: List<FissureResponse>,

    saveItems: () -> Unit,

    saveLocalProgress: () -> Unit,

    onDelete: () -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    var showEditDialog by remember {
        mutableStateOf(false)
    }

    var itemName by remember {
        mutableStateOf(item.name)
    }

    var newComponent by remember {
        mutableStateOf("")
    }

    val context = LocalContext.current

    val imagePath = "${
        item.name
            .lowercase()
            .replace(" ", "_")
            .replace("-", "_")
    }.png"

    var bitmap by remember {
        mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null)
    }

    LaunchedEffect(imagePath) {

        try {

            context.assets.open(imagePath).use { stream ->

                bitmap =
                    BitmapFactory
                        .decodeStream(stream)
                        .asImageBitmap()
            }

        } catch (_: Exception) {

            bitmap = null
        }
    }

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
            .border(
                width = 1.dp,
                color = AppColors.OrokinGold.copy(alpha = 0.45f),
                shape = AppShapes.Large
            )
            .clickable {

                expanded = !expanded
                item.isNew = false
            },

        shape = AppShapes.Large,

        colors = CardDefaults.cardColors(
            containerColor = AppColors.Card
        )
    ) {

        Column {

            Box {

                if (bitmap != null) {

                    Image(

                        bitmap = bitmap!!,

                        contentDescription = item.name,

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 28.dp,
                                    topEnd = 28.dp
                                )
                            ),

                        contentScale = ContentScale.Crop
                    )

                } else {

                    AsyncImage(

                        model = warframeItemImageUrl(item),

                        contentDescription = item.name,

                        placeholder = painterResource(R.drawable.placeholder),

                        error = painterResource(R.drawable.placeholder),

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 28.dp,
                                    topEnd = 28.dp
                                )
                            ),

                        contentScale = ContentScale.Crop
                    )
                }

                Box(

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(

                            brush = Brush.verticalGradient(

                                colors = listOf(

                                    Color.Transparent,

                                    Color(0xCC181825)
                                )
                            )
                        )
                )

                Row(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),

                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Column {

                        Text(

                            text = item.name,

                            style = MaterialTheme.typography.titleLarge,

                            color = Color.White
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        val checkedCount =
                            item.components.count { it.checked }

                        val totalCount =
                            item.components.size

                        Box(

                            modifier = Modifier
                                .background(
                                    Color(0x33C8A8FF),
                                    RoundedCornerShape(50)
                                )
                                .padding(
                                    horizontal = 12.dp,
                                    vertical = 6.dp
                                )
                        ) {

                            Text(

                                text = "$checkedCount / $totalCount Komponenten",

                                color = Color(0xFFC8A8FF)
                            )
                        }
                    }

                    IconButton(

                        onClick = {

                            showEditDialog = true
                        }
                    ) {

                        Icon(

                            imageVector = Icons.Default.Edit,

                            contentDescription = null,

                            tint = Color(0xFFC8A8FF)
                        )
                    }
                }
            }

            AnimatedVisibility(

                visible = expanded,

                enter = fadeIn() + expandVertically(),

                exit = fadeOut() + shrinkVertically()
            ) {

                Card(

                    shape = RoundedCornerShape(
                        bottomStart = 28.dp,
                        bottomEnd = 28.dp
                    ),

                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF232136)
                    )
                ) {

                    Column(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {

                        item.infoFields.forEach { field ->

                            Card(

                                shape = AppShapes.Medium,

                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF2B293D)
                                ),

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                            ) {

                                Column(

                                    modifier = Modifier.padding(14.dp)
                                ) {

                                    Text(

                                        text = field.title,

                                        color = AppColors.Accent
                                    )

                                    Spacer(
                                        modifier = Modifier.height(4.dp)
                                    )

                                    Text(

                                        text = field.value,

                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )

                        item.components.forEachIndexed { index, component ->

                            Row(

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),

                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Checkbox(

                                    checked = component.checked,

                                    onCheckedChange = { isChecked ->

                                        item.components[index] =
                                            component.copy(
                                                checked = isChecked
                                            )

                                        saveLocalProgress()

                                        saveItems()
                                    },

                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFFC8A8FF)
                                    )
                                )

                                Column {

                                    Text(

                                        text = translateComponent(
                                            component.name
                                        ),

                                        color = Color.White
                                    )

                                    if (
                                        !component.checked &&
                                        component.farmLocation.isNotEmpty()
                                    ) {

                                        Spacer(
                                            modifier = Modifier.height(2.dp)
                                        )

                                        Text(

                                            text = "Farmbar in:",

                                            color = Color.LightGray,

                                            style =
                                                MaterialTheme.typography.labelSmall
                                        )

                                        Text(

                                            text = component.farmLocation,

                                            color = Color(0xFFC8A8FF),

                                            style =
                                                MaterialTheme.typography.bodySmall
                                        )
                                    }


                                    val relicMap = remember {

                                        RelicLoader.loadRelics(context)
                                    }




                                    val fullPartName =

                                        "${item.name} ${component.name}"
                                            .trim()
                                            .lowercase()

                                    val relicData =
                                        relicMap[fullPartName]

                                    val autoRelic =

                                        if (component.relic.isNotEmpty()) {

                                            component.relic

                                        } else {

                                            relicData?.relic ?: ""
                                        }


                                    val autoRotation =

                                        if (component.rotation.isNotEmpty()) {

                                            component.rotation

                                        } else {

                                            relicData?.rotation ?: ""
                                        }

                                    val autoFarmLocation =

                                        if (component.farmLocation.isNotEmpty()) {

                                            component.farmLocation

                                        } else {

                                            relicData?.farmLocation ?: ""
                                        }

                                    if (autoRelic.isNotEmpty()) {

                                        Spacer(
                                            modifier = Modifier.height(2.dp)
                                        )

                                        Text(

                                            text = "Relikt: $autoRelic",

                                            color = Color.Cyan,

                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    val activeMission = findMatchingMission(

                                        autoRelic,

                                        fissuresData
                                    )

                                    if (activeMission.isNotEmpty()) {

                                        Spacer(
                                            modifier = Modifier.height(2.dp)
                                        )

                                        Spacer(
                                            modifier = Modifier.height(2.dp)
                                        )

                                        Text(

                                            text = "Aktive Mission: $activeMission",

                                            color = Color.Yellow,

                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        val checkedCount = item.components.count { it.checked }
        val totalCount = item.components.size
        val progress = if (totalCount == 0) 0f else checkedCount.toFloat() / totalCount.toFloat()

        Dialog(onDismissRequest = { showEditDialog = false }) {
            Card(
                shape = AppShapes.Large,
                colors = CardDefaults.cardColors(containerColor = Color(0xF01B1A28)),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 720.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("SAMMLUNG", color = AppColors.OrokinGold, style = MaterialTheme.typography.labelLarge)
                            Text("Eintrag bearbeiten", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                            Text(
                                "$checkedCount von $totalCount Komponenten abgeschlossen",
                                color = AppColors.TextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = { showEditDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Schließen", tint = AppColors.TextSecondary)
                        }
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
                        color = AppColors.Accent,
                        trackColor = Color.White.copy(alpha = 0.12f)
                    )

                    Surface(shape = AppShapes.Medium, color = Color.White.copy(alpha = 0.05f)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Basisdaten", color = AppColors.OrokinGold, style = MaterialTheme.typography.labelLarge)
                            OutlinedTextField(
                                value = itemName,
                                onValueChange = {
                                    itemName = it
                                    item.name = it
                                    saveItems()
                                },
                                label = { Text("Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = editDialogTextFieldColors()
                            )
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                EditInfoPill("Typ", item.type.ifBlank { "Warframe" }, Modifier.weight(1f))
                                EditInfoPill("Bereich", item.subTabName.ifBlank { item.tabName }, Modifier.weight(1f))
                            }
                        }
                    }

                    Surface(shape = AppShapes.Medium, color = Color.White.copy(alpha = 0.05f)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Komponenten", color = AppColors.OrokinGold, style = MaterialTheme.typography.labelLarge)
                                    Text("Namen, Relikte, Rotation und Farmort pflegen", color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                                }
                                Text("$checkedCount/$totalCount", color = AppColors.Accent, style = MaterialTheme.typography.titleMedium)
                            }

                            item.components.forEachIndexed { index, component ->
                                Surface(
                                    shape = AppShapes.Small,
                                    color = if (component.checked) Color(0x332FD6A2) else Color(0xFF232136),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(
                                                    checked = component.checked,
                                                    onCheckedChange = { isChecked ->
                                                        item.components[index] = component.copy(checked = isChecked)
                                                        saveLocalProgress()
                                                        saveItems()
                                                    },
                                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF55E6B5))
                                                )
                                                Text(
                                                    if (component.checked) "Vorhanden" else "Fehlt noch",
                                                    color = if (component.checked) Color(0xFF55E6B5) else AppColors.TextSecondary,
                                                    style = MaterialTheme.typography.labelLarge
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    item.components.removeAt(index)
                                                    saveItems()
                                                    saveLocalProgress()
                                                }
                                            ) {
                                                Icon(Icons.Default.DeleteOutline, contentDescription = "Komponente löschen", tint = Color(0xFFFF9B8F))
                                            }
                                        }

                                        OutlinedTextField(
                                            value = component.name,
                                            onValueChange = { newName ->
                                                item.components[index] = component.copy(name = newName)
                                                saveItems()
                                            },
                                            label = { Text("Komponente") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = editDialogTextFieldColors()
                                        )

                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            OutlinedTextField(
                                                value = component.relic,
                                                onValueChange = { newRelic ->
                                                    item.components[index] = component.copy(relic = newRelic)
                                                    saveItems()
                                                },
                                                label = { Text("Relikt") },
                                                singleLine = true,
                                                modifier = Modifier.weight(1f),
                                                colors = editDialogTextFieldColors()
                                            )
                                            OutlinedTextField(
                                                value = component.rotation,
                                                onValueChange = { newRotation ->
                                                    item.components[index] = component.copy(rotation = newRotation)
                                                    saveItems()
                                                },
                                                label = { Text("Rotation") },
                                                singleLine = true,
                                                modifier = Modifier.weight(1f),
                                                colors = editDialogTextFieldColors()
                                            )
                                        }

                                        OutlinedTextField(
                                            value = component.farmLocation,
                                            onValueChange = { newFarmLocation ->
                                                item.components[index] = component.copy(farmLocation = newFarmLocation)
                                                saveItems()
                                            },
                                            label = { Text("Farmort") },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = editDialogTextFieldColors()
                                        )
                                    }
                                }
                            }

                            Surface(shape = AppShapes.Small, color = Color.White.copy(alpha = 0.04f)) {
                                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = newComponent,
                                        onValueChange = { newComponent = it },
                                        label = { Text("Neue Komponente") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = editDialogTextFieldColors()
                                    )
                                    Button(
                                        onClick = {
                                            val cleanName = newComponent.trim()
                                            if (cleanName.isNotEmpty()) {
                                                item.components.add(ComponentItem(name = cleanName))
                                                newComponent = ""
                                                saveItems()
                                            }
                                        },
                                        enabled = newComponent.trim().isNotEmpty(),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Accent, contentColor = Color.Black),
                                        shape = AppShapes.Small
                                    ) {
                                        Icon(Icons.Default.AddCircle, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Komponente hinzufügen")
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onDelete()
                                saveItems()
                                saveLocalProgress()
                                showEditDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            shape = AppShapes.Small
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF9B8F))
                            Spacer(Modifier.width(8.dp))
                            Text("Löschen", color = Color(0xFFFF9B8F))
                        }

                        Button(
                            onClick = {
                                saveItems()
                                saveLocalProgress()
                                showEditDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Accent, contentColor = Color.Black),
                            shape = AppShapes.Small
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Speichern")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditInfoPill(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(shape = AppShapes.Small, color = Color.White.copy(alpha = 0.06f), modifier = modifier) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, color = AppColors.TextSecondary, style = MaterialTheme.typography.labelSmall)
            Text(value, color = Color.White, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun editDialogTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0xFF232136),
    unfocusedContainerColor = Color(0xFF232136),
    focusedBorderColor = AppColors.Accent,
    unfocusedBorderColor = Color.White.copy(alpha = 0.22f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = AppColors.Accent,
    focusedLabelColor = AppColors.Accent,
    unfocusedLabelColor = AppColors.TextSecondary
)

fun findMatchingMission(

    relic: String,

    fissuresData: List<FissureResponse>

): String {

    if (relic.isEmpty()) {

        return ""
    }

    val relicTier = relic
        .split(" ")
        .firstOrNull()
        ?.trim()
        ?.lowercase()

    val matchingFissure = fissuresData.firstOrNull {

        it.tier
            ?.lowercase()
            ?.contains(relicTier ?: "") == true
    }

    return if (matchingFissure != null) {

        "${matchingFissure.node} (${matchingFissure.missionType})"

    } else {

        ""
    }
}
