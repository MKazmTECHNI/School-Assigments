package com.example.agreementcomms

import android.net.Uri

data class Server(
    val id: String,
    val name: String,
    val icon: String,
    val channels: List<Channel>,
    val ownerId: String? = null,
    val inviteCode: String? = null
)

data class Channel(
    val id: String,
    val name: String,
    val serverId: String,
    val topic: String? = null,
    val category: String? = null,
    val slowmodeSeconds: Int = 0,
    val isNsfw: Boolean = false
)

data class Message(
    val id: String = "",
    val author: String,
    val text: String,
    val time: String,
    val isMine: Boolean,
    val authorId: String? = null,
    val attachments: List<MessageAttachment> = emptyList()
)

data class MessageAttachment(
    val type: AttachmentType,
    val name: String,
    val url: String? = null,
    val meta: String? = null
)

data class ComposerAttachment(
    val uri: Uri,
    val name: String,
    val type: AttachmentType,
    val sizeLabel: String? = null
)

data class Role(
    val id: String,
    val name: String,
    val color: String? = null,
    val position: Int = 0,
    val permissions: RolePermissions = RolePermissions()
)

data class RolePermissions(
    val manageServer: Boolean = false,
    val manageChannels: Boolean = false,
    val manageRoles: Boolean = false,
    val manageMessages: Boolean = false
)

data class RolePermissionsOverride(
    val manageServer: Boolean? = null,
    val manageChannels: Boolean? = null,
    val manageRoles: Boolean? = null,
    val manageMessages: Boolean? = null
)

data class ChannelSettings(
    val topic: String? = null,
    val slowmodeSeconds: Int = 0,
    val isNsfw: Boolean = false,
    val category: String? = null
)

enum class AttachmentType {
    Image,
    File
}

enum class MainSection {
    Chat,
    ProfileSettings,
    ServerSettings,
    ChannelSettings
}
