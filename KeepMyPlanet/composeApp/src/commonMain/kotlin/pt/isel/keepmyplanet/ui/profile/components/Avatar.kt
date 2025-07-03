package pt.isel.keepmyplanet.ui.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import pt.isel.keepmyplanet.domain.user.UserInfo

@Composable
fun Avatar(
    user: UserInfo?,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    photoUrl: String? = null,
    isUpdating: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val initial =
        user
            ?.name
            ?.value
            ?.firstOrNull()
            ?.uppercaseChar()
    val baseColor =
        user
            ?.id
            ?.value
            ?.rem(360u)
            ?.toFloat() ?: 0f
    val backgroundColor = Color.hsv(hue = baseColor, saturation = 0.25f, value = 0.85f)
    val foregroundColor = Color.hsv(hue = baseColor, saturation = 1.0f, value = 0.6f)

    val clickableModifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier

    Box(
        modifier =
            clickableModifier
                .size(size)
                .clip(CircleShape)
                .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isUpdating -> {
                CircularProgressIndicator(modifier = Modifier.size(size / 2))
            }
            photoUrl != null -> {
                SubcomposeAsyncImage(
                    model = photoUrl,
                    contentDescription = "${user?.name?.value} profile picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(size / 3))
                        }
                    },
                    error = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.BrokenImage,
                                contentDescription = "Failed to load image",
                                modifier = Modifier.size(size / 2),
                            )
                        }
                    },
                )
            }
            initial != null -> {
                Text(
                    text = initial.toString(),
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = (size.value / 2).sp),
                    color = foregroundColor,
                )
            }
        }
    }
}
