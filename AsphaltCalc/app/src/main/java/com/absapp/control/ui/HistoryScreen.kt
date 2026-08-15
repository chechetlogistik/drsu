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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import com.absapp.control.data.LogEntryEntity
import com.absapp.control.util.fmt
import com.absapp.control.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class Period(val label: String) {
    TODAY("Сегодня"), WEEK("Неделя"), MONTH("Месяц"), ALL("Всё время")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(vm: AppViewModel) {
    val allEntries by vm.logEntries.collectAsState()
    var period by remember { mutableStateOf(Period.ALL) }

    val fromMillis = remember(period) {
        val cal = Calendar.getInstance()
        when (period) {
            Period.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            Period.WEEK -> { cal.add(Calendar.DAY_OF_YEAR, -7); cal.timeInMillis }
            Period.MONTH -> { cal.add(Calendar.MONTH, -1); cal.timeInMillis }
            Period.ALL -> 0L
        }
    }

    val entries = remember(allEntries, fromMillis) {
        allEntries.filter { it.timestamp >= fromMillis }
    }
    val totalMass = entries.sumOf { it.massT }
    val totalDistance = entries.sumOf { it.targetDistanceM }

    Scaffold(topBar = { TopAppBar(title = { Text("Журнал") }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            ScrollableTabRow(selectedTabIndex = period.ordinal, edgePadding = 16.dp) {
                Period.entries.forEach { p ->
                    Tab(
                        selected = period == p,
                        onClick = { period = p },
                        text = { Text(p.label) }
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    SummaryStat("Записей", entries.size.toString())
                    SummaryStat("Тонн", fmt(totalMass))
                    SummaryStat("Метров", fmt(totalDistance))
                }
            }

            if (entries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Нет записей за этот период")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        LogEntryCard(entry, onDelete = { vm.deleteLogEntry(entry) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private val timeFormat = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())

@Composable
private fun LogEntryCard(entry: LogEntryEntity, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(timeFormat.format(Date(entry.timestamp)), style = MaterialTheme.typography.bodySmall)
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Удалить", modifier = Modifier.size(18.dp))
                }
            }
            Text(entry.objectName, fontWeight = FontWeight.Bold)
            Text(entry.materialName, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("${fmt(entry.massT)} т")
                Text("${fmt(entry.targetDistanceM)} м (расчёт)")
                entry.gpsDistanceM?.let { Text("${fmt(it)} м (GPS)") }
            }
        }
    }
}
