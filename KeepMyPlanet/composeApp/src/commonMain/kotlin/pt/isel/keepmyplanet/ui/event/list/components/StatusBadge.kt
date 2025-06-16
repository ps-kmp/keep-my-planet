package pt.isel.keepmyplanet.ui.event.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.domain.event.EventStatus

@Composable
fun StatusBadge(status: EventStatus) {
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(50))
                .background(getStatusColor(status))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = status.name,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.caption,
        )
    }
}
