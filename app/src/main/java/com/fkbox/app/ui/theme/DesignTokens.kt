package com.fkbox.app.ui.theme

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Design Tokens for fkBOX - Single source of truth for all visual design decisions.
 * Cinematic, premium dark theme optimized for streaming content.
 */
object DesignTokens {

    // ========================================================================
    // COLOR PALETTE - Cinematic Dark Theme
    // ========================================================================

    /** Base surfaces - layered depth system */
    object Surface {
        /** Deepest background - near black with subtle warmth */
        val Background = Color(0xFF0A0A0C)
        /** Primary surface for cards, sheets, bottom bars */
        val Surface = Color(0xFF141417)
        /** Elevated surfaces - dialogs, modals, dropdowns */
        val SurfaceElevated = Color(0xFF1C1C20)
        /** Highest elevation - tooltips, popovers */
        val SurfaceOverlay = Color(0xFF24242A)
        /** Subtle containers within surfaces */
        val SurfaceContainer = Color(0xFF1A1A1E)
        /** Pressed/active state backgrounds */
        val SurfacePressed = Color(0xFF2A2A30)
        /** Focus/selection indicator */
        val SurfaceFocused = Color(0xFF303038)
    }

    /** Content colors - text, icons, dividers */
    object Content {
        /** Primary text - pure white with slight warmth for readability */
        val Primary = Color(0xFFF5F4F6)
        /** Secondary text - muted but readable */
        val Secondary = Color(0xFFB8B5BD)
        /** Tertiary text - hints, captions, disabled */
        val Tertiary = Color(0xFF8A8890)
        /** Inverse text for colored backgrounds */
        val OnPrimary = Color(0xFF1A1A1E)
        /** Dividers and subtle borders */
        val Divider = Color(0xFF2A2A2E)
        /** Focus/selection borders */
        val Focus = Color(0xFFD2BCFC)
        /** Error states */
        val Error = Color(0xFFF2B8B5)
        val ErrorContainer = Color(0xFF8C1D18)
    }

    /** Brand/Accent colors - Purple cinematic identity */
    object Brand {
        val Primary = Color(0xFFD2BCFC)
        val PrimaryVariant = Color(0xFFB89CE8)
        val PrimaryContainer = Color(0xFF4C3889)
        val OnPrimaryContainer = Color(0xFFE9DDFF)
        val Secondary = Color(0xFFCDC1E1)
        val Tertiary = Color(0xFFEFB8C8)
    }

    /** Semantic colors for states */
    object State {
        val Success = Color(0xFFB8E8C8)
        val Warning = Color(0xFFF5D8A8)
        val Info = Color(0xFFB8D8F8)
        val Disabled = Color(0xFF4A4850)
        val Error = Content.Error
        val ErrorContainer = Content.ErrorContainer
    }

    // ========================================================================
    // GRADIENTS - For hero sections, overlays, premium touches
    // ========================================================================

    object Gradients {
        /** Hero backdrop overlay - cinematic fade to transparent */
        val HeroOverlay = listOf(
            Color(0xFF0A0A0C) to 0.0f,
            Color(0xFF0A0A0C) to 0.4f,
            Color(0xFF0A0A0C) to 1.0f
        )
        /** Card gradient for featured content */
        val CardFeatured = listOf(
            Color(0xFF1C1C20) to 0.0f,
            Color(0xFF2A2A30) to 1.0f
        )
        /** Button primary gradient */
        val ButtonPrimary = listOf(
            Brand.Primary to 1.0f,
            Brand.PrimaryVariant to 1.0f
        )
    }

    // ========================================================================
    // SPACING SYSTEM - 4dp base unit, consistent rhythm
    // ========================================================================

    object Spacing {
        val XS = 4.dp
        val SM = 8.dp
        val MD = 16.dp
        val LG = 24.dp
        val XL = 32.dp
        val XXL = 48.dp
        val XXXL = 64.dp

        /** Screen horizontal padding */
        val ScreenHorizontal = MD
        /** Screen vertical padding */
        val ScreenVertical = MD
        /** Card internal padding */
        val Card = MD
        /** Section gap */
        val Section = XL
        /** Item gap in lists/grids */
        val ItemGap = MD
        /** Compact item gap */
        val ItemGapCompact = SM
    }

    // ========================================================================
    // SHAPE SYSTEM - Rounded corners with purpose
    // ========================================================================

    object Shape {
        /** Small elements - chips, badges, avatar */
        val XS = RoundedCornerShape(8.dp)
        /** Buttons, inputs, small cards */
        val SM = RoundedCornerShape(12.dp)
        /** Standard cards, sheets, dialogs */
        val MD = RoundedCornerShape(20.dp)
        /** Hero images, featured cards, bottom sheets */
        val LG = RoundedCornerShape(28.dp)
        /** Full screen overlays, modal bottom sheets */
        val XL = RoundedCornerShape(32.dp)
        /** Circular - avatars, FABs, icon buttons */
        val Circle = RoundedCornerShape(50.dp)
        /** Pill - chips, pills, segmented controls */
        val Pill = RoundedCornerShape(100.dp)
    }

