@file:Suppress("ktlint:standard:function-naming")

package pt.isel.keepmyplanet.ui.screens.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.data.model.UserInfo
import pt.isel.keepmyplanet.data.service.UserService
import pt.isel.keepmyplanet.ui.screens.user.components.PasswordChangeSection
import pt.isel.keepmyplanet.ui.screens.user.components.ProfileInfoSection

@Composable
fun UserProfileScreen(
    userService: UserService,
    user: UserInfo,
    onNavigateToLogin: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
) {
    val viewModel = remember(user.id) { UserProfileViewModel(userService, user) }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UserProfileEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short,
                    )
                }

                is UserProfileEvent.NavigateToLogin -> {
                    onNavigateToLogin()
                }
            }
        }
    }

    UserProfileScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onNameChanged = viewModel::onNameChanged,
        onEmailChanged = viewModel::onEmailChanged,
        onOldPasswordChanged = viewModel::onOldPasswordChanged,
        onNewPasswordChanged = viewModel::onNewPasswordChanged,
        onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
        onEditProfileToggled = viewModel::onEditProfileToggled,
        onSaveProfileClicked = viewModel::onSaveProfileClicked,
        onPasswordChangeToggled = viewModel::onPasswordChangeToggled,
        onChangePasswordClicked = viewModel::onChangePasswordClicked,
        onConfirmDeleteAccount = viewModel::onDeleteAccountClicked,
    )
}

@Composable
fun UserProfileScreenContent(
    uiState: UserProfileUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onOldPasswordChanged: (String) -> Unit,
    onNewPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onEditProfileToggled: () -> Unit,
    onSaveProfileClicked: () -> Unit,
    onPasswordChangeToggled: () -> Unit,
    onChangePasswordClicked: () -> Unit,
    onConfirmDeleteAccount: () -> Unit,
) {
    val showDeleteConfirmDialog = remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog.value = false },
            title = { Text("Delete Account?") },
            text = { Text("Are you sure you want to permanently delete your account? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog.value = false
                        onConfirmDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colors.error),
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirmDialog.value = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("User Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.userDetails == null) {
                Text(
                    "Failed to load user profile.",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                )
            } else {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(100.dp),
                    )

                    ProfileInfoSection(
                        uiState = uiState,
                        onNameChanged = onNameChanged,
                        onEmailChanged = onEmailChanged,
                        onEditProfileToggled = onEditProfileToggled,
                        onSaveProfileClicked = onSaveProfileClicked,
                    )

                    Divider()

                    PasswordChangeSection(
                        uiState = uiState,
                        onOldPasswordChanged = onOldPasswordChanged,
                        onNewPasswordChanged = onNewPasswordChanged,
                        onConfirmPasswordChanged = onConfirmPasswordChanged,
                        onChangePasswordClicked = onChangePasswordClicked,
                    )

                    Button(
                        onClick = onPasswordChangeToggled,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (uiState.showPasswordChangeSection) "Cancel Password Change" else "Change Password")
                    }

                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog.value = true },
                        enabled = uiState.isDeleteAccountEnabled,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.error),
                    ) {
                        if (uiState.isDeletingAccount) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text("Delete Account")
                        }
                    }
                }
            }
        }
    }
}
