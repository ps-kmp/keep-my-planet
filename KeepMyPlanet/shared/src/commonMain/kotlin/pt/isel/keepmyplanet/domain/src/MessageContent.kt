package pt.isel.keepmyplanet.domain.src

import kotlin.jvm.JvmInline

@JvmInline
value class MessageContent(
    private val text: String,
) {
    init {
        require(text.isNotEmpty()) { "text length must be greater than 0." }
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
