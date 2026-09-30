package com.example.mad_assignment.Model

import android.content.Context
import android.net.Uri
import java.io.File

class CoverStorage(context: Context) {
    private val appContext = context.applicationContext

    fun copy(uri: Uri): String? {
        /* try to save the book cover picture in the inner storage and return an absolute file path*/
        /* So, even the user delete the book cover picture in the gallery, it also won't affect the app */
        /* can using the view->Tool Windows->Device Explorer to find the file path  */
        /* the file path is under /data/data/com.example.mad_assignment/files */
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

    /* delete the book cover pic from the fileDir*/
    fun delete(path: String) {
        File(path).delete()
    }
}