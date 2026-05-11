package com.example.agreementcomms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agreementcomms.data.ApiMember

@Composable
fun ProfileSettingsScreen(
    displayName: String,
    statusText: String,
    bio: String,
    avatarUrl: String,
    onDisplayNameChange: (String) -> Unit,
    onStatusTextChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onAvatarUrlChange: (String) -> Unit,
    onSaveSettings: () -> Unit,
    isLoading: Boolean,
    onClose: () -> Unit,
    onLogout: () -> Unit,
    compactMode: Boolean,
    onToggleCompactMode: () -> Unit,
    pushEnabled: Boolean,
    onTogglePush: () -> Unit,
    vibrationEnabled: Boolean,
    onToggleVibration: () -> Unit
) {
    SettingsScaffold(title = "Ustawienia użytkownika", onClose = onClose) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2D31))) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("PROFIL", color = Color(0xFFB5BAC1), style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(value = displayName, onValueChange = onDisplayNameChange, label = { Text("Pseudonim") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = statusText, onValueChange = onStatusTextChange, label = { Text("Status") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = bio, onValueChange = onBioChange, label = { Text("O mnie") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    Button(onClick = onSaveSettings, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) { Text("Zapisz zmiany profilu") }
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2D31))) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("POWIADOMIENIA", color = Color(0xFFB5BAC1), style = MaterialTheme.typography.labelMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Powiadomienia push", color = Color.White, modifier = Modifier.weight(1f))
                        Switch(checked = pushEnabled, onCheckedChange = { onTogglePush() })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Wibracje", color = Color.White, modifier = Modifier.weight(1f))
                        Switch(checked = vibrationEnabled, onCheckedChange = { onToggleVibration() })
                    }
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2D31))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("WYGLĄD", color = Color(0xFFB5BAC1), style = MaterialTheme.typography.labelMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Tryb kompaktowy", color = Color.White, modifier = Modifier.weight(1f))
                        Switch(checked = compactMode, onCheckedChange = { onToggleCompactMode() })
                    }
                }
            }
            Button(onClick = onLogout, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFED4245))) { Text("Wyloguj się") }
        }
    }
}

@Composable
fun ServerSettingsScreen(
    server: Server?,
    draftName: String,
    onDraftNameChange: (String) -> Unit,
    draftIcon: String,
    onDraftIconChange: (String) -> Unit,
    onSaveServer: () -> Unit,
    roles: List<Role>,
    onCreateRole: (String) -> Unit,
    onDeleteRole: (String) -> Unit,
    selectedRoleId: String,
    draftRoleName: String,
    onDraftRoleNameChange: (String) -> Unit,
    draftRolePermissions: RolePermissions,
    onDraftRolePermissionsChange: (RolePermissions) -> Unit,
    onRoleDraftSelect: (String) -> Unit,
    onSaveRole: () -> Unit,
    members: List<ApiMember>,
    canManageServer: Boolean,
    onAssignRole: (String, String) -> Unit,
    onRemoveRole: (String, String) -> Unit,
    onDeleteServer: () -> Unit,
    isLoading: Boolean,
    onClose: () -> Unit
) {
    var tab by remember { mutableStateOf(0) }

    SettingsScaffold(title = "Ustawienia serwera", onClose = onClose) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TabRow(selectedTabIndex = tab, containerColor = Color.Transparent, contentColor = Color.White) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Ogólne") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Role") })
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Członkowie") })
            }

            when (tab) {
                0 -> {
                    if (canManageServer) {
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2D31))) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("INFORMACJE", color = Color(0xFFB5BAC1), style = MaterialTheme.typography.labelMedium)
                                OutlinedTextField(value = draftName, onValueChange = onDraftNameChange, label = { Text("Nazwa serwera") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = draftIcon, onValueChange = onDraftIconChange, label = { Text("Ikona serwera") }, modifier = Modifier.fillMaxWidth())
                                Button(onClick = onSaveServer, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) { Text("Zapisz zmiany serwera") }
                            }
                        }
                        Button(onClick = onDeleteServer, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFED4245))) { Text("USUŃ SERWER") }
                    }
                }
                1 -> RolesManagement(roles, canManageServer, onCreateRole, onDeleteRole, selectedRoleId, draftRoleName, onDraftRoleNameChange, draftRolePermissions, onDraftRolePermissionsChange, onRoleDraftSelect, onSaveRole)
                2 -> MembersManagement(members, roles, canManageServer, onAssignRole, onRemoveRole)
            }
        }
    }
}

