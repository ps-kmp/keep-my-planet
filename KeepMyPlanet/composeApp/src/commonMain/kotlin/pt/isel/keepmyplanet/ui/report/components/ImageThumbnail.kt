package pt.isel.keepmyplanet.ui.report.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import pt.isel.keepmyplanet.ui.report.states.SelectedImage

@Composable
fun ImageThumbnail(
    image: SelectedImage,
    onRemove: () -> Unit,
    enabled: Boolean,
) {
    Card(
        modifier = Modifier.size(100.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp,
    ) {
        Box {
            val painter = rememberAsyncImagePainter(model = image.data)
            Image(
                painter = painter,
                contentDescription = "Selected photo ${image.filename}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            IconButton(
                onClick = onRemove,
                enabled = enabled,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .size(24.dp),
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove photo",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
