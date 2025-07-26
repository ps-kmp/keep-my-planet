package pt.isel.keepmyplanet.ui.event.history.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import pt.isel.keepmyplanet.dto.event.EventStateChangeResponse
import pt.isel.keepmyplanet.ui.components.InfoRow
import pt.isel.keepmyplanet.ui.components.StatusBadge
import pt.isel.keepmyplanet.ui.components.getStatusColorPair
import pt.isel.keepmyplanet.utils.toFormattedString

@Composable
fun StatusHistoryItem(item: EventStateChangeResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Status set to",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                val (bgColor, contentColor) = getStatusColorPair(item.newStatus)
                StatusBadge(
                    text = item.newStatus.name.replace('_', ' '),
                    backgroundColor = bgColor,
                    contentColor = contentColor,
                )
            }
            HorizontalDivider()
            InfoRow(
                icon = Icons.Default.Person,
                text = "By: ${item.changedBy.name}",
            )
            InfoRow(
                icon = Icons.Default.Schedule,
                text = "On: ${LocalDateTime.parse(item.changeTime).toFormattedString()}",
            )
        }
    }
}
