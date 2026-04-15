package com.example.agreementcomms.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.agreementcomms.Message
import com.example.agreementcomms.Server
import com.example.agreementcomms.conversationKey

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
                            Message(
                                author = it.author,
                                text = it.text,
                                time = it.time,
                                isMine = it.author.equals(nickname, ignoreCase = true)
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
        text: String
    ) {
        AccordanceApiClient.api.createMessage(
            serverId = serverId,
            channelId = channelId,
            request = CreateMessageRequest(
                author = author,
                text = text
            )
        )
    }
}

data class BackendBootstrap(
    val servers: List<Server>,
    val conversations: Map<String, SnapshotStateList<Message>>,
    val channelApiIds: Map<String, String>
)
