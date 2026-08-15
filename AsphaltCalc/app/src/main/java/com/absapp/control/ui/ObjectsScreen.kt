package com.absapp.control.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.absapp.control.data.MaterialEntity
import com.absapp.control.data.ObjectEntity
import com.absapp.control.util.fmt
import com.absapp.control.util.fmtTrim
import com.absapp.control.util.objectNorm
import com.absapp.control.util.parseDecimal
import com.absapp.control.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectsScreen(vm: AppViewModel) {
    val objects by vm.objects.collectAsState()
    val materials by vm.materials.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingObject by remember { mutableStateOf<ObjectEntity?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Объекты") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить объект")
            }
        }
    ) { padding ->
        if (objects.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Нет сохранённых объектов.\nНажмите + чтобы добавить.",
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(objects, key = { it.id }) { obj ->
                    ObjectCard(
                        obj = obj,
                        onSetActive = { vm.setActiveObject(obj.id) },
                        onEdit = { editingObject = obj },
                        onDelete = { vm.deleteObject(obj) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        ObjectEditDialog(
            materials = materials,
            existing = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, material, thickness, width ->
                vm.addObject(name, material, thickness, width)
                showAddDialog = false
            }
        )
    }

    editingObject?.let { obj ->
        ObjectEditDialog(
            materials = materials,
            existing = obj,
            onDismiss = { editingObject = null },
            onSave = { name, material, thickness, width ->
                vm.updateObject(
                    obj.copy(
                        name = name,
                        materialId = material.id,
                        materialName = material.name,
                        normPerCm = material.normPerCm,
                        thicknessCm = thickness,
                        widthM = width
                    )
                )
                editingObject = null
            }
        )
    }
}

@Composable
private fun ObjectCard(
    obj: ObjectEntity,
    onSetActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (obj.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(obj.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (obj.isActive) {
                    AssistChip(onClick = {}, label = { Text("Активен") })
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(obj.materialName, style = MaterialTheme.typography.bodyMedium)
            Text(
                "Толщина: ${fmt(obj.thicknessCm)} см · Ширина: ${fmt(obj.widthM)} м · Норма: ${fmt(objectNorm(obj))} т/1000м²",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!obj.isActive) {
                    FilledTonalButton(onClick = onSetActive) { Text("Активный") }
                }
                OutlinedButton(onClick = onEdit) { Text("Изменить") }
                OutlinedButton(onClick = onDelete) { Text("Удалить") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ObjectEditDialog(
    materials: List<MaterialEntity>,
    existing: ObjectEntity?,
    onDismiss: () -> Unit,
    onSave: (String, MaterialEntity, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var selectedMaterial by remember {
        mutableStateOf(materials.find { it.id == existing?.materialId } ?: materials.firstOrNull())
    }
    var thickness by remember { mutableStateOf(existing?.thicknessCm?.let { fmtTrim(it) } ?: "6") }
    var width by remember { mutableStateOf(existing?.widthM?.let { fmtTrim(it) } ?: "") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Новый объект" else "Изменить объект") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Название объекта") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (materials.isEmpty()) {
                    Text(
                        "Нет материалов. Сначала добавьте материал в разделе «Справочник».",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = selectedMaterial?.name ?: "Выберите материал",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Материал") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            materials.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m.name) },
                                    onClick = { selectedMaterial = m; expanded = false }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = thickness, onValueChange = { thickness = it },
                    label = { Text("Толщина слоя, см") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = width, onValueChange = { width = it },
                    label = { Text("Средняя ширина, м") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = materials.isNotEmpty(),
                onClick = {
                    val t = parseDecimal(thickness)
                    val w = parseDecimal(width)
                    val mat = selectedMaterial
                    if (name.isNotBlank() && mat != null && t != null && t > 0 && w != null && w > 0) {
                        onSave(name, mat, t, w)
                    }
                }
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
