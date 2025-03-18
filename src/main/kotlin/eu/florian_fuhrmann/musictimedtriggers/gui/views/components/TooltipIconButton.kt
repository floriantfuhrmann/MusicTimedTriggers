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
import eu.florian_fuhrmann.musictimedtriggers.utils.icons.MttIcons
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icon.IconKey
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
    iconKey: IconKey,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Tooltip(tooltip = { Text(tooltip) }) {
        SimpleIconButton(
            enabled = enabled,
            forceHoverHandCursor = forceHoverHandCursor,
            iconKey = iconKey,
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
    iconKey: IconKey,
    iconContentDescriptor: String = "",
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
            Icon(iconKey, iconContentDescriptor)
            //todo: maybe fix tint?
        }
    }
}

@Composable
fun OutlineIconButton(
    iconKey: IconKey,
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
            Icon(iconKey, null)
        }
    }
}
