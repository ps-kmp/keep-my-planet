import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.dto.message.MessageResponse

@Suppress("ktlint:standard:function-naming")
@Composable
fun MessageItem(
    message: MessageResponse,
    isFromCurrentUser: Boolean,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start,
    ) {
        Card(
            backgroundColor =
                if (isFromCurrentUser) {
                    MaterialTheme.colors.primary
                } else {
                    MaterialTheme.colors.surface
                },
            contentColor =
                if (isFromCurrentUser) {
                    MaterialTheme.colors.onPrimary
                } else {
                    MaterialTheme.colors.onSurface
                },
            elevation = 2.dp,
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
            ) {
                Text(
                    text = "User ${message.senderId}",
                    style = MaterialTheme.typography.caption,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(text = message.content)

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.caption,
                )
            }
        }
    }
}
