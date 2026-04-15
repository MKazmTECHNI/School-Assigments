package com.example.agreementcomms

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.agreementcomms.data.ApiAttachmentRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
        val firstChannel = server.channels.firstOrNull() ?: return

        uiState = uiState.copy(
            selectedServerId = serverId,
            selectedChannel = firstChannel,
            section = MainSection.Chat
        )
        unreadCounts[conversationKey(serverId, firstChannel)] = 0
    }

    fun onChannelSelected(channel: String) {
        uiState = uiState.copy(selectedChannel = channel, section = MainSection.Chat)
        unreadCounts[conversationKey(uiState.selectedServerId, channel)] = 0
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

        val finalText = if (messageText.isBlank()) "Załącznik" else messageText

        val convKey = conversationKey(uiState.selectedServerId, uiState.selectedChannel)
        conversations.getOrPut(convKey) { androidx.compose.runtime.mutableStateListOf() }
            .add(
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
                    }
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
        return conversations.getOrPut(key) { androidx.compose.runtime.mutableStateListOf() }
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

                    uiState = uiState.copy(
                        servers = payload.servers,
                        selectedServerId = payload.servers.first().id,
                        selectedChannel = payload.servers.first().channels.firstOrNull().orEmpty(),
                        backendConnected = true,
                        backendError = null
                    )
                }
            } catch (ex: Exception) {
                uiState = uiState.copy(
                    backendConnected = false,
                    backendError = ex.message ?: "Nie udało się połączyć z backendem"
                )
            }
        }
    }
}
