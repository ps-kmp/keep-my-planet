package pt.isel.keepmyplanet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import pt.isel.keepmyplanet.data.api.MessageClient
import pt.isel.keepmyplanet.data.api.createHttpClient
import pt.isel.keepmyplanet.data.model.UserSession
import pt.isel.keepmyplanet.data.service.ChatService
import pt.isel.keepmyplanet.ui.screens.chat.ChatScreen
import pt.isel.keepmyplanet.ui.screens.chat.ChatViewModel
import pt.isel.keepmyplanet.ui.screens.login.LoginScreen
import pt.isel.keepmyplanet.ui.screens.login.LoginViewModel

@Suppress("ktlint:standard:function-naming")
@Composable
fun App() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
    var userSession by remember { mutableStateOf<UserSession?>(null) }
    var currentEventId by remember { mutableStateOf<UInt?>(null) }

    val httpClient = remember { createHttpClient() }
    val messageClient = remember { MessageClient(httpClient) }
    val chatService = remember { ChatService(messageClient) }

    when (currentScreen) {
        is Screen.Login -> {
            val loginViewModel = remember { LoginViewModel(chatService) }

            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToChat = { session, eventId ->
                    userSession = session
                    currentEventId = eventId
                    currentScreen = Screen.Chat
                },
            )
        }

        is Screen.Chat -> {
            val session = userSession
            val eventId = currentEventId

            if (session != null && eventId != null) {
                val chatViewModel = remember { ChatViewModel(chatService) }

                ChatScreen(
                    viewModel = chatViewModel,
                    userSession = session,
                    eventId = eventId,
                    onNavigateBack = {
                        chatService.leaveChat()
                        userSession = null
                        currentEventId = null
                        currentScreen = Screen.Login
                    },
                )
            } else {
                // fallback safety
                LaunchedEffect(Unit) {
                    chatService.leaveChat()
                    userSession = null
                    currentEventId = null
                    currentScreen = Screen.Login
                }
            }
        }
    }
}

sealed class Screen {
    data object Login : Screen()

    data object Chat : Screen()
}
/*@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: $greeting")
                }
            }
        }
    }
}*/
