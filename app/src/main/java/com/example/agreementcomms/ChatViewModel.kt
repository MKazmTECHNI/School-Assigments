package com.example.agreementcomms

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agreementcomms.data.ApiAttachmentRequest
import com.example.agreementcomms.data.ChatRepository
import com.example.agreementcomms.data.SettingsStore
import com.example.agreementcomms.data.UserSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ChatUiState(
    val isLoggedIn: Boolean = false,
    val nickname: String = "",
    val draftNickname: String = "",
    val servers: List<Server> = defaultServers(),
    val selectedServerId: String = defaultServers().first().id,
    val selectedChannel: String = defaultServers().first().channels.first(),
    val section: MainSection = MainSection.Chat,
    val backendConnected: Boolean = false,
    val backendError: String? = null,
    val composerAttachment: ComposerAttachment? = null,
    val settingsDisplayName: String = "",
    val settingsStatusText: String = "Online",
    val settingsPushEnabled: Boolean = true,
    val settingsVibrationEnabled: Boolean = true,
    val settingsCompactModeEnabled: Boolean = false,
    val settingsSavedAtLeastOnce: Boolean = false
)

data class ComposerAttachment(
    val type: AttachmentType,
    val name: String,
    val localUri: String,
    val mimeType: String,
    val sizeLabel: String? = null
)

class ChatViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: ChatRepository = ChatRepository()
    private val settingsStore = SettingsStore(application.applicationContext)

    var uiState by mutableStateOf(ChatUiState())
        private set

    val conversations = mutableStateMapOf<String, SnapshotStateList<Message>>().apply {
        putAll(buildSampleConversations())
    }

    private val channelApiIds = mutableStateMapOf<String, String>()
    private val rolesByServer = mutableStateMapOf<String, SnapshotStateList<Role>>()
    private val actingRoleByServer = mutableStateMapOf<String, String>()

    val unreadCounts = mutableStateMapOf(
        conversationKey("general", "#offtopic") to 3,
        conversationKey("mobile-dev", "#android") to 2,
        conversationKey("szkola", "#terminy") to 1
    )

    private var backendTried = false

    init {
        viewModelScope.launch {
            val loaded = settingsStore.settingsFlow.first()
            uiState = uiState.copy(
                settingsDisplayName = loaded.displayName,
                settingsStatusText = loaded.statusText,
                settingsPushEnabled = loaded.pushEnabled,
                settingsVibrationEnabled = loaded.vibrationEnabled,
                settingsCompactModeEnabled = loaded.compactModeEnabled,
                settingsSavedAtLeastOnce =
                    loaded.displayName.isNotBlank() ||
                        loaded.statusText != "Online" ||
                        !loaded.pushEnabled ||
                        !loaded.vibrationEnabled ||
                        loaded.compactModeEnabled
            )
        }
    }

    fun onDraftNicknameChange(value: String) {
        uiState = uiState.copy(draftNickname = value)
    }

    fun login() {
        val nick = uiState.draftNickname.trim()
        if (nick.isBlank()) return
        uiState = uiState.copy(isLoggedIn = true, nickname = nick)
        bootstrapFromBackendIfNeeded()
    }

    fun setSection(section: MainSection) {
        uiState = uiState.copy(section = section)
    }

    fun onServerSelected(serverId: String) {
        val server = uiState.servers.firstOrNull { it.id == serverId } ?: return
        val firstChannel = server.channels.firstOrNull().orEmpty()
        uiState = uiState.copy(
            selectedServerId = serverId,
            selectedChannel = firstChannel,
            section = MainSection.Chat
        )
        if (firstChannel.isNotBlank()) {
            unreadCounts[conversationKey(serverId, firstChannel)] = 0
        }
        refreshRoles(serverId)
    }

    fun onChannelSelected(channel: String) {
        uiState = uiState.copy(selectedChannel = channel, section = MainSection.Chat)
        unreadCounts[conversationKey(uiState.selectedServerId, channel)] = 0
    }

    fun activeRoles(): List<Role> {
        return rolesByServer[uiState.selectedServerId] ?: emptyList()
    }

    fun activeRoleId(): String {
        return actingRoleByServer[uiState.selectedServerId].orEmpty()
    }

    fun selectActiveRole(roleId: String) {
        val serverId = uiState.selectedServerId
        if (serverId.isBlank() || roleId.isBlank()) return
        actingRoleByServer[serverId] = roleId
    }

    fun canManageServer(): Boolean = activePermission { it.manageServer }
    fun canManageChannels(): Boolean = activePermission { it.manageChannels }
    fun canManageRoles(): Boolean = activePermission { it.manageRoles }
    fun canManageMessages(): Boolean = activePermission { it.manageMessages }

    fun createServer(name: String, icon: String) {
        val cleanedName = name.trim()
        if (cleanedName.isBlank()) return

        viewModelScope.launch {
            try {
                val created = repository.createServer(
                    name = cleanedName,
                    icon = icon.trim().ifBlank { null }
                )
                val defaultChannel = repository.createChannel(created.id, "#ogólny")
                val serverWithChannel = created.copy(channels = listOf(defaultChannel.name))
                val servers = uiState.servers + serverWithChannel
                uiState = uiState.copy(
                    servers = servers,
                    selectedServerId = serverWithChannel.id,
                    selectedChannel = defaultChannel.name,
                    backendConnected = true,
                    backendError = null
                )
                val key = conversationKey(serverWithChannel.id, defaultChannel.name)
                channelApiIds[key] = defaultChannel.id
                conversations.getOrPut(key) { mutableStateListOf() }
                unreadCounts[key] = 0
                refreshRoles(serverWithChannel.id)
            } catch (ex: Exception) {
                uiState = uiState.copy(
                    backendConnected = false,
                    backendError = ex.message ?: "Nie udało się utworzyć serwera"
                )
            }
        }
    }

    fun updateSelectedServer(name: String, icon: String) {
        val serverId = uiState.selectedServerId
        if (serverId.isBlank()) return
        if (!canManageServer()) {
            uiState = uiState.copy(backendError = "Brak uprawnień: manageServer")
            return
        }

        val cleanedName = name.trim()
        val cleanedIcon = icon.trim()
        if (cleanedName.isBlank() || cleanedIcon.isBlank()) return

        viewModelScope.launch {
            try {
                val updated = repository.updateServer(
                    serverId = serverId,
                    name = cleanedName,
                    icon = cleanedIcon,
                    actorRoleId = activeRoleId().ifBlank { null }
                )
                uiState = uiState.copy(
                    servers = uiState.servers.map { server ->
                        if (server.id == serverId) {
                            server.copy(name = updated.name, icon = updated.icon)
                        } else {
                            server
                        }
                    },
                    backendConnected = true,
                    backendError = null
                )
            } catch (ex: Exception) {
                uiState = uiState.copy(
                    backendConnected = false,
                    backendError = ex.message ?: "Nie udało się zaktualizować serwera"
                )
            }
        }
    }

    fun deleteSelectedServer() {
        val serverId = uiState.selectedServerId
        if (serverId.isBlank()) return
        if (!canManageServer()) {
            uiState = uiState.copy(backendError = "Brak uprawnień: manageServer")
            return
        }

        viewModelScope.launch {
            try {
                repository.deleteServer(serverId, activeRoleId().ifBlank { null })

                val remaining = uiState.servers.filterNot { it.id == serverId }
                val nextServer = remaining.firstOrNull()
                val nextChannel = nextServer?.channels?.firstOrNull().orEmpty()

                val keysToRemove = channelApiIds.keys.filter { it.startsWith("$serverId|") }
                keysToRemove.forEach { key ->
                    channelApiIds.remove(key)
                    conversations.remove(key)
                    unreadCounts.remove(key)
                }
                rolesByServer.remove(serverId)
                actingRoleByServer.remove(serverId)

                uiState = uiState.copy(
                    servers = remaining,
                    selectedServerId = nextServer?.id.orEmpty(),
                    selectedChannel = nextChannel,
                    backendConnected = true,
                    backendError = null
                )
            } catch (ex: Exception) {
                uiState = uiState.copy(
                    backendConnected = false,
                    backendError = ex.message ?: "Nie udało się usunąć serwera"
                )
            }
        }
    }

    fun createChannel(name: String) {
        val serverId = uiState.selectedServerId
        val cleanedName = name.trim()
        if (serverId.isBlank() || cleanedName.isBlank()) return
        if (!canManageChannels()) {
            uiState = uiState.copy(backendError = "Brak uprawnień: manageChannels")
            return
        }

        viewModelScope.launch {
            try {
                val created = repository.createChannel(serverId, cleanedName, activeRoleId().ifBlank { null })
                uiState = uiState.copy(
                    servers = uiState.servers.map { server ->
                        if (server.id == serverId) {
                            server.copy(channels = (server.channels + created.name).distinct())
                        } else {
                            server
                        }
                    },
                    selectedChannel = if (uiState.selectedChannel.isBlank()) created.name else uiState.selectedChannel,
                    backendConnected = true,
                    backendError = null
                )
                val key = conversationKey(serverId, created.name)
                channelApiIds[key] = created.id
                conversations.getOrPut(key) { mutableStateListOf() }
                unreadCounts[key] = 0
            } catch (ex: Exception) {
                uiState = uiState.copy(
                    backendConnected = false,
                    backendError = ex.message ?: "Nie udało się utworzyć kanału"
                )
            }
        }
    }

    fun renameSelectedChannel(newName: String) {
        val serverId = uiState.selectedServerId
        val channelName = uiState.selectedChannel
        val cleanedName = newName.trim()
        if (serverId.isBlank() || channelName.isBlank() || cleanedName.isBlank()) return
        if (!canManageChannels()) {
            uiState = uiState.copy(backendError = "Brak uprawnień: manageChannels")
            return
        }

        val oldKey = conversationKey(serverId, channelName)
        val channelId = channelApiIds[oldKey] ?: return

        viewModelScope.launch {
            try {
                val updated = repository.updateChannel(
                    serverId = serverId,
                    channelId = channelId,
                    name = cleanedName,
                    actorRoleId = activeRoleId().ifBlank { null }
                )
                val newKey = conversationKey(serverId, updated.name)

                val movedConversation = conversations.remove(oldKey)
                if (movedConversation != null) {
                    conversations[newKey] = movedConversation
                }
                val unread = unreadCounts.remove(oldKey)
                if (unread != null) {
                    unreadCounts[newKey] = unread
                }
                channelApiIds.remove(oldKey)
                channelApiIds[newKey] = updated.id

                uiState = uiState.copy(
                    servers = uiState.servers.map { server ->
                        if (server.id == serverId) {
                            server.copy(channels = server.channels.map { if (it == channelName) updated.name else it })
                        } else {
                            server
                        }
                    },
                    selectedChannel = updated.name,
                    backendConnected = true,
                    backendError = null
                )
            } catch (ex: Exception) {
                uiState = uiState.copy(
                    backendConnected = false,
                    backendError = ex.message ?: "Nie udało się zmienić nazwy kanału"
                )
            }
        }
    }

    fun deleteSelectedChannel() {
        val serverId = uiState.selectedServerId
        val channelName = uiState.selectedChannel
        if (serverId.isBlank() || channelName.isBlank()) return
        if (!canManageChannels()) {
            uiState = uiState.copy(backendError = "Brak uprawnień: manageChannels")
            return
        }

        val oldKey = conversationKey(serverId, channelName)
        val channelId = channelApiIds[oldKey] ?: return

        viewModelScope.launch {
            try {
                repository.deleteChannel(serverId, channelId, activeRoleId().ifBlank { null })

                val updatedServers = uiState.servers.map { server ->
                    if (server.id == serverId) {
                        server.copy(channels = server.channels.filterNot { it == channelName })
                    } else {
                        server
                    }
                }
                val updatedServer = updatedServers.firstOrNull { it.id == serverId }
                val nextChannel = updatedServer?.channels?.firstOrNull().orEmpty()

                channelApiIds.remove(oldKey)
                conversations.remove(oldKey)
                unreadCounts.remove(oldKey)

                uiState = uiState.copy(
                    servers = updatedServers,
                    selectedChannel = nextChannel,
                    backendConnected = true,
                    backendError = null
                )
            } catch (ex: Exception) {
                uiState = uiState.copy(
                    backendConnected = false,
                    backendError = ex.message ?: "Nie udało się usunąć kanału"
                )
            }
        }
    }

    fun createRole(name: String, color: String?, position: Int, permissions: RolePermissions = RolePermissions()) {
        val serverId = uiState.selectedServerId
        val cleanedName = name.trim()
        if (serverId.isBlank() || cleanedName.isBlank()) return
        if (!canManageRoles()) {
            uiState = uiState.copy(backendError = "Brak uprawnień: manageRoles")
            return
        }

        viewModelScope.launch {
            try {
                val created = repository.createRole(
                    serverId = serverId,
                    name = cleanedName,
                    color = color?.trim()?.ifBlank { null },
                    position = position,
                    permissions = permissions,
                    actorRoleId = activeRoleId().ifBlank { null }
                )
                val list = rolesByServer.getOrPut(serverId) { mutableStateListOf() }
                list.add(created)
                sortRoles(list)
                uiState = uiState.copy(backendConnected = true, backendError = null)
            } catch (ex: Exception) {
                uiState = uiState.copy(
                    backendConnected = false,
                    backendError = ex.message ?: "Nie udało się utworzyć roli"
                )
            }
        }
    }

    fun updateRole(
        roleId: String,
        name: String,
        color: String?,
        position: Int?,
        permissions: RolePermissions? = null
    ) {
        val serverId = uiState.selectedServerId
        val cleanedName = name.trim()
        if (serverId.isBlank() || roleId.isBlank() || cleanedName.isBlank()) return
        if (!canManageRoles()) {
            uiState = uiState.copy(backendError = "Brak uprawnień: manageRoles")
            return
        }

        viewModelScope.launch {
            try {
                val updated = repository.updateRole(
                    serverId = serverId,
                    roleId = roleId,
                    name = cleanedName,
                    color = color?.trim()?.ifBlank { null },
                    position = position,
                    permissions = permissions,
                    actorRoleId = activeRoleId().ifBlank { null }
                )
                val list = rolesByServer.getOrPut(serverId) { mutableStateListOf() }
                val index = list.indexOfFirst { it.id == roleId }
                if (index >= 0) {
                    list[index] = updated
                } else {
                    list.add(updated)
                }
                sortRoles(list)
                uiState = uiState.copy(backendConnected = true, backendError = null)
            } catch (ex: Exception) {
                uiState = uiState.copy(
                    backendConnected = false,
                    backendError = ex.message ?: "Nie udało się zaktualizować roli"
                )
            }
        }
    }

    fun deleteRole(roleId: String) {
        val serverId = uiState.selectedServerId
        if (serverId.isBlank() || roleId.isBlank()) return
        if (!canManageRoles()) {
            uiState = uiState.copy(backendError = "Brak uprawnień: manageRoles")
            return
        }

        viewModelScope.launch {
            try {
                repository.deleteRole(serverId, roleId, activeRoleId().ifBlank { null })
                rolesByServer[serverId]?.removeAll { it.id == roleId }
                uiState = uiState.copy(backendConnected = true, backendError = null)
            } catch (ex: Exception) {
                uiState = uiState.copy(
                    backendConnected = false,
                    backendError = ex.message ?: "Nie udało się usunąć roli"
                )
            }
        }
    }

    fun setComposerAttachment(attachment: ComposerAttachment?) {
        uiState = uiState.copy(composerAttachment = attachment)
    }

    fun onSettingsDisplayNameChange(value: String) {
        uiState = uiState.copy(settingsDisplayName = value)
    }

    fun onSettingsStatusTextChange(value: String) {
        uiState = uiState.copy(settingsStatusText = value)
    }

    fun onSettingsPushEnabledChange(enabled: Boolean) {
        uiState = uiState.copy(settingsPushEnabled = enabled)
    }

    fun onSettingsVibrationEnabledChange(enabled: Boolean) {
        uiState = uiState.copy(settingsVibrationEnabled = enabled)
    }

    fun onSettingsCompactModeEnabledChange(enabled: Boolean) {
        uiState = uiState.copy(settingsCompactModeEnabled = enabled)
    }

    fun saveSettings() {
        viewModelScope.launch {
            settingsStore.save(
                UserSettings(
                    displayName = uiState.settingsDisplayName,
                    statusText = uiState.settingsStatusText,
                    pushEnabled = uiState.settingsPushEnabled,
                    vibrationEnabled = uiState.settingsVibrationEnabled,
                    compactModeEnabled = uiState.settingsCompactModeEnabled
                )
            )
            uiState = uiState.copy(settingsSavedAtLeastOnce = true)
        }
    }

    fun sendMessage(
        text: String,
        attachmentBytes: ByteArray? = null,
        attachmentFileName: String? = null,
        attachmentMimeType: String? = null
    ) {
        val messageText = text.trim()
        val localAttachment = uiState.composerAttachment
        if (messageText.isBlank() && localAttachment == null) return
        if (uiState.selectedChannel.isBlank()) return
        if (!canManageMessages()) {
            uiState = uiState.copy(backendError = "Brak uprawnień: manageMessages")
            return
        }

        val finalText = if (messageText.isBlank()) "Załącznik" else messageText
        val convKey = conversationKey(uiState.selectedServerId, uiState.selectedChannel)
        conversations.getOrPut(convKey) { mutableStateListOf() }.add(
            Message(
                author = uiState.nickname,
                text = finalText,
                time = "teraz",
                isMine = true,
                attachments = localAttachment?.let {
                    listOf(
                        MessageAttachment(
                            type = it.type,
                            name = it.name,
                            url = it.localUri,
                            meta = it.sizeLabel
                        )
                    )
                } ?: emptyList()
            )
        )

        uiState = uiState.copy(composerAttachment = null)

        val apiChannelId = channelApiIds[convKey] ?: return
        viewModelScope.launch {
            try {
                val uploadedAttachment = if (
                    attachmentBytes != null &&
                    attachmentFileName != null &&
                    attachmentMimeType != null
                ) {
                    repository.uploadAttachment(
                        fileName = attachmentFileName,
                        mimeType = attachmentMimeType,
                        content = attachmentBytes
                    )
                } else {
                    null
                }

                repository.sendMessage(
                    serverId = uiState.selectedServerId,
                    channelId = apiChannelId,
                    author = uiState.nickname,
                    text = finalText,
                    attachment = uploadedAttachment?.let {
                        ApiAttachmentRequest(
                            type = it.type,
                            name = it.name,
                            path = it.path
                        )
                    },
                    actorRoleId = activeRoleId().ifBlank { null }
                )
                uiState = uiState.copy(backendConnected = true, backendError = null)
            } catch (ex: Exception) {
                uiState = uiState.copy(
                    backendConnected = false,
                    backendError = ex.message ?: "Wysłanie do backendu nie powiodło się"
                )
            }
        }
    }

    fun activeMessages(): List<Message> {
        val key = conversationKey(uiState.selectedServerId, uiState.selectedChannel)
        return conversations.getOrPut(key) { mutableStateListOf() }
    }

    private fun bootstrapFromBackendIfNeeded() {
        if (backendTried) return
        backendTried = true

        viewModelScope.launch {
            try {
                val payload = repository.fetchBackendBootstrap(uiState.nickname)
                if (payload.servers.isNotEmpty()) {
                    conversations.clear()
                    conversations.putAll(payload.conversations)
                    channelApiIds.clear()
                    channelApiIds.putAll(payload.channelApiIds)
                    rolesByServer.clear()

                    uiState = uiState.copy(
                        servers = payload.servers,
                        selectedServerId = payload.servers.first().id,
                        selectedChannel = payload.servers.first().channels.firstOrNull().orEmpty(),
                        backendConnected = true,
                        backendError = null
                    )
                    payload.servers.forEach { server ->
                        refreshRoles(server.id)
                    }
                }
            } catch (ex: Exception) {
                uiState = uiState.copy(
                    backendConnected = false,
                    backendError = ex.message ?: "Nie udało się połączyć z backendem"
                )
            }
        }
    }

    private fun refreshRoles(serverId: String) {
        if (serverId.isBlank()) return
        viewModelScope.launch {
            try {
                val roles = repository.getRoles(serverId)
                val sorted = roles.sortedByDescending { it.position }
                rolesByServer[serverId] = mutableStateListOf<Role>().apply { addAll(sorted) }
                val selectedRoleId = actingRoleByServer[serverId]
                if (selectedRoleId.isNullOrBlank() || sorted.none { it.id == selectedRoleId }) {
                    actingRoleByServer[serverId] = sorted.firstOrNull()?.id.orEmpty()
                }
                uiState = uiState.copy(backendConnected = true, backendError = null)
            } catch (_: Exception) {
                // Keep chat functional even if roles endpoint is unavailable.
            }
        }
    }

    private fun activePermission(selector: (RolePermissions) -> Boolean): Boolean {
        if (activeRoles().isEmpty()) return true
        val roleId = activeRoleId()
        if (roleId.isBlank()) return true
        val role = activeRoles().firstOrNull { it.id == roleId } ?: return false
        return selector(role.permissions)
    }

    private fun sortRoles(list: SnapshotStateList<Role>) {
        val sorted = list.sortedByDescending { it.position }
        list.clear()
        list.addAll(sorted)
    }
}
