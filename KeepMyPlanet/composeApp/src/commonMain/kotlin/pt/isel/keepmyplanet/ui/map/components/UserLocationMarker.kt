package pt.isel.keepmyplanet.ui.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun UserLocationMarker() {
    Box(
        modifier =
            Modifier
                .size(24.dp)
                .shadow(elevation = 4.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF4285F4)),
        contentAlignment = Alignment.Center,
    ) {
        Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(Color.White))
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFF4285F4)))
    }
}
