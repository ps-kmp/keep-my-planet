package pt.isel.keepmyplanet.ui.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.domain.user.UserInfo

@Composable
fun ProfileHeader(
    user: UserInfo,
    photoUrl: String?,
    isUpdating: Boolean,
    onAvatarClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Avatar(
            user = user,
            size = 120.dp,
            photoUrl = photoUrl,
            isUpdating = isUpdating,
            onClick = onAvatarClick,
        )
        Text(
            text = user.name.value,
            style = MaterialTheme.typography.h4,
        )
    }
}
