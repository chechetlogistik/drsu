package com.absapp.control.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.absapp.control.data.MaterialEntity
import com.absapp.control.util.fmt
import com.absapp.control.util.parseDecimal
import com.absapp.control.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialsScreen(vm: AppViewModel) {
    val materials by vm.materials.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMaterial by remember { mutableStateOf<MaterialEntity?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Справочник материалов") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить материал")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 16.dp, 16.dp, 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Text(
                    "Значения перенесены из фотографии документа. Сверьте их с оригиналом перед использованием.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(materials, key = { it.id }) { m ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(m.name, fontWeight = FontWeight.Medium)
                                Text(
                                    "На 1 см: ${fmt(m.normPerCm)} т/1000м²" +
                                        (m.normPer4cm?.let { " · На 4 см: ${fmt(it)} т/1000м²" } ?: ""),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row {
                                IconButton(onClick = { editingMaterial = m }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Изменить")
                                }
                                IconButton(onClick = { vm.deleteMaterial(m) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Удалить")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        MaterialEditDialog(
            existing = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, perCm, per4cm ->
                vm.addMaterial(name, perCm, per4cm)
                showAddDialog = false
            }
        )
    }
    editingMaterial?.let { m ->
        MaterialEditDialog(
            existing = m,
            onDismiss = { editingMaterial = null },
            onSave = { name, perCm, per4cm ->
                vm.updateMaterial(m.copy(name = name, normPerCm = perCm, normPer4cm = per4cm))
                editingMaterial = null
            }
        )
    }
}

@Composable
private fun MaterialEditDialog(
    existing: MaterialEntity?,
    onDismiss: () -> Unit,
    onSave: (String, Double, Double?) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var perCm by remember { mutableStateOf(existing?.normPerCm?.let { it.toString().replace('.', ',') } ?: "") }
    var per4cm by remember { mutableStateOf(existing?.normPer4cm?.let { it.toString().replace('.', ',') } ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Новый материал" else "Изменить материал") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Название") }, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = perCm, onValueChange = { perCm = it },
                    label = { Text("Норма на 1 см толщины, т/1000м²") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = per4cm, onValueChange = { per4cm = it },
                    label = { Text("Норма на 4 см (справочно), т/1000м²") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val p = parseDecimal(perCm)
                val p4 = parseDecimal(per4cm)
                if (name.isNotBlank() && p != null && p > 0) onSave(name, p, p4)
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
