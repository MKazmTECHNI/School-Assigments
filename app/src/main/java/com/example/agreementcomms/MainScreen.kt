package com.example.agreementcomms

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.agreementcomms.data.ApiMember
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    nickname: String,
    servers: List<Server>,
    section: MainSection,
    onSectionChange: (MainSection) -> Unit,
    selectedServerId: String,
    onServerSelected: (String) -> Unit,
    selectedChannelId: String,
    onChannelSelected: (String) -> Unit,
    unreadCounts: Map<String, Int>,
    messages: List<Message>,
    onSendMessage: (String) -> Unit,
    onTyping: () -> Unit,
    typingText: String?,
    members: List<ApiMember>,
    isLoading: Boolean,
    onLogout: () -> Unit,
    // Profile
    displayName: String,
    statusText: String,
    bio: String,
    avatarUrl: String,
    onSaveProfile: () -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onStatusTextChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onAvatarUrlChange: (String) -> Unit,
    roles: List<Role>,
    canManageServer: Boolean,
    onAssignRole: (String, String) -> Unit,
    onRemoveRole: (String, String) -> Unit,
    onCreateServer: (String) -> Unit,
    onJoinServer: (String) -> Unit,
    onCreateChannel: (String, String) -> Unit,
    onDeleteChannel: (String) -> Unit,
    onDeleteServer: () -> Unit,
    // Preferences
    compactMode: Boolean,
    onToggleCompactMode: () -> Unit,
    pushEnabled: Boolean,
    onTogglePush: () -> Unit,
    vibrationEnabled: Boolean,
    onToggleVibration: () -> Unit,
    // Server Editing
    draftServerName: String,
    onDraftServerNameChange: (String) -> Unit,
    draftServerIcon: String,
    onDraftServerIconChange: (String) -> Unit,
    onSaveServer: () -> Unit,
    // Channel Editing
    draftChannelName: String,
    onDraftChannelNameChange: (String) -> Unit,
    draftChannelTopic: String,
    onDraftChannelTopicChange: (String) -> Unit,
    draftChannelCategory: String,
    onDraftChannelCategoryChange: (String) -> Unit,
    draftChannelSlowmode: Int,
    onDraftChannelSlowmodeChange: (Int) -> Unit,
    draftChannelNsfw: Boolean,
    onDraftChannelNsfwChange: (Boolean) -> Unit,
    onSaveChannel: () -> Unit,
    // Role Editing
    selectedRoleId: String,
    draftRoleName: String,
    onDraftRoleNameChange: (String) -> Unit,
    draftRolePermissions: RolePermissions,
    onDraftRolePermissionsChange: (RolePermissions) -> Unit,
    onRoleDraftSelect: (String) -> Unit,
    onSaveRole: () -> Unit,
    onCreateRole: (String) -> Unit,
    onDeleteRole: (String) -> Unit
) {
    val selectedServer = servers.find { it.id == selectedServerId } ?: servers.firstOrNull()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showMembers by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- BASE LAYER: Navigation & Chat ---
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.fillMaxHeight().width(310.dp),
                    drawerContainerColor = Color(0xFF2B2D31)
                ) {
                    if (selectedServer != null) {
                        DiscordSidebar(
                            servers = servers,
                            selectedServer = selectedServer,
                            selectedChannelId = selectedChannelId,
                            onServerSelected = { onServerSelected(it); onSectionChange(MainSection.Chat) },
                            onChannelSelected = { 
                                onChannelSelected(it)
                                scope.launch { drawerState.close() }
                                onSectionChange(MainSection.Chat)
                            },
                            onOpenProfileSettings = { 
                                onSectionChange(MainSection.ProfileSettings)
                                scope.launch { drawerState.close() }
                            },
                            onOpenServerSettings = { 
                                onSectionChange(MainSection.ServerSettings)
                                scope.launch { drawerState.close() }
                            },
                            onOpenChannelSettings = { cid ->
                                onChannelSelected(cid)
                                onSectionChange(MainSection.ChannelSettings)
                                scope.launch { drawerState.close() }
                            },
                            userNickname = nickname,
                            status = statusText,
                            onCreateServer = onCreateServer,
                            onJoinServer = onJoinServer
                        )
                    } else {
                        EmptyServerSidebar(
                            onCreateServer = onCreateServer,
                            onJoinServer = onJoinServer,
                            userNickname = nickname,
                            status = statusText,
                            onOpenSettings = { 
                                onSectionChange(MainSection.ProfileSettings)
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                }
            }
        ) {
            val chanName = selectedServer?.channels?.find { it.id == selectedChannelId }?.name ?: "kanał"
            ChatPane(
                modifier = Modifier.fillMaxSize(),
                nickname = nickname,
                serverName = selectedServer?.name ?: "Brak serwera",
                selectedChannel = chanName,
                selectedChannelId = selectedChannelId,
                messages = messages,
                onSendMessage = onSendMessage,
                onTyping = onTyping,
                typingText = typingText,
                backendConnected = true,
                backendError = null,
                isLoading = isLoading,
                composerAttachment = null,
                onPickFromGallery = {}, onPickFile = {}, onTakePhoto = {}, onClearComposerAttachment = {},
                compactMode = compactMode,
                onOpenSidebar = { scope.launch { drawerState.open() } },
                onOpenMembers = { showMembers = !showMembers }
            )
        }

        // --- OVERLAY: Members List (Right Side) ---
        AnimatedVisibility(
            visible = showMembers && selectedServer != null && section == MainSection.Chat,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd).zIndex(1f)
        ) {
            Surface(
                modifier = Modifier.fillMaxHeight().width(280.dp),
                color = Color(0xFF2B2D31),
                shadowElevation = 8.dp
            ) {
                DiscordMembersList(members = members, onClose = { showMembers = false })
            }
        }

        // --- OVERLAY: Settings Layer ---
        AnimatedVisibility(
            visible = section != MainSection.Chat,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.fillMaxSize().zIndex(2f)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF313338))) {
                when (section) {
                    MainSection.ProfileSettings -> ProfileSettingsScreen(
                        displayName = displayName, statusText = statusText,
                        bio = bio, avatarUrl = avatarUrl,
                        onDisplayNameChange = onDisplayNameChange, onStatusTextChange = onStatusTextChange,
                        onBioChange = onBioChange, onAvatarUrlChange = onAvatarUrlChange,
                        onSaveSettings = onSaveProfile, isLoading = isLoading,
                        onClose = { onSectionChange(MainSection.Chat) },
                        onLogout = onLogout,
                        compactMode = compactMode, onToggleCompactMode = onToggleCompactMode,
                        pushEnabled = pushEnabled, onTogglePush = onTogglePush,
                        vibrationEnabled = vibrationEnabled, onToggleVibration = onToggleVibration
                    )

                    MainSection.ServerSettings -> ServerSettingsScreen(
                        server = selectedServer,
                        draftName = draftServerName,
                        onDraftNameChange = onDraftServerNameChange,
                        draftIcon = draftServerIcon,
                        onDraftIconChange = onDraftServerIconChange,
                        onSaveServer = onSaveServer,
                        roles = roles,
                        onCreateRole = onCreateRole,
                        onDeleteRole = onDeleteRole,
                        selectedRoleId = selectedRoleId,
                        draftRoleName = draftRoleName,
                        onDraftRoleNameChange = onDraftRoleNameChange,
                        draftRolePermissions = draftRolePermissions,
                        onDraftRolePermissionsChange = onDraftRolePermissionsChange,
                        onRoleDraftSelect = onRoleDraftSelect,
                        onSaveRole = onSaveRole,
                        members = members,
                        canManageServer = canManageServer,
                        onAssignRole = onAssignRole,
                        onRemoveRole = onRemoveRole,
                        onDeleteServer = onDeleteServer,
                        isLoading = isLoading,
                        onClose = { onSectionChange(MainSection.Chat) }
                    )

                    MainSection.ChannelSettings -> ChannelSettingsScreen(
                        channel = selectedServer?.channels?.find { it.id == selectedChannelId },
                        draftName = draftChannelName,
                        onDraftNameChange = onDraftChannelNameChange,
                        draftTopic = draftChannelTopic,
                        onDraftTopicChange = onDraftChannelTopicChange,
                        draftCategory = draftChannelCategory,
                        onDraftCategoryChange = onDraftChannelCategoryChange,
                        draftSlowmode = draftChannelSlowmode,
                        onDraftSlowmodeChange = onDraftChannelSlowmodeChange,
                        draftNsfw = draftChannelNsfw,
                        onDraftNsfwChange = onDraftChannelNsfwChange,
                        onSaveChannel = onSaveChannel,
                        onDeleteChannel = { cid ->
                            onDeleteChannel(cid)
                            onSectionChange(MainSection.Chat)
                        },
                        isLoading = isLoading,
                        onClose = { onSectionChange(MainSection.Chat) }
                    )
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun EmptyServerSidebar(
    onCreateServer: (String) -> Unit,
    onJoinServer: (String) -> Unit,
    userNickname: String,
    status: String,
    onOpenSettings: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF2B2D31))) {
        Column(
            modifier = Modifier.weight(1f).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Witaj w Accordance", color = Color.White, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { showAddDialog = true }, modifier = Modifier.fillMaxWidth()) { Text("Stwórz serwer") }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = { showJoinDialog = true }, modifier = Modifier.fillMaxWidth()) { Text("Dołącz do serwera") }
        }

        Surface(color = Color(0xFF232428), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                AvatarBubble(userNickname, size = 32.dp)
                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Text(userNickname, color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Text(status, color = Color(0xFFB5BAC1), style = MaterialTheme.typography.labelSmall)
                }
                IconButton(onClick = onOpenSettings) { Text("⚙", color = Color.White) }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(onDismissRequest = { showAddDialog = false }, 
            title = { Text("Nowy serwer") },
            text = { OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("Nazwa serwera") }) },
            confirmButton = { Button(onClick = { onCreateServer(input); showAddDialog = false; input = "" }) { Text("Stwórz") } }
        )
    }
    if (showJoinDialog) {
        AlertDialog(onDismissRequest = { showJoinDialog = false }, 
            title = { Text("Dołącz do serwera") },
            text = { OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("Kod zaproszenia") }) },
            confirmButton = { Button(onClick = { onJoinServer(input); showJoinDialog = false; input = "" }) { Text("Dołącz") } }
        )
    }
}

