package pt.isel.keepmyplanet.ui.event.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.domain.event.EventFilterType

@Composable
fun SearchBarAndFilters(
    query: String,
    onQueryChange: (String) -> Unit,
    activeFilter: EventFilterType,
    onFilterChange: (EventFilterType) -> Unit,
    isLoading: Boolean,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search events...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
            },
            singleLine = true,
            enabled = true,
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            val filtersEnabled = !isLoading
            FilterButton("All", activeFilter == EventFilterType.ALL, filtersEnabled) {
                onFilterChange(EventFilterType.ALL)
            }
            Spacer(Modifier.width(8.dp))
            FilterButton("Organized", activeFilter == EventFilterType.ORGANIZED, filtersEnabled) {
                onFilterChange(EventFilterType.ORGANIZED)
            }
            Spacer(Modifier.width(8.dp))
            FilterButton("Joined", activeFilter == EventFilterType.JOINED, filtersEnabled) {
                onFilterChange(EventFilterType.JOINED)
            }
        }
    }
}

@Composable
private fun FilterButton(
    text: String,
    isActive: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
    ) {
        Text(
            text = text,
        )
    }
}
