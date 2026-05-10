package com.example.agreementcomms.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.agreementcomms.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ChatRepository {
    suspend fun register(username: String, password: String): AuthResponse {
        val resp = AccordanceApiClient.api.register(AuthRequest(username, password))
        AccordanceApiClient.authInterceptor.token = resp.token
        return resp
    }

    suspend fun login(username: String, password: String): AuthResponse {
        val resp = AccordanceApiClient.api.login(AuthRequest(username, password))
        AccordanceApiClient.authInterceptor.token = resp.token
        return resp
    }

    suspend fun updateProfile(displayName: String?, statusText: String?, bio: String?, avatarUrl: String?): ApiUser {
        return AccordanceApiClient.api.updateProfile(ApiUser("", "", displayName, avatarUrl, statusText, bio))
    }

    suspend fun fetchBackendBootstrap(nickname: String, userId: String): BackendBootstrap {
        val api = AccordanceApiClient.api
        val apiServers = api.getServers()

        val mappedServers = mutableListOf<Server>()
        val mappedConversations = mutableMapOf<String, SnapshotStateList<Message>>()
        val channelSettings = mutableMapOf<String, ChannelSettings>()

        for (server in apiServers) {
            val apiChannels = api.getChannels(server.id)
            val channels = apiChannels.map {
                Channel(
                    id = it.id,
                    name = it.name,
                    serverId = server.id,
                    topic = it.topic,
                    category = it.category,
                    slowmodeSeconds = it.slowmodeSeconds,
                    isNsfw = it.isNsfw
                )
            }
            mappedServers.add(
                Server(
                    id = server.id,
                    name = server.name,
                    icon = server.icon,
                    channels = channels,
                    ownerId = server.ownerId,
                    inviteCode = server.inviteCode
                )
            )

            for (channel in channels) {
                val key = channel.id
                channelSettings[key] = ChannelSettings(
                    topic = channel.topic,
                    slowmodeSeconds = channel.slowmodeSeconds,
                    isNsfw = channel.isNsfw,
                    category = channel.category
                )

                val messages = try { api.getMessages(server.id, channel.id) } catch (e: Exception) { emptyList() }
                mappedConversations[key] = mutableStateListOf<Message>().apply {
                    addAll(
                        messages.map {
                            Message(
                                id = it.id,
                                author = it.author,
                                text = it.text,
                                time = it.time,
                                isMine = it.authorId == userId,
                                authorId = it.authorId,
                                attachments = it.attachment?.let { att ->
                                    listOf(MessageAttachment(
                                        type = if (att.type == "image") AttachmentType.Image else AttachmentType.File,
                                        name = att.name,
                                        url = att.url
                                    ))
                                } ?: emptyList()
                            )
                        }
                    )
                }
            }
        }

        return BackendBootstrap(
            servers = mappedServers,
            conversations = mappedConversations,
            channelSettings = channelSettings
        )
    }

    suspend fun getMembers(serverId: String): List<ApiMember> = AccordanceApiClient.api.getMembers(serverId)
    
    suspend fun getRoles(serverId: String): List<Role> {
        return AccordanceApiClient.api.getRoles(serverId).map {
            Role(it.id, it.name, it.color, it.position, RolePermissions(
                it.permissions.manageServer, it.permissions.manageChannels,
                it.permissions.manageRoles, it.permissions.manageMessages
            ))
        }
    }

    suspend fun addRoleToMember(serverId: String, userId: String, roleId: String) = 
        AccordanceApiClient.api.addRoleToMember(serverId, userId, roleId)
    
    suspend fun removeRoleFromMember(serverId: String, userId: String, roleId: String) = 
        AccordanceApiClient.api.removeRoleFromMember(serverId, userId, roleId)

    suspend fun createServer(name: String, icon: String? = null): Server {
        val created = AccordanceApiClient.api.createServer(CreateServerRequest(name, icon))
        return Server(created.id, created.name, created.icon, emptyList(), ownerId = created.ownerId, inviteCode = created.inviteCode)
    }

    suspend fun joinServer(inviteCode: String): Server {
        val joined = AccordanceApiClient.api.joinServer(inviteCode)
        return Server(joined.id, joined.name, joined.icon, emptyList(), ownerId = joined.ownerId)
    }

    suspend fun updateServer(serverId: String, name: String?, icon: String?): ApiServer {
        return AccordanceApiClient.api.updateServer(serverId, UpdateServerRequest(name, icon))
    }

    suspend fun deleteServer(serverId: String) = AccordanceApiClient.api.deleteServer(serverId)

    suspend fun createChannel(serverId: String, name: String, category: String? = "KANAŁY TEKSTOWE"): Channel {
        val created = AccordanceApiClient.api.createChannel(serverId, CreateChannelRequest(name, category = category))
        return Channel(created.id, created.name, serverId, category = created.category)
    }

    suspend fun updateChannel(serverId: String, channelId: String, name: String?, topic: String?, category: String?): ApiChannel {
        return AccordanceApiClient.api.updateChannel(serverId, channelId, UpdateChannelRequest(name, topic, category = category))
    }

    suspend fun deleteChannel(serverId: String, channelId: String) = AccordanceApiClient.api.deleteChannel(serverId, channelId)

    suspend fun createRole(serverId: String, name: String, color: String?, position: Int, permissions: RolePermissions): Role {
        val apiPerms = ApiRolePermissions(permissions.manageServer, permissions.manageChannels, permissions.manageRoles, permissions.manageMessages)
        val created = AccordanceApiClient.api.createRole(serverId, CreateRoleRequest(name, color, position, apiPerms))
        return Role(created.id, created.name, created.color, created.position, permissions)
    }

    suspend fun updateRole(serverId: String, roleId: String, name: String?, color: String?, position: Int?, permissions: RolePermissions?): Role {
        val apiPerms = permissions?.let { ApiRolePermissions(it.manageServer, it.manageChannels, it.manageRoles, it.manageMessages) }
        val updated = AccordanceApiClient.api.updateRole(serverId, roleId, UpdateRoleRequest(name, color, position, apiPerms))
        return Role(updated.id, updated.name, updated.color, updated.position, permissions ?: RolePermissions())
    }

    suspend fun deleteRole(serverId: String, roleId: String) = AccordanceApiClient.api.deleteRole(serverId, roleId)

    suspend fun getChannelRoleOverrides(serverId: String, channelId: String): Map<String, RolePermissionsOverride> {
        val overrides = AccordanceApiClient.api.getChannelRoleOverrides(serverId, channelId)
        return overrides.associate { 
            it.roleId to RolePermissionsOverride(
                it.permissions.manageServer, it.permissions.manageChannels, 
                it.permissions.manageRoles, it.permissions.manageMessages
            )
        }
    }

    suspend fun uploadAttachment(fileName: String, mimeType: String, content: ByteArray): UploadResponse {
        val body = content.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", fileName, body)
        return AccordanceApiClient.api.uploadFile(part)
    }
}

data class BackendBootstrap(
    val servers: List<Server>,
    val conversations: Map<String, SnapshotStateList<Message>>,
    val channelSettings: Map<String, ChannelSettings>
)
