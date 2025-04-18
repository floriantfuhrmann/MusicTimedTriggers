package eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.theme.colorPalette
import org.jetbrains.jewel.ui.theme.iconButtonStyle
import org.jetbrains.jewel.ui.util.thenIf

// based on Jewels' SelectableIconButton
@Composable
fun InputFieldIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selectable: Boolean = false,
    selected: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable (BoxScope.() -> Unit)
) {
    // Interaction state
    var pressed by remember { mutableStateOf(false) }
    var hovered by remember { mutableStateOf(false) }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> pressed = true
                is PressInteraction.Cancel, is PressInteraction.Release -> pressed = false

                is HoverInteraction.Enter -> hovered = true
                is HoverInteraction.Exit -> hovered = false
            }
        }
    }

    // UI
    val shape = RoundedCornerShape(JewelTheme.iconButtonStyle.metrics.cornerSize)
    val backgroundColor = if(MainUiState.theme.isDark()) {
        when {
            !selected && pressed -> JewelTheme.colorPalette.gray(5)
            !selected && hovered -> JewelTheme.colorPalette.gray(3)
            selected && pressed -> JewelTheme.colorPalette.blue(3)
            selected && hovered -> JewelTheme.colorPalette.blue(2)
            selected -> JewelTheme.colorPalette.blue(1)
            else -> Color.Transparent
        }
    } else {
        when {
            !selected && pressed -> JewelTheme.colorPalette.gray(11)
            !selected && hovered -> JewelTheme.colorPalette.gray(12)
            selected && pressed -> JewelTheme.colorPalette.blue(10)
            selected && hovered -> JewelTheme.colorPalette.blue(11)
            selected -> JewelTheme.colorPalette.blue(12)
            else -> Color.Transparent
        }
    }
    Box(
        modifier =
            Modifier.focusProperties { canFocus = false }
                .then(modifier)
                .thenIf(selectable) {
                    selectable(
                        onClick = onClick,
                        enabled = enabled,
                        role = Role.RadioButton,
                        interactionSource = interactionSource,
                        indication = null,
                        selected = selected,
                    )
                }.thenIf(!selectable) {
                    clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = enabled,
                        role = Role.RadioButton,
                        onClick = onClick
                    )
                }
                .defaultMinSize(
                    JewelTheme.iconButtonStyle.metrics.minSize.width,
                    JewelTheme.iconButtonStyle.metrics.minSize.height
                )
                .padding(JewelTheme.iconButtonStyle.metrics.padding)
                .background(backgroundColor, shape),
        contentAlignment = Alignment.Center,
        content = { content() },
    )
}