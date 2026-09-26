package com.fkbox.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fkbox.app.R

/** Purple dark scheme (values from the design brief; unspecified roles use matching M3 tones). */
private val FkboxDarkColors = darkColorScheme(
    primary = Color(0xFFD2BCFC),
    onPrimary = Color(0xFF32226F),
    primaryContainer = Color(0xFF4C3889),
    onPrimaryContainer = Color(0xFFE9DDFF),
    inversePrimary = Color(0xFF6750A4),
    secondary = Color(0xFFCDC1E1),
    onSecondary = Color(0xFF342D45),
    secondaryContainer = Color(0xFF4B425D),
    onSecondaryContainer = Color(0xFFE9DDFD),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF6C3644),
    onTertiaryContainer = Color(0xFFFDDAE1),
    background = Color(0xFF141317),
    onBackground = Color(0xFFE4E1E7),
    surface = Color(0xFF141317),
    onSurface = Color(0xFFE4E1E7),
    surfaceVariant = Color(0xFF494550),
    onSurfaceVariant = Color(0xFFC9C4D1),
    surfaceTint = Color(0xFFD2BCFC),
    inverseSurface = Color(0xFFE4E1E7),
    inverseOnSurface = Color(0xFF313034),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    outline = Color(0xFF938F9B),
    outlineVariant = Color(0xFF494550),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF3A383D),
    surfaceDim = Color(0xFF141317),
    surfaceContainerLowest = Color(0xFF0F0E12),
    surfaceContainerLow = Color(0xFF1C1B1F),
    surfaceContainer = Color(0xFF201F23),
    surfaceContainerHigh = Color(0xFF2B292D),
    surfaceContainerHighest = Color(0xFF363438),
)

/** Maximum rounding: 32dp cards/images, ~40dp dialogs and sheets. Menus keep 4dp. */
private val FkboxShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(32.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(40.dp),
)

private val RobotoSerif = FontFamily(
    Font(R.font.roboto_serif_regular, FontWeight.Normal),
    Font(R.font.roboto_serif_medium, FontWeight.Medium),
    Font(R.font.roboto_serif_bold, FontWeight.Bold),
)

private val base = Typography()
private val FkboxTypography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = RobotoSerif),
    displayMedium = base.displayMedium.copy(fontFamily = RobotoSerif),
    displaySmall = base.displaySmall.copy(fontFamily = RobotoSerif),
    headlineLarge = base.headlineLarge.copy(fontFamily = RobotoSerif),
    headlineMedium = base.headlineMedium.copy(fontFamily = RobotoSerif),
    headlineSmall = base.headlineSmall.copy(fontFamily = RobotoSerif),
    titleLarge = base.titleLarge.copy(fontFamily = RobotoSerif),
    titleMedium = base.titleMedium.copy(fontFamily = RobotoSerif),
    titleSmall = base.titleSmall.copy(fontFamily = RobotoSerif),
    bodyLarge = base.bodyLarge.copy(fontFamily = RobotoSerif),
    bodyMedium = base.bodyMedium.copy(fontFamily = RobotoSerif),
    bodySmall = base.bodySmall.copy(fontFamily = RobotoSerif),
    labelLarge = base.labelLarge.copy(fontFamily = RobotoSerif),
    labelMedium = base.labelMedium.copy(fontFamily = RobotoSerif),
    labelSmall = base.labelSmall.copy(fontFamily = RobotoSerif),
)

@Composable
fun FkboxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FkboxDarkColors,
        shapes = FkboxShapes,
        typography = FkboxTypography,
        content = content,
    )
}
