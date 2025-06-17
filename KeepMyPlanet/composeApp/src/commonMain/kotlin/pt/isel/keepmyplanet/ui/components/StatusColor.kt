package pt.isel.keepmyplanet.ui.components

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.zone.ZoneStatus

@Composable
fun getStatusColor(status: ZoneStatus): Color =
    when (status) {
        ZoneStatus.REPORTED -> Color(0xFFFFA000)
        ZoneStatus.CLEANING_SCHEDULED -> MaterialTheme.colors.primary
        ZoneStatus.CLEANED -> Color(0xFF388E3C)
    }

@Composable
fun getStatusColor(status: EventStatus): Color =
    when (status) {
        EventStatus.PLANNED -> MaterialTheme.colors.primary
        EventStatus.IN_PROGRESS -> Color(0xFFFFA000)
        EventStatus.COMPLETED -> Color(0xFF388E3C)
        EventStatus.CANCELLED -> MaterialTheme.colors.error
        EventStatus.UNKNOWN -> Color.Gray
    }
