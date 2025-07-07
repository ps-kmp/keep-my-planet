package pt.isel.keepmyplanet.ui.event.stats.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import pt.isel.keepmyplanet.domain.event.EventStats
import pt.isel.keepmyplanet.ui.theme.onSurfaceLight
import pt.isel.keepmyplanet.ui.theme.primaryLight
import pt.isel.keepmyplanet.ui.theme.surfaceLight

@Composable
fun EventStatsSummaryCard(stats: EventStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = surfaceLight,
                contentColor = onSurfaceLight,
            ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatItem(
                    value = "${stats.totalAttendees}/${stats.totalParticipants}",
                    label = "Attendance",
                    icon = Icons.Default.EventAvailable,
                )
                HorizontalDivider(
                    Modifier.height(60.dp).width(1.dp),
                    DividerDefaults.Thickness,
                    DividerDefaults.color,
                )
                StatItem(
                    value = "${stats.checkInRate.roundToInt()}%",
                    label = "Check-in Rate",
                    icon = Icons.Default.Percent,
                )
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatItem(
                    value = stats.totalParticipants.toString(),
                    label = "Participants",
                    icon = Icons.Default.Groups,
                )
                HorizontalDivider(
                    Modifier.height(60.dp).width(1.dp),
                    DividerDefaults.Thickness,
                    DividerDefaults.color,
                )
                StatItem(
                    value = "≈${(stats.totalHoursVolunteered * 10).roundToInt() / 10.0}",
                    label = "Hours Volunteered",
                    icon = Icons.Default.Timer,
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    icon: ImageVector,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = primaryLight,
            modifier = Modifier.size(32.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
        )
    }
}
