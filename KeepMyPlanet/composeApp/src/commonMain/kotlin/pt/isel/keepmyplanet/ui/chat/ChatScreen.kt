package pt.isel.keepmyplanet.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pt.isel.keepmyplanet.ui.chat.components.MessageInput
import pt.isel.keepmyplanet.ui.chat.components.MessageItem
import pt.isel.keepmyplanet.ui.chat.states.ChatEvent
import pt.isel.keepmyplanet.ui.chat.states.ChatUiState
import pt.isel.keepmyplanet.ui.common.FullScreenLoading
import pt.isel.keepmyplanet.ui.components.AppTopBar
import pt.isel.keepmyplanet.ui.components.ErrorState

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ChatEvent.ShowSnackbar -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short,
                    )
                }

                is ChatEvent.ScrollToBottom -> {
                    if (uiState.messages.isNotEmpty()) {
                        coroutineScope.launch { listState.animateScrollToItem(0) }
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AppTopBar(
                title = uiState.chatInfo.eventTitle.value,
                onNavigateBack = onNavigateBack,
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading && uiState.messages.isEmpty() -> {
                    FullScreenLoading()
                }

                uiState.error != null && uiState.messages.isEmpty() -> {
                    ErrorState(message = uiState.error!!, onRetry = viewModel::loadMessages)
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp),
                        reverseLayout = true,
                        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
                        contentPadding = PaddingValues(vertical = 8.dp),
                    ) {
                        items(uiState.messages, key = { it.id.toString() }) { message ->
                            MessageItem(message = message, currentUserId = uiState.user.id.value)
                        }
                    }

                    MessageInput(
                        message = uiState.messageInput,
                        onMessageChange = viewModel::onMessageChanged,
                        onSendClick = viewModel::sendMessage,
                        isSending = uiState.actionState is ChatUiState.ActionState.Sending,
                        sendEnabled = uiState.isSendEnabled,
                        errorText = uiState.messageInputError,
                        maxLength = ChatViewModel.MAX_MESSAGE_LENGTH,
                    )
                }
            }
        }
    }
}
