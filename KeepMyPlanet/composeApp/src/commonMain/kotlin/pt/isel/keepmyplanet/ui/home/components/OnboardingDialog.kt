package pt.isel.keepmyplanet.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private data class OnboardingStep(
    val icon: ImageVector,
    val title: String,
    val description: String,
)

private val onboardingSteps =
    listOf(
        OnboardingStep(
            icon = Icons.Default.Map,
            title = "Explore the Map",
            description =
                "Discover polluted zones reported by our community. " +
                    "See where help is needed most.",
        ),
        OnboardingStep(
            icon = Icons.Default.AddLocationAlt,
            title = "Report Polluted Zones",
            description =
                "Found a spot that needs cleaning? Report it on the map " +
                    "with a description and photos to rally support.",
        ),
        OnboardingStep(
            icon = Icons.AutoMirrored.Filled.ListAlt,
            title = "Join Cleanup Events",
            description =
                "Browse events created by others, join a team, " +
                    "and make a real difference together.",
        ),
    )

@Composable
fun OnboardingDialog(onDismiss: () -> Unit) {
    var currentStepIndex by remember { mutableStateOf(0) }
    val currentStep = onboardingSteps[currentStepIndex]

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = currentStep.title) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(
                    imageVector = currentStep.icon,
                    contentDescription = currentStep.title,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = currentStep.description,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (currentStepIndex > 0) {
                    TextButton(onClick = { currentStepIndex-- }) {
                        Text("Previous")
                    }
                } else {
                    Spacer(Modifier)
                }

                if (currentStepIndex < onboardingSteps.lastIndex) {
                    Button(onClick = { currentStepIndex++ }) {
                        Text("Next")
                    }
                } else {
                    Button(onClick = onDismiss) {
                        Text("Get Started!")
                    }
                }
            }
        },
    )
}
