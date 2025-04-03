package pt.isel.keepmyplanet.domain.message

import kotlin.jvm.JvmInline

@JvmInline
value class MessageContent(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Message content cannot be blank." }
        require(value.length <= 1000) { "Message content cannot exceed 1000 characters." }
    }
}

/*
Possible implementation that allows different types of content
sealed class MessageContent {
    data class Text(val text: String) : MessageContent()
    data class Image(val imageUrl: String) : MessageContent()
    data class Video(val videoUrl: String) : MessageContent()
    data class Audio(val audioUrl: String) : MessageContent()
    data class File(val fileName: String, val fileUrl: String) : MessageContent()
}*/
