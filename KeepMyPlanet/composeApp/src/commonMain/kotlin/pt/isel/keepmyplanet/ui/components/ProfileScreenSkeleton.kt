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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreenSkeleton() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Profile Header Skeleton
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SkeletonBox(modifier = Modifier.size(120.dp).clip(CircleShape))
            SkeletonBox(
                modifier = Modifier.width(200.dp).height(28.dp).clip(RoundedCornerShape(4.dp)),
            )
        }

        // Detail Card Skeletons
        repeat(2) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Title skeleton
                    SkeletonBox(
                        modifier =
                            Modifier
                                .width(200.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp)),
                    )
                    SkeletonBox(modifier = Modifier.fillMaxWidth().height(1.dp))

                    // InfoRow skeleton
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SkeletonBox(modifier = Modifier.size(24.dp).clip(CircleShape))
                            Spacer(modifier = Modifier.width(16.dp))
                            SkeletonBox(
                                modifier =
                                    Modifier
                                        .fillMaxWidth(0.7f)
                                        .height(20.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SkeletonBox(modifier = Modifier.size(24.dp).clip(CircleShape))
                            Spacer(modifier = Modifier.width(16.dp))
                            SkeletonBox(
                                modifier =
                                    Modifier
                                        .fillMaxWidth(0.8f)
                                        .height(20.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                            )
                        }
                    }
                }
            }
        }
    }
}
