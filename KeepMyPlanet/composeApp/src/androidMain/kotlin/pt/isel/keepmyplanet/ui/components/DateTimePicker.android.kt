package pt.isel.keepmyplanet.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.format.DateTimeFormatter
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun DateTimePicker(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier,
    errorText: String?,
    enabled: Boolean,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val initialDateTime =
        remember(value) {
            try {
                LocalDateTime.parse(value)
            } catch (_: Exception) {
                null
            }
        }

    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis =
                initialDateTime?.toInstant(TimeZone.UTC)?.toEpochMilliseconds(),
        )
    val timePickerState =
        rememberTimePickerState(
            initialHour = initialDateTime?.hour ?: 0,
            initialMinute = initialDateTime?.minute ?: 0,
            is24Hour = true,
        )

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
                    Modifier
                        .matchParentSize()
                        .clickable(enabled = enabled) {
                            showDatePicker =
                                true
                        },
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

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        showTimePicker = true
                    },
                    enabled = datePickerState.selectedDateMillis != null,
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val selectedDate =
            remember(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let {
                    Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC)
                }
            }

        if (selectedDate != null) {
            TimePickerDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val newDateTime =
                                LocalDateTime(
                                    year = selectedDate.year,
                                    monthNumber = selectedDate.monthNumber,
                                    dayOfMonth = selectedDate.dayOfMonth,
                                    hour = timePickerState.hour,
                                    minute = timePickerState.minute,
                                    second = 0,
                                    nanosecond = 0,
                                )
                            onValueChange(newDateTime.toString())
                            showTimePicker = false
                        },
                    ) { Text("OK") }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showTimePicker = false },
                    ) { Text("Cancel") }
                },
            ) { TimePicker(state = timePickerState) }
        }
    }
}

@Composable
private fun TimePickerDialog(
    title: String = "Select Time",
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(shape = MaterialTheme.shapes.extraLarge) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = title, style = MaterialTheme.typography.labelMedium)
                content()
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                        dismissButton()
                        confirmButton()
                    }
                }
            }
        }
    }
}
