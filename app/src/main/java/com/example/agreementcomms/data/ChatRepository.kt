package com.example.agreementcomms.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.agreementcomms.AttachmentType
import com.example.agreementcomms.Message
import com.example.agreementcomms.MessageAttachment
import com.example.agreementcomms.Server
import com.example.agreementcomms.conversationKey
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ChatRepository {
    suspend fun fetchBackendBootstrap(nickname: String): BackendBootstrap {
        val api = AccordanceApiClient.api
        val apiServers = api.getServers()

        val mappedServers = mutableListOf<Server>()
        val mappedConversations = mutableMapOf<String, SnapshotStateList<Message>>()
        val channelApiIds = mutableMapOf<String, String>()

        for (server in apiServers) {
            val channels = api.getChannels(server.id)
            mappedServers.add(
                Server(
                    id = server.id,
                    name = server.name,
                    icon = server.icon,
                    channels = channels.map { it.name }
                )
            )

            for (channel in channels) {
                val key = conversationKey(server.id, channel.name)
                channelApiIds[key] = channel.id

                val messages = api.getMessages(server.id, channel.id)
                mappedConversations[key] = mutableStateListOf<Message>().apply {
                    addAll(
                        messages.map {
                            val attachment = it.attachment
                            val mappedAttachment = if (attachment != null) {
                                listOf(
                                    MessageAttachment(
                                        type = if (attachment.type.equals("image", ignoreCase = true)) {
                                            AttachmentType.Image
                                        } else {
                                            AttachmentType.File
                                        },
                                        name = attachment.name,
                                        url = attachment.url?.let(::toAbsoluteUrl)
                                    )
                                )
                            } else {
                                emptyList()
                            }

                            Message(
                                author = it.author,
                                text = it.text,
                                time = it.time,
                                isMine = it.author.equals(nickname, ignoreCase = true),
                                attachments = mappedAttachment
                            )
                        }
                    )
                }
            }
        }

        return BackendBootstrap(
            servers = mappedServers,
            conversations = mappedConversations,
            channelApiIds = channelApiIds
        )
    }

    suspend fun sendMessage(
        serverId: String,
        channelId: String,
        author: String,
        text: String,
        attachment: ApiAttachmentRequest? = null
    ) {
        AccordanceApiClient.api.createMessage(
            serverId = serverId,
            channelId = channelId,
            request = CreateMessageRequest(
                author = author,
                text = text,
                attachment = attachment
            )
        )
    }

    suspend fun uploadAttachment(
        fileName: String,
        mimeType: String,
        content: ByteArray
    ): UploadResponse {
        val requestBody = content.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", fileName, requestBody)
        return AccordanceApiClient.api.uploadFile(part)
    }

    private fun toAbsoluteUrl(url: String): String {
        return if (url.startsWith("http://") || url.startsWith("https://")) {
            url
        } else {
            AccordanceApiClient.BASE_URL.trimEnd('/') + "/" + url.trimStart('/')
        }
    }
}

data class BackendBootstrap(
    val servers: List<Server>,
    val conversations: Map<String, SnapshotStateList<Message>>,
    val channelApiIds: Map<String, String>
)
