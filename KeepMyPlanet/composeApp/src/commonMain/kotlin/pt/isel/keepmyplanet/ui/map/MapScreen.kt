package pt.isel.keepmyplanet.ui.map

import androidx.compose.runtime.Composable

@Composable
@Suppress("ktlint:standard:function-naming")
expect fun MapScreen(
    viewModel: MapViewModel,
    onNavigateToZoneDetails: (zoneId: UInt) -> Unit,
    onNavigateBack: () -> Unit,
)
