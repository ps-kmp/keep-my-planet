package pt.isel.keepmyplanet.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.ui.theme.errorLight
import pt.isel.keepmyplanet.ui.theme.onErrorLight

@Composable
fun FormApiError(
    errorText: String?,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = errorText != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.fillMaxWidth(),
    ) {
        errorText?.let {
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor = errorLight,
                        contentColor = onErrorLight,
                    ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error Icon",
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
