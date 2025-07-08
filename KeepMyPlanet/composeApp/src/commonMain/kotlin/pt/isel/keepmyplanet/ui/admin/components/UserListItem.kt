package pt.isel.keepmyplanet.ui.admin.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.domain.user.UserRole
import pt.isel.keepmyplanet.ui.components.LoadingOutlinedButton
import pt.isel.keepmyplanet.ui.profile.components.Avatar
import pt.isel.keepmyplanet.ui.theme.primaryLight

@Composable
fun UserListItem(
    user: UserInfo,
    isUpdatingRole: Boolean,
    isDeletingUser: Boolean,
    onChangeRoleClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(user = user, size = 48.dp)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    user.name.value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    user.email.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Role: ${user.role}",
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        if (user.role ==
                            UserRole.ADMIN
                        ) {
                            primaryLight
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LoadingOutlinedButton(
                onClick = onChangeRoleClicked,
                isLoading = isUpdatingRole,
                text = if (user.role == UserRole.USER) "Promote" else "Demote",
                modifier = Modifier.widthIn(min = 90.dp),
            )
            LoadingOutlinedButton(
                onClick = onDeleteClicked,
                isLoading = isDeletingUser,
                text = "Delete",
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                loadingIndicatorColor = MaterialTheme.colorScheme.error,
                modifier = Modifier.widthIn(min = 90.dp),
            )
        }
    }
}
