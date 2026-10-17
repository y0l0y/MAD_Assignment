package com.example.mad_assignment.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.mad_assignment.ui.components.FolioBanner
import com.example.mad_assignment.ui.components.Tone

/* shown on the book list while there is no internet */
@Composable
fun OfflineBanner(modifier: Modifier = Modifier) {
    FolioBanner(
        title = "You are offline",
        body = "You can still browse the catalog, but borrowing and returning need an internet connection.",
        modifier = modifier,
        tone = Tone.Danger,
        icon = Icons.Outlined.CloudOff
    )
}