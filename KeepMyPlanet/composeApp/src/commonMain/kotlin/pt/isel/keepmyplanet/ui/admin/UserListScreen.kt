package pt.isel.keepmyplanet.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.ui.admin.components.RoleChangeDialog
import pt.isel.keepmyplanet.ui.admin.components.UserListItem
import pt.isel.keepmyplanet.ui.admin.states.UserListEvent
import pt.isel.keepmyplanet.ui.base.koinViewModel
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.ErrorState
import pt.isel.keepmyplanet.ui.components.FullScreenLoading

@Composable
fun UserListScreen(
    viewModel: UserListViewModel = koinViewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val showRoleChangeDialog = remember { mutableStateOf(false) }
    val showDeleteDialog = remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UserListEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                UserListEvent.ShowRoleChangeDialog -> showRoleChangeDialog.value = true
                UserListEvent.HideRoleChangeDialog -> showRoleChangeDialog.value = false
                UserListEvent.ShowDeleteDialog -> showDeleteDialog.value = true
                UserListEvent.HideDeleteDialog -> showDeleteDialog.value = false
            }
        }
    }

    if (showRoleChangeDialog.value && uiState.userToUpdate != null) {
        RoleChangeDialog(
            user = uiState.userToUpdate!!,
            onConfirm = { viewModel.confirmRoleChange() },
            onDismiss = { viewModel.cancelRoleChange() },
        )
    }

    if (showDeleteDialog.value && uiState.userToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Delete User?") },
            text = {
                Text(
                    "Are you sure you want to permanently delete user " +
                        "'${uiState.userToDelete!!.name.value}'? This action cannot be undone.",
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDelete() },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                        ),
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text("Cancel")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "User Management",
                onNavigateBack = onNavigateBack,
                onNavigateToHome = onNavigateToHome,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> FullScreenLoading()
                uiState.error != null ->
                    ErrorState(
                        message = uiState.error!!,
                    ) { viewModel.loadUsers() }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(uiState.users, key = { it.id.value }) { user ->
                            UserListItem(
                                user = user,
                                isUpdatingRole =
                                    uiState.isUpdatingRole && uiState.userToUpdate?.id == user.id,
                                isDeletingUser =
                                    uiState.isDeletingUser && uiState.userToDelete?.id == user.id,
                                onChangeRoleClicked = { viewModel.startRoleChange(user) },
                                onDeleteClicked = { viewModel.startDelete(user) },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}
