package com.absapp.control.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.absapp.control.data.ObjectEntity
import com.absapp.control.location.GpsState
import com.absapp.control.ui.theme.SafetyGreen
import com.absapp.control.util.calcTargetDistanceM
import com.absapp.control.util.fmt
import com.absapp.control.util.objectNorm
import com.absapp.control.util.openInYandexMaps
import com.absapp.control.util.parseDecimal
import com.absapp.control.viewmodel.AppViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    vm: AppViewModel,
    locationPermissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    onGoToObjects: () -> Unit
) {
    val activeObject by vm.activeObject.collectAsState()
    val massText by vm.massText.collectAsState()
    val gpsState by vm.gpsState.collectAsState()
    val context = LocalContext.current

    var showNormCalc by remember { mutableStateOf(false) }
    var savedTick by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    val target = remember(activeObject, massText) {
        calcTargetDistanceM(activeObject, parseDecimal(massText))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("АБС Контроль", fontWeight = FontWeight.Bold)
                        Text(
                            activeObject?.name ?: "Объект не выбран",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showNormCalc = true }) {
                        Icon(Icons.Filled.Build, contentDescription = "Расчёт нормы по факту")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val obj = activeObject
            if (obj == null) {
                NoActiveObjectCard(onGoToObjects)
            } else {
                ObjectInfoCard(obj)

                MassInputSection(
                    massText = massText,
                    onMassChange = vm::setMassText,
                    onQuick = { vm.setMassQuick(it) }
                )

                ResultCard(target)

                GpsSection(
                    gpsState = gpsState,
                    targetDistance = target,
                    permissionGranted = locationPermissionGranted,
                    onStart = { if (locationPermissionGranted) vm.startGps() else onRequestPermission() },
                    onStop = { vm.stopGps() },
                    onReset = { vm.resetGps() },
                    onOpenYandex = { openInYandexMaps(context, gpsState.currentLocation) },
                    onCalcActualNorm = { showNormCalc = true }
                )

                Button(
                    onClick = { vm.saveLogEntry { savedTick++ } },
                    enabled = target != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Записать в журнал", fontSize = 18.sp)
                }
            }
        }
    }

    LaunchedEffect(savedTick) {
        if (savedTick > 0) {
            snackbarHostState.showSnackbar("Запись сохранена")
        }
    }

    if (showNormCalc) {
        NormCalcDialog(
            defaultWidth = activeObject?.widthM,
            gpsDistance = gpsState.distanceM.takeIf { it > 0.0 },
            onDismiss = { showNormCalc = false }
        )
    }
}

@Composable
private fun NoActiveObjectCard(onGoToObjects: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(48.dp))
            Text("Объект не выбран", style = MaterialTheme.typography.titleMedium)
            Text(
                "Создайте объект (участок), чтобы начать расчёты",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Button(onClick = onGoToObjects) { Text("Перейти к объектам") }
        }
    }
}

@Composable
private fun ObjectInfoCard(obj: ObjectEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            InfoRow("Материал", obj.materialName)
            InfoRow("Толщина слоя", "${fmt(obj.thicknessCm)} см")
            InfoRow("Средняя ширина", "${fmt(obj.widthM)} м")
            InfoRow("Норма расхода", "${fmt(objectNorm(obj))} т/1000 м²")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MassInputSection(
    massText: String,
    onMassChange: (String) -> Unit,
    onQuick: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = massText,
            onValueChange = onMassChange,
            label = { Text("Масса, т") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf(10, 20, 30).forEach { t ->
                OutlinedButton(
                    onClick = { onQuick(t) },
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                ) {
                    Text("$t т", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ResultCard(targetDistance: Double?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (targetDistance != null) SafetyGreen else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ПРОЙТИ",
                color = if (targetDistance != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = if (targetDistance != null) "${fmt(targetDistance)} м" else "—",
                color = if (targetDistance != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 56.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun GpsSection(
    gpsState: GpsState,
    targetDistance: Double?,
    permissionGranted: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onReset: () -> Unit,
    onOpenYandex: () -> Unit,
    onCalcActualNorm: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("GPS-контроль", style = MaterialTheme.typography.titleSmall)
                IconButton(onClick = onOpenYandex, enabled = gpsState.currentLocation != null) {
                    Icon(Icons.Filled.LocationOn, contentDescription = "Открыть в Яндекс.Картах")
                }
            }

            gpsState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            when {
                gpsState.isTracking -> {
                    Text("${fmt(gpsState.distanceM)} м", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = SafetyGreen)
                    if (targetDistance != null) {
                        val remaining = targetDistance - gpsState.distanceM
                        Text(
                            text = if (remaining > 0) "Осталось ~${fmt(remaining)} м" else "Пройдено на ${fmt(-remaining)} м больше нормы",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    gpsState.accuracyM?.let {
                        Text(
                            "Точность: ±${it.roundToInt()} м",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = onStop,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) { Text("Стоп") }
                }
                gpsState.distanceM > 0.0 -> {
                    Text("Пройдено: ${fmt(gpsState.distanceM)} м", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f)) { Text("Заново") }
                        Button(onClick = onCalcActualNorm, modifier = Modifier.weight(1f)) { Text("Факт. норма") }
                    }
                }
                else -> {
                    Button(
                        onClick = onStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (permissionGranted) "Старт GPS" else "Разрешить и запустить GPS")
                    }
                }
            }
        }
    }
}
