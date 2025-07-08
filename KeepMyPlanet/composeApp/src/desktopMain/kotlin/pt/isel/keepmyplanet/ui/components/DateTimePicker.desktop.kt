package pt.isel.keepmyplanet.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime

@Composable
actual fun DateTimePicker(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier,
    errorText: String?,
    enabled: Boolean,
) {
    var showDialog by remember { mutableStateOf(false) }

    val initialDateTime =
        remember(value) {
            try {
                LocalDateTime.parse(value)
            } catch (_: Exception) {
                null
            }
        }

    Column(modifier = modifier) {
        Box {
            OutlinedTextField(
                value =
                    initialDateTime
                        ?.toJavaLocalDateTime()
                        ?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) ?: value,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text(label) },
                trailingIcon = { Icon(Icons.Default.DateRange, "Select Date") },
                readOnly = true,
                enabled = false,
                isError = errorText != null,
                colors =
                    OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
            )
            Box(
                modifier =
                    Modifier.matchParentSize().clickable(enabled = enabled) { showDialog = true },
            )
        }
        if (errorText != null) {
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
            )
        }
    }

    if (showDialog) {
        var year by remember { mutableStateOf(initialDateTime?.year?.toString() ?: "") }
        var month by remember { mutableStateOf(initialDateTime?.monthNumber?.toString() ?: "") }
        var day by remember { mutableStateOf(initialDateTime?.dayOfMonth?.toString() ?: "") }
        var hour by remember { mutableStateOf(initialDateTime?.hour?.toString() ?: "") }
        var minute by remember { mutableStateOf(initialDateTime?.minute?.toString() ?: "") }

        val isFormValid by remember {
            derivedStateOf {
                val y = year.toIntOrNull()
                val mo = month.toIntOrNull()
                val d = day.toIntOrNull()
                val h = hour.toIntOrNull()
                val m = minute.toIntOrNull()
                y != null && mo in 1..12 && d in 1..31 && h in 0..23 && m in 0..59
            }
        }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Date and Time") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DialogTextField(day, { day = it }, "DD", Modifier.weight(1f))
                        DialogTextField(month, { month = it }, "MM", Modifier.weight(1f))
                        DialogTextField(year, { year = it }, "YYYY", Modifier.weight(1.5f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DialogTextField(hour, { hour = it }, "HH", Modifier.weight(1f))
                        DialogTextField(minute, { minute = it }, "mm", Modifier.weight(1f))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val dateTime =
                            LocalDateTime(
                                year.toInt(),
                                month.toInt(),
                                day.toInt(),
                                hour.toInt(),
                                minute.toInt(),
                            )
                        onValueChange(dateTime.toString())
                        showDialog = false
                    },
                    enabled = isFormValid,
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun DialogTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.width(80.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
    )
}
