package com.example.agreementcomms

import androidx.compose.foundation.background
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
    // Preferences
    compactMode: Boolean,
    onToggleCompactMode: () -> Unit,
    pushEnabled: Boolean,
    onTogglePush: () -> Unit,
    vibrationEnabled: Boolean,
    onToggleVibration: () -> Unit
) {
    SettingsScaffold(title = "Ustawienia użytkownika", onClose = onClose) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            // Profile Section
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2D31))) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("PROFIL", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        value = displayName, onValueChange = onDisplayNameChange,
                        label = { Text("Pseudonim") }, modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = Color.White, focusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = statusText, onValueChange = onStatusTextChange,
                        label = { Text("Status") }, modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = Color.White, focusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = bio, onValueChange = onBioChange,
                        label = { Text("O mnie") }, modifier = Modifier.fillMaxWidth(), minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = Color.White, focusedTextColor = Color.White)
                    )
                    Button(
                        onClick = onSaveSettings,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5865F2))
                    ) {
                        Text("Zapisz zmiany")
                    }
                }
            }

            // Appearance Section
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2D31))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("WYGLĄD", color = Color.Gray, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tryb kompaktowy", color = Color.White)
                            Text("Więcej wiadomości na ekranie", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = compactMode, onCheckedChange = { onToggleCompactMode() })
                    }
                }
            }

            // Notifications Section
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2D31))) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("POWIADOMIENIA", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Powiadomienia Push", color = Color.White, modifier = Modifier.weight(1f))
                        Switch(checked = pushEnabled, onCheckedChange = { onTogglePush() })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Wibracje", color = Color.White, modifier = Modifier.weight(1f))
                        Switch(checked = vibrationEnabled, onCheckedChange = { onToggleVibration() })
                    }
                }
            }

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFED4245))
            ) {
                Text("Wyloguj się")
            }
        }
    }
}

@Composable
fun ServerSettingsScreen(
    server: Server?,
    roles: List<Role>,
    members: List<ApiMember>,
    canManageServer: Boolean,
    onAssignRole: (String, String) -> Unit,
    onRemoveRole: (String, String) -> Unit,
    onDeleteServer: () -> Unit,
    isLoading: Boolean,
    onClose: () -> Unit
) {
    var showMembers by remember { mutableStateOf(false) }

    SettingsScaffold(title = "Ustawienia serwera", onClose = onClose) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(server?.name ?: "Brak serwera", color = Color.White, style = MaterialTheme.typography.headlineMedium)
            
            if (canManageServer) {
                Button(
                    onClick = { showMembers = !showMembers },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4E5058))
                ) {
                    Text(if (showMembers) "Ukryj listę członków" else "Zarządzaj członkami i rolami")
                }

                if (showMembers) {
                    MembersManagement(members, roles, canManageServer, onAssignRole, onRemoveRole, isLoading)
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDeleteServer,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFED4245))
                ) {
                    Text("USUŃ SERWER")
                }
            } else {
                Text("Nie masz uprawnień administratora na tym serwerze.", color = Color.Gray)
            }
        }
    }
}

@Composable
fun ChannelSettingsScreen(
    channel: Channel?,
    onDeleteChannel: (String) -> Unit,
    isLoading: Boolean,
    onClose: () -> Unit
) {
    SettingsScaffold(title = "Ustawienia kanału", onClose = onClose) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("# ${channel?.name ?: "Brak kanału"}", color = Color.White, style = MaterialTheme.typography.headlineMedium)
            
            if (channel != null) {
                Button(
                    onClick = { onDeleteChannel(channel.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFED4245))
                ) {
                    Text("USUŃ KANAŁ")
                }
            }
        }
    }
}

@Composable
private fun SettingsScaffold(
    title: String,
    onClose: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF313338))
            .statusBarsPadding()
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
            IconButton(onClick = onClose) {
                Text("✕", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.headlineSmall, color = Color.White)
        }
        
        Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MembersManagement(
    members: List<ApiMember>,
    roles: List<Role>,
    canManage: Boolean,
    onAssignRole: (String, String) -> Unit,
    onRemoveRole: (String, String) -> Unit,
    isLoading: Boolean
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 400.dp)) {
        items(members) { member ->
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2D31))) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarBubble(author = member.username, size = 32.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(member.nickname ?: member.username, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        member.roles.forEach { role ->
                            AssistChip(
                                onClick = { if (canManage) onRemoveRole(member.userId, role.id) },
                                label = { Text(role.name) },
                                colors = AssistChipDefaults.assistChipColors(labelColor = Color.White)
                            )
                        }
                        if (canManage) {
                            var showRoleMenu by remember { mutableStateOf(false) }
                            Box {
                                SuggestionChip(onClick = { showRoleMenu = true }, label = { Text("+") })
                                DropdownMenu(
                                    expanded = showRoleMenu,
                                    onDismissRequest = { showRoleMenu = false },
                                    modifier = Modifier.background(Color(0xFF1E1F22))
                                ) {
                                    roles.filter { r -> member.roles.none { it.id == r.id } }.forEach { role ->
                                        DropdownMenuItem(
                                            text = { Text(role.name, color = Color.White) },
                                            onClick = { onAssignRole(member.userId, role.id); showRoleMenu = false }
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
}

@Composable
private fun AvatarBubble(author: String, size: androidx.compose.ui.unit.Dp = 32.dp) {
    val colors = listOf(Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7), Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF00BCD4), Color(0xFF009688), Color(0xFF4CAF50))
    val bgColor = colors[author.hashCode().let { if (it < 0) -it else it } % colors.size]
    Box(modifier = Modifier.size(size).clip(CircleShape).background(bgColor), contentAlignment = Alignment.Center) {
        Text(author.firstOrNull()?.uppercase() ?: "?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.4).sp)
    }
}
