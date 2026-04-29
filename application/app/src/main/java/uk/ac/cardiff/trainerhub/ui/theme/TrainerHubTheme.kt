package uk.ac.cardiff.trainerhub.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColours = darkColorScheme(
    primary = Color(0xFFD4B26A),
    onPrimary = Color(0xFF1B1B1B),
    secondary = Color(0xFF7FB3A1),
    onSecondary = Color(0xFF102019),
    background = Color(0xFF101413),
    onBackground = Color(0xFFF2EEE7),
    surface = Color(0xFF171D1B),
    onSurface = Color(0xFFF2EEE7),
    surfaceVariant = Color(0xFF232B28),
    onSurfaceVariant = Color(0xFFD0C7BB),
    error = Color(0xFFE57373),
)

private val LightColours = lightColorScheme(
    primary = Color(0xFF5C4330),
    onPrimary = Color.White,
    secondary = Color(0xFF2F6B5C),
    onSecondary = Color.White,
    background = Color(0xFFF8F4EE),
    onBackground = Color(0xFF1A1612),
    surface = Color(0xFFFFFBF6),
    onSurface = Color(0xFF1A1612),
    surfaceVariant = Color(0xFFE7DED2),
    onSurfaceVariant = Color(0xFF51453A),
    error = Color(0xFFB3261E),
)

@Composable
fun TrainerHubTheme(
    content: @Composable () -> Unit,
) {
    val colours = if (isSystemInDarkTheme()) DarkColours else LightColours

    MaterialTheme(
        colorScheme = colours,
        content = content,
    )
}
