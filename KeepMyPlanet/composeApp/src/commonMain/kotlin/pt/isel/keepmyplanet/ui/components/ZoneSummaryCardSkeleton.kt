package pt.isel.keepmyplanet.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.theme.onSurfaceLight
import pt.isel.keepmyplanet.ui.theme.surfaceLight

@Composable
fun ZoneSummaryCardSkeleton() {
    Card(
        modifier = Modifier.width(220.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = surfaceLight,
                contentColor = onSurfaceLight,
            ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SkeletonBox(
                modifier = Modifier.width(180.dp).height(20.dp).clip(RoundedCornerShape(4.dp)),
            )
            SkeletonBox(
                modifier = Modifier.width(120.dp).height(16.dp).clip(RoundedCornerShape(4.dp)),
            )
            SkeletonBox(
                modifier = Modifier.width(90.dp).height(28.dp).clip(RoundedCornerShape(50)),
            )
        }
    }
}
