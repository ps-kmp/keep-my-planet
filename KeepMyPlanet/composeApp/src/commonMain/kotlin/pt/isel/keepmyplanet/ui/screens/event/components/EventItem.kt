package pt.isel.keepmyplanet.ui.screens.event.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.data.model.EventInfo

@Suppress("ktlint:standard:function-naming")
@Composable
fun EventItem(
    event: EventInfo,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(8.dp),
        elevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = event.title.value,
                style = MaterialTheme.typography.h6,
            )
            Text(
                text = event.description.value,
                style = MaterialTheme.typography.body2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Start: ${event.period.start.toFormattedString()}",
                    style = MaterialTheme.typography.caption,
                )
                Text(
                    text = event.status.name,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.secondary,
                )
            }
        }
    }
}
