package pt.isel.keepmyplanet.ui.stats.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.keepmyplanet.domain.user.UserStats

@Composable
fun StatsSummaryCard(stats: UserStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatItem(
                value = stats.totalEventsAttended.toString(),
                label = "Events Attended",
                icon = Icons.Default.Event,
            )
            Divider(modifier = Modifier.height(60.dp).width(1.dp))
            StatItem(
                value = ((stats.totalHoursVolunteered * 10).toInt() / 10.0).toString(),
                label = "Hours Volunteered",
                icon = Icons.Default.Timer,
            )
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
            tint = MaterialTheme.colors.primary,
            modifier = Modifier.size(32.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.h4,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.caption.copy(fontSize = 14.sp),
        )
    }
}
