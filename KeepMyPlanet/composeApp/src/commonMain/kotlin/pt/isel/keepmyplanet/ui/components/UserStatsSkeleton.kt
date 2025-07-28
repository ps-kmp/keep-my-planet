package pt.isel.keepmyplanet.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun UserStatsSkeleton() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().shimmerBackground().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SkeletonBox(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(4.dp)))
                SkeletonBox(
                    modifier = Modifier.width(80.dp).height(28.dp).clip(RoundedCornerShape(4.dp)),
                )
                SkeletonBox(
                    modifier = Modifier.width(100.dp).height(16.dp).clip(RoundedCornerShape(4.dp)),
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SkeletonBox(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(4.dp)))
                SkeletonBox(
                    modifier = Modifier.width(80.dp).height(28.dp).clip(RoundedCornerShape(4.dp)),
                )
                SkeletonBox(
                    modifier = Modifier.width(100.dp).height(16.dp).clip(RoundedCornerShape(4.dp)),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        SkeletonBox(
            modifier = Modifier.fillMaxWidth(0.5f).height(24.dp).clip(RoundedCornerShape(4.dp)),
        )
        repeat(3) { EventItemSkeleton() }
    }
}
