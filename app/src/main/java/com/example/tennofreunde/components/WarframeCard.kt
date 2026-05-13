package com.example.tennofreunde.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tennofreunde.models.*

@Composable
fun WarframeCard(
    item: WarframeItem,
    saveItems: () -> Unit,
    onDelete: () -> Unit
) {


    var expanded by remember {
        mutableStateOf(item.isNew)

    }

    var editMode by remember {
        mutableStateOf(item.isNew)
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {

        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                if (editMode) {

                    OutlinedTextField(
                        value = itemName,

                        onValueChange = {

                            itemName = it

                            item.name = it

                            saveItems()
                        },

                        label = {
                            Text("Name")
                        }
                    )

                } else {

                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                IconButton(
                    onClick = {
                        expanded = !expanded
                    }
                ) {

                    Icon(
                        imageVector =
                            if (expanded)
                                Icons.Default.KeyboardArrowDown
                            else
                                Icons.Default.KeyboardArrowRight,

                        contentDescription = null
                    )
                }
            }

            if (expanded) {

                Spacer(modifier = Modifier.height(8.dp))

                for (index in item.infoFields.indices.toList()) {

                    val field = item.infoFields[index]

                    Row(
                        modifier = Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            if (editMode) {

                                OutlinedTextField(
                                    value = field.title,

                                    onValueChange = {

                                        item.infoFields[index] =
                                            field.copy(title = it)

                                        saveItems()
                                    },

                                    label = {
                                        Text("von")
                                    },

                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedTextField(
                                    value = field.value,

                                    onValueChange = {

                                        item.infoFields[index] =
                                            field.copy(value = it)

                                        saveItems()
                                    },

                                    label = {
                                        Text("bei")
                                    },

                                    modifier = Modifier.fillMaxWidth()
                                )

                            } else {

                                Text("${field.title}: ${field.value}")
                            }
                        }


                        IconButton(

                            onClick = {

                                item.infoFields.removeAt(index)

                                saveItems()
                            }
                        ) {

                            Text("X")
                        }

                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (editMode) {

                    OutlinedTextField(
                        value = newTitle,

                        onValueChange = {
                            newTitle = it
                        },

                        label = {
                            Text("von")
                        },

                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newValue,

                        onValueChange = {
                            newValue = it
                        },

                        label = {
                            Text("bei")
                        },

                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
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

                                saveItems()
                            }
                        }
                    ) {

                        Text("+ Info hinzufügen")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = newComponent,

                        onValueChange = {
                            newComponent = it
                        },

                        label = {
                            Text("Komponente")
                        },

                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {

                            if (newComponent.isNotEmpty()) {

                                item.components.add(
                                    ComponentItem(newComponent)
                                )

                                newComponent = ""

                                saveItems()
                            }
                        }
                    ) {

                        Text("+ Komponente hinzufügen")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {

                        editMode = !editMode

                        item.isNew = false
                    }
                ) {

                    Text(
                        if (editMode)
                            "Speichern"
                        else
                            "Bearbeiten"
                    )
                }

                Button(
                    onClick = {

                        onDelete()

                        saveItems()
                    }
                ) {

                    Text("Löschen")
                }

                Spacer(modifier = Modifier.height(12.dp))


                for (index in item.components.indices) {

                    val component = item.components[index]

                    for (index in item.components.indices) {

                        val component = item.components[index]

                        Row {

                            var checked by remember {
                                mutableStateOf(component.checked)
                            }

                            Checkbox(

                                checked = checked,

                                onCheckedChange = { isChecked ->

                                    checked = isChecked

                                    item.components[index].checked = isChecked

                                    saveItems()
                                }
                            )

                            Text(component.name)
                        }
                    }

                }
            }
        }
    }
}