    // ========================================================================
    // TYPOGRAPHY - Cinematic hierarchy with Roboto Serif for headlines
    // ========================================================================

    private val RobotoSerif = FontFamily(
        Font(resId = com.fkbox.app.R.font.roboto_serif_regular, weight = FontWeight.Normal),
        Font(resId = com.fkbox.app.R.font.roboto_serif_medium, weight = FontWeight.Medium),
        Font(resId = com.fkbox.app.R.font.roboto_serif_bold, weight = FontWeight.SemiBold),
        Font(resId = com.fkbox.app.R.font.roboto_serif_bold, weight = FontWeight.Bold),
    )

    /** System font family for body text - uses platform default */
    private val SystemFont = FontFamily.Default

    /** Base typography with proper line heights for readability */
    val Typography = Typography(
        // Display - Hero titles, large headlines
        displayLarge = Typography().displayLarge.copy(
            fontFamily = RobotoSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 40.sp,
            lineHeight = 48.sp,
            letterSpacing = -0.5.sp
        ),
        displayMedium = Typography().displayMedium.copy(
            fontFamily = RobotoSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = -0.25.sp
        ),
        displaySmall = Typography().displaySmall.copy(
            fontFamily = RobotoSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 32.sp
        ),

        // Headlines - Screen titles, section headers
        headlineLarge = Typography().headlineLarge.copy(
            fontFamily = RobotoSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = -0.25.sp
        ),
        headlineMedium = Typography().headlineMedium.copy(
            fontFamily = RobotoSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 30.sp
        ),
        headlineSmall = Typography().headlineSmall.copy(
            fontFamily = RobotoSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 26.sp
        ),

        // Titles - Card titles, list items
        titleLarge = Typography().titleLarge.copy(
            fontFamily = SystemFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 24.sp
        ),
        titleMedium = Typography().titleMedium.copy(
            fontFamily = SystemFont,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        titleSmall = Typography().titleSmall.copy(
            fontFamily = SystemFont,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),

        // Body - Descriptions, paragraphs
        bodyLarge = Typography().bodyLarge.copy(
            fontFamily = SystemFont,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp
        ),
        bodyMedium = Typography().bodyMedium.copy(
            fontFamily = SystemFont,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodySmall = Typography().bodySmall.copy(
            fontFamily = SystemFont,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),

        // Labels - Buttons, captions, metadata
        labelLarge = Typography().labelLarge.copy(
            fontFamily = SystemFont,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        labelMedium = Typography().labelMedium.copy(
            fontFamily = SystemFont,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        labelSmall = Typography().labelSmall.copy(
            fontFamily = SystemFont,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            lineHeight = 14.sp
        ),
    )

    // ========================================================================
    // MOTION - Consistent, purposeful animation
    // ========================================================================

    object Motion {
        /** Quick micro-interactions - button press, ripple */
        val Fast = 120
        /** Standard transitions - screen, dialog, sheet */
        val Medium = 250
        /** Complex transitions - shared element, hero */
        val Slow = 400

        /** Standard easing - natural, not mechanical */
        val EasingStandard: Easing = Easing { fraction -> 0.2f + fraction * (0.0f - 0.2f) + (1.0f - 1.0f) * fraction * fraction } // Simplified cubic-bezier(0.2, 0, 0, 1)
        /** Emphasized easing - for important transitions */
        val EasingEmphasized: Easing = Easing { fraction -> 0.4f + fraction * (0.0f - 0.4f) + (0.2f - 0.0f) * fraction * fraction } // Simplified cubic-bezier(0.4, 0, 0.2, 1)
        /** Decelerate - for exiting elements */
        val EasingDecelerate: Easing = Easing { fraction -> 0.0f + fraction * (0.0f - 0.0f) + (0.2f - 0.0f) * fraction * fraction } // Simplified cubic-bezier(0, 0, 0.2, 1)
        /** Accelerate - for entering elements */
        val EasingAccelerate: Easing = Easing { fraction -> 0.4f + fraction * (0.0f - 0.4f) + (1.0f - 1.0f) * fraction * fraction } // Simplified cubic-bezier(0.4, 0, 1, 1)

        /** Spring for playful interactions - press scale, toggles */
        val SpringBouncy = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
        /** Spring for UI transitions - smooth, controlled */
        val SpringSmooth = spring(
            dampingRatio = 0.8f,
            stiffness = Spring.StiffnessMediumLow
        )
    }

    // ========================================================================
    // SIZING - Consistent dimensions
    // ========================================================================

    object Size {
        /** Touch target minimum - accessibility */
        val TouchTarget = 48.dp
        /** Icon sizes */
        val IconXS = 16.dp
        val IconSM = 20.dp
        val IconMD = 24.dp
        val IconLG = 28.dp
        val IconXL = 32.dp
        /** Avatar sizes */
        val AvatarXS = 28.dp
        val AvatarSM = 40.dp
        val AvatarMD = 56.dp
        val AvatarLG = 72.dp
        val AvatarXL = 96.dp
        /** Poster aspect ratio (2:3) */
        val PosterAspectRatio = 2f / 3f
        /** Backdrop aspect ratio (16:9) */
        val BackdropAspectRatio = 16f / 9f
        /** Hero backdrop height */
        val HeroHeight = 320.dp
        /** Compact hero height */
        val HeroHeightCompact = 240.dp
        /** Bottom bar height */
        val BottomBarHeight = 72.dp
        /** Top bar height */
        val TopBarHeight = 56.dp
    }

    // ========================================================================
    // Z-INDEX / ELEVATION - Layer hierarchy
    // ========================================================================

    object Elevation {
        val Level0 = 0.dp   // Background
        val Level1 = 1.dp   // Surface
        val Level2 = 4.dp   // Card default
        val Level3 = 8.dp   // Card hover, FAB
        val Level4 = 16.dp  // Modal, bottom sheet
        val Level5 = 24.dp  // Dialog, dropdown
        val Level6 = 32.dp  // Toast, snackbar
    }

    // ========================================================================
    // BREAKPOINTS - Responsive design
    // ========================================================================

    object Breakpoint {
        val Compact = 600.dp   // Phones
        val Medium = 840.dp    // Large phones / small tablets
        val Expanded = 1200.dp // Tablets
    }
}

/**
 * Material3 ColorScheme built from DesignTokens for seamless M3 integration.
 * Used by MaterialTheme to provide consistent colors to all M3 components.
 */
@Composable
fun FkboxColorScheme() = androidx.compose.material3.darkColorScheme(
    // Primary - Brand purple
    primary = DesignTokens.Brand.Primary,
    onPrimary = DesignTokens.Content.OnPrimary,
    primaryContainer = DesignTokens.Brand.PrimaryContainer,
    onPrimaryContainer = DesignTokens.Brand.OnPrimaryContainer,

    // Secondary - Subtle purple-gray
    secondary = DesignTokens.Brand.Secondary,
    onSecondary = DesignTokens.Content.Primary,
    secondaryContainer = DesignTokens.Surface.SurfaceContainer,
    onSecondaryContainer = DesignTokens.Content.Primary,

    // Tertiary - Warm accent
    tertiary = DesignTokens.Brand.Tertiary,
    onTertiary = DesignTokens.Content.OnPrimary,
    tertiaryContainer = DesignTokens.Surface.SurfaceContainer,
    onTertiaryContainer = DesignTokens.Content.Primary,

    // Surface hierarchy
    surface = DesignTokens.Surface.Surface,
    onSurface = DesignTokens.Content.Primary,
    surfaceVariant = DesignTokens.Surface.SurfaceContainer,
    onSurfaceVariant = DesignTokens.Content.Secondary,

    // Background
    background = DesignTokens.Surface.Background,
    onBackground = DesignTokens.Content.Primary,

    // Inverse
    inverseSurface = DesignTokens.Content.Primary,
    inverseOnSurface = DesignTokens.Surface.Background,
    inversePrimary = DesignTokens.Brand.PrimaryVariant,

    // Outline
    outline = DesignTokens.Content.Divider,
    outlineVariant = DesignTokens.Content.Divider.copy(alpha = 0.5f),

    // Scrim
    scrim = Color.Black.copy(alpha = 0.6f),
    surfaceTint = DesignTokens.Brand.Primary,

    // Error
    error = DesignTokens.State.Error,
    onError = DesignTokens.Content.OnPrimary,
    errorContainer = DesignTokens.State.ErrorContainer,
    onErrorContainer = DesignTokens.Content.OnPrimary,

    // Surface containers (M3 Expressive)
    surfaceContainerLowest = DesignTokens.Surface.Background,
    surfaceContainerLow = DesignTokens.Surface.Surface,
    surfaceContainer = DesignTokens.Surface.SurfaceContainer,
    surfaceContainerHigh = DesignTokens.Surface.SurfaceElevated,
    surfaceContainerHighest = DesignTokens.Surface.SurfaceOverlay,
    surfaceBright = DesignTokens.Surface.SurfaceElevated,
    surfaceDim = DesignTokens.Surface.Background,
)

/**
 * Material3 Shapes built from DesignTokens.
 */
@Composable
fun FkboxShapes() = androidx.compose.material3.Shapes(
    extraSmall = DesignTokens.Shape.XS,
    small = DesignTokens.Shape.SM,
    medium = DesignTokens.Shape.MD,
    large = DesignTokens.Shape.LG,
    extraLarge = DesignTokens.Shape.XL,
)

/**
 * Material3 Typography built from DesignTokens.
 */
@Composable
fun FkboxTypography() = DesignTokens.Typography