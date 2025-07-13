package pt.isel.keepmyplanet.ui.downloads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.DesktopMac
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.components.AppTopBar

private const val GITHUB_RELEASES_URL =
    "https://github.com/ps-kmp/keep-my-planet/releases/download/0.0.1"
private const val ANDROID_APK_URL = "$GITHUB_RELEASES_URL/keepmyplanet.apk"
private const val WINDOWS_MSI_URL = "$GITHUB_RELEASES_URL/keepmyplanet.msi"
private const val MACOS_DMG_URL = "$GITHUB_RELEASES_URL/keepmyplanet.dmg"
private const val LINUX_DEB_URL = "$GITHUB_RELEASES_URL/keepmyplanet.deb"

@Composable
fun DownloadsScreen(
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Downloads",
                onNavigateBack = onNavigateBack,
                onNavigateToHome = onNavigateToHome,
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Download KeepMyPlanet",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Get the app for your favorite platform and help us keep our planet clean!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            DownloadSection(title = "Mobile") {
                DownloadCard(
                    platformName = "Android",
                    description = "Get the APK for your Android device.",
                    downloadUrl = ANDROID_APK_URL,
                    icon = Icons.Default.Android,
                )
            }

            DownloadSection(title = "Desktop") {
                DownloadCard(
                    platformName = "Windows",
                    description = "Installer for Windows (.msi)",
                    downloadUrl = WINDOWS_MSI_URL,
                    icon = Icons.Default.DesktopWindows,
                )
                DownloadCard(
                    platformName = "macOS",
                    description = "Disk image for macOS (.dmg)",
                    downloadUrl = MACOS_DMG_URL,
                    icon = Icons.Default.DesktopMac,
                )
                DownloadCard(
                    platformName = "Linux",
                    description = "Debian package for Linux (.deb)",
                    downloadUrl = LINUX_DEB_URL,
                    icon = Icons.Default.Terminal,
                )
            }
        }
    }
}

@Composable
private fun DownloadSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        HorizontalDivider()
        content()
    }
}

@Composable
private fun DownloadCard(
    platformName: String,
    description: String,
    downloadUrl: String,
    icon: ImageVector,
) {
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = platformName,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = platformName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { uriHandler.openUri(downloadUrl) }) {
                Icon(Icons.Default.Download, contentDescription = "Download")
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text("Download")
            }
        }
    }
}