@Composable
fun ChannelSettingsScreen(
    channel: Channel?,
    draftName: String,
    onDraftNameChange: (String) -> Unit,
    draftTopic: String,
    onDraftTopicChange: (String) -> Unit,
    draftCategory: String,
    onDraftCategoryChange: (String) -> Unit,
    draftSlowmode: Int,
    onDraftSlowmodeChange: (Int) -> Unit,
    draftNsfw: Boolean,
    onDraftNsfwChange: (Boolean) -> Unit,
    onSaveChannel: () -> Unit,
    onDeleteChannel: (String) -> Unit,
    isLoading: Boolean,
    onClose: () -> Unit
) {
    SettingsScaffold(title = "Ustawienia kanału", onClose = onClose) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (channel != null) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2D31))) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("KONFIGURACJA", color = Color(0xFFB5BAC1), style = MaterialTheme.typography.labelMedium)
                        OutlinedTextField(value = draftName, onValueChange = onDraftNameChange, label = { Text("Nazwa kanału") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = draftTopic, onValueChange = onDraftTopicChange, label = { Text("Temat kanału") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = draftCategory, onValueChange = onDraftCategoryChange, label = { Text("Kategoria") }, modifier = Modifier.fillMaxWidth())
                        
                        Column {
                            Text("Tryb wolny (Slowmode): ${draftSlowmode}s", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            Slider(value = draftSlowmode.toFloat(), onValueChange = { onDraftSlowmodeChange(it.toInt()) }, valueRange = 0f..60f, steps = 11)
                            Text("Użytkownicy mogą wysyłać jedną wiadomość na ten czas.", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Kanał NSFW (18+)", color = Color.White, modifier = Modifier.weight(1f))
                            Switch(checked = draftNsfw, onCheckedChange = onDraftNsfwChange)
                        }
                        
                        Button(onClick = onSaveChannel, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) { Text("Zapisz zmiany kanału") }
                    }
                }
                Button(onClick = { onDeleteChannel(channel.id) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFED4245))) { Text("USUŃ KANAŁ") }
            }
        }
    }
}

@Composable
private fun SettingsScaffold(title: String, onClose: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF313338)).statusBarsPadding().padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
            IconButton(onClick = onClose) { Text("✕", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.headlineSmall, color = Color.White)
        }
        Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) { content() }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MembersManagement(members: List<ApiMember>, roles: List<Role>, canManage: Boolean, onAssignRole: (String, String) -> Unit, onRemoveRole: (String, String) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(members) { member ->
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2D31))) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(member.nickname ?: member.username, color = Color.White, fontWeight = FontWeight.Bold)
                    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        member.roles.forEach { role ->
                            AssistChip(onClick = { if (canManage) onRemoveRole(member.userId, role.id) }, label = { Text(role.name) }, colors = AssistChipDefaults.assistChipColors(labelColor = Color.White))
                        }
                        if (canManage) {
                            var expanded by remember { mutableStateOf(false) }
                            Box {
                                SuggestionChip(onClick = { expanded = true }, label = { Text("+") })
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    roles.filter { r -> member.roles.none { it.id == r.id } }.forEach { role ->
                                        DropdownMenuItem(text = { Text(role.name) }, onClick = { onAssignRole(member.userId, role.id); expanded = false })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RolesManagement(
    roles: List<Role>,
    canManage: Boolean,
    onCreate: (String) -> Unit,
    onDelete: (String) -> Unit,
    selectedRoleId: String,
    draftName: String,
    onDraftNameChange: (String) -> Unit,
    draftPerms: RolePermissions,
    onDraftPermsChange: (RolePermissions) -> Unit,
    onSelectRole: (String) -> Unit,
    onSave: () -> Unit
) {
    var newName by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (selectedRoleId.isEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Nowa rola") }, modifier = Modifier.weight(1f))
                Button(onClick = { onCreate(newName); newName = "" }) { Text("Dodaj") }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(roles) { role ->
                    Card(modifier = Modifier.clickable { onSelectRole(role.id) }, colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2D31))) {
                        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(role.name, color = Color.White, modifier = Modifier.weight(1f))
                            IconButton(onClick = { onDelete(role.id) }) { Text("✕", color = Color(0xFFED4245)) }
                        }
                    }
                }
            }
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2D31))) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = { onSelectRole("") }) { Text("← Powrót do listy") }
                    OutlinedTextField(value = draftName, onValueChange = onDraftNameChange, label = { Text("Nazwa roli") }, modifier = Modifier.fillMaxWidth())
                    PermissionToggle("Zarządzanie serwerem", draftPerms.manageServer) { onDraftPermsChange(draftPerms.copy(manageServer = it)) }
                    PermissionToggle("Zarządzanie kanałami", draftPerms.manageChannels) { onDraftPermsChange(draftPerms.copy(manageChannels = it)) }
                    PermissionToggle("Zarządzanie rolami", draftPerms.manageRoles) { onDraftPermsChange(draftPerms.copy(manageRoles = it)) }
                    PermissionToggle("Wysyłanie wiadomości", draftPerms.manageMessages) { onDraftPermsChange(draftPerms.copy(manageMessages = it)) }
                    Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) { Text("Zapisz rolę") }
                }
            }
        }
    }
}

@Composable
private fun PermissionToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun AvatarBubble(author: String, size: androidx.compose.ui.unit.Dp = 32.dp) {
    val colors = listOf(Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7), Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF00BCD4), Color(0xFF009688), Color(0xFF4CAF50))
    val bgColor = colors[author.hashCode().let { if (it < 0) -it else it } % colors.size]
    Box(modifier = Modifier.size(size).clip(CircleShape).background(bgColor), contentAlignment = Alignment.Center) {
        Text(author.firstOrNull()?.uppercase() ?: "?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.4).sp)
    }
}
