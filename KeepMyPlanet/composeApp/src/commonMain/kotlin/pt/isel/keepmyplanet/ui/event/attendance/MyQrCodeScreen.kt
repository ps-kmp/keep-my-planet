package pt.isel.keepmyplanet.ui.event.attendance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import pt.isel.keepmyplanet.domain.common.Id
import pt.isel.keepmyplanet.ui.components.AppTopBar
import androidx.compose.material.Text
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.components.QrCodeDisplay


@Composable
fun MyQrCodeScreen(userId: Id, onNavigateBack: () -> Unit) {
    Scaffold(topBar = { AppTopBar("My Check-in Code", onNavigateBack) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Show this code to the event organizer", style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(32.dp))
            QrCodeDisplay(data = userId.value.toString(), modifier = Modifier.size(250.dp))
        }
    }
}
