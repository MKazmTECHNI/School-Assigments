package com.example.agreementcomms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AgreementApp() {
    val vm: ChatViewModel = viewModel()
    val state = vm.uiState
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.errorEvents.collectLatest { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (!state.isLoggedIn) {
                LoginScreen(
                    nickname = state.draftNickname,
                    onNicknameChange = vm::onDraftNicknameChange,
                    password = state.draftPassword,
                    onPasswordChange = vm::onDraftPasswordChange,
                    isRegisterMode = state.isRegisterMode,
                    onToggleMode = vm::toggleRegisterMode,
                    onEnter = { if (state.isRegisterMode) vm.register() else vm.login() },
                    isLoading = state.isLoading
                )
            } else {
                MainScreen(
                    nickname = state.nickname,
                    servers = state.servers,
                    section = state.section,
                    onSectionChange = vm::setSection,
                    selectedServerId = state.selectedServerId,
                    onServerSelected = vm::onServerSelected,
                    selectedChannelId = state.selectedChannelId,
                    onChannelSelected = vm::onChannelSelected,
                    unreadCounts = vm.unreadCounts,
                    messages = vm.conversations[state.selectedChannelId] ?: emptyList(),
                    onSendMessage = vm::sendMessage,
                    onTyping = vm::sendTyping,
                    typingText = state.typingUsers[state.selectedChannelId],
                    members = state.members,
                    isLoading = state.isLoading,
                    onLogout = vm::logout,
                    displayName = state.settingsDisplayName,
                    statusText = state.settingsStatusText,
                    bio = state.settingsBio,
                    avatarUrl = state.settingsAvatarUrl,
                    onSaveProfile = vm::saveProfile,
                    onDisplayNameChange = vm::onSettingsDisplayNameChange,
                    onStatusTextChange = vm::onSettingsStatusTextChange,
                    onBioChange = vm::onSettingsBioChange,
                    onAvatarUrlChange = vm::onSettingsAvatarUrlChange,
                    roles = state.serverRoles,
                    canManageServer = vm.canManageServer(),
                    onAssignRole = vm::assignRole,
                    onRemoveRole = vm::removeRole,
                    onCreateServer = vm::createServer,
                    onJoinServer = vm::joinServer,
                    onCreateChannel = vm::createChannel,
                    onDeleteChannel = vm::deleteChannel,
                    onDeleteServer = vm::deleteServer,
                    // Preferences
                    compactMode = state.compactMode,
                    onToggleCompactMode = vm::toggleCompactMode,
                    pushEnabled = state.pushEnabled,
                    onTogglePush = vm::togglePush,
                    vibrationEnabled = state.vibrationEnabled,
                    onToggleVibration = vm::toggleVibration,
                    // Server Editing
                    draftServerName = state.draftServerName,
                    onDraftServerNameChange = vm::onDraftServerNameChange,
                    draftServerIcon = state.draftServerIcon,
                    onDraftServerIconChange = vm::onDraftServerIconChange,
                    onSaveServer = vm::saveServerSettings,
                    // Channel Editing
                    draftChannelName = state.draftChannelName,
                    onDraftChannelNameChange = vm::onDraftChannelNameChange,
                    draftChannelTopic = state.draftChannelTopic,
                    onDraftChannelTopicChange = vm::onDraftChannelTopicChange,
                    draftChannelCategory = state.draftChannelCategory,
                    onDraftChannelCategoryChange = vm::onDraftChannelCategoryChange,
                    draftChannelSlowmode = state.draftChannelSlowmode,
                    onDraftChannelSlowmodeChange = vm::onDraftChannelSlowmodeChange,
                    draftChannelNsfw = state.draftChannelNsfw,
                    onDraftChannelNsfwChange = vm::onDraftChannelNsfwChange,
                    onSaveChannel = vm::saveChannelSettings,
                    // Role Editing
                    selectedRoleId = state.selectedRoleId,
                    draftRoleName = state.draftRoleName,
                    onDraftRoleNameChange = vm::onDraftRoleNameChange,
                    draftRolePermissions = state.draftRolePermissions,
                    onDraftRolePermissionsChange = vm::onDraftRolePermissionsChange,
                    onRoleDraftSelect = vm::onRoleDraftSelect,
                    onSaveRole = vm::saveRoleSettings,
                    onCreateRole = vm::createRole,
                    onDeleteRole = vm::deleteRole
                )
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
