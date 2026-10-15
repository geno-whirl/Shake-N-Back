package app.shakenback.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

private val LightColors = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = Color(0xFF0D9488),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF042F2E),
    tertiary = Color(0xFFDB2777),
    onTertiary = Color.White,
    background = Color(0xFFF7F7FB),
    onBackground = Color(0xFF16172B),
    surface = Color(0xFFF7F7FB),
    onSurface = Color(0xFF16172B),
    surfaceVariant = Color(0xFFE4E5F1),
    onSurfaceVariant = Color(0xFF55576E),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF1F1F8),
    surfaceContainer = Color(0xFFEBEBF4),
    surfaceContainerHigh = Color(0xFFE5E5F0),
    surfaceContainerHighest = Color(0xFFDFDFEB),
    outline = Color(0xFF8A8CA3),
    outlineVariant = Color(0xFFD4D5E3),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA5B4FC),
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF3730A3),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFF5EEAD4),
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF115E59),
    onSecondaryContainer = Color(0xFFCCFBF1),
    tertiary = Color(0xFFF9A8D4),
    onTertiary = Color(0xFF500724),
    background = Color(0xFF0E0F1A),
    onBackground = Color(0xFFE6E6F2),
    surface = Color(0xFF0E0F1A),
    onSurface = Color(0xFFE6E6F2),
    surfaceVariant = Color(0xFF2A2C40),
    onSurfaceVariant = Color(0xFFB4B6CC),
    surfaceContainerLowest = Color(0xFF090A12),
    surfaceContainerLow = Color(0xFF151626),
    surfaceContainer = Color(0xFF1A1B2D),
    surfaceContainerHigh = Color(0xFF222438),
    surfaceContainerHighest = Color(0xFF2B2D43),
    outline = Color(0xFF7A7C94),
    outlineVariant = Color(0xFF34364C),
)

/** Colours Material 3 doesn't name: answer feedback. */
@Immutable
data class FeedbackColors(
    val correct: Color,
    val onCorrect: Color,
    val wrong: Color,
    val onWrong: Color,
)

private val LightFeedback = FeedbackColors(
    correct = Color(0xFFD1FAE5),
    onCorrect = Color(0xFF065F46),
    wrong = Color(0xFFFFE4E6),
    onWrong = Color(0xFF9F1239),
)

private val DarkFeedback = FeedbackColors(
    correct = Color(0xFF064E3B),
    onCorrect = Color(0xFFA7F3D0),
    wrong = Color(0xFF4C0519),
    onWrong = Color(0xFFFECDD3),
)

val LocalFeedbackColors = staticCompositionLocalOf { LightFeedback }

private val AppTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontWeight = FontWeight.Bold),
        displayMedium = displayMedium.copy(fontWeight = FontWeight.Bold),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.SemiBold),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
    )
}

/**
 * A fixed brand palette rather than Material You dynamic colour, so the game
 * looks the same on every device.
 */
@Composable
fun ShakeNBackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalFeedbackColors provides if (darkTheme) DarkFeedback else LightFeedback) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = AppTypography,
            content = content,
        )
    }
}
