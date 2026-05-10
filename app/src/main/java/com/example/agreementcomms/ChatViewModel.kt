package com.example.agreementcomms

import android.app.Application
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agreementcomms.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

data class ChatUiState(
    val isLoggedIn: Boolean = false,
    val userId: String = "",
    val token: String = "",
    val nickname: String = "",
    val avatarUrl: String? = null,
    val bio: String? = null,
    val draftNickname: String = "",
    val draftPassword: String = "",
    val isRegisterMode: Boolean = false,
    val servers: List<Server> = emptyList(),
    val selectedServerId: String = "",
    val selectedChannelId: String = "",
    val section: MainSection = MainSection.Chat,
    val backendConnected: Boolean = false,
    val backendError: String? = null,
    val isLoading: Boolean = false,
    val settingsDisplayName: String = "",
    val settingsStatusText: String = "Online",
    val settingsBio: String = "",
    val settingsAvatarUrl: String = "",
    val typingUsers: Map<String, String> = emptyMap(),
    val members: List<ApiMember> = emptyList(),
    val serverRoles: List<Role> = emptyList(),
    val channelOverrides: Map<String, RolePermissionsOverride> = emptyMap(),
    // Preferences
    val compactMode: Boolean = false,
    val pushEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository()
    private val settingsStore = SettingsStore(application.applicationContext)
    private val socketHandler = SocketHandler()
    private val encryptionKey = "AccordanceKey123".toByteArray() 

    var uiState by mutableStateOf(ChatUiState())
        private set

    private val _errorEvents = MutableSharedFlow<String>()
    val errorEvents = _errorEvents.asSharedFlow()

    val conversations = mutableStateMapOf<String, SnapshotStateList<Message>>()
    val unreadCounts = mutableStateMapOf<String, Int>()
    private val typingMap = mutableStateMapOf<String, MutableSet<String>>()

    init {
        viewModelScope.launch {
            val loaded = settingsStore.settingsFlow.first()
            if (loaded.token.isNotBlank()) {
                AccordanceApiClient.authInterceptor.token = loaded.token
                uiState = uiState.copy(
                    isLoggedIn = true, userId = loaded.userId, token = loaded.token,
                    nickname = loaded.username, settingsDisplayName = loaded.displayName,
                    settingsStatusText = loaded.statusText, settingsBio = loaded.bio,
                    settingsAvatarUrl = loaded.avatarUrl,
                    compactMode = loaded.compactModeEnabled,
                    pushEnabled = loaded.pushEnabled,
                    vibrationEnabled = loaded.vibrationEnabled
                )
                initSocket()
                bootstrapFromBackend()
            }
        }
    }

    private fun initSocket() {
        socketHandler.establishConnection()
        val socket = socketHandler.getSocket() ?: return
        
        socket.on(io.socket.client.Socket.EVENT_CONNECT) {
            socket.emit("authenticate", JSONObject().apply { put("token", uiState.token) })
            viewModelScope.launch { uiState = uiState.copy(backendConnected = true) }
        }

        socket.on(io.socket.client.Socket.EVENT_DISCONNECT) {
            viewModelScope.launch { uiState = uiState.copy(backendConnected = false) }
        }

        socketHandler.onNewMessage { data -> handleIncomingSocketMessage(data) }

        socket.on("user_typing") { args ->
            val data = args[0] as JSONObject
            val cid = data.getString("channelId")
            val username = data.getString("username")
            if (data.getString("userId") != uiState.userId) {
                viewModelScope.launch {
                    val users = typingMap.getOrPut(cid) { mutableSetOf() }
                    users.add(username)
                    updateTypingState()
                    delay(4000)
                    users.remove(username)
                    updateTypingState()
                }
            }
        }

        socket.on("presence_update") { args ->
            val data = args[0] as JSONObject
            val uid = data.getString("userId")
            val isOnline = data.getBoolean("isOnline")
            viewModelScope.launch {
                uiState = uiState.copy(members = uiState.members.map {
                    if (it.userId == uid) it.copy(isOnline = isOnline) else it
                })
            }
        }
        socket.connect()
    }

    private fun handleIncomingSocketMessage(data: JSONObject) {
        val cid = data.getString("channelId")
        val encryptedText = data.getString("text")
        val decryptedText = try { decrypt(encryptedText) } catch (e: Exception) { encryptedText }

        val msg = Message(
            id = data.optString("id", data.optInt("id", 0).toString()),
            author = data.getString("author"),
            text = decryptedText,
            time = data.getString("time"),
            isMine = data.getString("authorId") == uiState.userId
        )
        viewModelScope.launch {
            val list = conversations.getOrPut(cid) { mutableStateListOf() }
            if (list.none { it.id == msg.id }) {
                list.add(msg)
                if (cid != uiState.selectedChannelId) {
                    unreadCounts[cid] = (unreadCounts[cid] ?: 0) + 1
                }
            }
        }
    }

    private fun updateTypingState() {
        uiState = uiState.copy(typingUsers = typingMap.mapValues { if (it.value.isEmpty()) "" else it.value.joinToString(", ") + " pisze..." })
    }

    fun login() {
        if (uiState.draftNickname.isBlank() || uiState.draftPassword.isBlank()) return
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val resp = repository.login(uiState.draftNickname, uiState.draftPassword)
                AccordanceApiClient.authInterceptor.token = resp.token
                uiState = uiState.copy(
                    isLoggedIn = true, token = resp.token, userId = resp.user.id, 
                    nickname = resp.user.username, settingsDisplayName = resp.user.displayName ?: resp.user.username,
                    settingsStatusText = resp.user.statusText ?: "Online",
                    settingsBio = resp.user.bio ?: "", settingsAvatarUrl = resp.user.avatarUrl ?: ""
                )
                saveSettings()
                initSocket()
                bootstrapFromBackend()
            } catch (e: Exception) {
                _errorEvents.emit("Logowanie nieudane: ${e.message}")
            } finally {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun register() {
        if (uiState.draftNickname.isBlank() || uiState.draftPassword.isBlank()) return
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val resp = repository.register(uiState.draftNickname, uiState.draftPassword)
                AccordanceApiClient.authInterceptor.token = resp.token
                uiState = uiState.copy(
                    isLoggedIn = true, token = resp.token, userId = resp.user.id, 
                    nickname = resp.user.username, settingsDisplayName = resp.user.username,
                    settingsStatusText = "Online", settingsBio = "", settingsAvatarUrl = ""
                )
                saveSettings()
                _errorEvents.emit("Zarejestrowano pomyślnie!")
                initSocket()
                bootstrapFromBackend()
            } catch (e: Exception) {
                _errorEvents.emit("Rejestracja nieudana: ${e.message}")
            } finally {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun joinServer(inviteCode: String) = viewModelScope.launch {
        uiState = uiState.copy(isLoading = true)
        try {
            repository.joinServer(inviteCode)
            _errorEvents.emit("Dołączono do serwera!")
            bootstrapFromBackend()
        } catch (e: Exception) { _errorEvents.emit("Nieprawidłowy kod zaproszenia") }
        finally { uiState = uiState.copy(isLoading = false) }
    }

    fun createServer(name: String) = viewModelScope.launch {
        uiState = uiState.copy(isLoading = true)
        try {
            repository.createServer(name)
            bootstrapFromBackend()
        } catch (e: Exception) { _errorEvents.emit("Błąd tworzenia serwera: ${e.message}") }
        finally { uiState = uiState.copy(isLoading = false) }
    }

    fun onServerSelected(serverId: String) {
        val server = uiState.servers.find { it.id == serverId } ?: return
        uiState = uiState.copy(selectedServerId = serverId, selectedChannelId = server.channels.firstOrNull()?.id ?: "")
        refreshMembers(serverId)
        refreshRoles(serverId)
        refreshChannelOverrides()
        socketHandler.joinChannel(uiState.selectedChannelId)
    }

    fun onChannelSelected(channelId: String) {
        uiState = uiState.copy(selectedChannelId = channelId)
        refreshChannelOverrides()
        socketHandler.joinChannel(channelId)
        unreadCounts[channelId] = 0
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || uiState.selectedChannelId.isBlank()) return
        if (!canSendMessages()) {
            viewModelScope.launch { _errorEvents.emit("Brak uprawnień do pisania") }
            return
        }
        val encrypted = encrypt(text)
        socketHandler.sendMessage(uiState.token, uiState.selectedServerId, uiState.selectedChannelId, encrypted)
    }

    fun sendTyping() {
        if (uiState.selectedChannelId.isBlank()) return
        socketHandler.getSocket()?.emit("typing_start", JSONObject().apply {
            put("channelId", uiState.selectedChannelId)
        })
    }

    private fun bootstrapFromBackend() {
        viewModelScope.launch {
            try {
                val payload = repository.fetchBackendBootstrap(uiState.nickname, uiState.userId)
                conversations.putAll(payload.conversations)
                uiState = uiState.copy(
                    servers = payload.servers,
                    selectedServerId = uiState.selectedServerId.ifBlank { payload.servers.firstOrNull()?.id ?: "" },
                    selectedChannelId = uiState.selectedChannelId.ifBlank { payload.servers.firstOrNull()?.channels?.firstOrNull()?.id ?: "" },
                    backendConnected = true,
                    backendError = null
                )
                if (uiState.selectedServerId.isNotBlank()) {
                    refreshMembers(uiState.selectedServerId)
                    refreshRoles(uiState.selectedServerId)
                    refreshChannelOverrides()
                }
                if (uiState.selectedChannelId.isNotBlank()) {
                    socketHandler.joinChannel(uiState.selectedChannelId)
                }
            } catch (e: Exception) {
                uiState = uiState.copy(backendConnected = false, backendError = e.message)
            }
        }
    }

    private fun refreshMembers(sid: String) = viewModelScope.launch {
        try { uiState = uiState.copy(members = repository.getMembers(sid)) } catch (_: Exception) {}
    }

    private fun refreshRoles(sid: String) = viewModelScope.launch {
        try { uiState = uiState.copy(serverRoles = repository.getRoles(sid)) } catch (_: Exception) {}
    }

    private fun refreshChannelOverrides() = viewModelScope.launch {
        if (uiState.selectedServerId.isBlank() || uiState.selectedChannelId.isBlank()) return@launch
        try {
            val ovs = repository.getChannelRoleOverrides(uiState.selectedServerId, uiState.selectedChannelId)
            uiState = uiState.copy(channelOverrides = ovs)
        } catch (_: Exception) {}
    }

    // --- PERMISSIONS Logic ---
    private fun getEffectivePermissions(): RolePermissions {
        val server = uiState.servers.find { it.id == uiState.selectedServerId }
        if (server?.ownerId == uiState.userId) {
            return RolePermissions(true, true, true, true)
        }
        
        val member = uiState.members.find { it.userId == uiState.userId } ?: return RolePermissions()
        val roleIds = member.roles.map { it.id }
        val roles = uiState.serverRoles.filter { it.id in roleIds }
        
        val base = RolePermissions(
            manageServer = roles.any { it.permissions.manageServer },
            manageChannels = roles.any { it.permissions.manageChannels },
            manageRoles = roles.any { it.permissions.manageRoles },
            manageMessages = roles.any { it.permissions.manageMessages }
        )
        
        val overrides = uiState.channelOverrides.filter { it.key in roleIds }.values
        return RolePermissions(
            manageServer = overrides.mapNotNull { it.manageServer }.firstOrNull() ?: base.manageServer,
            manageChannels = overrides.mapNotNull { it.manageChannels }.firstOrNull() ?: base.manageChannels,
            manageRoles = overrides.mapNotNull { it.manageRoles }.firstOrNull() ?: base.manageRoles,
            manageMessages = overrides.mapNotNull { it.manageMessages }.firstOrNull() ?: base.manageMessages
        )
    }

    fun canManageServer() = getEffectivePermissions().manageServer
    fun canManageChannels() = getEffectivePermissions().manageChannels
    fun canManageRoles() = getEffectivePermissions().manageRoles
    fun canSendMessages() = getEffectivePermissions().manageMessages

    // --- Actions ---
    fun assignRole(uid: String, rid: String) = viewModelScope.launch {
        if (!canManageRoles()) return@launch
        try { repository.addRoleToMember(uiState.selectedServerId, uid, rid); refreshMembers(uiState.selectedServerId) }
        catch (e: Exception) { _errorEvents.emit("Błąd uprawnień") }
    }

    fun removeRole(uid: String, rid: String) = viewModelScope.launch {
        if (!canManageRoles()) return@launch
        try { repository.removeRoleFromMember(uiState.selectedServerId, uid, rid); refreshMembers(uiState.selectedServerId) }
        catch (e: Exception) { _errorEvents.emit("Błąd uprawnień") }
    }

    fun createChannel(serverId: String, name: String) = viewModelScope.launch {
        if (!canManageChannels()) return@launch
        uiState = uiState.copy(isLoading = true)
        try {
            repository.createChannel(serverId, name)
            bootstrapFromBackend()
        } catch (e: Exception) { _errorEvents.emit("Błąd tworzenia kanału") }
        finally { uiState = uiState.copy(isLoading = false) }
    }

    fun deleteChannel(channelId: String) = viewModelScope.launch {
        if (!canManageChannels()) return@launch
        uiState = uiState.copy(isLoading = true)
        try {
            repository.deleteChannel(uiState.selectedServerId, channelId)
            bootstrapFromBackend()
        } catch (e: Exception) { _errorEvents.emit("Błąd usuwania kanału") }
        finally { uiState = uiState.copy(isLoading = false) }
    }

    fun deleteServer() = viewModelScope.launch {
        if (!canManageServer()) return@launch
        uiState = uiState.copy(isLoading = true)
        try {
            repository.deleteServer(uiState.selectedServerId)
            uiState = uiState.copy(selectedServerId = "", selectedChannelId = "")
            bootstrapFromBackend()
            _errorEvents.emit("Serwer został usunięty")
        } catch (e: Exception) { _errorEvents.emit("Błąd usuwania serwera") }
        finally { uiState = uiState.copy(isLoading = false) }
    }

    fun saveProfile() = viewModelScope.launch {
        uiState = uiState.copy(isLoading = true)
        try {
            repository.updateProfile(uiState.settingsDisplayName, uiState.settingsStatusText, uiState.settingsBio, uiState.settingsAvatarUrl)
            saveSettings()
            _errorEvents.emit("Profil zaktualizowany")
        } catch (e: Exception) { _errorEvents.emit("Błąd zapisu") }
        finally { uiState = uiState.copy(isLoading = false) }
    }

    private fun saveSettings() = viewModelScope.launch {
        settingsStore.save(UserSettings(
            token = uiState.token, userId = uiState.userId, username = uiState.nickname,
            displayName = uiState.settingsDisplayName, statusText = uiState.settingsStatusText,
            bio = uiState.settingsBio, avatarUrl = uiState.settingsAvatarUrl,
            compactModeEnabled = uiState.compactMode, pushEnabled = uiState.pushEnabled,
            vibrationEnabled = uiState.vibrationEnabled
        ))
    }

    fun toggleCompactMode() { uiState = uiState.copy(compactMode = !uiState.compactMode); saveSettings() }
    fun togglePush() { uiState = uiState.copy(pushEnabled = !uiState.pushEnabled); saveSettings() }
    fun toggleVibration() { uiState = uiState.copy(vibrationEnabled = !uiState.vibrationEnabled); saveSettings() }

    fun logout() {
        viewModelScope.launch {
            settingsStore.clearAuth()
            socketHandler.closeConnection()
            uiState = ChatUiState()
            conversations.clear()
        }
    }

    private fun encrypt(text: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(encryptionKey, "AES"))
        return Base64.getEncoder().encodeToString(cipher.doFinal(text.toByteArray()))
    }

    private fun decrypt(encrypted: String): String {
        return try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(encryptionKey, "AES"))
            String(cipher.doFinal(Base64.getDecoder().decode(encrypted)))
        } catch (e: Exception) { encrypted }
    }

    fun onSettingsDisplayNameChange(v: String) { uiState = uiState.copy(settingsDisplayName = v) }
    fun onSettingsStatusTextChange(v: String) { uiState = uiState.copy(settingsStatusText = v) }
    fun onSettingsBioChange(v: String) { uiState = uiState.copy(settingsBio = v) }
    fun onSettingsAvatarUrlChange(v: String) { uiState = uiState.copy(settingsAvatarUrl = v) }
    fun onDraftNicknameChange(v: String) { uiState = uiState.copy(draftNickname = v) }
    fun onDraftPasswordChange(v: String) { uiState = uiState.copy(draftPassword = v) }
    fun toggleRegisterMode() { uiState = uiState.copy(isRegisterMode = !uiState.isRegisterMode) }
    fun setSection(s: MainSection) { uiState = uiState.copy(section = s) }
}
