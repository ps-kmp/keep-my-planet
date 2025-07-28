package pt.isel.keepmyplanet.ui.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import keepmyplanet.composeapp.generated.resources.Res
import keepmyplanet.composeapp.generated.resources.ic_app_logo
import org.jetbrains.compose.resources.painterResource
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.MaximizeScreenBrightness
import pt.isel.keepmyplanet.ui.components.QrCodeDisplay

@Composable
fun MyQrCodeScreen(
    userId: Id,
    onNavigateToHome: () -> Unit,
    organizerName: String,
    onNavigateBack: () -> Unit,
) {
    MaximizeScreenBrightness()

    Scaffold(
        topBar = { AppTopBar("My Check-in Code", onNavigateToHome, onNavigateBack) },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "Show this to the event organizer",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = organizerName,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    Spacer(Modifier.height(16.dp))

                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        QrCodeDisplay(
                            data = userId.value.toString(),
                            modifier = Modifier.size(260.dp),
                        )
                        Icon(
                            painter = painterResource(Res.drawable.ic_app_logo),
                            contentDescription = "App Logo",
                            modifier =
                                Modifier
                                    .size(50.dp)
                                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                                    .padding(8.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "KeepMyPlanet",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}
