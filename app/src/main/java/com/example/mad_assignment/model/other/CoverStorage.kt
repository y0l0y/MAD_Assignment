package com.example.mad_assignment.model.other

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream

class CoverStorage(context: Context) {
    private val appContext = context.applicationContext

    private companion object {
        const val MAX_SIZE = 400
        const val QUALITY = 70 // JPEG quality, 0 to 100
        const val MAX_BASE64_LENGTH = 700_000 // max text length (keep document < 1MiB)
    }

    /* return base64 text in String or null */
    fun encode(uri: Uri): String? {
        return try {
            val resolver = appContext.contentResolver
            /* read the size of the pic without load the pic */
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            /* use to calculate how much to shrink while loading */
            var sample = 1
            while (
                bounds.outWidth / (sample * 2) >= MAX_SIZE && bounds.outHeight / (sample * 2) >= MAX_SIZE
            ) sample *= 2

            /* load the smaller picture based on the sample */
            val options = BitmapFactory.Options().apply { inSampleSize = sample }
            val bitmap = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return null

            /* resize it to exact 400 px */
            val longest = maxOf(bitmap.width, bitmap.height)
            val scaled = if (longest > MAX_SIZE) {
                val ratio = MAX_SIZE.toFloat() / longest
                bitmap.scale(
                    (bitmap.width * ratio).toInt().coerceAtLeast(1),
                    (bitmap.height * ratio).toInt().coerceAtLeast(1)
                )
            } else bitmap

            /* compress to JPEG */
            val out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, QUALITY, out)
            /* compress the JPEG and convert to a String */
            val text = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
            /* recheck the size */
            if (text.length > MAX_BASE64_LENGTH) null else text
        } catch (_: Exception) {
            null
        }
    }
}

/* convert base64 String to bitmap for AsyncImage */
fun decodeCover(base64: String?): Bitmap? {
    if (base64.isNullOrEmpty()) return null
    return try {
        val bytes = Base64.decode(base64, Base64.NO_WRAP)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (_: IllegalArgumentException) {
        null
    }
}