package pt.isel.keepmyplanet.ui.components

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import pt.isel.keepmyplanet.utils.toFormattedString

@OptIn(ExperimentalMaterial3Api::class)
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
    val context = LocalContext.current

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
                enabled = false, // Visually disabled
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
                    Modifier
                        .matchParentSize()
                        .clickable(
                            enabled = enabled,
                        ) { showDialog = true },
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
        var isPickingDate by remember { mutableStateOf(true) }
        val now = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) }

        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis =
                    value
                        ?.toInstant(
                            TimeZone.currentSystemDefault(),
                        )?.toEpochMilliseconds()
                        ?: Clock.System.now().toEpochMilliseconds(),
            )
        val timePickerState =
            rememberTimePickerState(
                initialHour = value?.hour ?: now.hour,
                initialMinute = value?.minute ?: now.minute,
                is24Hour = DateFormat.is24HourFormat(context),
            )

        AlertDialog(
            onDismissRequest = { showDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            title = {
                Text(if (isPickingDate) "Select Date" else "Select Time")
            },
            text = {
                AnimatedContent(
                    targetState = isPickingDate,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                    },
                    label = "PickerAnimation",
                ) { isDateView ->
                    if (isDateView) {
                        DatePicker(state = datePickerState)
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            TimePicker(state = timePickerState)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isPickingDate) {
                            isPickingDate = false
                        } else {
                            val selectedInstant =
                                Instant.fromEpochMilliseconds(
                                    datePickerState.selectedDateMillis!!,
                                )
                            val selectedDate = selectedInstant.toLocalDateTime(TimeZone.UTC)

                            val newDateTime =
                                LocalDateTime(
                                    year = selectedDate.year,
                                    monthNumber = selectedDate.monthNumber,
                                    dayOfMonth = selectedDate.dayOfMonth,
                                    hour = timePickerState.hour,
                                    minute = timePickerState.minute,
                                )
                            onValueChange(newDateTime)
                            showDialog = false
                        }
                    },
                    enabled = datePickerState.selectedDateMillis != null,
                ) {
                    Text(if (isPickingDate) "Next" else "OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (isPickingDate) {
                            showDialog = false
                        } else {
                            isPickingDate = true
                        }
                    },
                ) {
                    Text(if (isPickingDate) "Cancel" else "Back")
                }
            },
        )
    }
}
