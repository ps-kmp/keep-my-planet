package pt.isel.keepmyplanet.ui.screens.event

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.keepmyplanet.data.model.EventInfo

@Suppress("ktlint:standard:function-naming")
@Composable
fun EventDetailsScreen(
    eventId: UInt,
    uiState: EventDetailsUiState,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (EventInfo) -> Unit,
    onLoadEventDetails: (UInt) -> Unit,
) {
    val event = uiState.event

    LaunchedEffect(eventId) {
        onLoadEventDetails(eventId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event?.title ?: "Detalhes do Evento") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            } else if (event != null) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Título
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.h5,
                    )

                    // Descrição
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.body1,
                    )

                    // Datas
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Data de Início: ${event.startDate}",
                            style = MaterialTheme.typography.body2,
                        )
                        Text(
                            text = "Data de Fim: ${event.endDate}",
                            style = MaterialTheme.typography.body2,
                        )
                    }

                    // Status e Participantes
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Status: ${event.status}",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.secondary,
                        )
                        event.maxParticipants?.let {
                            Text(
                                text = "Participantes: ${event.participantsIds.size}/$it",
                                style = MaterialTheme.typography.body2,
                            )
                        }
                    }

                    // Datas de criação/atualização
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "Criado em: ${event.createdAt}",
                            style = MaterialTheme.typography.caption,
                        )
                        Text(
                            text = "Última atualização: ${event.updatedAt}",
                            style = MaterialTheme.typography.caption,
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Botão Chat
                    Button(
                        onClick = {
                            onNavigateToChat(
                                EventInfo(
                                    id = event.id,
                                    title = event.title,
                                    description = event.description,
                                    startDate = event.startDate,
                                    endDate = event.endDate,
                                    status = event.status,
                                ),
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Entrar no Chat")
                    }
                }
            }

            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colors.error,
                    modifier =
                        Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                )
            }
        }
    }
}
