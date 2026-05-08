package com.example.agreementcomms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    nickname: String,
    servers: List<Server>,
    section: MainSection,
    onSectionChange: (MainSection) -> Unit,
    selectedServerId: String,
    onServerSelected: (String) -> Unit,
    selectedChannel: String,
    onChannelSelected: (String) -> Unit,
    unreadCounts: Map<String, Int>,
    backendConnected: Boolean,
    backendError: String?,
    composerAttachment: ComposerAttachment?,
    onPickFromGallery: () -> Unit,
    onPickFile: () -> Unit,
    onTakePhoto: () -> Unit,
    onClearComposerAttachment: () -> Unit,
    settingsDisplayName: String,
    settingsStatusText: String,
    settingsPushEnabled: Boolean,
    settingsVibrationEnabled: Boolean,
    settingsCompactModeEnabled: Boolean,
    settingsSavedAtLeastOnce: Boolean,
    onSettingsDisplayNameChange: (String) -> Unit,
    onSettingsStatusTextChange: (String) -> Unit,
    onSettingsPushEnabledChange: (Boolean) -> Unit,
    onSettingsVibrationEnabledChange: (Boolean) -> Unit,
    onSettingsCompactModeEnabledChange: (Boolean) -> Unit,
    onSaveSettings: () -> Unit,
    messages: List<Message>,
    onSendMessage: (String) -> Unit
) {
    val selectedServer = servers.firstOrNull { it.id == selectedServerId } ?: servers.first()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(340.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                SidebarDrawer(
                    section = section,
                    onSectionChange = {
                        onSectionChange(it)
                        scope.launch { drawerState.close() }
                    },
                    servers = servers,
                    selectedServerId = selectedServerId,
                    onServerSelected = {
                        onServerSelected(it)
                        onSectionChange(MainSection.Chat)
                        scope.launch { drawerState.close() }
                    },
                    selectedServer = selectedServer,
                    selectedChannel = selectedChannel,
                    onChannelSelected = {
                        onChannelSelected(it)
                        onSectionChange(MainSection.Chat)
                        scope.launch { drawerState.close() }
                    },
                    unreadCounts = unreadCounts,
                    onClose = { scope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        when (section) {
            MainSection.Chat -> ChatPane(
                modifier = Modifier.fillMaxSize(),
                nickname = nickname,
                serverName = selectedServer.name,
                selectedChannel = selectedChannel,
                messages = messages,
                onSendMessage = onSendMessage,
                backendConnected = backendConnected,
                backendError = backendError,
                composerAttachment = composerAttachment,
                onPickFromGallery = onPickFromGallery,
                onPickFile = onPickFile,
                onTakePhoto = onTakePhoto,
                onClearComposerAttachment = onClearComposerAttachment,
                compactMode = settingsCompactModeEnabled,
                onOpenSidebar = { scope.launch { drawerState.open() } }
            )

            MainSection.Settings -> SettingsScreen(
                modifier = Modifier.fillMaxSize(),
                nickname = nickname,
                displayName = settingsDisplayName,
                statusText = settingsStatusText,
                pushEnabled = settingsPushEnabled,
                vibrationEnabled = settingsVibrationEnabled,
                compactModeEnabled = settingsCompactModeEnabled,
                savedAtLeastOnce = settingsSavedAtLeastOnce,
                onDisplayNameChange = onSettingsDisplayNameChange,
                onStatusTextChange = onSettingsStatusTextChange,
                onPushEnabledChange = onSettingsPushEnabledChange,
                onVibrationEnabledChange = onSettingsVibrationEnabledChange,
                onCompactModeEnabledChange = onSettingsCompactModeEnabledChange,
                onSaveSettings = onSaveSettings,
                onOpenSidebar = { scope.launch { drawerState.open() } }
            )
        }
    }
}

@Composable
private fun SidebarDrawer(
    section: MainSection,
    onSectionChange: (MainSection) -> Unit,
    servers: List<Server>,
    selectedServerId: String,
    onServerSelected: (String) -> Unit,
    selectedServer: Server,
    selectedChannel: String,
    onChannelSelected: (String) -> Unit,
    unreadCounts: Map<String, Int>,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp, bottom = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onClose) {
                Text(
                    text = "←",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                text = "Accordance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider()

        Row(modifier = Modifier.weight(1f)) {
            ServerRail(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(74.dp),
                servers = servers,
                selectedServerId = selectedServerId,
                onServerSelected = onServerSelected
            )
            ChannelSidebar(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                server = selectedServer,
                selectedChannel = selectedChannel,
                onChannelSelected = onChannelSelected,
                unreadCounts = unreadCounts
            )
        }

        HorizontalDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DrawerAction(
                modifier = Modifier.weight(1f),
                label = "💬 Czat",
                selected = section == MainSection.Chat,
                onClick = { onSectionChange(MainSection.Chat) }
            )
            DrawerAction(
                modifier = Modifier.weight(1f),
                label = "⚙ Ustawienia",
                selected = section == MainSection.Settings,
                onClick = { onSectionChange(MainSection.Settings) }
            )
        }
    }
}

@Composable
private fun DrawerAction(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ServerRail(
    modifier: Modifier = Modifier,
    servers: List<Server>,
    selectedServerId: String,
    onServerSelected: (String) -> Unit
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(top = 10.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(servers, key = { it.id }) { server ->
                val selected = server.id == selectedServerId
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onServerSelected(server.id) },
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = CircleShape
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = server.icon,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelSidebar(
    modifier: Modifier = Modifier,
    server: Server,
    selectedChannel: String,
    onChannelSelected: (String) -> Unit,
    unreadCounts: Map<String, Int>
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp, start = 10.dp, end = 10.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = server.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider()
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(server.channels, key = { it }) { channel ->
                    val selected = channel == selectedChannel
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onChannelSelected(channel) },
                        color = if (selected) {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = channel,
                                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                            val unread = unreadCounts[conversationKey(server.id, channel)] ?: 0
                            if (!selected && unread > 0) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = unread.toString(),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
