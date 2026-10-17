package com.example.mad_assignment.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mad_assignment.model.other.BookRules
import com.example.mad_assignment.model.other.decodeCover
import com.example.mad_assignment.ui.theme.CoverCream
import com.example.mad_assignment.ui.theme.CoverForest
import com.example.mad_assignment.ui.theme.CoverRed
import com.example.mad_assignment.ui.theme.CoverSlate
import com.example.mad_assignment.ui.theme.CoverTerracotta
import com.example.mad_assignment.ui.theme.FolioSerif
import com.example.mad_assignment.ui.theme.Ink
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

fun formatDay(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))

/* whole days until the due date (0 or negative = due today / already late), display only */
fun daysLeft(dueAt: Long): Int =
    ceil((dueAt - System.currentTimeMillis()).toDouble() / BookRules.DAY_IN_MILLIS).toInt()

@Composable
fun DueDateText(dueAt: Long, modifier: Modifier = Modifier) {
    if (dueAt <= 0L) return
    val overdue = BookRules.isOverdue(dueAt)
    Text(
        text = if (overdue) "Overdue · due ${formatDay(dueAt)}" else "Due ${formatDay(dueAt)}",
        color = if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = if (overdue) FontWeight.Medium else null,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier
    )
}

/*
 real cover (photo or url) when the book has one, otherwise a drawn cover in the
 folio design. Give it the size you want, e.g. Modifier.width(110.dp).aspectRatio(0.72f)
*/
@Composable
fun BookCover(
    coverBase64: String?,
    coverUrl: String?,
    title: String,
    modifier: Modifier = Modifier,
    author: String = ""
) {
    val photo = remember(coverBase64) { decodeCover(coverBase64) }
    val picture: Any? = photo ?: coverUrl
    val shape = RoundedCornerShape(3.dp)
    val framed = modifier
        .shadow(2.dp, shape)
        .clip(shape)
    if (picture != null) {
        AsyncImage(
            model = picture,
            contentDescription = "Cover of $title",
            modifier = framed,
            contentScale = ContentScale.Crop
        )
    } else {
        PlaceholderCover(title = title, author = author, modifier = framed)
    }
}

private val coverPalette = listOf(
    CoverTerracotta to Color.White,
    CoverCream to Ink,
    CoverForest to Color.White,
    CoverSlate to Ink,
    CoverRed to Color.White
)

/* the colour is picked from the title, so a book always keeps the same cover */
@Composable
private fun PlaceholderCover(title: String, author: String, modifier: Modifier) {
    val (bg, fg) = coverPalette[(title.hashCode() and Int.MAX_VALUE) % coverPalette.size]
    BoxWithConstraints(modifier.background(bg)) {
        val base = if (maxWidth == Dp.Infinity) 60.dp else maxWidth
        val titleSize = (base.value * 0.13f).coerceIn(7f, 26f)
        val pad = (titleSize * 0.6f).dp
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            if (author.isNotBlank() && titleSize >= 9f) {
                Text(
                    text = author.uppercase(),
                    color = fg.copy(alpha = 0.8f),
                    fontSize = (titleSize * 0.5f).sp,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(pad / 2))
            }
            Text(
                text = title,
                color = fg,
                fontFamily = FolioSerif,
                fontSize = titleSize.sp,
                lineHeight = (titleSize * 1.1f).sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.weight(1f))
            Box(
                Modifier
                    .align(Alignment.End)
                    .size((titleSize * 1.8f).dp)
                    .clip(CircleShape)
                    .background(fg.copy(alpha = 0.22f))
            )
        }
    }
}