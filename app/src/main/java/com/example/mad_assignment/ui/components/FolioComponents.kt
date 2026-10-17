package com.example.mad_assignment.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mad_assignment.ui.theme.FolioSerif

/*
 Shared building blocks from the folio design.
 They only read MaterialTheme.colorScheme / typography (see ui/theme), so they
 follow the light / dark switch automatically. No data or logic lives here.
*/

/* ------------------------------------------------------------------ */
/* Screen size                                                         */
/* ------------------------------------------------------------------ */

/* phones in landscape, tablets and wide multi-window panes count as "wide" */
const val WIDE_SCREEN_DP = 600

/* re-evaluated on rotation / window resize */
@Composable
fun isWideScreen(): Boolean = LocalConfiguration.current.screenWidthDp >= WIDE_SCREEN_DP

/* ------------------------------------------------------------------ */
/* Text helpers                                                        */
/* ------------------------------------------------------------------ */

@Composable
fun Eyebrow(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Text(text.uppercase(), modifier, color = color, style = MaterialTheme.typography.labelSmall)
}

@Composable
fun Muted(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        text = text,
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = style,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

/* "Ada Lovelace" -> "AL", "ada" -> "AD" */
fun initialsOf(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> (parts.first().take(1) + parts.last().take(1)).uppercase()
    }
}

/* ------------------------------------------------------------------ */
/* Top bars                                                            */
/* ------------------------------------------------------------------ */

/* home screen: menu button + folio logo + something on the right */
@Composable
fun FolioHomeBar(
    onMenu: () -> Unit,
    trailing: @Composable () -> Unit = {}
) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.background(cs.background).statusBarsPadding()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenu) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Menu",
                    tint = cs.onBackground
                )
            }
            Icon(Icons.Outlined.MenuBook, null, Modifier.size(24.dp), tint = cs.primary)
            Spacer(Modifier.width(6.dp))
            Text("CIS", fontFamily = FolioSerif, fontSize = 28.sp, color = cs.primary)
            Spacer(Modifier.width(10.dp))
            Column {
                Text("CURTIN ISLAMIC", style = MaterialTheme.typography.labelSmall, color = cs.onBackground, fontSize = 10.sp)
                Text("SOCIETY LIBRARY", style = MaterialTheme.typography.labelSmall, color = cs.onBackground, fontSize = 10.sp)
            }
            Spacer(Modifier.weight(1f))
            trailing()
        }
        HorizontalDivider(color = cs.outline)
    }
}

/* every other screen: back arrow + title */
@Composable
fun FolioBackBar(
    title: String,
    onBack: () -> Unit,
    trailing: @Composable () -> Unit = {}
) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.background(cs.background).statusBarsPadding()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = cs.onBackground
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = cs.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            trailing()
        }
        HorizontalDivider(color = cs.outline)
    }
}

@Composable
fun Avatar(initials: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier
            .size(40.dp)
            .clip(CircleShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .background(cs.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(initials, color = cs.primary, style = MaterialTheme.typography.labelLarge, fontSize = 13.sp)
    }
}

@Composable
fun CommitteeBadge(modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(cs.primary)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            "COMMITTEE",
            color = cs.onPrimary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/* ------------------------------------------------------------------ */
/* Chips                                                               */
/* ------------------------------------------------------------------ */

enum class Tone { Good, Neutral, Warn, Danger }

/* (background, foreground) for a tone */
@Composable
private fun toneColors(tone: Tone): Pair<Color, Color> {
    val cs = MaterialTheme.colorScheme
    return when (tone) {
        Tone.Good -> cs.primaryContainer to cs.primary
        Tone.Neutral -> cs.surfaceVariant to cs.onSurface
        Tone.Warn -> cs.tertiaryContainer to cs.tertiary
        Tone.Danger -> cs.errorContainer to cs.error
    }
}

/* small status pill with a dot: Available, Overdue, Return pending ... */
@Composable
fun StatusChip(text: String, tone: Tone, modifier: Modifier = Modifier) {
    val (bg, fg) = toneColors(tone)
    Row(
        modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(fg))
        Spacer(Modifier.width(6.dp))
        Text(text, color = fg, style = MaterialTheme.typography.labelMedium)
    }
}

/* round filter / sort / tab pill */
@Composable
fun FolioChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(50)
    Box(
        modifier
            .clip(shape)
            .background(if (selected) cs.primary else Color.Transparent)
            .border(1.dp, if (selected) cs.primary else cs.outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            color = if (selected) cs.onPrimary else cs.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            fontSize = 13.sp
        )
    }
}

/* horizontally scrollable row for FolioChip */
@Composable
fun FolioChipRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

/* ------------------------------------------------------------------ */
/* Cards, banners, stats, empty state                                  */
/* ------------------------------------------------------------------ */

@Composable
fun FolioCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(cs.surface)
            .border(1.dp, cs.outline, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun FolioBanner(
    body: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    tone: Tone = Tone.Good,
    icon: ImageVector = Icons.Outlined.Info
) {
    val cs = MaterialTheme.colorScheme
    val (bg, fg) = toneColors(tone)
    val textColor = if (tone == Tone.Danger) cs.onErrorContainer else cs.onSurface
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(16.dp)
    ) {
        Icon(icon, null, Modifier.size(20.dp), tint = fg)
        Spacer(Modifier.width(12.dp))
        Column {
            if (title != null) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Spacer(Modifier.height(2.dp))
            }
            Text(body, style = MaterialTheme.typography.bodyMedium, color = textColor.copy(alpha = 0.85f))
        }
    }
}

@Composable
fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier
            .clip(shape)
            .background(cs.surface)
            .border(1.dp, cs.outline, shape)
            .padding(14.dp)
    ) {
        Text(value, color = valueColor, style = MaterialTheme.typography.headlineMedium.copy(fontSize = 32.sp))
        Muted(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.MenuBook
) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(64.dp).clip(CircleShape).background(cs.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = cs.primary)
        }
        Spacer(Modifier.height(16.dp))
        Muted(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    }
}

/* ------------------------------------------------------------------ */
/* Inputs and buttons                                                  */
/* ------------------------------------------------------------------ */

val FolioFieldShape = RoundedCornerShape(12.dp)

/* apply to every OutlinedTextField:  shape = FolioFieldShape, colors = folioFieldColors() */
@Composable
fun folioFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    cursorColor = MaterialTheme.colorScheme.primary
)

/* looks like a search field but just opens the real search screen */
@Composable
fun SearchEntry(placeholder: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier
            .fillMaxWidth()
            .clip(FolioFieldShape)
            .background(cs.surface)
            .border(1.dp, cs.outline, FolioFieldShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Search, null, tint = cs.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Text(
            placeholder,
            color = cs.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun FolioButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    filled: Boolean = true,
    enabled: Boolean = true,
    loading: Boolean = false,
    height: Dp = 52.dp
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(10.dp)
    val content: @Composable RowScope.() -> Unit = {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = if (filled) cs.onPrimary else cs.primary
            )
        } else {
            if (icon != null) {
                Icon(icon, null, Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
    if (filled) {
        Button(
            onClick = onClick,
            modifier = modifier.fillMaxWidth().height(height),
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = cs.primary, contentColor = cs.onPrimary),
            content = content
        )
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.fillMaxWidth().height(height),
            enabled = enabled,
            shape = shape,
            border = BorderStroke(1.dp, cs.outline),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = cs.surface, contentColor = cs.primary),
            content = content
        )
    }
}