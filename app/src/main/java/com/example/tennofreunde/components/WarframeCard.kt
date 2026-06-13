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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.tennofreunde.R
import com.example.tennofreunde.models.ComponentItem
import com.example.tennofreunde.models.WarframeItem
import com.example.tennofreunde.ui.theme.AppColors
import com.example.tennofreunde.ui.theme.AppShapes
import com.example.tennofreunde.api.FissureResponse
import com.example.tennofreunde.RelicLoader
import com.example.tennofreunde.utils.translateComponent

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
                color = Color.White.copy(alpha = 0.08f),
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

                    Image(

                        painter = painterResource(R.drawable.placeholder),

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

                                    android.util.Log.d(
                                        "PART_TEST",
                                        "SUCHE: $fullPartName"
                                    )

                                    val relicData =
                                        relicMap[fullPartName]

                                    android.util.Log.d(
                                        "PART_TEST",
                                        "GEFUNDEN: ${relicData?.part}"
                                    )

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

        Dialog(

            onDismissRequest = {
                showEditDialog = false
            }
        ) {

            Card(

                shape = AppShapes.Large,

                colors = CardDefaults.cardColors(
                    containerColor = Color(0xDD232136)
                ),

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {

                Column(

                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 700.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {

                    Text(

                        text = "Eintrag bearbeiten",

                        color = Color.White,

                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    OutlinedTextField(

                        value = itemName,

                        onValueChange = {

                            itemName = it
                            item.name = it
                        },

                        label = {
                            Text("Name")
                        },

                        modifier = Modifier.fillMaxWidth(),

                        colors = OutlinedTextFieldDefaults.colors(

                            focusedContainerColor = Color(0xFF232136),

                            unfocusedContainerColor = Color(0xFF232136),

                            focusedBorderColor = AppColors.Accent,

                            unfocusedBorderColor = Color(0x33FFFFFF),

                            focusedTextColor = Color.White,

                            unfocusedTextColor = Color.White,

                            cursorColor = AppColors.Accent
                        )
                    )

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    item.components.forEachIndexed { index, component ->

                        Column(

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        ) {

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            OutlinedTextField(

                                value = component.relic,

                                onValueChange = { newRelic ->

                                    item.components[index] =
                                        component.copy(
                                            relic = newRelic
                                        )

                                    saveItems()
                                },

                                label = {
                                    Text("Relikt")
                                },

                                modifier = Modifier.fillMaxWidth(),

                                colors = OutlinedTextFieldDefaults.colors(

                                    focusedContainerColor = Color(0xFF232136),

                                    unfocusedContainerColor = Color(0xFF232136),

                                    focusedBorderColor = AppColors.Accent,

                                    unfocusedBorderColor = Color(0x33FFFFFF),

                                    focusedTextColor = Color.White,

                                    unfocusedTextColor = Color.White,

                                    cursorColor = AppColors.Accent,

                                    focusedLabelColor = AppColors.Accent,

                                    unfocusedLabelColor = Color.LightGray
                                )
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            OutlinedTextField(

                                value = component.rotation,

                                onValueChange = { newRotation ->

                                    item.components[index] =
                                        component.copy(
                                            rotation = newRotation
                                        )

                                    saveItems()
                                },

                                label = {
                                    Text("Rotation")
                                },

                                modifier = Modifier.fillMaxWidth(),

                                colors = OutlinedTextFieldDefaults.colors(

                                    focusedContainerColor = Color(0xFF232136),

                                    unfocusedContainerColor = Color(0xFF232136),

                                    focusedBorderColor = AppColors.Accent,

                                    unfocusedBorderColor = Color(0x33FFFFFF),

                                    focusedTextColor = Color.White,

                                    unfocusedTextColor = Color.White,

                                    cursorColor = AppColors.Accent,

                                    focusedLabelColor = AppColors.Accent,

                                    unfocusedLabelColor = Color.LightGray
                                )
                            )

                            Row(

                                modifier = Modifier.fillMaxWidth(),

                                horizontalArrangement =
                                    Arrangement.SpaceBetween,

                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                Row(

                                    verticalAlignment =
                                        Alignment.CenterVertically
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
                                        }
                                    )

                                    Text(

                                        text = component.name,

                                        color = Color.White
                                    )
                                }

                                IconButton(

                                    onClick = {

                                        item.components.removeAt(index)

                                        saveItems()

                                        saveLocalProgress()
                                    }
                                ) {

                                    Icon(

                                        imageVector = Icons.Default.Delete,

                                        contentDescription = null,

                                        tint = Color.Red
                                    )
                                }
                            }

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            OutlinedTextField(

                                value = component.farmLocation,

                                onValueChange = { newFarmLocation ->

                                    item.components[index] =
                                        component.copy(
                                            farmLocation = newFarmLocation
                                        )

                                    saveItems()
                                },

                                label = {
                                    Text("Farmort")
                                },

                                modifier = Modifier.fillMaxWidth(),

                                colors = OutlinedTextFieldDefaults.colors(

                                    focusedContainerColor = Color(0xFF232136),

                                    unfocusedContainerColor = Color(0xFF232136),

                                    focusedBorderColor = AppColors.Accent,

                                    unfocusedBorderColor = Color(0x33FFFFFF),

                                    focusedTextColor = Color.White,

                                    unfocusedTextColor = Color.White,

                                    cursorColor = AppColors.Accent,

                                    focusedLabelColor = AppColors.Accent,

                                    unfocusedLabelColor = Color.LightGray
                                )
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    OutlinedTextField(

                        value = newComponent,

                        onValueChange = {
                            newComponent = it
                        },

                        label = {
                            Text("Neue Komponente")
                        },

                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Button(

                        onClick = {

                            if (newComponent.isNotEmpty()) {

                                item.components.add(

                                    ComponentItem(
                                        name = newComponent
                                    )
                                )

                                newComponent = ""

                                saveItems()
                            }
                        },

                        modifier = Modifier.fillMaxWidth(),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Accent,
                            contentColor = Color.Black
                        )
                    ) {

                        Text("+ Komponente hinzufügen")
                    }

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    Row(

                        modifier = Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        OutlinedButton(

                            onClick = {

                                onDelete()

                                saveItems()

                                saveLocalProgress()

                                showEditDialog = false
                            },

                            modifier = Modifier.weight(1f)
                        ) {

                            Text("Löschen")
                        }

                        Button(

                            onClick = {

                                saveItems()

                                saveLocalProgress()

                                showEditDialog = false
                            },

                            modifier = Modifier.weight(1f),

                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Accent,
                                contentColor = Color.Black
                            )
                        ) {

                            Text("Speichern")
                        }
                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )
                    }
                }
            }
        }
    }
}
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

        "Keine aktive Mission"
    }
}