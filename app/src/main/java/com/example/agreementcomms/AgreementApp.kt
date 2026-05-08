package com.example.agreementcomms

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agreementcomms.ui.theme.AgreementCommsTheme

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
