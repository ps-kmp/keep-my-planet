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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import pt.isel.keepmyplanet.utils.toFormattedString

@Composable
actual fun DateTimePicker(
    value: LocalDateTime?,
    onValueChange: (LocalDateTime) -> Unit,
    label: String,
    modifier: Modifier,
    errorText: String?,
    enabled: Boolean,
    isOptional: Boolean,
    onClear: (() -> Unit)?,
) {
    var showDialog by remember { mutableStateOf(false) }

    val displayValue = remember(value) { value?.toFormattedString() ?: "" }

    Column(modifier = modifier) {
        Box {
            OutlinedTextField(
                value = displayValue,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text(label) },
                trailingIcon = {
                    Row {
                        if (isOptional && onClear != null && value != null) {
                            IconButton(onClick = onClear, enabled = enabled) {
                                Icon(Icons.Default.Clear, "Clear")
                            }
                        }
                        Icon(Icons.Default.DateRange, "Select Date")
                    }
                },
                readOnly = true,
                enabled = false,
                isError = errorText != null,
                colors =
                    OutlinedTextFieldDefaults.colors(
                        disabledTextColor =
                            MaterialTheme.colorScheme.onSurface.copy(
                                alpha = if (value != null) 1f else 0.6f,
                            ),
                        disabledBorderColor =
                            if (errorText != null) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
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
        val now = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) }

        var year by remember { mutableStateOf(value?.year?.toString() ?: now.year.toString()) }
        var month by remember {
            mutableStateOf(value?.monthNumber?.toString() ?: now.monthNumber.toString())
        }
        var day by remember {
            mutableStateOf(value?.dayOfMonth?.toString() ?: now.dayOfMonth.toString())
        }
        var hour by remember { mutableStateOf(value?.hour?.toString() ?: now.hour.toString()) }
        var minute by remember {
            mutableStateOf(
                value?.minute?.toString() ?: now.minute.toString(),
            )
        }

        val isFormValid by remember(year, month, day, hour, minute) {
            derivedStateOf {
                runCatching {
                    LocalDateTime(
                        year.toInt(),
                        month.toInt(),
                        day.toInt(),
                        hour.toInt(),
                        minute.toInt(),
                    )
                }.isSuccess
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
                        onValueChange(dateTime)
                        showDialog = false
                    },
                    enabled = isFormValid,
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            },
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
