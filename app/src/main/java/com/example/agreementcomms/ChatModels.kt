package com.example.agreementcomms

data class Server(
    val id: String,
    val name: String,
    val icon: String,
    val channels: List<String>
)

data class Message(
    val author: String,
    val text: String,
    val time: String,
    val isMine: Boolean,
    val attachments: List<MessageAttachment> = emptyList()
)

data class MessageAttachment(
    val type: AttachmentType,
    val name: String,
    val url: String? = null,
    val meta: String? = null
)

data class Role(
    val id: String,
    val name: String,
    val color: String? = null,
    val position: Int = 0
)

enum class AttachmentType {
    Image,
    File
}

enum class MainSection {
    Chat,
    Settings
}

fun conversationKey(serverId: String, channel: String): String = "$serverId|$channel"
