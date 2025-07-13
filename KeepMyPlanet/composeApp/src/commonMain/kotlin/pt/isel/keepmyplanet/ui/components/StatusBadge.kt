package pt.isel.keepmyplanet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import pt.isel.keepmyplanet.ui.theme.customColors

@Composable
fun StatusBadge(
    text: String,
    backgroundColor: Color,
    contentColor: Color,
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
                    tint = contentColor,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun getStatusColorPair(status: ZoneStatus): Pair<Color, Color> =
    when (status) {
        ZoneStatus.REPORTED ->
            customColors.warningContainer to
                customColors.onWarningContainer
        ZoneStatus.CLEANING_SCHEDULED ->
            MaterialTheme.colorScheme.primaryContainer to
                MaterialTheme.colorScheme.onPrimaryContainer
        ZoneStatus.CLEANED ->
            customColors.successContainer to
                customColors.onSuccessContainer
    }

@Composable
fun getStatusColorPair(status: EventStatus): Pair<Color, Color> =
    when (status) {
        EventStatus.PLANNED ->
            MaterialTheme.colorScheme.tertiaryContainer to
                MaterialTheme.colorScheme.onTertiaryContainer
        EventStatus.IN_PROGRESS ->
            customColors.warningContainer to
                customColors.onWarningContainer
        EventStatus.COMPLETED ->
            customColors.successContainer to
                customColors.onSuccessContainer
        EventStatus.CANCELLED ->
            MaterialTheme.colorScheme.errorContainer to
                MaterialTheme.colorScheme.onErrorContainer
        EventStatus.UNKNOWN ->
            MaterialTheme.colorScheme.surfaceVariant to
                MaterialTheme.colorScheme.onSurfaceVariant
    }

@Composable
fun getSeverityColorPair(severity: ZoneSeverity): Pair<Color, Color> =
    when (severity) {
        ZoneSeverity.LOW ->
            customColors.successContainer to
                customColors.onSuccessContainer
        ZoneSeverity.MEDIUM ->
            customColors.warningContainer to
                customColors.onWarningContainer
        ZoneSeverity.HIGH ->
            MaterialTheme.colorScheme.errorContainer to
                MaterialTheme.colorScheme.onErrorContainer
        ZoneSeverity.UNKNOWN ->
            MaterialTheme.colorScheme.surfaceVariant to
                MaterialTheme.colorScheme.onSurfaceVariant
    }
