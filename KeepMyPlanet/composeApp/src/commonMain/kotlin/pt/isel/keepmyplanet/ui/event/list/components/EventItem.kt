package pt.isel.keepmyplanet.ui.event.list.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.domain.event.EventListItem
import pt.isel.keepmyplanet.ui.components.StatusBadge
import pt.isel.keepmyplanet.ui.components.getStatusColor
import pt.isel.keepmyplanet.ui.theme.onSurfaceLight
import pt.isel.keepmyplanet.ui.theme.surfaceLight
import pt.isel.keepmyplanet.utils.toFormattedString

@Composable
fun EventItem(
    event: EventListItem,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceLight,
            contentColor = onSurfaceLight,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = event.title.value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = event.description.value,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Starts: ${event.period.start.toFormattedString()}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Participants",
                            modifier = Modifier.padding(end = 4.dp),
                        )
                        Text(
                            text =
                                "${event.participantCount}" +
                                    (event.maxParticipants?.let { "/$it" } ?: ""),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(
                    text = event.status.name,
                    backgroundColor = getStatusColor(event.status),
                    modifier = Modifier.align(Alignment.Bottom),
                )
            }
        }
    }
}
