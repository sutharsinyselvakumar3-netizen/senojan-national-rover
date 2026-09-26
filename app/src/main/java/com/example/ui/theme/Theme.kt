package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.data.model.RobotMode

data class RobotModeAccentColors(
    val primaryAccent: Color,
    val headingColor: Color,
    val containerColor: Color,
    val borderColor: Color,
    val modeName: String
)

val LocalRobotModeColors = staticCompositionLocalOf {
    RobotModeAccentColors(
        primaryAccent = ManualDarkGreen,
        headingColor = ManualDarkGreenHeading,
        containerColor = ManualDarkGreenContainer,
        borderColor = ManualDarkGreenBorder,
        modeName = "MANUAL"
    )
}

@Composable
fun AiCompanionTheme(
    robotMode: RobotMode = RobotMode.MANUAL,
    content: @Composable () -> Unit
) {
    val modeColors = if (robotMode == RobotMode.MANUAL) {
        RobotModeAccentColors(
            primaryAccent = ManualDarkGreen,
            headingColor = ManualDarkGreenHeading,
            containerColor = ManualDarkGreenContainer,
            borderColor = ManualDarkGreenBorder,
            modeName = "MANUAL"
        )
    } else {
        RobotModeAccentColors(
            primaryAccent = AutoDarkBlue,
            headingColor = AutoDarkBlueHeading,
            containerColor = AutoDarkBlueContainer,
            borderColor = AutoDarkBlueBorder,
            modeName = "AUTO"
        )
    }

    val colorScheme = lightColorScheme(
        primary = modeColors.primaryAccent,
        onPrimary = PureWhite,
        primaryContainer = modeColors.containerColor,
        onPrimaryContainer = modeColors.headingColor,
        secondary = modeColors.primaryAccent,
        onSecondary = PureWhite,
        tertiary = modeColors.primaryAccent,
        background = RoyalWhite,
        onBackground = modeColors.headingColor,
        surface = CardSurfaceWhite,
        onSurface = modeColors.headingColor,
        surfaceVariant = SurfaceSubtle,
        onSurfaceVariant = TextMuted,
        outline = modeColors.borderColor,
        outlineVariant = CardBorderLight,
        error = StatusCritical,
        onError = PureWhite
    )

    CompositionLocalProvider(LocalRobotModeColors provides modeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
