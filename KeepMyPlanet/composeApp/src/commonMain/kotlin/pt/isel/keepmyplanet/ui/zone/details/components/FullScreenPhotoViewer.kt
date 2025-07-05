package pt.isel.keepmyplanet.ui.zone.details.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.rememberAsyncImagePainter
import kotlin.math.roundToInt

@Composable
fun FullScreenPhotoViewer(
    model: Any,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnClickOutside = true,
            ),
    ) {
        var scale by remember { mutableStateOf(1f) }
        var offsetX by remember { mutableStateOf(0f) }
        var offsetY by remember { mutableStateOf(0f) }

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable { onDismiss() }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale *= zoom
                            scale = scale.coerceIn(1f, 5f)
                            if (scale > 1f) {
                                offsetX += pan.x
                                offsetY += pan.y
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = model),
                contentDescription = "Full screen zone photo",
                contentScale = ContentScale.Fit,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                        ),
            )
            IconButton(
                onClick = onDismiss,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close photo viewer",
                    tint = Color.White,
                )
            }
        }
    }
}
