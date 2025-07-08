package pt.isel.keepmyplanet.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.browser.document
import kotlinx.datetime.LocalDateTime
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import pt.isel.keepmyplanet.utils.toFormattedString

@Composable
actual fun DateTimePicker(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier,
    errorText: String?,
    enabled: Boolean,
) {
    val initialDateTime =
        remember(value) {
            try {
                LocalDateTime.parse(value)
            } catch (_: Exception) {
                null
            }
        }

    val displayValue = initialDateTime?.toFormattedString() ?: value

    Column(modifier = modifier) {
        Box {
            OutlinedTextField(
                value = displayValue,
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
                    Modifier.matchParentSize().clickable(enabled = enabled) {
                        val input =
                            (document.createElement("input") as HTMLInputElement).apply {
                                type = "datetime-local"
                                style.display = "none"
                                this.value = initialDateTime?.toString()?.take(16) ?: ""
                                onchange = { event: Event ->
                                    val result = (event.target as? HTMLInputElement)?.value
                                    if (result != null && result.isNotEmpty()) {
                                        onValueChange("$result:00")
                                    }
                                }
                            }
                        document.body?.appendChild(input)
                        input.click()
                        document.body?.removeChild(input)
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
}
