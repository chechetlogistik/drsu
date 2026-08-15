package com.absapp.control.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.absapp.control.util.calcActualNorm
import com.absapp.control.util.fmtTrim
import com.absapp.control.util.parseDecimal

@Composable
fun NormCalcDialog(
    defaultWidth: Double?,
    gpsDistance: Double?,
    onDismiss: () -> Unit
) {
    var mass by remember { mutableStateOf("") }
    var length by remember { mutableStateOf(gpsDistance?.let { fmtTrim(it) } ?: "") }
    var width by remember { mutableStateOf(defaultWidth?.let { fmtTrim(it) } ?: "") }

    val massD = parseDecimal(mass)
    val lengthD = parseDecimal(length)
    val widthD = parseDecimal(width)
    val area = if (lengthD != null && widthD != null) lengthD * widthD else null
    val norm = calcActualNorm(massD, lengthD, widthD)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Норма по факту") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Норма = масса / площадь × 1000", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = mass, onValueChange = { mass = it },
                    label = { Text("Масса, т") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = length, onValueChange = { length = it },
                    label = { Text("Длина участка, м") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = if (gpsDistance != null) {
                        { Text("По GPS: ${fmtTrim(gpsDistance)} м") }
                    } else null
                )
                OutlinedTextField(
                    value = width, onValueChange = { width = it },
                    label = { Text("Ширина, м") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider()
                Text(
                    text = norm?.let { "Норма: ${fmtTrim(it)} т/1000 м²" } ?: "Заполните все поля",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                area?.let { Text("Площадь: ${fmtTrim(it)} м²", style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } }
    )
}
