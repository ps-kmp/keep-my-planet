package pt.isel.keepmyplanet.ui.chat.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.domain.message.Message
import pt.isel.keepmyplanet.ui.theme.primaryLight
import pt.isel.keepmyplanet.utils.formatTimestamp

@Composable
fun MessageItem(
    message: Message,
    currentUserId: UInt,
) {
    val isCurr = message.senderId.value == currentUserId

    val alignment = if (isCurr) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (isCurr) primaryLight else MaterialTheme.colorScheme.surface
    val textColor = if (isCurr) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val startPadding = if (isCurr) 48.dp else 8.dp
    val endPadding = if (isCurr) 8.dp else 48.dp

    Box(modifier = Modifier.fillMaxWidth().padding(start = startPadding, end = endPadding)) {
        Surface(
            modifier = Modifier.align(alignment),
            shape = MaterialTheme.shapes.medium,
            color = backgroundColor,
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (!isCurr) {
                    Text(
                        text = message.senderName.value,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                }
                Text(
                    text = message.content.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.End),
                    color = textColor.copy(alpha = 0.7f),
                )
            }
        }
    }
}
