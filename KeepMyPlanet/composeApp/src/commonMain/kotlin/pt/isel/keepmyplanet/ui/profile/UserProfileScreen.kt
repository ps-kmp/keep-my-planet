package pt.isel.keepmyplanet.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import pt.isel.keepmyplanet.domain.user.UserInfo
import pt.isel.keepmyplanet.navigation.rememberSavableScrollState
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.DetailCard
import pt.isel.keepmyplanet.ui.components.ErrorState
import pt.isel.keepmyplanet.ui.components.InfoRow
import pt.isel.keepmyplanet.ui.components.ProfileScreenSkeleton
import pt.isel.keepmyplanet.ui.components.rememberPhotoPicker
import pt.isel.keepmyplanet.ui.profile.components.DeleteAccountSection
import pt.isel.keepmyplanet.ui.profile.components.PasswordChangeSection
import pt.isel.keepmyplanet.ui.profile.components.ProfileHeader
import pt.isel.keepmyplanet.ui.profile.components.ProfileInfoDisplay
import pt.isel.keepmyplanet.ui.profile.components.ProfileInfoSection
import pt.isel.keepmyplanet.ui.profile.states.UserProfileEvent
import pt.isel.keepmyplanet.ui.profile.states.UserProfileUiState

@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel,
    onNavigateToHome: () -> Unit,
    onAccountDeleted: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToStats: () -> Unit,
    onProfileUpdated: (UserInfo) -> Unit,
    routeKey: String,
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberSavableScrollState(key = routeKey)
    val snackbarHostState = remember { SnackbarHostState() }

    val launchPhotoPicker =
        rememberPhotoPicker { imageData, filename ->
            viewModel.onProfilePictureSelected(imageData, filename)
        }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UserProfileEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is UserProfileEvent.AccountDeleted -> onAccountDeleted()
                is UserProfileEvent.ProfileUpdated -> onProfileUpdated(event.userInfo)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "User Profile",
                onNavigateBack = onNavigateBack,
                onNavigateToHome = onNavigateToHome,
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> ProfileScreenSkeleton()
                uiState.error != null ->
                    ErrorState(
                        message = uiState.error!!,
                        onRetry = viewModel::loadUserProfile,
                    )

                uiState.userDetails != null -> {
                    UserProfileDetails(
                        uiState = uiState,
                        scrollState = scrollState,
                        onNameChanged = viewModel::onNameChanged,
                        onEmailChanged = viewModel::onEmailChanged,
                        onOldPasswordChanged = viewModel::onOldPasswordChanged,
                        onNewPasswordChanged = viewModel::onNewPasswordChanged,
                        onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
                        onEditProfileToggled = viewModel::onEditProfileToggled,
                        onSaveProfileClicked = viewModel::onSaveProfileClicked,
                        onPasswordChangeToggled = viewModel::onPasswordChangeToggled,
                        onChangePasswordClicked = viewModel::onChangePasswordClicked,
                        onDeleteAccountClicked = viewModel::onDeleteAccountClicked,
                        onAvatarClick = { if (uiState.isEditingProfile) launchPhotoPicker() },
                        onNavigateToStats = onNavigateToStats,
                    )
                }
            }
        }
    }
}

@Composable
private fun UserProfileDetails(
    uiState: UserProfileUiState,
    scrollState: androidx.compose.foundation.ScrollState,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onOldPasswordChanged: (String) -> Unit,
    onNewPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onEditProfileToggled: () -> Unit,
    onSaveProfileClicked: () -> Unit,
    onPasswordChangeToggled: () -> Unit,
    onChangePasswordClicked: () -> Unit,
    onDeleteAccountClicked: () -> Unit,
    onAvatarClick: () -> Unit,
    onNavigateToStats: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        ProfileHeader(
            user = uiState.userDetails!!,
            photoUrl = uiState.photoUrl,
            isUpdating = uiState.isUpdatingPhoto,
            onAvatarClick = onAvatarClick,
        )

        DetailCard(
            title = "Personal Information",
            actions = {
                if (!uiState.isEditingProfile) {
                    IconButton(onClick = onEditProfileToggled) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                    }
                }
            },
        ) {
            AnimatedVisibility(visible = uiState.isEditingProfile) {
                ProfileInfoSection(
                    uiState = uiState,
                    onNameChanged = onNameChanged,
                    onEmailChanged = onEmailChanged,
                    onSaveProfileClicked = onSaveProfileClicked,
                    onCancel = onEditProfileToggled,
                )
            }
            AnimatedVisibility(visible = !uiState.isEditingProfile) {
                ProfileInfoDisplay(user = uiState.userDetails)
            }
        }

        DetailCard(title = "My Activity") {
            InfoRow(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                text = "View your contribution and attended events.",
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onNavigateToStats,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("View My Stats")
            }
        }

        DetailCard(title = "Security") {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = onPasswordChangeToggled,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (uiState.showPasswordChangeSection) "Cancel" else "Change Password")
                }
                PasswordChangeSection(
                    uiState = uiState,
                    onOldPasswordChanged = onOldPasswordChanged,
                    onNewPasswordChanged = onNewPasswordChanged,
                    onConfirmPasswordChanged = onConfirmPasswordChanged,
                    onChangePasswordClicked = onChangePasswordClicked,
                )
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                DeleteAccountSection(uiState, onDeleteAccountClicked)
            }
        }
    }
}
