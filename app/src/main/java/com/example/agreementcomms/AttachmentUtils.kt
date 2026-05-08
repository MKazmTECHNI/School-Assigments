package com.example.agreementcomms

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun createTempCameraUri(context: Context): Uri {
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

fun resolveFileName(context: Context, uri: Uri): String? {
    val projection = arrayOf(android.provider.OpenableColumns.DISPLAY_NAME)
    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) {
            return cursor.getString(index)
        }
    }
    return uri.lastPathSegment
}

fun resolveSizeLabel(context: Context, uri: Uri): String? {
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

fun resolveMimeType(context: Context, uri: Uri): String {
    return context.contentResolver.getType(uri) ?: "application/octet-stream"
}

fun readUriBytes(context: Context, uri: Uri): ByteArray? {
    return context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
}
