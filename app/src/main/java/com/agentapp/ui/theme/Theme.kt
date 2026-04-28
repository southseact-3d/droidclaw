package com.agentapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Palette — dark industrial with amber pulse ────────────────────────────────

val Obsidian     = Color(0xFF0D0D0F)
val Surface1     = Color(0xFF141416)
val Surface2     = Color(0xFF1C1C20)
val Surface3     = Color(0xFF242428)
val Border       = Color(0xFF2E2E34)
val BorderBright = Color(0xFF3D3D45)
val TextPrimary  = Color(0xFFE8E8EC)
val TextSecondary= Color(0xFF8A8A94)
val TextMuted    = Color(0xFF55555F)
val Amber        = Color(0xFFF5A623)
val AmberDim     = Color(0xFF8A5C10)
val AmberGlow    = Color(0xFFFFBF47)
val Green        = Color(0xFF4CAF82)
val Red          = Color(0xFFE05555)
val Blue         = Color(0xFF5B9BF5)

private val DarkColorScheme = darkColorScheme(
    primary          = Amber,
    onPrimary        = Obsidian,
    primaryContainer = AmberDim,
    onPrimaryContainer = AmberGlow,
    secondary        = TextSecondary,
    onSecondary      = Obsidian,
    background       = Obsidian,
    onBackground     = TextPrimary,
    surface          = Surface1,
    onSurface        = TextPrimary,
    surfaceVariant   = Surface2,
    onSurfaceVariant = TextSecondary,
    outline          = Border,
    outlineVariant   = BorderBright,
    error            = Red,
    onError          = Color.White,
    tertiary         = Green,
    onTertiary       = Obsidian
)

@Composable
fun AgentAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        content = content
    )
}

// ── Typography — JetBrains Mono feel via system monospace fallback ────────────

val AppTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 32.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 26.sp, letterSpacing = (-0.25).sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    headlineMedium= TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, letterSpacing = 0.1.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 14.sp, letterSpacing = 0.1.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal,  fontSize = 15.sp, lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal,  fontSize = 13.sp, lineHeight = 20.sp),
    bodySmall     = TextStyle(fontWeight = FontWeight.Normal,  fontSize = 11.sp, lineHeight = 16.sp),
    labelLarge    = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 13.sp, letterSpacing = 0.5.sp),
    labelMedium   = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 11.sp, letterSpacing = 0.5.sp),
    labelSmall    = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 10.sp, letterSpacing = 0.8.sp),
)
