package pt.isel.keepmyplanet.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toLocalDateTime
import pt.isel.keepmyplanet.utils.toLocalFormattedString

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

    val displayValue = remember(value) { value?.toLocalFormattedString() ?: "" }

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
                    Modifier
                        .matchParentSize()
                        .clickable(enabled = enabled) { showDialog = true },
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
        DateTimePickerDialog(
            initialDateTime = value,
            onDismiss = { showDialog = false },
            onConfirm = {
                onValueChange(it)
                showDialog = false
            },
        )
    }
}

@Composable
private fun DateTimePickerDialog(
    initialDateTime: LocalDateTime?,
    onConfirm: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit,
) {
    var isPickingDate by remember { mutableStateOf(true) }
    val now = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) }

    var selectedDate by remember { mutableStateOf(initialDateTime?.date ?: now.date) }
    var selectedHour by remember { mutableStateOf(initialDateTime?.hour ?: now.hour) }
    var selectedMinute by remember { mutableStateOf(initialDateTime?.minute ?: now.minute) }

    AlertDialog(
        onDismissRequest = onDismiss,
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
                label = "DateTimePickerAnimation",
            ) { isDateView ->
                if (isDateView) {
                    CalendarView(
                        selectedDate = selectedDate,
                        onDateSelected = { selectedDate = it },
                    )
                } else {
                    TimePickerView(
                        hour = selectedHour,
                        minute = selectedMinute,
                        onHourChange = { selectedHour = it },
                        onMinuteChange = { selectedMinute = it },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isPickingDate) {
                        isPickingDate = false
                    } else {
                        val localDateTime =
                            LocalDateTime(
                                year = selectedDate.year,
                                monthNumber = selectedDate.monthNumber,
                                dayOfMonth = selectedDate.dayOfMonth,
                                hour = selectedHour,
                                minute = selectedMinute,
                            )
                        val utcDateTime =
                            localDateTime
                                .toInstant(TimeZone.currentSystemDefault())
                                .toLocalDateTime(TimeZone.UTC)
                        onConfirm(utcDateTime)
                    }
                },
            ) {
                Text(if (isPickingDate) "Next" else "OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (isPickingDate) {
                        onDismiss()
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

@Composable
private fun CalendarView(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    var displayedDate by remember { mutableStateOf(selectedDate) }
    val today =
        remember {
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
        }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = {
                    displayedDate =
                        displayedDate.toJavaLocalDate().minusMonths(1).toKotlinLocalDate()
                },
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous Month")
            }
            Text(
                text = "${
                    displayedDate.month.getDisplayName(
                        TextStyle.FULL,
                        Locale.getDefault(),
                    )
                } ${displayedDate.year}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
            IconButton(
                onClick = {
                    displayedDate =
                        displayedDate.toJavaLocalDate().plusMonths(1).toKotlinLocalDate()
                },
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next Month")
            }
        }
        Spacer(Modifier.height(16.dp))

        // Day of Week Headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            val daysOfWeek = DayOfWeek.entries.toTypedArray()
            val sortedDays = daysOfWeek.sortedBy { it.value }
            sortedDays.forEach { dayOfWeek ->
                Text(
                    text = dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        // Calendar Grid
        val firstDayOfMonth = displayedDate.toJavaLocalDate().withDayOfMonth(1)
        val daysInMonth = displayedDate.toJavaLocalDate().lengthOfMonth()
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // Monday=1, Sunday=7

        Column {
            var dayCounter = 1
            repeat(6) {
                // Max 6 rows in a month view
                if (dayCounter > daysInMonth) return@repeat
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(7) { dayIndex ->
                        val dayOfWeek = dayIndex + 1
                        if ((it == 0 && dayOfWeek < firstDayOfWeek) || dayCounter > daysInMonth) {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            val currentDate =
                                displayedDate
                                    .toJavaLocalDate()
                                    .withDayOfMonth(
                                        dayCounter,
                                    ).toKotlinLocalDate()
                            DayCell(
                                day = dayCounter,
                                isSelected = currentDate == selectedDate,
                                isToday = currentDate == today,
                                onClick = { onDateSelected(currentDate) },
                                modifier = Modifier.weight(1f),
                            )
                            dayCounter++
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor =
        when {
            isSelected -> MaterialTheme.colorScheme.primary
            else -> Color.Transparent
        }
    val contentColor =
        when {
            isSelected -> MaterialTheme.colorScheme.onPrimary
            isToday -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurface
        }
    val borderModifier =
        if (isToday && !isSelected) {
            Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
        } else {
            Modifier
        }

    Box(
        modifier =
            modifier
                .aspectRatio(1f)
                .padding(2.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .then(borderModifier)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = day.toString(),
            color = contentColor,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun TimePickerView(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TimeSegment(
            value = hour,
            onValueChange = onHourChange,
            range = 0..23,
        )
        Text(
            ":",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        TimeSegment(
            value = minute,
            onValueChange = onMinuteChange,
            range = 0..59,
        )
    }
}

@Composable
private fun TimeSegment(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = {
                onValueChange(
                    (value + 1).let {
                        if (it >
                            range.last
                        ) {
                            range.first
                        } else {
                            it
                        }
                    },
                )
            },
        ) {
            Icon(Icons.Default.KeyboardArrowUp, "Increase")
        }
        Text(
            text = value.toString().padStart(2, '0'),
            style = MaterialTheme.typography.headlineMedium,
        )
        IconButton(
            onClick = {
                onValueChange((value - 1).let { if (it < range.first) range.last else it })
            },
        ) {
            Icon(Icons.Default.KeyboardArrowDown, "Decrease")
        }
    }
}
