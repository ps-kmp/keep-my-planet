package pt.isel.keepmyplanet.ui.zone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Report
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.domain.zone.ZoneSeverity
import pt.isel.keepmyplanet.domain.zone.ZoneStatus
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.FullScreenLoading

@Suppress("ktlint:standard:function-naming")
@Composable
fun ZoneDetailsScreen(
    viewModel: ZoneViewModel,
    zoneId: UInt,
    onNavigateBack: () -> Unit,
    onNavigateToCreateEvent: (zoneId: UInt) -> Unit,
    onNavigateToEventDetails: (eventId: UInt) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val zone = uiState.zoneDetails

    LaunchedEffect(zoneId) {
        viewModel.loadZoneDetails(zoneId)
    }

    Scaffold(
        topBar = { AppTopBar(title = "Zone Details", onNavigateBack = onNavigateBack) },
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.isLoading -> FullScreenLoading()
                zone != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text("Zone #${zone.id.value}", style = MaterialTheme.typography.h4)

                        Card(elevation = 4.dp) {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text("Details", style = MaterialTheme.typography.h6)
                                InfoRow(
                                    icon = Icons.Default.Category,
                                    text = zone.description.value,
                                )
                                InfoRow(
                                    icon = Icons.Default.Report,
                                    text = "Reported by User #${zone.reporterId.value}",
                                )
                            }
                        }

                        Card(elevation = 4.dp) {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text("Status", style = MaterialTheme.typography.h6)
                                StatusBadge(
                                    text = zone.status.name.replace('_', ' '),
                                    backgroundColor = getStatusColor(zone.status),
                                )
                                StatusBadge(
                                    text = zone.zoneSeverity.name,
                                    backgroundColor = getSeverityColor(zone.zoneSeverity),
                                    icon = Icons.Default.ErrorOutline,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        if (zone.eventId != null) {
                            Button(
                                onClick = { onNavigateToEventDetails(zone.eventId!!.value) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp),
                                )
                                Text("View Associated Event")
                            }
                        } else if (zone.status == ZoneStatus.REPORTED) {
                            Button(
                                onClick = { onNavigateToCreateEvent(zone.id.value) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp),
                                )
                                Text("Create Cleanup Event")
                            }
                        }
                    }
                }

                else -> {
                    Text("Could not load zone details.")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colors.primary)
        Text(text, style = MaterialTheme.typography.body1)
    }
}

@Composable
private fun StatusBadge(
    text: String,
    backgroundColor: Color,
    icon: ImageVector? = null,
) {
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(50))
                .background(backgroundColor)
                .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.caption,
            )
        }
    }
}

@Composable
private fun getStatusColor(status: ZoneStatus): Color =
    when (status) {
        ZoneStatus.REPORTED -> Color(0xFFFFA000) // Amber
        ZoneStatus.CLEANING_SCHEDULED -> MaterialTheme.colors.primary
        ZoneStatus.CLEANED -> Color(0xFF388E3C) // Green
    }

@Composable
private fun getSeverityColor(severity: ZoneSeverity): Color =
    when (severity) {
        ZoneSeverity.LOW -> Color(0xFF34A853)
        ZoneSeverity.MEDIUM -> Color(0xFFFBBC05)
        ZoneSeverity.HIGH -> MaterialTheme.colors.error
        ZoneSeverity.UNKNOWN -> Color.Gray
    }
