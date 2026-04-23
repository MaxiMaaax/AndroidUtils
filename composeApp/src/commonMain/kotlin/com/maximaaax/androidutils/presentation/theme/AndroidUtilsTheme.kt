package com.maximaaax.androidutils.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Couleurs du thème (design « liquid glass », dark-first).
 */
data object AndroidUtilsPalette {
    val BackgroundTop: Color = Color(0xFF06070F)
    val BackgroundBottom: Color = Color(0xFF100A1F)
    val GlowViolet: Color = Color(0xFF8B5CF6)
    val GlowIndigo: Color = Color(0xFF4F46E5)
    val GlowCyan: Color = Color(0xFF22D3EE)
    val GlowPink: Color = Color(0xFFEC4899)
    val Surface: Color = Color(0xFF0F0F18)
    val SurfaceElevated: Color = Color(0xFF15151F)
    val SurfaceMuted: Color = Color(0xFF1C1C28)
    const val GlassFillTop: Long = 0x1AFFFFFF
    const val GlassFillBottom: Long = 0x08FFFFFF
    const val GlassBorderTop: Long = 0x40FFFFFF
    const val GlassBorderBottom: Long = 0x10FFFFFF
    const val GlassHover: Long = 0x24FFFFFF
    val Accent: Color = Color(0xFFA78BFA)
    val AccentStrong: Color = Color(0xFF8B5CF6)
    val AccentSoft: Color = Color(0xFFC4B5FD)
    val OnBackground: Color = Color(0xFFF6F6FA)
    val OnBackgroundMuted: Color = Color(0xFFA5A5BD)
    val OnBackgroundDim: Color = Color(0xFF6B6B82)
    val Danger: Color = Color(0xFFF87171)
    val Success: Color = Color(0xFF34D399)
    val Warning: Color = Color(0xFFFBBF24)
}

@Immutable
data class OlaniExtraTokens(
    val glassFillTop: Color = Color(AndroidUtilsPalette.GlassFillTop),
    val glassFillBottom: Color = Color(AndroidUtilsPalette.GlassFillBottom),
    val glassBorderTop: Color = Color(AndroidUtilsPalette.GlassBorderTop),
    val glassBorderBottom: Color = Color(AndroidUtilsPalette.GlassBorderBottom),
    val glassHover: Color = Color(AndroidUtilsPalette.GlassHover),
    val glowViolet: Color = AndroidUtilsPalette.GlowViolet,
    val glowIndigo: Color = AndroidUtilsPalette.GlowIndigo,
    val glowCyan: Color = AndroidUtilsPalette.GlowCyan,
    val glowPink: Color = AndroidUtilsPalette.GlowPink,
    val textMuted: Color = AndroidUtilsPalette.OnBackgroundMuted,
    val textDim: Color = AndroidUtilsPalette.OnBackgroundDim,
    val success: Color = AndroidUtilsPalette.Success,
    val warning: Color = AndroidUtilsPalette.Warning,
    val eyebrow: TextStyle = TextStyle(
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = FontFamily.Default,
        letterSpacing = 1.6.sp,
    ),
)

val LocalOlaniTokens = staticCompositionLocalOf { OlaniExtraTokens() }

