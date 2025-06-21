package pt.isel.keepmyplanet.ui.user.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.keepmyplanet.ui.user.profile.model.UserInfo

@Composable
fun Avatar(
    user: UserInfo?,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
) {
    val initial =
        user
            ?.name
            ?.value
            ?.firstOrNull()
            ?.uppercaseChar()
    val a =
        user
            ?.id
            ?.value
            ?.rem(360u)
            ?.toFloat() ?: 0f
    val backgroundColor = Color.hsv(hue = a, saturation = 0.25f, value = 0.85f)
    val foregroundColor = Color.hsv(hue = a, saturation = 1.0f, value = 0.6f)

    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        if (initial != null) {
            Text(
                text = initial.toString(),
                style = MaterialTheme.typography.h3.copy(fontSize = (size.value / 2).sp),
                color = foregroundColor,
            )
        }
    }
}
