package eu.florian_fuhrmann.musictimedtriggers.gui.styles

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.styling.ButtonMetrics
import org.jetbrains.jewel.ui.component.styling.ButtonStyle
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
            )
        )
    }
