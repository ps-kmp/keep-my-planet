package pt.isel.keepmyplanet.ui.screens.chat.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.screens.chat.ChatMessageUi

@Suppress("ktlint:standard:function-naming")
@Composable
fun MessageItem(message: ChatMessageUi) {
    val alignment = if (message.isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor =
        if (message.isCurrentUser) MaterialTheme.colors.primary else MaterialTheme.colors.surface
    val textColor =
        if (message.isCurrentUser) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface
    val startPadding = if (message.isCurrentUser) 48.dp else 8.dp
    val endPadding = if (message.isCurrentUser) 8.dp else 48.dp

    Box(modifier = Modifier.fillMaxWidth().padding(start = startPadding, end = endPadding)) {
        Surface(
            modifier = Modifier.align(alignment),
            shape = MaterialTheme.shapes.medium,
            color = backgroundColor,
            elevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (!message.isCurrentUser) {
                    Text(
                        text = "Sender: ${message.senderId}",
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold,
                        color = textColor.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                }
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.body1,
                    color = textColor,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.align(Alignment.End),
                    color = textColor.copy(alpha = 0.7f),
                )
            }
        }
    }
}
