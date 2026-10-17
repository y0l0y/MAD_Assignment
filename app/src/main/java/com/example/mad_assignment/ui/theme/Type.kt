package com.example.mad_assignment.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// System fallbacks so this compiles with zero setup.
// The design uses a serif (Newsreader / Source Serif) + a sans (Inter).
// To use them, add the font files to res/font and replace these two lines, e.g.:
//   val FolioSerif = FontFamily(Font(R.font.newsreader_regular))
val FolioSerif: FontFamily = FontFamily.Serif
val FolioSans: FontFamily = FontFamily.SansSerif

val FolioTypography = Typography(
    // Screen hero: "A good book, a little closer."
    displayLarge = TextStyle(
        fontFamily = FolioSerif, fontWeight = FontWeight.Normal,
        fontSize = 38.sp, lineHeight = 42.sp, letterSpacing = (-0.5).sp,
    ),
    // Book title on detail / edit screens
    displayMedium = TextStyle(
        fontFamily = FolioSerif, fontWeight = FontWeight.Normal,
        fontSize = 34.sp, lineHeight = 38.sp, letterSpacing = (-0.3).sp,
    ),
    // Big dates, stat numbers, section titles
    headlineMedium = TextStyle(
        fontFamily = FolioSerif, fontWeight = FontWeight.Normal,
        fontSize = 28.sp, lineHeight = 32.sp,
    ),
    // Dialog titles
    headlineSmall = TextStyle(
        fontFamily = FolioSerif, fontWeight = FontWeight.Normal,
        fontSize = 24.sp, lineHeight = 30.sp,
    ),
    // Card titles, top bar titles
    titleLarge = TextStyle(
        fontFamily = FolioSerif, fontWeight = FontWeight.Normal,
        fontSize = 20.sp, lineHeight = 26.sp,
    ),
    // Grid tile titles
    titleMedium = TextStyle(
        fontFamily = FolioSerif, fontWeight = FontWeight.Normal,
        fontSize = 18.sp, lineHeight = 24.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FolioSans, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FolioSans, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FolioSans, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FolioSans, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp,
    ),
    // Buttons
    labelLarge = TextStyle(
        fontFamily = FolioSans, fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp, lineHeight = 20.sp,
    ),
    // Chips, tab labels
    labelMedium = TextStyle(
        fontFamily = FolioSans, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp,
    ),
    // Eyebrows ("THE CLUB'S SHARED SHELF") - call .uppercase() on the text
    labelSmall = TextStyle(
        fontFamily = FolioSans, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.08.em,
    ),
)
