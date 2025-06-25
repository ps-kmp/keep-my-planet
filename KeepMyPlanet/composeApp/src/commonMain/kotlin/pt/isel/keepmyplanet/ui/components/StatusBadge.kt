package pt.isel.keepmyplanet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.domain.event.EventStatus
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.domain.zone.ZoneStatus

@Composable
fun StatusBadge(
    text: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(50))
                .background(backgroundColor)
                .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.caption,
            )
        }
    }
}

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

@Composable
fun getSeverityColor(severity: ZoneSeverity): Color =
    when (severity) {
        ZoneSeverity.LOW -> Color(0xFF34A853)
        ZoneSeverity.MEDIUM -> Color(0xFFFBBC05)
        ZoneSeverity.HIGH -> MaterialTheme.colors.error
        ZoneSeverity.UNKNOWN -> Color.Gray
    }
