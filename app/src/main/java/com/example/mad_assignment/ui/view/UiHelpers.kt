package com.example.mad_assignment.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mad_assignment.model.other.BookRules
import com.example.mad_assignment.model.other.decodeCover
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDay(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))

@Composable
fun DueDateText(dueAt: Long) {
    if (dueAt <= 0L) return
    val overdue = BookRules.isOverdue(dueAt)
    Text(
        text = if (overdue) "Overdue! Due: ${formatDay(dueAt)}" else "Due: ${formatDay(dueAt)}",
        color = if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
fun BookCover(
    coverBase64: String?,
    coverUrl: String?,
    title: String,
    modifier: Modifier = Modifier
) {
    val photo = remember(coverBase64) { decodeCover(coverBase64) }
    val picture: Any? = photo ?: coverUrl
    val shape = RoundedCornerShape(6.dp)
    if (picture != null) {
        AsyncImage(
            model = picture,
            contentDescription = "Cover of $title",
            modifier = modifier.clip(shape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Book,
                contentDescription = "No book cover",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}




