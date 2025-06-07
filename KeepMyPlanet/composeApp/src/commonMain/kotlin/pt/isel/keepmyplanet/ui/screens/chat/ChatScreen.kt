package pt.isel.keepmyplanet.ui.screens.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.primarySurface
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
import pt.isel.keepmyplanet.data.model.EventInfo
import pt.isel.keepmyplanet.data.model.UserInfo
import pt.isel.keepmyplanet.data.service.ChatHttpClient
import pt.isel.keepmyplanet.ui.screens.chat.components.MessageInput
import pt.isel.keepmyplanet.ui.screens.chat.components.MessageItem

@Suppress("ktlint:standard:function-naming")
@Composable
fun ChatScreen(
    chatHttpClient: ChatHttpClient,
    user: UserInfo,
    event: EventInfo,
    onNavigateBack: () -> Unit,
) {
    val viewModel = remember(event.id) { ChatViewModel(chatHttpClient, user, event) }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val reversedMessages = remember(uiState.messages) { uiState.messages.reversed() }

    LaunchedEffect(Unit) {
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
            TopAppBar(
                title = {
                    Text(
                        uiState.event.title.value
                            .ifBlank { "Chat" },
                    )
                },
                backgroundColor = MaterialTheme.colors.primarySurface,
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
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading && uiState.messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp),
                    reverseLayout = true,
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    items(reversedMessages, key = { it.id.toString() }) { message ->
                        MessageItem(
                            message = message,
                            currentUserId = uiState.user.id,
                        )
                    }
                }

                MessageInput(
                    message = uiState.messageInput,
                    onMessageChange = viewModel::onMessageChanged,
                    onSendClick = viewModel::sendMessage,
                    isSending = uiState.isSending,
                    sendEnabled = uiState.isSendEnabled,
                )
            }
        }
    }
}
