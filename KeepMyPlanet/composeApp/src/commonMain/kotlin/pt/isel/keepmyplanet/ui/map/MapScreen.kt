package pt.isel.keepmyplanet.ui.map

import androidx.compose.runtime.Composable
import pt.isel.keepmyplanet.domain.common.Id

@Composable
@Suppress("ktlint:standard:function-naming")
expect fun MapScreen(
    viewModel: MapViewModel,
    onNavigateToZoneDetails: (zoneId: Id) -> Unit,
    onNavigateToReportZone: (latitude: Double, longitude: Double) -> Unit,
    onNavigateBack: () -> Unit,
)
