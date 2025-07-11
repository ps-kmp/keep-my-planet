package pt.isel.keepmyplanet.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.theme.onSurfaceLight
import pt.isel.keepmyplanet.ui.theme.surfaceLight

@Composable
fun EventItemSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = surfaceLight,
                contentColor = onSurfaceLight,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Title
            SkeletonBox(
                modifier = Modifier.fillMaxWidth(0.7f).height(24.dp).clip(RoundedCornerShape(4.dp)),
            )

            // Description
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SkeletonBox(
                    modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(4.dp)),
                )
                SkeletonBox(
                    modifier =
                        Modifier.fillMaxWidth(0.8f).height(16.dp).clip(RoundedCornerShape(4.dp)),
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Bottom row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SkeletonBox(
                        modifier =
                            Modifier.width(140.dp).height(14.dp).clip(RoundedCornerShape(4.dp)),
                    )
                    SkeletonBox(
                        modifier =
                            Modifier.width(100.dp).height(14.dp).clip(RoundedCornerShape(4.dp)),
                    )
                }
                SkeletonBox(
                    modifier = Modifier.width(90.dp).height(32.dp).clip(RoundedCornerShape(50)),
                )
            }
        }
    }
}
