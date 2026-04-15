package com.example.agreementcomms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.agreementcomms.data.AccordanceApiClient
import com.example.agreementcomms.ui.theme.AgreementCommsTheme
import kotlinx.coroutines.launch

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
    var isLoggedIn by rememberSaveable { mutableStateOf(false) }
    var nickname by rememberSaveable { mutableStateOf("") }
    val appScope = rememberCoroutineScope()

    var servers by remember { mutableStateOf(defaultServers()) }

    var selectedServerId by rememberSaveable { mutableStateOf(servers.first().id) }
    var selectedChannel by rememberSaveable { mutableStateOf(servers.first().channels.first()) }
    var section by rememberSaveable { mutableStateOf(MainSection.Chat) }

    val conversations = remember {
        mutableStateMapOf<String, SnapshotStateList<Message>>().apply {
            putAll(buildSampleConversations())
        }
    }
    val channelApiIds = remember { mutableStateMapOf<String, String>() }
    var backendConnected by rememberSaveable { mutableStateOf(false) }
    var backendError by rememberSaveable { mutableStateOf<String?>(null) }
    var backendTried by rememberSaveable { mutableStateOf(false) }
    val unreadCounts = remember {
        mutableStateMapOf(
            conversationKey("general", "#offtopic") to 3,
            conversationKey("mobile-dev", "#android") to 2,
            conversationKey("szkola", "#terminy") to 1
        )
    }

    if (!isLoggedIn) {
        LoginScreen(
            nickname = nickname,
            onNicknameChange = { nickname = it },
            onEnter = { if (nickname.isNotBlank()) isLoggedIn = true }
        )
    } else {
        LaunchedEffect(isLoggedIn) {
            if (isLoggedIn && !backendTried) {
                backendTried = true
                try {
                    val payload = fetchBackendBootstrap(nickname)
                    if (payload.servers.isNotEmpty()) {
                        servers = payload.servers
                        conversations.clear()
                        conversations.putAll(payload.conversations)
                        channelApiIds.clear()
                        channelApiIds.putAll(payload.channelApiIds)
                        selectedServerId = payload.servers.first().id
                        selectedChannel = payload.servers.first().channels.firstOrNull().orEmpty()
                        backendConnected = true
                        backendError = null
                    }
                } catch (ex: Exception) {
                    backendConnected = false
                    backendError = ex.message ?: "Nie udało się połączyć z backendem"
                }
            }
        }

        val activeConversationKey = conversationKey(selectedServerId, selectedChannel)
        val activeMessages = conversations.getOrPut(activeConversationKey) { mutableStateListOf() }

        MainScreen(
            nickname = nickname,
            servers = servers,
            section = section,
            onSectionChange = { section = it },
            selectedServerId = selectedServerId,
            onServerSelected = { serverId ->
                selectedServerId = serverId
                val firstChannel = servers.first { it.id == serverId }.channels.first()
                selectedChannel = firstChannel
                unreadCounts[conversationKey(serverId, firstChannel)] = 0
            },
            selectedChannel = selectedChannel,
            onChannelSelected = {
                selectedChannel = it
                unreadCounts[conversationKey(selectedServerId, it)] = 0
            },
            unreadCounts = unreadCounts,
            backendConnected = backendConnected,
            backendError = backendError,
            messages = activeMessages,
            onSendMessage = { messageText ->
                val conversationKey = conversationKey(selectedServerId, selectedChannel)
                conversations
                    .getOrPut(conversationKey) { mutableStateListOf() }
                    .add(
                    Message(
                        author = nickname,
                        text = messageText,
                        time = "teraz",
                        isMine = true
                    )
                )

                val apiChannelId = channelApiIds[conversationKey]
                if (apiChannelId != null) {
                    appScope.launch {
                        try {
                            AccordanceApiClient.api.createMessage(
                                serverId = selectedServerId,
                                channelId = apiChannelId,
                                request = com.example.agreementcomms.data.CreateMessageRequest(
                                    author = nickname,
                                    text = messageText
                                )
                            )
                            backendConnected = true
                            backendError = null
                        } catch (ex: Exception) {
                            backendConnected = false
                            backendError = ex.message ?: "Wysłanie do backendu nie powiodło się"
                        }
                    }
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
                onOpenSidebar = { scope.launch { drawerState.open() } }
            )

            MainSection.Settings -> SettingsScreen(
                modifier = Modifier.fillMaxSize(),
                nickname = nickname,
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
                Text("←")
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
    onOpenSidebar: () -> Unit
) {
    var input by rememberSaveable { mutableStateOf("") }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val visibleMessages = if (searchQuery.isBlank()) {
        messages
    } else {
        messages.filter {
            it.author.contains(searchQuery, ignoreCase = true) ||
                it.text.contains(searchQuery, ignoreCase = true)
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
                Text("←")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = selectedChannel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
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

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Szukaj w kanale") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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

        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Napisz wiadomość") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(
                onClick = {
                    if (input.isNotBlank()) {
                        onSendMessage(input.trim())
                        input = ""
                    }
                },
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Wyślij")
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
    onOpenSidebar: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onOpenSidebar) {
                Text("←")
            }
            Text(
                text = "Ustawienia",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "Panel konta i projektu",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "Konto", fontWeight = FontWeight.SemiBold)
                Text(text = "Zalogowany jako: $nickname")
                Text(text = "Status: online")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "Projekt", fontWeight = FontWeight.SemiBold)
                Text(text = "Etap 1 gotowy ✅")
                Text(
                    text = "Następny krok: backend i prawdziwe pokoje.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        TextButton(onClick = { }, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Wyloguj (mock)")
        }
    }
}

private data class Server(
    val id: String,
    val name: String,
    val icon: String,
    val channels: List<String>
)

private data class Message(
    val author: String,
    val text: String,
    val time: String,
    val isMine: Boolean
)

private enum class MainSection {
    Chat,
    Settings
}

private fun conversationKey(serverId: String, channel: String): String = "$serverId|$channel"

private fun buildSampleConversations(): MutableMap<String, SnapshotStateList<Message>> {
    return mutableMapOf(
        conversationKey("general", "#ogólny") to mutableStateListOf(
            Message("Ola", "Ej, kto ma plan na wieczór?", "17:05", false),
            Message("Bartek", "Ja klasycznie: serial + kebs 😎", "17:06", false),
            Message("Natalia", "Brzmi uczciwie", "17:07", false),
            Message("Kuba", "Ja może wyskoczę na kosza", "17:09", false),
            Message("Ola", "Jak coś to jestem chętna po 19", "17:10", false),
            Message("Bartek", "To ja tylko buty ogarnę i lecimy", "17:11", false),
            Message("Natalia", "W końcu aktywnie, wow", "17:12", false),
            Message("Kuba", "screen tego momentu 📸", "17:13", false)
        ),
        conversationKey("general", "#nauka") to mutableStateListOf(
            Message("Ola", "Czy tylko ja się uczę lepiej w nocy?", "18:11", false),
            Message("Bartek", "+1, po 22 nagle mózg: let's go", "18:12", false),
            Message("Natalia", "A rano ten sam mózg: nope", "18:13", false),
            Message("Kuba", "Rel", "18:13", false),
            Message("Ola", "Polecacie jakieś lofi playlisty?", "18:14", false),
            Message("Bartek", "lofi girl i zero powiadomień", "18:15", false)
        ),
        conversationKey("general", "#offtopic") to mutableStateListOf(
            Message("Kuba", "wrzucam mema dnia", "19:21", false),
            Message("Kuba", "[meme: 'when code works on first run']", "19:21", false),
            Message("Ola", "to fake, takie rzeczy nie istnieją", "19:22", false),
            Message("Bartek", "dokładnie, to AI-generated", "19:23", false),
            Message("Natalia", "xDDD", "19:23", false),
            Message("Kuba", "mam jeszcze jednego z kotem programistą", "19:24", false),
            Message("Ola", "dawaj", "19:24", false),
            Message("Kuba", "[meme: kot przy 4 monitorach]", "19:25", false),
            Message("Bartek", "to ja po 3 kawie", "19:25", false),
            Message("Natalia", "i z deadline'em za 15 minut", "19:26", false),
            Message("Ola", "literally", "19:26", false),
            Message("Kuba", "ok koniec spamu, pa 😅", "19:27", false)
        ),
        conversationKey("mobile-dev", "#android") to mutableStateListOf(
            Message("Bartek", "Czy tylko mnie emulator czasem nienawidzi?", "16:45", false),
            Message("Ola", "Cold boot i modlitwa", "16:46", false),
            Message("Natalia", "Najskuteczniejszy fix ever", "16:48", false),
            Message("Kuba", "u mnie działa dopiero po restartcie laptopa", "16:49", false),
            Message("Bartek", "to już rytuał", "16:50", false),
            Message("Ola", "przynajmniej adb jeszcze żyje", "16:51", false),
            Message("Natalia", "czasem…", "16:51", false)
        ),
        conversationKey("mobile-dev", "#ios") to mutableStateListOf(
            Message("Kuba", "Ktoś faktycznie lubi Xcode? pytam dla kolegi", "15:30", false),
            Message("Ola", "lubię… jak się nie crashuje", "15:31", false),
            Message("Bartek", "czyli 2 razy w miesiącu?", "15:33", false),
            Message("Natalia", "💀", "15:33", false)
        ),
        conversationKey("mobile-dev", "#react-native") to mutableStateListOf(
            Message("Natalia", "RN hot reload to nadal magia", "14:12", false),
            Message("Kuba", "true, to jest najlepsza część", "14:13", false)
        ),
        conversationKey("szkola", "#projekt") to mutableStateListOf(
            Message("Natalia", "Kto widział moją czarną bluzę z kapturem?", "13:40", false),
            Message("Bartek", "ta z małym logo?", "13:41", false),
            Message("Natalia", "tak", "13:41", false),
            Message("Ola", "chyba została w sali obok okna", "13:42", false),
            Message("Natalia", "ratujecie życie, dzięki", "13:43", false)
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
            Message("Natalia", "update: jednak normalnie jest", "19:48", false)
        ),
        conversationKey("szkola", "#pomoc") to mutableStateListOf(
            Message("Ola", "Jak usunąć plamę po kawie z notatek?", "10:10", false),
            Message("Kuba", "ryż 😂", "10:11", false),
            Message("Natalia", "Kuba pls", "10:11", false),
            Message("Bartek", "chusteczki + delikatnie wodą, tylko nie trzeć mocno", "10:12", false),
            Message("Ola", "ok, testuję", "10:13", false),
            Message("Ola", "działa, dzięki!", "10:16", false)
        )
    )
}

private fun defaultServers(): List<Server> {
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

private data class BackendBootstrap(
    val servers: List<Server>,
    val conversations: Map<String, SnapshotStateList<Message>>,
    val channelApiIds: Map<String, String>
)

private suspend fun fetchBackendBootstrap(nickname: String): BackendBootstrap {
    val api = AccordanceApiClient.api
    val apiServers = api.getServers()

    val mappedServers = mutableListOf<Server>()
    val mappedConversations = mutableMapOf<String, SnapshotStateList<Message>>()
    val channelApiIds = mutableMapOf<String, String>()

    for (server in apiServers) {
        val channels = api.getChannels(server.id)
        mappedServers.add(
            Server(
                id = server.id,
                name = server.name,
                icon = server.icon,
                channels = channels.map { it.name }
            )
        )

        for (channel in channels) {
            val key = conversationKey(server.id, channel.name)
            channelApiIds[key] = channel.id

            val messages = api.getMessages(server.id, channel.id)
            mappedConversations[key] = mutableStateListOf<Message>().apply {
                addAll(
                    messages.map {
                        Message(
                            author = it.author,
                            text = it.text,
                            time = it.time,
                            isMine = it.author.equals(nickname, ignoreCase = true)
                        )
                    }
                )
            }
        }
    }

    return BackendBootstrap(
        servers = mappedServers,
        conversations = mappedConversations,
        channelApiIds = channelApiIds
    )
}