package com.example.agreementcomms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

@Composable
fun ChatPane(
    modifier: Modifier = Modifier,
    nickname: String,
    serverName: String,
    selectedChannel: String,
    selectedChannelId: String,
    messages: List<Message>,
    onSendMessage: (String) -> Unit,
    onTyping: () -> Unit,
    typingText: String?,
    backendConnected: Boolean,
    backendError: String?,
    isLoading: Boolean,
    composerAttachment: ComposerAttachment?,
    onPickFromGallery: () -> Unit,
    onPickFile: () -> Unit,
    onTakePhoto: () -> Unit,
    onClearComposerAttachment: () -> Unit,
    compactMode: Boolean,
    onOpenSidebar: () -> Unit,
    onOpenMembers: () -> Unit
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

    // Debounced typing notification
    LaunchedEffect(input) {
        if (input.isNotBlank()) {
            onTyping()
            delay(2000)
        }
    }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onOpenSidebar) {
                    Text("☰", fontSize = 20.sp)
                }
                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Text(
                        text = "# $selectedChannel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = serverName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onOpenMembers) {
                    Text("👥")
                }
                IconButton(onClick = {
                    showSearch = !showSearch
                    if (!showSearch) searchQuery = ""
                }) {
                    Text("🔍")
                }
            }
        }

        if (!backendError.isNullOrBlank()) {
            Text(
                text = backendError,
                modifier = Modifier.padding(horizontal = 14.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (showSearch) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Szukaj w kanale...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Messages Area
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(if (compactMode) 2.dp else 8.dp),
                contentPadding = PaddingValues(bottom = 8.dp, top = 8.dp)
            ) {
                itemsIndexed(visibleMessages, key = { _, m -> m.id.ifBlank { m.hashCode().toString() } }) { index, message ->
                    val groupedWithPrevious =
                        index > 0 &&
                            visibleMessages[index - 1].author == message.author &&
                            visibleMessages[index - 1].isMine == message.isMine
                    MessageItem(
                        message = message,
                        groupedWithPrevious = groupedWithPrevious,
                        compactMode = compactMode
                    )
                }
            }
        }

        // Typing indicator
        Box(modifier = Modifier.height(20.dp).padding(horizontal = 16.dp)) {
            if (!typingText.isNullOrBlank()) {
                Text(
                    text = typingText,
                    style = MaterialTheme.typography.labelSmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Input Area
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                if (composerAttachment != null) {
                    AttachmentPreview(composerAttachment, onClearComposerAttachment)
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box {
                        IconButton(onClick = { attachmentMenuExpanded = true }) {
                            Text("＋", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        DropdownMenu(
                            expanded = attachmentMenuExpanded,
                            onDismissRequest = { attachmentMenuExpanded = false }
                        ) {
                            DropdownMenuItem(text = { Text("🖼 Galeria") }, onClick = { attachmentMenuExpanded = false; onPickFromGallery() })
                            DropdownMenuItem(text = { Text("📷 Aparat") }, onClick = { attachmentMenuExpanded = false; onTakePhoto() })
                            DropdownMenuItem(text = { Text("📎 Plik") }, onClick = { attachmentMenuExpanded = false; onPickFile() })
                        }
                    }

                    TextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = { Text("Napisz na #$selectedChannel") },
                        modifier = Modifier.weight(1f),
                        maxLines = 4,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        enabled = !isLoading
                    )

                    IconButton(
                        onClick = {
                            if (input.isNotBlank() || composerAttachment != null) {
                                onSendMessage(input.trim())
                                input = ""
                            }
                        },
                        enabled = !isLoading && (input.isNotBlank() || composerAttachment != null)
                    ) {
                        Text("➤", fontSize = 24.sp, color = if (input.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentPreview(attachment: ComposerAttachment, onRemove: () -> Unit) {
    Surface(
        modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (attachment.type == AttachmentType.Image) "🖼 " else "📎 ",
                fontSize = 18.sp
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(attachment.name, style = MaterialTheme.typography.labelLarge, maxLines = 1)
                attachment.sizeLabel?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
            }
            IconButton(onClick = onRemove) {
                Text("✕")
            }
        }
    }
}

@Composable
private fun MessageItem(
    message: Message,
    groupedWithPrevious: Boolean,
    compactMode: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = if (groupedWithPrevious) 0.dp else 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        if (!groupedWithPrevious) {
            AvatarBubble(author = message.author, size = if (compactMode) 32.dp else 40.dp)
            Spacer(modifier = Modifier.width(12.dp))
        } else {
            Spacer(modifier = Modifier.width(if (compactMode) 44.dp else 52.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            if (!groupedWithPrevious) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = message.author,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = message.time,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = message.text,
                style = if (compactMode) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            message.attachments.forEach { attachment ->
                Spacer(modifier = Modifier.height(4.dp))
                AttachmentItem(attachment)
            }
        }
    }
}

@Composable
private fun AttachmentItem(attachment: MessageAttachment) {
    if (attachment.type == AttachmentType.Image && attachment.url != null) {
        AsyncImage(
            model = attachment.url,
            contentDescription = attachment.name,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .heightIn(max = 240.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Fit
        )
    } else {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("📄", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(attachment.name, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun AvatarBubble(author: String, size: androidx.compose.ui.unit.Dp = 40.dp) {
    val colors = listOf(Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7), Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF00BCD4), Color(0xFF009688), Color(0xFF4CAF50))
    val bgColor = colors[author.hashCode().let { if (it < 0) -it else it } % colors.size]

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = author.firstOrNull()?.uppercase() ?: "?",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.4).sp
        )
    }
}
