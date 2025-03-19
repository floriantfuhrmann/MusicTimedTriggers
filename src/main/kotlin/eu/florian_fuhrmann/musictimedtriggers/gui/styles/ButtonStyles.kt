package eu.florian_fuhrmann.musictimedtriggers.gui.styles

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.styling.*
import org.jetbrains.jewel.ui.theme.dropdownStyle
import org.jetbrains.jewel.ui.theme.iconButtonStyle
import org.jetbrains.jewel.ui.theme.outlinedButtonStyle

val outlinedButtonStyleWithNoPadding: ButtonStyle
    @Composable
    get() {
        return ButtonStyle(
            JewelTheme.outlinedButtonStyle.colors,
            ButtonMetrics(
                cornerSize = JewelTheme.outlinedButtonStyle.metrics.cornerSize,
                padding = PaddingValues(0.dp),
                minSize = JewelTheme.outlinedButtonStyle.metrics.minSize,
                borderWidth = 0.dp, //JewelTheme.outlinedButtonStyle.metrics.borderWidth
                focusOutlineExpand = JewelTheme.outlinedButtonStyle.metrics.focusOutlineExpand,
            ),
            focusOutlineAlignment = JewelTheme.outlinedButtonStyle.focusOutlineAlignment
        )
    }

/**
 * IconButtonStyle mimicking the dropdown style
 */
val dropdownLikeIconButtonStyle: IconButtonStyle
    @Composable
    get() {
        return IconButtonStyle(
            colors = IconButtonColors(
                backgroundDisabled = JewelTheme.dropdownStyle.colors.backgroundDisabled,
                backgroundSelected = JewelTheme.iconButtonStyle.colors.backgroundSelected,
                backgroundSelectedActivated = JewelTheme.iconButtonStyle.colors.backgroundSelectedActivated,
                backgroundFocused = JewelTheme.dropdownStyle.colors.backgroundFocused,
                backgroundPressed = JewelTheme.dropdownStyle.colors.backgroundPressed,
                backgroundHovered = JewelTheme.dropdownStyle.colors.backgroundHovered,
                border = JewelTheme.dropdownStyle.colors.border,
                borderDisabled = JewelTheme.dropdownStyle.colors.borderDisabled,
                borderSelected = JewelTheme.iconButtonStyle.colors.borderSelected,
                borderSelectedActivated = JewelTheme.iconButtonStyle.colors.borderSelectedActivated,
                borderFocused = JewelTheme.dropdownStyle.colors.borderFocused,
                borderPressed = JewelTheme.dropdownStyle.colors.borderPressed,
                borderHovered = JewelTheme.dropdownStyle.colors.borderHovered,
                foregroundSelectedActivated = JewelTheme.iconButtonStyle.colors.foregroundSelectedActivated,
                background = JewelTheme.dropdownStyle.colors.background
            ),
            metrics = JewelTheme.iconButtonStyle.metrics
        )
    }
