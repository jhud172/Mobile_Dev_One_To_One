package uk.ac.cardiff.trainerhub.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkColours = darkColorScheme(
    primary = Color(0xFF67B79E),
    onPrimary = Color(0xFF071F18),
    primaryContainer = Color(0xFF173F35),
    onPrimaryContainer = Color(0xFFD7F3EA),
    secondary = Color(0xFFC8914D),
    onSecondary = Color(0xFF241404),
    secondaryContainer = Color(0xFF3E2B14),
    onSecondaryContainer = Color(0xFFF8DFBD),
    tertiary = Color(0xFF86A7B5),
    background = Color(0xFF101312),
    onBackground = Color(0xFFF1F3EF),
    surface = Color(0xFF171B19),
    onSurface = Color(0xFFF1F3EF),
    surfaceVariant = Color(0xFF22312D),
    onSurfaceVariant = Color(0xFFC9D2CD),
    outline = Color(0xFF789087),
    error = Color(0xFFFFB4AB),
)

private val LightColours = lightColorScheme(
    primary = Color(0xFF4B947E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7ECE5),
    onPrimaryContainer = Color(0xFF14372E),
    secondary = Color(0xFFB47A34),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF2DEC1),
    onSecondaryContainer = Color(0xFF3A240D),
    tertiary = Color(0xFF416E7E),
    background = Color(0xFFE7E8E6),
    onBackground = Color(0xFF1C2024),
    surface = Color(0xFFF6F7F4),
    onSurface = Color(0xFF1C2024),
    surfaceVariant = Color(0xFFE0E7E3),
    onSurfaceVariant = Color(0xFF56615D),
    outline = Color(0xFFB9C4BF),
    error = Color(0xFFB3261E),
)

private val DefaultTypography = Typography()

private val TrainerHubTypography = DefaultTypography.copy(
        headlineLarge = DefaultTypography.headlineLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            lineHeight = 40.sp,
        ),
        headlineMedium = DefaultTypography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            lineHeight = 36.sp,
        ),
        headlineSmall = DefaultTypography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            lineHeight = 31.sp,
        ),
        titleLarge = DefaultTypography.titleLarge.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
        ),
        titleMedium = DefaultTypography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
        ),
        labelLarge = DefaultTypography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 18.sp,
        ),
        bodyLarge = DefaultTypography.bodyLarge.copy(
            fontSize = 17.sp,
            lineHeight = 25.sp,
        ),
        bodyMedium = DefaultTypography.bodyMedium.copy(
            fontSize = 15.sp,
            lineHeight = 22.sp,
        ),
    )

private val TrainerHubShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
)

@Composable
fun TrainerHubTheme(
    content: @Composable () -> Unit,
) {
    val colours = if (isSystemInDarkTheme()) DarkColours else LightColours

    MaterialTheme(
        colorScheme = colours,
        typography = TrainerHubTypography,
        shapes = TrainerHubShapes,
        content = content,
    )
}