@Composable
private fun DiscordSidebar(
    servers: List<Server>,
    selectedServer: Server,
    selectedChannelId: String,
    onServerSelected: (String) -> Unit,
    onChannelSelected: (String) -> Unit,
    onOpenProfileSettings: () -> Unit,
    onOpenServerSettings: () -> Unit,
    onOpenChannelSettings: (String) -> Unit,
    userNickname: String,
    status: String,
    onCreateServer: (String) -> Unit,
    onJoinServer: (String) -> Unit
) {
    var showAddServerDialog by remember { mutableStateOf(false) }
    var showJoinServerDialog by remember { mutableStateOf(false) }
    var serverNameInput by remember { mutableStateOf("") }
    var inviteCodeInput by remember { mutableStateOf("") }

    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.width(72.dp).fillMaxHeight().background(Color(0xFF1E1F22)).padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            servers.forEach { s ->
                ServerIcon(s.icon, s.id == selectedServer.id) { onServerSelected(s.id) }
            }
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF313338)).clickable { showAddServerDialog = true }, contentAlignment = Alignment.Center) {
                Text("+", color = Color(0xFF23A559), fontSize = 24.sp)
            }
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF313338)).clickable { showJoinServerDialog = true }, contentAlignment = Alignment.Center) {
                Text("🔗", color = Color(0xFF5865F2), fontSize = 20.sp)
            }
        }

        Column(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF2B2D31))) {
            Column(modifier = Modifier.clickable { onOpenServerSettings() }.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(selectedServer.name, modifier = Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("⚙", color = Color.Gray, fontSize = 14.sp)
                }
                Text("KOD: ${selectedServer.inviteCode ?: "---"}", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
            }
            HorizontalDivider(color = Color(0xFF1F2124))

            val grouped = selectedServer.channels.groupBy { it.category ?: "PODSTAWOWE" }
            LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                grouped.forEach { (cat, chans) ->
                    item {
                        Text(cat.uppercase(), modifier = Modifier.padding(top = 16.dp, start = 8.dp, bottom = 4.dp), 
                            color = Color(0xFF949BA4), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    items(chans) { chan ->
                        ChannelItem(chan.name, chan.id == selectedChannelId, onOpenSettings = { onOpenChannelSettings(chan.id) }) { onChannelSelected(chan.id) }
                    }
                }
            }

            Surface(color = Color(0xFF232428), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    AvatarBubble(userNickname, size = 32.dp)
                    Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                        Text(userNickname, color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text(status, color = Color(0xFFB5BAC1), style = MaterialTheme.typography.labelSmall)
                    }
                    IconButton(onClick = onOpenProfileSettings) { Text("⚙", color = Color.White) }
                }
            }
        }
    }

    if (showAddServerDialog) {
        AlertDialog(onDismissRequest = { showAddServerDialog = false }, 
            title = { Text("Stwórz serwer") },
            text = { OutlinedTextField(value = serverNameInput, onValueChange = { serverNameInput = it }, label = { Text("Nazwa serwera") }) },
            confirmButton = { Button(onClick = { onCreateServer(serverNameInput); showAddServerDialog = false; serverNameInput = "" }) { Text("Stwórz") } }
        )
    }
    if (showJoinServerDialog) {
        AlertDialog(onDismissRequest = { showJoinServerDialog = false }, 
            title = { Text("Dołącz do serwera") },
            text = { OutlinedTextField(value = inviteCodeInput, onValueChange = { inviteCodeInput = it }, label = { Text("Kod zaproszenia") }) },
            confirmButton = { Button(onClick = { onJoinServer(inviteCodeInput); showJoinServerDialog = false; inviteCodeInput = "" }) { Text("Dołącz") } }
        )
    }
}

