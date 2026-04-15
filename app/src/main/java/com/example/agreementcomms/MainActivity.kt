package com.example.agreementcomms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.agreementcomms.ui.theme.AgreementCommsTheme
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AgreementCommsTheme(darkTheme = true, dynamicColor = false) {
                AgreementApp()
            }
        }
    }
}

@Composable
fun AgreementApp() {
    val vm: ChatViewModel = viewModel()
    val state = vm.uiState
    val context = LocalContext.current

    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            vm.setComposerAttachment(
                ComposerAttachment(
                    type = AttachmentType.Image,
                    name = resolveFileName(context, uri) ?: "image.jpg",
                    localUri = uri.toString(),
                    mimeType = resolveMimeType(context, uri),
                    sizeLabel = resolveSizeLabel(context, uri)
                )
            )
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            vm.setComposerAttachment(
                ComposerAttachment(
                    type = if (resolveMimeType(context, uri).startsWith("image/")) AttachmentType.Image else AttachmentType.File,
                    name = resolveFileName(context, uri) ?: "file",
                    localUri = uri.toString(),
                    mimeType = resolveMimeType(context, uri),
                    sizeLabel = resolveSizeLabel(context, uri)
                )
            )
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCameraUri != null) {
            val uri = pendingCameraUri!!
            vm.setComposerAttachment(
                ComposerAttachment(
                    type = AttachmentType.Image,
                    name = resolveFileName(context, uri) ?: "camera_photo.jpg",
                    localUri = uri.toString(),
                    mimeType = resolveMimeType(context, uri),
                    sizeLabel = resolveSizeLabel(context, uri)
                )
            )
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createTempCameraUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    if (!state.isLoggedIn) {
        LoginScreen(
            nickname = state.draftNickname,
            onNicknameChange = vm::onDraftNicknameChange,
            onEnter = vm::login
        )
    } else {
        MainScreen(
            nickname = state.nickname,
            servers = state.servers,
            section = state.section,
            onSectionChange = vm::setSection,
            selectedServerId = state.selectedServerId,
            onServerSelected = vm::onServerSelected,
            selectedChannel = state.selectedChannel,
            onChannelSelected = vm::onChannelSelected,
            unreadCounts = vm.unreadCounts,
            backendConnected = state.backendConnected,
            backendError = state.backendError,
            composerAttachment = state.composerAttachment,
            onPickFromGallery = {
                galleryPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onPickFile = {
                filePicker.launch(arrayOf("*/*"))
            },
            onTakePhoto = {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    val uri = createTempCameraUri(context)
                    pendingCameraUri = uri
                    cameraLauncher.launch(uri)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onClearComposerAttachment = { vm.setComposerAttachment(null) },
            settingsDisplayName = state.settingsDisplayName,
            settingsStatusText = state.settingsStatusText,
            settingsPushEnabled = state.settingsPushEnabled,
            settingsVibrationEnabled = state.settingsVibrationEnabled,
            settingsCompactModeEnabled = state.settingsCompactModeEnabled,
            settingsSavedAtLeastOnce = state.settingsSavedAtLeastOnce,
            onSettingsDisplayNameChange = vm::onSettingsDisplayNameChange,
            onSettingsStatusTextChange = vm::onSettingsStatusTextChange,
            onSettingsPushEnabledChange = vm::onSettingsPushEnabledChange,
            onSettingsVibrationEnabledChange = vm::onSettingsVibrationEnabledChange,
            onSettingsCompactModeEnabledChange = vm::onSettingsCompactModeEnabledChange,
            onSaveSettings = vm::saveSettings,
            messages = vm.activeMessages(),
            onSendMessage = { text ->
                val attachment = state.composerAttachment
                if (attachment != null) {
                    val bytes = readUriBytes(context, Uri.parse(attachment.localUri))
                    vm.sendMessage(
                        text = text,
                        attachmentBytes = bytes,
                        attachmentFileName = attachment.name,
                        attachmentMimeType = attachment.mimeType
                    )
                } else {
                    vm.sendMessage(text = text)
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AgreementPreview() {
    AgreementCommsTheme {
        AgreementApp()
    }
}

@Composable
private fun LoginScreen(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    onEnter: () -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Accordance",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Etap 1: logowanie (mock), serwery, kanały i czat lokalny.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Konta testowe: mkazm, ola.dev, bartek.ui, natalia.pm",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = nickname,
                onValueChange = onNicknameChange,
                label = { Text("Twój nick") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(onClick = onEnter, modifier = Modifier.fillMaxWidth()) {
                Text("Wejdź do aplikacji")
            }
        }
    }
}

@Composable
private fun MainScreen(
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
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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

@Composable
private fun ChatPane(
    modifier: Modifier = Modifier,
    nickname: String,
    serverName: String,
    selectedChannel: String,
    messages: List<Message>,
    onSendMessage: (String) -> Unit,
    backendConnected: Boolean,
    backendError: String?,
    composerAttachment: ComposerAttachment?,
    onPickFromGallery: () -> Unit,
    onPickFile: () -> Unit,
    onTakePhoto: () -> Unit,
    onClearComposerAttachment: () -> Unit,
    compactMode: Boolean,
    onOpenSidebar: () -> Unit
) {
    var input by rememberSaveable { mutableStateOf("") }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var attachmentMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val visibleMessages = if (searchQuery.isBlank()) {
        messages
    } else {
        messages.filter {
            it.author.contains(searchQuery, ignoreCase = true) ||
                it.text.contains(searchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(messages.size, searchQuery) {
        if (searchQuery.isBlank() && visibleMessages.isNotEmpty()) {
            listState.animateScrollToItem(visibleMessages.lastIndex)
        }
    }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = 22.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onOpenSidebar) {
                Text(
                    text = "←",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = selectedChannel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Serwer: $serverName",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (backendConnected) "Backend: online" else "Backend: offline (mock fallback)",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (backendConnected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = nickname,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(onClick = {
                showSearch = !showSearch
                if (!showSearch) searchQuery = ""
            }) {
                Text("🔍")
            }
        }

        if (!backendError.isNullOrBlank()) {
            Text(
                text = backendError,
                modifier = Modifier.padding(horizontal = 14.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        if (showSearch) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Szukaj w kanale") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(if (compactMode) 4.dp else 8.dp)
        ) {
            itemsIndexed(visibleMessages) { index, message ->
                val groupedWithPrevious =
                    index > 0 &&
                        visibleMessages[index - 1].author == message.author &&
                        visibleMessages[index - 1].isMine == message.isMine
                MessageItem(
                    message = message,
                    groupedWithPrevious = groupedWithPrevious
                )
            }
        }

        if (composerAttachment != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (composerAttachment.type == AttachmentType.Image) {
                                "🖼 ${composerAttachment.name}"
                            } else {
                                "📎 ${composerAttachment.name}"
                            },
                            style = MaterialTheme.typography.labelLarge
                        )
                        composerAttachment.sizeLabel?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    TextButton(onClick = onClearComposerAttachment) {
                        Text("Usuń")
                    }
                }
            }
        }

        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                IconButton(onClick = { attachmentMenuExpanded = !attachmentMenuExpanded }) {
                    Text(
                        text = "＋",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                DropdownMenu(
                    expanded = attachmentMenuExpanded,
                    onDismissRequest = { attachmentMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("🖼 Galeria") },
                        onClick = {
                            attachmentMenuExpanded = false
                            onPickFromGallery()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("📷 Aparat") },
                        onClick = {
                            attachmentMenuExpanded = false
                            onTakePhoto()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("📎 Plik") },
                        onClick = {
                            attachmentMenuExpanded = false
                            onPickFile()
                        }
                    )
                }
            }
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Napisz wiadomość") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(
                onClick = {
                    if (input.isNotBlank() || composerAttachment != null) {
                        onSendMessage(input.trim())
                        input = ""
                    }
                },
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("➤")
            }
        }
    }
}

@Composable
private fun MessageItem(
    message: Message,
    groupedWithPrevious: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMine) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isMine) {
            if (groupedWithPrevious) {
                Spacer(modifier = Modifier.width(34.dp))
            } else {
                AvatarBubble(author = message.author)
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        Card(
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(0.82f)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                if (!groupedWithPrevious) {
                    Text(
                        text = "${message.author} • ${message.time}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (message.isMine) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(text = message.text, style = MaterialTheme.typography.bodyLarge)

                if (message.attachments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        message.attachments.forEach { attachment ->
                            AttachmentItem(attachment = attachment)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AttachmentItem(attachment: MessageAttachment) {
    when (attachment.type) {
        AttachmentType.Image -> {
            if (attachment.url != null) {
                AsyncImage(
                    model = attachment.url,
                    contentDescription = attachment.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        AttachmentType.File -> {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📄 ${attachment.name}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        attachment.meta?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "Pobierz",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun AvatarBubble(author: String) {
    val palette = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.surfaceContainerHighest
    )
    val color = palette[kotlin.math.abs(author.hashCode()) % palette.size]

    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = author.firstOrNull()?.uppercase() ?: "?",
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SettingsScreen(
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
    onOpenSidebar: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .statusBarsPadding()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onOpenSidebar) {
                Text(
                    text = "←",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                text = "Ustawienia",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Text(
            text = "Panel konta, powiadomień i czatu",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "Konto", fontWeight = FontWeight.SemiBold)
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
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "Powiadomienia", fontWeight = FontWeight.SemiBold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Push")
                    Switch(checked = pushEnabled, onCheckedChange = onPushEnabledChange)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Wibracje")
                    Switch(checked = vibrationEnabled, onCheckedChange = onVibrationEnabledChange)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Tryb kompaktowy czatu")
                    Switch(checked = compactModeEnabled, onCheckedChange = onCompactModeEnabledChange)
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "Podgląd", fontWeight = FontWeight.SemiBold)
                Text(text = "Nick: ${displayName.ifBlank { nickname }}")
                Text(text = "Status: ${statusText.ifBlank { "Brak" }}")
                Text(
                    text = "Push: ${if (pushEnabled) "włączone" else "wyłączone"} • Wibracje: ${if (vibrationEnabled) "włączone" else "wyłączone"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (savedAtLeastOnce) {
            Text(
                text = "Ustawienia zapisane lokalnie",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Button(onClick = onSaveSettings, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Zapisz ustawienia (mock)")
        }

        TextButton(onClick = { }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Wyloguj (mock)")
        }
    }
}

data class Server(
    val id: String,
    val name: String,
    val icon: String,
    val channels: List<String>
)

data class Message(
    val author: String,
    val text: String,
    val time: String,
    val isMine: Boolean,
    val attachments: List<MessageAttachment> = emptyList()
)

data class MessageAttachment(
    val type: AttachmentType,
    val name: String,
    val url: String? = null,
    val meta: String? = null
)

enum class AttachmentType {
    Image,
    File
}

enum class MainSection {
    Chat,
    Settings
}

fun conversationKey(serverId: String, channel: String): String = "$serverId|$channel"

fun buildSampleConversations(): MutableMap<String, SnapshotStateList<Message>> {
    return mutableMapOf(
        conversationKey("general", "#ogólny") to mutableStateListOf(
            Message("Ola", "Ej, kto ma plan na wieczór?", "17:05", false),
            Message("Bartek", "Ja klasycznie: serial + kebs 😎", "17:06", false),
            Message("Natalia", "Brzmi uczciwie", "17:07", false),
            Message("Kuba", "Ja może wyskoczę na kosza", "17:09", false),
            Message("Ola", "Jak coś to jestem chętna po 19", "17:10", false),
            Message("Bartek", "To ja tylko buty ogarnę i lecimy", "17:11", false),
            Message("Natalia", "W końcu aktywnie, wow", "17:12", false),
            Message("Kuba", "screen tego momentu 📸", "17:13", false),
            Message("Ola", "to po 19:15 przy boisku?", "17:14", false),
            Message("Bartek", "pasuje", "17:14", false),
            Message("Natalia", "ja 10 min później, korki", "17:15", false)
        ),
        conversationKey("general", "#nauka") to mutableStateListOf(
            Message("Ola", "Czy tylko ja się uczę lepiej w nocy?", "18:11", false),
            Message("Bartek", "+1, po 22 nagle mózg: let's go", "18:12", false),
            Message("Natalia", "A rano ten sam mózg: nope", "18:13", false),
            Message("Kuba", "Rel", "18:13", false),
            Message("Ola", "Polecacie jakieś lofi playlisty?", "18:14", false),
            Message("Bartek", "lofi girl i zero powiadomień", "18:15", false),
            Message("Natalia", "i technika pomodoro 25/5 serio działa", "18:16", false),
            Message("Kuba", "ja robię 40/10 bo 25 to rozgrzewka", "18:17", false),
            Message("Ola", "notuję, thanks", "18:18", false)
        ),
        conversationKey("general", "#offtopic") to mutableStateListOf(
            Message("Kuba", "wrzucam mema dnia", "19:21", false),
            Message(
                "Kuba",
                "idealne podsumowanie dnia",
                "19:21",
                false,
                attachments = listOf(
                    MessageAttachment(
                        type = AttachmentType.Image,
                        name = "meme_first_try.jpg",
                        url = "https://images.unsplash.com/photo-1517336714739-489689fd1ca8?auto=format&fit=crop&w=1000&q=80"
                    )
                )
            ),
            Message("Ola", "to fake, takie rzeczy nie istnieją", "19:22", false),
            Message("Bartek", "dokładnie, to AI-generated", "19:23", false),
            Message("Natalia", "xDDD", "19:23", false),
            Message("Kuba", "mam jeszcze jednego z kotem programistą", "19:24", false),
            Message("Ola", "dawaj", "19:24", false),
            Message(
                "Kuba",
                "kot dev edition",
                "19:25",
                false,
                attachments = listOf(
                    MessageAttachment(
                        type = AttachmentType.Image,
                        name = "cat_dev.png",
                        url = "https://images.unsplash.com/photo-1511044568932-338cba0ad803?auto=format&fit=crop&w=1000&q=80"
                    )
                )
            ),
            Message("Bartek", "to ja po 3 kawie", "19:25", false),
            Message("Natalia", "i z deadline'em za 15 minut", "19:26", false),
            Message("Ola", "literally", "19:26", false),
            Message("Kuba", "ok koniec spamu, pa 😅", "19:27", false),
            Message("Bartek", "nie no jeszcze jeden i serio kończymy", "19:28", false),
            Message("Natalia", "klasyk", "19:28", false),
            Message("Ola", "ten kanał nigdy nie śpi", "19:29", false)
        ),
        conversationKey("mobile-dev", "#android") to mutableStateListOf(
            Message("Bartek", "Czy tylko mnie emulator czasem nienawidzi?", "16:45", false),
            Message("Ola", "Cold boot i modlitwa", "16:46", false),
            Message("Natalia", "Najskuteczniejszy fix ever", "16:48", false),
            Message("Kuba", "u mnie działa dopiero po restartcie laptopa", "16:49", false),
            Message("Bartek", "to już rytuał", "16:50", false),
            Message("Ola", "przynajmniej adb jeszcze żyje", "16:51", false),
            Message("Natalia", "czasem…", "16:51", false),
            Message("Kuba", "jak gradle cache pęknie to już tylko płacz", "16:52", false),
            Message("Bartek", "i invalidate caches + restart studio", "16:53", false),
            Message("Ola", "to powinno być oficjalne zaklęcie", "16:54", false)
        ),
        conversationKey("mobile-dev", "#ios") to mutableStateListOf(
            Message("Kuba", "Ktoś faktycznie lubi Xcode? pytam dla kolegi", "15:30", false),
            Message("Ola", "lubię… jak się nie crashuje", "15:31", false),
            Message("Bartek", "czyli 2 razy w miesiącu?", "15:33", false),
            Message("Natalia", "💀", "15:33", false),
            Message("Kuba", "simulator też dziś wolniejszy niż ja rano", "15:34", false),
            Message("Ola", "to akurat normalne", "15:35", false),
            Message("Bartek", "kawa dla ciebie i dla Maca", "15:35", false)
        ),
        conversationKey("mobile-dev", "#react-native") to mutableStateListOf(
            Message("Natalia", "RN hot reload to nadal magia", "14:12", false),
            Message("Kuba", "true, to jest najlepsza część", "14:13", false),
            Message("Ola", "plus jeden codebase i mniej bólu", "14:14", false),
            Message("Bartek", "dopóki native module nie powie stop", "14:15", false),
            Message("Natalia", "facts", "14:15", false)
        ),
        conversationKey("szkola", "#projekt") to mutableStateListOf(
            Message("Natalia", "Kto widział moją czarną bluzę z kapturem?", "13:40", false),
            Message("Bartek", "ta z małym logo?", "13:41", false),
            Message("Natalia", "tak", "13:41", false),
            Message("Ola", "chyba została w sali obok okna", "13:42", false),
            Message(
                "Natalia",
                "ratujecie życie, dzięki",
                "13:43",
                false,
                attachments = listOf(
                    MessageAttachment(
                        type = AttachmentType.File,
                        name = "lista_zakupow_weekend.pdf",
                        meta = "PDF • 1.2 MB"
                    )
                )
            ),
            Message("Kuba", "znalazłem jeszcze powerbank, czyj?", "13:44", false),
            Message("Bartek", "mój! oddam ci jutro batonika", "13:45", false),
            Message("Natalia", "deal accepted", "13:45", false)
        ),
        conversationKey("szkola", "#terminy") to mutableStateListOf(
            Message("Kuba", "Jutro pierwsza lekcja odwołana czy plotka?", "11:02", false),
            Message("Natalia", "Podobno odwołana, ale czekam na potwierdzenie", "11:03", false),
            Message("Bartek", "u mnie na librusie jeszcze cisza", "11:05", false),
            Message("Ola", "jak nic nie wrzucą do 20:00 to i tak przyjdę na później", "11:06", false),
            Message("Kuba", "fair", "11:06", false),
            Message("Natalia", "dam znać jak coś się pojawi", "11:07", false),
            Message("Bartek", "🙏", "11:07", false),
            Message("Ola", "dzięki", "11:08", false),
            Message("Natalia", "update: jednak normalnie jest", "19:48", false),
            Message("Kuba", "czyli budzik na 6:30, super…", "19:49", false),
            Message("Bartek", "trzymajcie się tam", "19:49", false),
            Message("Ola", "weźcie termos, będzie zimno", "19:50", false)
        ),
        conversationKey("szkola", "#pomoc") to mutableStateListOf(
            Message("Ola", "Jak usunąć plamę po kawie z notatek?", "10:10", false),
            Message("Kuba", "ryż 😂", "10:11", false),
            Message("Natalia", "Kuba pls", "10:11", false),
            Message("Bartek", "chusteczki + delikatnie wodą, tylko nie trzeć mocno", "10:12", false),
            Message("Ola", "ok, testuję", "10:13", false),
            Message("Ola", "działa, dzięki!", "10:16", false),
            Message("Kuba", "ej no ryż też działa na wszystko", "10:17", false),
            Message("Natalia", "tylko nie na ten argument", "10:17", false),
            Message("Bartek", "przynajmniej morale podniósł", "10:18", false)
        )
    )
}

fun defaultServers(): List<Server> {
    return listOf(
        Server(
            id = "general",
            name = "General",
            icon = "G",
            channels = listOf("#ogólny", "#nauka", "#offtopic")
        ),
        Server(
            id = "mobile-dev",
            name = "Mobile Dev",
            icon = "M",
            channels = listOf("#android", "#ios", "#react-native")
        ),
        Server(
            id = "szkola",
            name = "Szkoła",
            icon = "S",
            channels = listOf("#projekt", "#terminy", "#pomoc")
        )
    )
}

private fun createTempCameraUri(context: Context): Uri {
    val fileName = "camera_${System.currentTimeMillis()}.jpg"
    val imagesDir = File(context.cacheDir, "camera")
    if (!imagesDir.exists()) imagesDir.mkdirs()
    val imageFile = File(imagesDir, fileName)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

private fun resolveFileName(context: Context, uri: Uri): String? {
    val projection = arrayOf(android.provider.OpenableColumns.DISPLAY_NAME)
    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) {
            return cursor.getString(index)
        }
    }
    return uri.lastPathSegment
}

private fun resolveSizeLabel(context: Context, uri: Uri): String? {
    val projection = arrayOf(android.provider.OpenableColumns.SIZE)
    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
        if (index >= 0 && cursor.moveToFirst()) {
            val bytes = cursor.getLong(index)
            if (bytes > 0) {
                val kb = bytes / 1024.0
                return if (kb < 1024) {
                    String.format("%.0f KB", kb)
                } else {
                    String.format("%.1f MB", kb / 1024.0)
                }
            }
        }
    }
    return null
}

private fun resolveMimeType(context: Context, uri: Uri): String {
    return context.contentResolver.getType(uri) ?: "application/octet-stream"
}

private fun readUriBytes(context: Context, uri: Uri): ByteArray? {
    return context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
}

