package eu.florian_fuhrmann.musictimedtriggers.gui.views.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.styles.outlinedButtonStyleWithNoPadding
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import eu.florian_fuhrmann.musictimedtriggers.utils.IconsDummy
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.theme.dropdownStyle
import org.jetbrains.jewel.ui.theme.outlinedButtonStyle
import org.jetbrains.jewel.ui.util.thenIf
import java.awt.Cursor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TooltipIconButton(
    enabled: Boolean = true,
    forceHoverHandCursor: Boolean = false,
    tooltip: String,
    iconName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Tooltip(tooltip = { Text(tooltip) }) {
        SimpleIconButton(
            enabled = enabled,
            forceHoverHandCursor = forceHoverHandCursor,
            iconName = iconName,
            iconContentDescriptor = tooltip,
            onClick = onClick,
            modifier = modifier
        )
    }
}

@Composable
fun SimpleIconButton(
    enabled: Boolean = true,
    forceHoverHandCursor: Boolean = false,
    iconName: String,
    iconContentDescriptor: String = iconName,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        enabled = enabled,
        onClick = onClick,
        modifier = modifier
            .fillMaxHeight()
            .thenIf(forceHoverHandCursor) {
                this.pointerHoverIcon(icon = PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
            }
            .trackActivation()
    ) {
        Box(modifier = Modifier
            .padding(5.dp)
            .alpha( if(enabled) { 1f } else { 0.5f } )
        ) {
            Icon(
                when (MainUiState.theme.isDark()) {
                    true -> "icons/${iconName}_dark.svg"
                    else -> "icons/${iconName}.svg"
                },
                iconContentDescriptor,
                IconsDummy::class.java
            )
        }
    }
}

@Composable
fun SimpleIcon(iconName: String, iconContentDescriptor: String = iconName, modifier: Modifier = Modifier) {
    Icon(
        when (MainUiState.theme.isDark()) {
            true -> "icons/${iconName}_dark.svg"
            else -> "icons/${iconName}.svg"
        },
        iconContentDescriptor,
        IconsDummy::class.java,
        modifier = modifier
    )
}

@Composable
fun OutlineIconButton(
    iconName: String,
    onClick: () -> Unit = {},
    size: Float = JewelTheme.dropdownStyle.metrics.minSize.height.value,
    iconPadding: Float = 5f,
    backgroundColor: Color = Color.Transparent,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        modifier = modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(JewelTheme.outlinedButtonStyle.metrics.cornerSize))
            .height(size.dp)
            .width(size.dp),
        onClick = onClick,
        style = outlinedButtonStyleWithNoPadding
    ) {
        Box(modifier = Modifier
            //.background(color = backgroundColor, shape = RoundedCornerShape(JewelTheme.outlinedButtonStyle.metrics.cornerSize))
            .padding(iconPadding.dp)) {
            SimpleIcon(iconName)
        }
    }
}
