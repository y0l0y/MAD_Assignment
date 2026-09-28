package com.example.mad_assignment.Model

import android.content.Context
import android.net.Uri
import java.io.File

class CoverStorage(context: Context) {
    private val appContext = context.applicationContext

    fun copy(uri: Uri): String? {
        return try {
            val input = appContext.contentResolver.openInputStream(uri) ?: return null
            val file = File(appContext.filesDir, "cover_${System.currentTimeMillis()}.jpg")
            input.use { inStream ->
                file.outputStream().use {
                    outStream -> inStream.copyTo(outStream)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun delete(path: String) {
        File(path).delete()
    }
}