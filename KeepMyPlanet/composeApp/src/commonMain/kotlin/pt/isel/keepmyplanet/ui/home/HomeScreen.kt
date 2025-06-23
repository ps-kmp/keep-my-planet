package pt.isel.keepmyplanet.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.dto.user.UserInfo
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.home.components.DashboardItem

@Composable
fun HomeScreen(
    user: UserInfo,
    onNavigateToEventList: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToMap: () -> Unit,
    onLogout: () -> Unit,
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "KeepMyPlanet",
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Welcome, ${user.name.value}!",
                style = MaterialTheme.typography.h4,
                modifier = Modifier.padding(vertical = 16.dp),
            )

            DashboardItem(
                icon = Icons.Default.Map,
                title = "Interactive Map",
                description = "View polluted zones or report a new one.",
                onClick = onNavigateToMap,
            )

            DashboardItem(
                icon = Icons.AutoMirrored.Filled.ListAlt,
                title = "Cleanup Events",
                description = "Find and join events organized by the community.",
                onClick = onNavigateToEventList,
            )

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colors.error,
                    ),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text("Logout")
            }
        }
    }
}
