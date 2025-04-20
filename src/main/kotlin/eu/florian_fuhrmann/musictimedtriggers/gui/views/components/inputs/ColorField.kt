package eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.godaddy.android.colorpicker.ClassicColorPicker
import eu.florian_fuhrmann.musictimedtriggers.utils.color.GenericColor
import org.jetbrains.jewel.foundation.Stroke
import org.jetbrains.jewel.foundation.modifier.border
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Outline
import org.jetbrains.jewel.ui.component.PopupContainer
import org.jetbrains.jewel.ui.theme.textFieldStyle
import org.jetbrains.jewel.ui.util.thenIf

@Composable
fun ColorField(
    state: ColorFieldState,
    modifier: Modifier = Modifier,
    outline: Outline = Outline.None
) {
    var fieldFocused by remember { mutableStateOf(false) }
    var popupVisible by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(JewelTheme.textFieldStyle.metrics.cornerSize)
    Box(modifier
        .height(28.dp).width(50.dp)
        .background(when {
            outline == Outline.Error && fieldFocused -> JewelTheme.globalColors.outlines.focusedError
            outline == Outline.Warning && fieldFocused -> JewelTheme.globalColors.outlines.focusedWarning
            outline == Outline.None && fieldFocused -> JewelTheme.globalColors.outlines.focused
            outline == Outline.Error -> JewelTheme.globalColors.outlines.error
            outline == Outline.Warning -> JewelTheme.globalColors.outlines.warning
            else -> JewelTheme.textFieldStyle.colors.border
        }, shape)
        .padding(JewelTheme.textFieldStyle.metrics.borderWidth)
        .background(state.value.toComposeColor(), shape)
        .thenIf(fieldFocused) {
            border(
                Stroke.Alignment.Outside,
                JewelTheme.globalMetrics.outlineWidth,
                when (outline) {
                    Outline.Error -> JewelTheme.globalColors.outlines.focusedError
                    Outline.Warning -> JewelTheme.globalColors.outlines.focusedWarning
                    else -> JewelTheme.globalColors.outlines.focused
                },
                shape
            )
        }
        .focusable()
        .onFocusChanged { fieldFocused = it.isFocused }
        .clickable {
            popupVisible = true
        }
    )
    // Color picker popup
    if(popupVisible) {
        PopupContainer(
            onDismissRequest = { popupVisible = false },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.padding(10.dp).width(140.dp).height(120.dp)) {
                ClassicColorPicker(
                    modifier = Modifier.fillMaxSize(),
                    color = state.value.toHsvColor(),
                    showAlphaBar = state.alphaInput,
                    onColorChanged = {
                        state.value = GenericColor.fromHsvColor(it)
                    }
                )
            }
        }
    }
}

class ColorFieldState(initialValue: GenericColor) {
    var value by mutableStateOf(initialValue)

    /** Whether to show the alpha bar in the color picker. (default: false) */
    var alphaInput by mutableStateOf(false)
}