@Composable
fun AndroidUtilsTheme(content: @Composable () -> Unit) {
    val colorScheme: ColorScheme = darkColorScheme(
        primary = AndroidUtilsPalette.Accent,
        onPrimary = Color(0xFF0A0812),
        primaryContainer = Color(0xFF3D2A6B),
        onPrimaryContainer = AndroidUtilsPalette.OnBackground,
        secondary = AndroidUtilsPalette.GlowViolet,
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = AndroidUtilsPalette.SurfaceElevated,
        onSecondaryContainer = AndroidUtilsPalette.OnBackground,
        tertiary = AndroidUtilsPalette.GlowPink,
        onTertiary = Color(0xFF1A0A10),
        tertiaryContainer = Color(0xFF5A1F3D),
        onTertiaryContainer = AndroidUtilsPalette.OnBackground,
        error = AndroidUtilsPalette.Danger,
        onError = Color(0xFF1A0505),
        errorContainer = Color(0xFF3D1515),
        onErrorContainer = Color(0xFFFFDAD4),
        background = AndroidUtilsPalette.BackgroundTop,
        onBackground = AndroidUtilsPalette.OnBackground,
        surface = AndroidUtilsPalette.Surface,
        onSurface = AndroidUtilsPalette.OnBackground,
        surfaceVariant = AndroidUtilsPalette.SurfaceMuted,
        onSurfaceVariant = AndroidUtilsPalette.OnBackgroundMuted,
        surfaceContainer = AndroidUtilsPalette.SurfaceElevated,
        surfaceContainerHigh = AndroidUtilsPalette.SurfaceElevated,
        surfaceContainerHighest = AndroidUtilsPalette.SurfaceMuted,
        surfaceDim = Color(0xFF080810),
        surfaceBright = AndroidUtilsPalette.SurfaceElevated,
        surfaceContainerLow = AndroidUtilsPalette.Surface,
        surfaceContainerLowest = Color(0xFF040408),
        outline = Color(0x33FFFFFF),
        outlineVariant = Color(0x1FFFFFFF),
        scrim = Color(0x66000000),
    )

    val defaultTypography = Typography()
    val lsDisplay = -0.6.sp
    val lsHeadline = -0.5.sp
    val lsTitle = -0.3.sp
    val typography = Typography(
        displayLarge = defaultTypography.displayLarge.withSemiBoldTight().copy(letterSpacing = lsDisplay),
        displayMedium = defaultTypography.displayMedium.withSemiBoldTight().copy(letterSpacing = lsDisplay),
        displaySmall = defaultTypography.displaySmall.withSemiBoldTight().copy(letterSpacing = lsDisplay),
        headlineLarge = defaultTypography.headlineLarge.withSemiBoldTight().copy(letterSpacing = lsHeadline),
        headlineMedium = defaultTypography.headlineMedium.withSemiBoldTight().copy(letterSpacing = lsHeadline),
        headlineSmall = defaultTypography.headlineSmall.withSemiBoldTight().copy(letterSpacing = lsHeadline),
        titleLarge = defaultTypography.titleLarge.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = lsTitle,
        ),
        titleMedium = defaultTypography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = lsTitle,
        ),
        titleSmall = defaultTypography.titleSmall.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = lsTitle,
        ),
        bodyLarge = defaultTypography.bodyLarge,
        bodyMedium = defaultTypography.bodyMedium,
        bodySmall = defaultTypography.bodySmall,
        labelLarge = defaultTypography.labelLarge,
        labelMedium = defaultTypography.labelMedium,
        labelSmall = defaultTypography.labelSmall,
    )

    val extra = OlaniExtraTokens()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
    ) {
        CompositionLocalProvider(LocalOlaniTokens provides extra, content = content)
    }
}

private fun TextStyle.withSemiBoldTight() = copy(
    fontWeight = FontWeight.SemiBold,
)

@Composable
@ReadOnlyComposable
fun olaniTokens(): OlaniExtraTokens = LocalOlaniTokens.current

@Composable
@ReadOnlyComposable
fun eyebrowTextStyle(): TextStyle = olaniTokens().eyebrow

@Composable
@ReadOnlyComposable
fun descriptionTextStyle() = MaterialTheme.typography.bodyMedium.copy(
    color = olaniTokens().textMuted,
)

@Composable
@ReadOnlyComposable
fun tightSemiBold(
    base: TextStyle = MaterialTheme.typography.titleSmall,
    letterSpacing: TextUnit = (-0.3).sp,
) = base.copy(letterSpacing = letterSpacing, fontWeight = FontWeight.SemiBold)
