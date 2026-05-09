package com.example.agreementcomms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class SettingsTab(val label: String) {
    Account("Konto"),
    App("Aplikacja"),
    Server("Serwer"),
    Channel("Kanały"),
    Roles("Role")
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    nickname: String,
    displayName: String,
    statusText: String,
    pushEnabled: Boolean,
    vibrationEnabled: Boolean,
    compactModeEnabled: Boolean,
    savedAtLeastOnce: Boolean,
    onDisplayNameChange: (String) -> Unit,
    onStatusTextChange: (String) -> Unit,
    onPushEnabledChange: (Boolean) -> Unit,
    onVibrationEnabledChange: (Boolean) -> Unit,
    onCompactModeEnabledChange: (Boolean) -> Unit,
    onSaveSettings: () -> Unit,
    servers: List<Server>,
    selectedServerId: String,
    selectedChannel: String,
    onServerSelected: (String) -> Unit,
    onChannelSelected: (String) -> Unit,
    roles: List<Role>,
    onCreateServer: (String, String) -> Unit,
    onUpdateSelectedServer: (String, String) -> Unit,
    onDeleteSelectedServer: () -> Unit,
    onCreateChannel: (String) -> Unit,
    onRenameSelectedChannel: (String) -> Unit,
    onDeleteSelectedChannel: () -> Unit,
    onCreateRole: (String, String?, Int, RolePermissions) -> Unit,
    onUpdateRole: (String, String, String?, Int?, RolePermissions?) -> Unit,
    onDeleteRole: (String) -> Unit,
    activeRoleId: String,
    onSelectActiveRole: (String) -> Unit,
    canManageServer: Boolean,
    canManageChannels: Boolean,
    canManageRoles: Boolean,
    canManageMessages: Boolean,
    onOpenSidebar: () -> Unit
) {
    val selectedServer = servers.firstOrNull { it.id == selectedServerId }

    var tab by rememberSaveable { mutableStateOf(SettingsTab.Account) }
    var serverNameInput by rememberSaveable { mutableStateOf("") }
    var serverIconInput by rememberSaveable { mutableStateOf("") }
    var channelNameInput by rememberSaveable { mutableStateOf("") }
    var roleNameInput by rememberSaveable { mutableStateOf("") }
    var roleColorInput by rememberSaveable { mutableStateOf("") }
    var rolePositionInput by rememberSaveable { mutableStateOf("0") }
    var selectedRoleId by rememberSaveable { mutableStateOf("") }
    var permManageServer by rememberSaveable { mutableStateOf(false) }
    var permManageChannels by rememberSaveable { mutableStateOf(false) }
    var permManageRoles by rememberSaveable { mutableStateOf(false) }
    var permManageMessages by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(selectedServerId, selectedChannel, selectedServer?.name, selectedServer?.icon) {
        serverNameInput = selectedServer?.name.orEmpty()
        serverIconInput = selectedServer?.icon.orEmpty()
        channelNameInput = selectedChannel
    }

    LaunchedEffect(roles, activeRoleId) {
        if (activeRoleId.isNotBlank() && roles.any { it.id == activeRoleId }) {
            selectedRoleId = activeRoleId
        } else if (selectedRoleId.isBlank() || roles.none { it.id == selectedRoleId }) {
            selectedRoleId = roles.firstOrNull()?.id.orEmpty()
        }
        val role = roles.firstOrNull { it.id == selectedRoleId }
        roleNameInput = role?.name.orEmpty()
        roleColorInput = role?.color.orEmpty()
        rolePositionInput = (role?.position ?: 0).toString()
        permManageServer = role?.permissions?.manageServer ?: false
        permManageChannels = role?.permissions?.manageChannels ?: false
        permManageRoles = role?.permissions?.manageRoles ?: false
        permManageMessages = role?.permissions?.manageMessages ?: false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .statusBarsPadding()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onOpenSidebar) {
                Text("←", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            }
            Text(
                text = "Ustawienia użytkownika",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SettingsTab.entries.forEach { section ->
                val selected = section == tab
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                ) {
                    TextButton(onClick = { tab = section }) {
                        Text(
                            text = section.label,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (tab) {
                SettingsTab.Account -> {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Profil", fontWeight = FontWeight.SemiBold)
                                OutlinedTextField(
                                    value = displayName,
                                    onValueChange = onDisplayNameChange,
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    label = { Text("Nazwa wyświetlana") }
                                )
                                OutlinedTextField(
                                    value = statusText,
                                    onValueChange = onStatusTextChange,
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    label = { Text("Status") }
                                )
                                Text(
                                    text = "Podgląd: ${displayName.ifBlank { nickname }} • ${statusText.ifBlank { "Brak statusu" }}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(onClick = onSaveSettings, modifier = Modifier.fillMaxWidth()) {
                                    Text("Zapisz")
                                }
                                if (savedAtLeastOnce) {
                                    Text(
                                        text = "Ustawienia zapisane lokalnie",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                SettingsTab.App -> {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Zachowanie aplikacji", fontWeight = FontWeight.SemiBold)
                                SettingSwitchRow("Powiadomienia push", pushEnabled, onPushEnabledChange)
                                SettingSwitchRow("Wibracje", vibrationEnabled, onVibrationEnabledChange)
                                SettingSwitchRow("Tryb kompaktowy", compactModeEnabled, onCompactModeEnabledChange)
                                Text(
                                    text = "Aktywna rola może pisać wiadomości: ${if (canManageMessages) "tak" else "nie"}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(onClick = onSaveSettings, modifier = Modifier.fillMaxWidth()) {
                                    Text("Zapisz")
                                }
                            }
                        }
                    }
                }

                SettingsTab.Server -> {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Serwer", fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "Aktywny: ${selectedServer?.name ?: "Brak"}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                servers.forEach { server ->
                                    val selected = server.id == selectedServerId
                                    Surface(
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        TextButton(onClick = { onServerSelected(server.id) }) {
                                            Text(
                                                text = server.name,
                                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = serverNameInput,
                                    onValueChange = { serverNameInput = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    label = { Text("Nazwa serwera") }
                                )
                                OutlinedTextField(
                                    value = serverIconInput,
                                    onValueChange = { serverIconInput = it.take(1) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    label = { Text("Ikona (1 znak)") }
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = { onUpdateSelectedServer(serverNameInput, serverIconInput) },
                                        modifier = Modifier.weight(1f),
                                        enabled = selectedServer != null && canManageServer
                                    ) { Text("Zapisz") }
                                    Button(
                                        onClick = onDeleteSelectedServer,
                                        modifier = Modifier.weight(1f),
                                        enabled = selectedServer != null && canManageServer
                                    ) { Text("Usuń") }
                                }

                                Text(
                                    text = "Nowy serwer dodasz szybciej z przycisku + w lewym pasku serwerów.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                SettingsTab.Channel -> {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Kanały", fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "Wybrany: ${selectedChannel.ifBlank { "Brak"} }",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                selectedServer?.channels?.forEach { channel ->
                                    TextButton(onClick = { onChannelSelected(channel) }) {
                                        val marker = if (channel == selectedChannel) "• " else ""
                                        Text("$marker$channel")
                                    }
                                }

                                OutlinedTextField(
                                    value = channelNameInput,
                                    onValueChange = { channelNameInput = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    label = { Text("Nazwa kanału") }
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = { onCreateChannel(channelNameInput) },
                                        modifier = Modifier.weight(1f),
                                        enabled = selectedServer != null && channelNameInput.isNotBlank() && canManageChannels
                                    ) { Text("Dodaj") }
                                    Button(
                                        onClick = { onRenameSelectedChannel(channelNameInput) },
                                        modifier = Modifier.weight(1f),
                                        enabled = selectedChannel.isNotBlank() && canManageChannels
                                    ) { Text("Zmień") }
                                    Button(
                                        onClick = onDeleteSelectedChannel,
                                        modifier = Modifier.weight(1f),
                                        enabled = selectedChannel.isNotBlank() && canManageChannels
                                    ) { Text("Usuń") }
                                }
                            }
                        }
                    }
                }

                SettingsTab.Roles -> {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Role serwera", fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "Aktywna rola: ${roles.firstOrNull { it.id == activeRoleId }?.name ?: "Brak"}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                roles.forEach { role ->
                                    TextButton(onClick = {
                                        selectedRoleId = role.id
                                        onSelectActiveRole(role.id)
                                    }) {
                                        val marker = if (role.id == selectedRoleId) "• " else ""
                                        val activeMarker = if (role.id == activeRoleId) " (active)" else ""
                                        Text("$marker${role.name}  (${role.position})$activeMarker")
                                    }
                                }

                                OutlinedTextField(
                                    value = roleNameInput,
                                    onValueChange = { roleNameInput = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    label = { Text("Nazwa roli") }
                                )
                                OutlinedTextField(
                                    value = roleColorInput,
                                    onValueChange = { roleColorInput = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    label = { Text("Kolor (opcjonalnie)") }
                                )
                                OutlinedTextField(
                                    value = rolePositionInput,
                                    onValueChange = { rolePositionInput = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    label = { Text("Pozycja") }
                                )

                                SettingSwitchRow("manageServer", permManageServer) { permManageServer = it }
                                SettingSwitchRow("manageChannels", permManageChannels) { permManageChannels = it }
                                SettingSwitchRow("manageRoles", permManageRoles) { permManageRoles = it }
                                SettingSwitchRow("manageMessages", permManageMessages) { permManageMessages = it }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = {
                                            onCreateRole(
                                                roleNameInput,
                                                roleColorInput.ifBlank { null },
                                                rolePositionInput.toIntOrNull() ?: 0,
                                                RolePermissions(
                                                    manageServer = permManageServer,
                                                    manageChannels = permManageChannels,
                                                    manageRoles = permManageRoles,
                                                    manageMessages = permManageMessages
                                                )
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        enabled = roleNameInput.isNotBlank() && canManageRoles
                                    ) { Text("Dodaj") }
                                    Button(
                                        onClick = {
                                            onUpdateRole(
                                                selectedRoleId,
                                                roleNameInput,
                                                roleColorInput.ifBlank { null },
                                                rolePositionInput.toIntOrNull(),
                                                RolePermissions(
                                                    manageServer = permManageServer,
                                                    manageChannels = permManageChannels,
                                                    manageRoles = permManageRoles,
                                                    manageMessages = permManageMessages
                                                )
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        enabled = selectedRoleId.isNotBlank() && canManageRoles
                                    ) { Text("Zapisz") }
                                    Button(
                                        onClick = { onDeleteRole(selectedRoleId) },
                                        modifier = Modifier.weight(1f),
                                        enabled = selectedRoleId.isNotBlank() && canManageRoles
                                    ) { Text("Usuń") }
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
private fun SettingSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
