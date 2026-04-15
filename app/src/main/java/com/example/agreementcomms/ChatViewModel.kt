package com.example.agreementcomms

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agreementcomms.data.ChatRepository
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
    val backendError: String? = null
)

class ChatViewModel(
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {

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

    fun sendMessage(text: String) {
        val messageText = text.trim()
        if (messageText.isBlank()) return

        val convKey = conversationKey(uiState.selectedServerId, uiState.selectedChannel)
        conversations.getOrPut(convKey) { androidx.compose.runtime.mutableStateListOf() }
            .add(
                Message(
                    author = uiState.nickname,
                    text = messageText,
                    time = "teraz",
                    isMine = true
                )
            )

        val apiChannelId = channelApiIds[convKey] ?: return
        viewModelScope.launch {
            try {
                repository.sendMessage(
                    serverId = uiState.selectedServerId,
                    channelId = apiChannelId,
                    author = uiState.nickname,
                    text = messageText
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
