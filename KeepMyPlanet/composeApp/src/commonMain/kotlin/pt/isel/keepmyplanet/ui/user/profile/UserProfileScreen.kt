package pt.isel.keepmyplanet.ui.user.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.ErrorState
import pt.isel.keepmyplanet.ui.components.FullScreenLoading
import pt.isel.keepmyplanet.ui.user.profile.components.UserProfileDetails
import pt.isel.keepmyplanet.ui.user.profile.model.UserProfileEvent
import pt.isel.keepmyplanet.ui.user.profile.model.UserProfileUiState

@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel,
    onAccountDeleted: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToStats: () -> Unit,
) {
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

                is UserProfileEvent.AccountDeleted -> onAccountDeleted()
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
        onRetry = viewModel::loadUserProfile,
        onNavigateToStats = onNavigateToStats,
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
    onNavigateToStats: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopBar(title = "User Profile", onNavigateBack = onNavigateBack) },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> FullScreenLoading()
                uiState.error != null -> ErrorState(message = uiState.error, onRetry = onRetry)
                uiState.userDetails != null -> {
                    UserProfileDetails(
                        uiState = uiState,
                        onNameChanged = onNameChanged,
                        onEmailChanged = onEmailChanged,
                        onOldPasswordChanged = onOldPasswordChanged,
                        onNewPasswordChanged = onNewPasswordChanged,
                        onConfirmPasswordChanged = onConfirmPasswordChanged,
                        onEditProfileToggled = onEditProfileToggled,
                        onSaveProfileClicked = onSaveProfileClicked,
                        onPasswordChangeToggled = onPasswordChangeToggled,
                        onChangePasswordClicked = onChangePasswordClicked,
                        onConfirmDeleteAccount = onConfirmDeleteAccount,
                        onNavigateToStats = onNavigateToStats,
                    )
                }
            }
        }
    }
}
