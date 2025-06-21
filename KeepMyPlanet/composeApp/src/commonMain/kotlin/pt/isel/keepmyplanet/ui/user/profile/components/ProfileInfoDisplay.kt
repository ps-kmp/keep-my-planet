package pt.isel.keepmyplanet.ui.user.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.components.InfoRow
import pt.isel.keepmyplanet.ui.user.profile.model.UserInfo

@Composable
fun ProfileInfoDisplay(user: UserInfo) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        InfoRow(
            icon = Icons.Default.Person,
            text = user.name.value,
        )
        InfoRow(
            icon = Icons.Default.Email,
            text = user.email.value,
        )
    }
}