@Composable
private fun ServerIcon(icon: String, isSel: Boolean, onClick: () -> Unit) {
    Box(contentAlignment = Alignment.CenterStart) {
        if (isSel) Box(modifier = Modifier.width(4.dp).height(32.dp).clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)).background(Color.White))
        Box(
            modifier = Modifier.padding(start = 12.dp).size(48.dp)
                .clip(if (isSel) RoundedCornerShape(16.dp) else CircleShape)
                .background(if (isSel) Color(0xFF5865F2) else Color(0xFF313338))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) { Text(icon, color = Color.White, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun ChannelItem(name: String, isSel: Boolean, onOpenSettings: () -> Unit, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(34.dp).padding(vertical = 1.dp).clip(RoundedCornerShape(4.dp)).clickable { onClick() },
        color = if (isSel) Color(0xFF3F4248) else Color.Transparent
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
            Text("#", color = Color(0xFF80848E), fontSize = 20.sp)
            Text(name, modifier = Modifier.padding(start = 8.dp).weight(1f), color = if (isSel) Color.White else Color(0xFF80848E), fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium)
            if (isSel) {
                IconButton(onClick = onOpenSettings, modifier = Modifier.size(24.dp)) {
                    Text("⚙", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun DiscordMembersList(members: List<ApiMember>, onClose: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("UŻYTKOWNICY — ${members.size}", modifier = Modifier.weight(1f), color = Color(0xFF949BA4), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            IconButton(onClick = onClose) {
                Text("✕", color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(members) { m ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarBubble(m.username, size = 32.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(m.nickname ?: m.username, color = if (m.isOnline) Color.White else Color.Gray, style = MaterialTheme.typography.bodyMedium)
                    if (m.isOnline) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF23A559)))
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarBubble(author: String, size: androidx.compose.ui.unit.Dp = 32.dp) {
    val colors = listOf(Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF2196F3), Color(0xFF4CAF50))
    val bgColor = colors[author.hashCode().let { if (it < 0) -it else it } % colors.size]
    Box(modifier = Modifier.size(size).clip(CircleShape).background(bgColor), contentAlignment = Alignment.Center) {
        Text(author.firstOrNull()?.uppercase() ?: "?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.4).sp)
    }
}
