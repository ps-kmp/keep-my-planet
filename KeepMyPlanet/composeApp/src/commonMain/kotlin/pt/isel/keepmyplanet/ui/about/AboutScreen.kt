package pt.isel.keepmyplanet.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.theme.onSurfaceLight
import pt.isel.keepmyplanet.ui.theme.primaryLight
import pt.isel.keepmyplanet.ui.theme.surfaceLight
import pt.isel.keepmyplanet.ui.theme.tertiaryLight

private const val ABOUT_TEXT =
    "A multiplatform system developed with Kotlin Multiplatform (KMP) that allows the " +
        "identification and mapping of polluted zones, as well as organizing and participating " +
        "in community cleanup events. The system provides an interactive interface where " +
        "volunteers can report areas as polluted, share photos and descriptions of the " +
        "conditions found, and create or join cleanup initiatives organized for these zones. " +
        "KeepMyPlanet is the rallying point for community and environmental action, addressing a " +
        "real and increasingly relevant and emerging problem."

@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = { AppTopBar(title = "About KeepMyPlanet", onNavigateBack = onNavigateBack) },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                shape = CircleShape,
                color = surfaceLight.copy(alpha = 0.1f),
                modifier = Modifier.size(96.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "About Icon",
                    modifier = Modifier.padding(20.dp),
                    tint = tertiaryLight,
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Our Mission",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = ABOUT_TEXT,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Divider(modifier = Modifier.padding(vertical = 24.dp))

            Text(
                text = "Core Features",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(16.dp))

            FeatureCard(
                icon = Icons.Default.TrackChanges,
                title = "Map & Report",
                description = "Easily identify and report polluted zones on an interactive map.",
            )
            FeatureCard(
                icon = Icons.Default.Groups,
                title = "Organize & Participate",
                description = "Create or join community cleanup events to make a real impact.",
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = "Thank You!",
                style = MaterialTheme.typography.bodyMedium,
                color = primaryLight,
                modifier = Modifier.padding(top = 32.dp, bottom = 16.dp),
            )

            Text(
                text = "for being part of the solution.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceLight,
            contentColor = onSurfaceLight,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = tertiaryLight,
                modifier = Modifier.size(40.dp),
            )
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Text(text = description, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
            }
        }
    }
}
