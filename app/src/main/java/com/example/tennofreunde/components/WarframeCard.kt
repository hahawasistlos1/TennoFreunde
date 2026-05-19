package com.example.tennofreunde.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.tennofreunde.models.ComponentItem
import com.example.tennofreunde.models.InfoField
import com.example.tennofreunde.models.WarframeItem
import androidx.compose.foundation.background
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import com.example.tennofreunde.R
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.runtime.*
import com.example.tennofreunde.ui.theme.AppColors
import com.example.tennofreunde.ui.theme.AppShapes
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ButtonDefaults






@Composable
fun WarframeCard(
    item: WarframeItem,
    saveItems: () -> Unit,
    saveLocalProgress: () -> Unit,
    onDelete: () -> Unit
) {

    var showEditDialog by remember {
        mutableStateOf(false)

    }

    var expanded by remember {
        mutableStateOf(false)
    }

    var showDetailsDialog by remember {
        mutableStateOf(false)
    }

    var itemName by remember {
        mutableStateOf(item.name)
    }

    var newTitle by remember {
        mutableStateOf("")
    }

    var newValue by remember {
        mutableStateOf("")
    }


    var newComponent by remember {
        mutableStateOf("")
    }

    val cardElevation =
        if (expanded) 18.dp else 10.dp
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
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = cardElevation
        )
    ) {

        Column {

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

                        if (item.isNew) {

                            Box(

                                modifier = Modifier
                                    .fillMaxWidth(),

                                contentAlignment =
                                    androidx.compose.ui.Alignment.TopEnd
                            ) {

                                Box(

                                    modifier = Modifier

                                        .background(
                                            Color(0xFFFF1744),
                                            RoundedCornerShape(50)
                                        )

                                        .padding(
                                            horizontal = 14.dp,
                                            vertical = 6.dp
                                        )
                                ) {

                                    Text(

                                        text = "NEU",

                                        color = Color.White,

                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }

                            Spacer(
                                modifier = Modifier.height(10.dp)
                            )
                        }

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


                val cardElevation =
                    if (expanded) 18.dp else 10.dp

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
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 16.dp
                            )
                    ) {

                        item.infoFields.forEach { field ->

                            Card(

                                shape = AppShapes.Medium,

                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF232136)
                                ),

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {

                                Column(

                                    modifier = Modifier.padding(14.dp)
                                ) {

                                    Text(

                                        text = field.title,

                                        color = AppColors.Accent,

                                        style = MaterialTheme.typography.labelLarge
                                    )

                                    Spacer(
                                        modifier = Modifier.height(6.dp)
                                    )

                                    Text(

                                        text = field.value,

                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        item.components.forEach { component ->

                            Row(

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),

                                verticalAlignment =
                                    androidx.compose.ui.Alignment.CenterVertically
                            ) {

                                Checkbox(

                                    checked = component.checked,

                                    onCheckedChange = { isChecked ->

                                        component.checked = isChecked
                                        saveLocalProgress()

                                    },

                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFFC8A8FF)
                                    )
                                )

                                Text(

                                    text = component.name,

                                    color = Color.White,

                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }






        if (showEditDialog) {

            AnimatedVisibility(

                visible = showEditDialog,

                enter = fadeIn() + expandVertically(),

                exit = fadeOut() + shrinkVertically()
            ) {

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

                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 12.dp
                        ),

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.08f),
                                shape = AppShapes.Large
                            )
                    ) {


                        Column(

                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 700.dp)

                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF232136),
                                            Color(0xFF1E1E2E)
                                        )
                                    )
                                )

                                .imePadding()

                                .padding(20.dp)

                                .verticalScroll(
                                    rememberScrollState()
                                )
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

                                colors = OutlinedTextFieldDefaults.colors(

                                    focusedContainerColor = Color(0xFF232136),
                                    unfocusedContainerColor = Color(0xFF232136),

                                    focusedBorderColor = AppColors.Accent,
                                    unfocusedBorderColor = Color(0x33FFFFFF),

                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,

                                    focusedLabelColor = AppColors.Accent,
                                    unfocusedLabelColor = Color.LightGray
                                ),

                                label = {
                                    Text("Name")
                                },

                                modifier = Modifier.fillMaxWidth()
                            )



                            Spacer(
                                modifier = Modifier.height(20.dp)
                            )


                            item.infoFields.forEachIndexed { index, field ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {

                                        OutlinedTextField(

                                            value = field.title,

                                            onValueChange = {

                                                item.infoFields[index] =
                                                    field.copy(title = it)
                                            },

                                            colors = OutlinedTextFieldDefaults.colors(

                                                focusedContainerColor = Color(0xFF232136),
                                                unfocusedContainerColor = Color(0xFF232136),

                                                focusedBorderColor = AppColors.Accent,
                                                unfocusedBorderColor = Color(0x33FFFFFF),

                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,

                                                focusedLabelColor = AppColors.Accent,
                                                unfocusedLabelColor = Color.LightGray
                                            ),

                                            label = {
                                                Text("Von")
                                            },

                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(
                                            modifier = Modifier.height(8.dp)
                                        )

                                        OutlinedTextField(

                                            value = field.value,

                                            onValueChange = {

                                                item.infoFields[index] =
                                                    field.copy(value = it)
                                            },

                                            colors = OutlinedTextFieldDefaults.colors(

                                                focusedContainerColor = Color(0xFF232136),
                                                unfocusedContainerColor = Color(0xFF232136),

                                                focusedBorderColor = AppColors.Accent,
                                                unfocusedBorderColor = Color(0x33FFFFFF),

                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,

                                                focusedLabelColor = AppColors.Accent,
                                                unfocusedLabelColor = Color.LightGray
                                            ),

                                            label = {
                                                Text("Bei")
                                            },

                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    IconButton(

                                        onClick = {

                                            item.infoFields.removeAt(index)
                                        }
                                    ) {

                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            tint = Color.Red,
                                            contentDescription = "Info löschen"

                                        )
                                    }
                                }

                                Spacer(
                                    modifier = Modifier.height(12.dp)
                                )
                            }



                            OutlinedTextField(

                                value = newTitle,

                                onValueChange = {
                                    newTitle = it
                                },

                                colors = OutlinedTextFieldDefaults.colors(

                                    focusedContainerColor = Color(0xFF232136),
                                    unfocusedContainerColor = Color(0xFF232136),

                                    focusedBorderColor = AppColors.Accent,
                                    unfocusedBorderColor = Color(0x33FFFFFF),

                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,

                                    focusedLabelColor = AppColors.Accent,
                                    unfocusedLabelColor = Color.LightGray
                                ),

                                label = {
                                    Text("Von")
                                },

                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            OutlinedTextField(

                                value = newValue,

                                onValueChange = {
                                    newValue = it
                                },

                                colors = OutlinedTextFieldDefaults.colors(

                                    focusedContainerColor = Color(0xFF232136),
                                    unfocusedContainerColor = Color(0xFF232136),

                                    focusedBorderColor = AppColors.Accent,
                                    unfocusedBorderColor = Color(0x33FFFFFF),

                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,

                                    focusedLabelColor = AppColors.Accent,
                                    unfocusedLabelColor = Color.LightGray
                                ),

                                label = {
                                    Text("Bei")
                                },

                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            Button(

                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.Accent,
                                    contentColor = Color.Black
                                ),

                                shape = AppShapes.Medium,

                                onClick = {

                                    if (
                                        newTitle.isNotEmpty() &&
                                        newValue.isNotEmpty()
                                    ) {

                                        item.infoFields.add(

                                            InfoField(
                                                title = newTitle,
                                                value = newValue
                                            )
                                        )

                                        newTitle = ""
                                        newValue = ""
                                    }
                                },

                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Text("+ Info hinzufügen")
                            }



                            Spacer(
                                modifier = Modifier.height(20.dp)
                            )



                            item.components.forEachIndexed { index, component ->

                                Row(

                                    modifier = Modifier.fillMaxWidth(),

                                    horizontalArrangement =
                                        Arrangement.SpaceBetween
                                ) {

                                    Row(

                                        verticalAlignment =
                                            androidx.compose.ui.Alignment.CenterVertically
                                    ) {

                                        Checkbox(

                                            checked = component.checked,

                                            onCheckedChange = { isChecked ->

                                                component.checked = isChecked
                                                saveLocalProgress()

                                            },

                                            colors = CheckboxDefaults.colors(
                                                checkedColor = Color(0xFFC8A8FF)
                                            )
                                        )

                                        Text(

                                            text = component.name,

                                            color = Color.White,

                                            modifier = Modifier.padding(start = 6.dp)
                                        )
                                    }

                                    IconButton(

                                        onClick = {

                                            item.components.removeAt(index)

                                            saveLocalProgress()
                                        }
                                    ) {

                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Komponente löschen"
                                        )
                                    }
                                }
                            }

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )



                            OutlinedTextField(

                                value = newComponent,

                                onValueChange = {
                                    newComponent = it
                                },

                                colors = OutlinedTextFieldDefaults.colors(

                                    focusedContainerColor = Color(0xFF232136),
                                    unfocusedContainerColor = Color(0xFF232136),

                                    focusedBorderColor = AppColors.Accent,
                                    unfocusedBorderColor = Color(0x33FFFFFF),

                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,

                                    focusedLabelColor = AppColors.Accent,
                                    unfocusedLabelColor = Color.LightGray
                                ),

                                label = {
                                    Text("Komponente")
                                },

                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            Button(

                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.Accent,
                                    contentColor = Color.Black
                                ),

                                shape = AppShapes.Medium,

                                onClick = {

                                    if (
                                        item.components.none {
                                            it.name == newComponent
                                        }
                                    ) {

                                        item.components.add(
                                            ComponentItem(newComponent)
                                        )

                                        newComponent = ""
                                    }
                                },

                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Text("+ Komponente hinzufügen")
                            }



                            Spacer(
                                modifier = Modifier.height(24.dp)
                            )



                            Row(

                                horizontalArrangement =
                                    Arrangement.spacedBy(12.dp),

                                modifier = Modifier.fillMaxWidth()
                            ) {

                                OutlinedButton(

                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.Red
                                    ),

                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = Brush.horizontalGradient(
                                            listOf(
                                                Color.Red.copy(alpha = 0.7f),
                                                Color.Red.copy(alpha = 0.4f)
                                            )
                                        )
                                    ),

                                    shape = AppShapes.Medium,

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

                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppColors.Accent,
                                        contentColor = Color.Black
                                    ),

                                    shape = AppShapes.Medium,

                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 6.dp
                                    ),

                                    onClick = {

                                        item.isNew = true

                                        saveItems()


                                        showEditDialog = false
                                    },

                                    modifier = Modifier.weight(1f)
                                ) {

                                    Text("Speichern")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


