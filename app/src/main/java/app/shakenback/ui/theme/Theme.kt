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

// Stock-car palette: racing red, number-board blue and caution-flag yellow on
// warm white (light) or asphalt (dark). A wink at Talladega Nights.
private val LightColors = lightColorScheme(
    primary = Color(0xFFD62828),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFF1D4ED8),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDBE4FF),
    onSecondaryContainer = Color(0xFF001A41),
    tertiary = Color(0xFFF2B600),
    onTertiary = Color(0xFF261A00),
    tertiaryContainer = Color(0xFFFFE08A),
    onTertiaryContainer = Color(0xFF261A00),
    background = Color(0xFFFBF8F4),
    onBackground = Color(0xFF1E1B18),
    surface = Color(0xFFFBF8F4),
    onSurface = Color(0xFF1E1B18),
    surfaceVariant = Color(0xFFECE6E0),
    onSurfaceVariant = Color(0xFF57524D),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF6F2EE),
    surfaceContainer = Color(0xFFF0ECE7),
    surfaceContainerHigh = Color(0xFFEAE5E0),
    surfaceContainerHighest = Color(0xFFE4DFDA),
    outline = Color(0xFF8A847E),
    outlineVariant = Color(0xFFD9D2CB),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE5383B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF8C1D18),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFF8AB4FF),
    onSecondary = Color(0xFF002E69),
    secondaryContainer = Color(0xFF1E3A8A),
    onSecondaryContainer = Color(0xFFDBE4FF),
    tertiary = Color(0xFFFFD54A),
    onTertiary = Color(0xFF3A2A00),
    tertiaryContainer = Color(0xFF5C4300),
    onTertiaryContainer = Color(0xFFFFE08A),
    background = Color(0xFF121316),
    onBackground = Color(0xFFE6E2DE),
    surface = Color(0xFF121316),
    onSurface = Color(0xFFE6E2DE),
    surfaceVariant = Color(0xFF2C2D33),
    onSurfaceVariant = Color(0xFFC4C1C9),
    surfaceContainerLowest = Color(0xFF0C0D10),
    surfaceContainerLow = Color(0xFF1A1B1F),
    surfaceContainer = Color(0xFF1E1F24),
    surfaceContainerHigh = Color(0xFF26272D),
    surfaceContainerHighest = Color(0xFF303138),
    outline = Color(0xFF8E8B93),
    outlineVariant = Color(0xFF3A3B42),
)

/** Navy for race numbers on white roundels; the same in light and dark themes. */
val RaceNavy = Color(0xFF1E3A8A)

/** Second stop of the hero gradient, red into orange like a sunset at the track. */
val RaceOrange = Color(0xFFF97316)

/**
 * Colours for the game screen, which stays calm (indigo) so the bright racing
 * palette doesn't tire the eyes mid-session, plus answer feedback.
 */
@Immutable
data class GameColors(
    val tile: Color,
    val onTile: Color,
    val tileContainer: Color,
    val onTileContainer: Color,
    val correct: Color,
    val onCorrect: Color,
    val wrong: Color,
    val onWrong: Color,
)

private val LightGame = GameColors(
    tile = Color(0xFF4F46E5),
    onTile = Color.White,
    tileContainer = Color(0xFFE0E7FF),
    onTileContainer = Color(0xFF1E1B4B),
    correct = Color(0xFFD1FAE5),
    onCorrect = Color(0xFF065F46),
    wrong = Color(0xFFFFE4E6),
    onWrong = Color(0xFF9F1239),
)

private val DarkGame = GameColors(
    tile = Color(0xFFA5B4FC),
    onTile = Color(0xFF1E1B4B),
    tileContainer = Color(0xFF3730A3),
    onTileContainer = Color(0xFFE0E7FF),
    correct = Color(0xFF064E3B),
    onCorrect = Color(0xFFA7F3D0),
    wrong = Color(0xFF4C0519),
    onWrong = Color(0xFFFECDD3),
)

val LocalGameColors = staticCompositionLocalOf { LightGame }

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
 * A fixed racing palette rather than Material You dynamic colour, so the game
 * looks the same on every device.
 */
@Composable
fun ShakeNBackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalGameColors provides if (darkTheme) DarkGame else LightGame) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = AppTypography,
            content = content,
        )
    }
}
