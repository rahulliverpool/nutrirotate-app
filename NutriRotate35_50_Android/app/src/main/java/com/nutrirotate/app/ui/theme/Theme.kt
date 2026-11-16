package com.nutrirotate.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BgDark = Color(0xFF0B0F17)
val CardDark = Color(0xFF131A26)
val CardBorder = Color(0xFF1E293B)
val EmeraldAccent = Color(0xFF34D399)
val TextPrimary = Color(0xFFF1F5F9)
val TextMuted = Color(0xFF94A3B8)

private val DarkPalette = darkColorScheme(
    primary = EmeraldAccent,
    background = BgDark,
    surface = CardDark,
    onPrimary = Color(0xFF052E16),
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun NutriRotateTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkPalette, content = content)
}
