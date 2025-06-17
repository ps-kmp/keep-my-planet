package pt.isel.keepmyplanet.ui.event.list.components

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity

@Composable
fun getSeverityColor(severity: ZoneSeverity): Color =
    when (severity) {
        ZoneSeverity.LOW -> Color(0xFF34A853)
        ZoneSeverity.MEDIUM -> Color(0xFFFBBC05)
        ZoneSeverity.HIGH -> MaterialTheme.colors.error
        ZoneSeverity.UNKNOWN -> Color.Gray
    }
