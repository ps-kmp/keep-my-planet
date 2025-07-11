package pt.isel.keepmyplanet.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun EventDetailsSkeleton() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Description Card Skeleton
        Column(modifier = Modifier.shimmerBackground().padding(16.dp)) {
            SkeletonBox(
                modifier = Modifier.fillMaxWidth(0.5f).height(20.dp).clip(RoundedCornerShape(4.dp)),
            )
            Spacer(modifier = Modifier.height(16.dp))
            SkeletonBox(
                modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(4.dp)),
            )
            SkeletonBox(
                modifier = Modifier.fillMaxWidth(0.8f).height(16.dp).clip(RoundedCornerShape(4.dp)),
            )
        }

        // Information Card Skeleton
        Column(modifier = Modifier.shimmerBackground().padding(16.dp)) {
            SkeletonBox(
                modifier = Modifier.fillMaxWidth(0.4f).height(20.dp).clip(RoundedCornerShape(4.dp)),
            )
            Spacer(modifier = Modifier.height(16.dp))
            repeat(3) {
                Row(modifier = Modifier.padding(vertical = 6.dp)) {
                    SkeletonBox(
                        modifier =
                            Modifier
                                .width(
                                    24.dp,
                                ).height(24.dp)
                                .clip(RoundedCornerShape(4.dp)),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    SkeletonBox(
                        modifier =
                            Modifier
                                .fillMaxWidth(
                                    0.7f,
                                ).height(20.dp)
                                .clip(RoundedCornerShape(4.dp)),
                    )
                }
            }
        }

        // Actions Card Skeleton
        Column(modifier = Modifier.shimmerBackground().padding(16.dp)) {
            SkeletonBox(
                modifier = Modifier.fillMaxWidth(0.6f).height(20.dp).clip(RoundedCornerShape(4.dp)),
            )
            Spacer(modifier = Modifier.height(16.dp))
            SkeletonBox(
                modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(8.dp)),
            )
        }
    }
}
