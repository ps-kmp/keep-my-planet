package pt.isel.keepmyplanet.ui.event.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun EmptyState(onActionClick: () -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ListAlt,
            contentDescription = "No events",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No events found.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h6,
        )
        Text(
            text = "Try adjusting your search or create a new event.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body1,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onActionClick) {
            Text("Create an Event")
        }
    }
}
