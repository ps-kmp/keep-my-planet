package pt.isel.keepmyplanet.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.user.model.UserInfo

@Suppress("ktlint:standard:function-naming")
@Composable
fun HomeScreen(
    user: UserInfo,
    onNavigateToEventList: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToMap: () -> Unit,
    onLogout: () -> Unit,
) {
    Scaffold(topBar = { AppTopBar(title = "Home") }) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Welcome, ${user.name.value}!",
                style = MaterialTheme.typography.h5,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = onNavigateToProfile) {
                Text("View User")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = onNavigateToEventList) {
                Text("View Events")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = onNavigateToMap) {
                Text("View Map")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = onLogout) {
                Text("Logout")
            }
        }
    }
}
