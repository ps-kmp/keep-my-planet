package pt.isel.keepmyplanet.ui.screens.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageUi(
    val id: UInt,
    val senderId: UInt,
    val content: String,
    val timestamp: String,
    val isCurrentUser: Boolean,
)
