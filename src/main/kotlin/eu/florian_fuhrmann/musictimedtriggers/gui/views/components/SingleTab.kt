package eu.florian_fuhrmann.musictimedtriggers.gui.views.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isTertiary
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.foundation.theme.LocalContentColor
import org.jetbrains.jewel.ui.NoIndication
import org.jetbrains.jewel.ui.component.ButtonState
import org.jetbrains.jewel.ui.component.TabState
import org.jetbrains.jewel.ui.painter.hints.Stateful
import org.jetbrains.jewel.ui.theme.defaultTabStyle
import org.jetbrains.jewel.ui.theme.editorTabStyle

/*
This is a modified version of TabImpl from org.jetbrains.jewel.ui.component
Mainly the tabData argument has been replaced with boolean arguments
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SingleTab(
    modifier: Modifier = Modifier,
    editorStyle: Boolean = false,
    isActive: Boolean = false,
    selected: Boolean = false,
    closable: Boolean = false,
    onClose: () -> Unit = {},
    onClick: () -> Unit = {},
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val tabStyle = when (editorStyle) {
        false -> JewelTheme.defaultTabStyle
        true -> JewelTheme.editorTabStyle
    }

    var tabState by remember {
        mutableStateOf(TabState.of(selected = selected, active = isActive))
    }
    remember(selected, isActive) {
        tabState = tabState.copy(selected = selected, active = isActive)
    }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> tabState = tabState.copy(pressed = true)
                is PressInteraction.Cancel, is PressInteraction.Release -> tabState = tabState.copy(pressed = false)
                is HoverInteraction.Enter -> tabState = tabState.copy(hovered = true)
                is HoverInteraction.Exit -> tabState = tabState.copy(hovered = false)
            }
        }
    }
    var closeButtonState by remember(isActive) { mutableStateOf(ButtonState.of(active = isActive)) }
    val lineColor by tabStyle.colors.underlineFor(tabState)
    val lineThickness = tabStyle.metrics.underlineThickness
    val backgroundColor by tabStyle.colors.backgroundFor(state = tabState)

    val resolvedContentColor =
        tabStyle.colors.contentFor(tabState).value
            .takeOrElse { LocalContentColor.current }

    CompositionLocalProvider(LocalContentColor provides resolvedContentColor) {
        Row(
            modifier.height(tabStyle.metrics.tabHeight)
                .background(backgroundColor)
                .focusProperties { canFocus = false }
                .selectable(
                    onClick = onClick,
                    selected = selected,
                    interactionSource = interactionSource,
                    indication = NoIndication,
                    role = Role.Tab,
                )
                .drawBehind {
                    val strokeThickness = lineThickness.toPx()
                    val startY = size.height - (strokeThickness / 2f)
                    val endX = size.width
                    val capDxFix = strokeThickness / 2f

                    drawLine(
                        brush = SolidColor(lineColor),
                        start = Offset(0 + capDxFix, startY),
                        end = Offset(endX - capDxFix, startY),
                        strokeWidth = strokeThickness,
                        cap = StrokeCap.Round,
                    )
                }
                .padding(tabStyle.metrics.tabPadding)
                .onPointerEvent(PointerEventType.Release) { if (it.button.isTertiary) onClose() },
            horizontalArrangement = Arrangement.spacedBy(tabStyle.metrics.closeContentGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()

            val showCloseIcon =
                when (editorStyle) {
                    false -> closable
                    true -> closable && (tabState.isHovered || tabState.isSelected)
                }

            if (showCloseIcon) {
                val closeActionInteractionSource = remember { MutableInteractionSource() }
                LaunchedEffect(closeActionInteractionSource) {
                    closeActionInteractionSource.interactions.collect { interaction ->
                        when (interaction) {
                            is PressInteraction.Press -> closeButtonState = closeButtonState.copy(pressed = true)
                            is PressInteraction.Cancel, is PressInteraction.Release -> {
                                closeButtonState = closeButtonState.copy(pressed = false)
                            }

                            is HoverInteraction.Enter -> closeButtonState = closeButtonState.copy(hovered = true)

                            is HoverInteraction.Exit -> closeButtonState = closeButtonState.copy(hovered = false)
                        }
                    }
                }

                val closePainter by tabStyle.icons.close.getPainter(Stateful(closeButtonState))
                Image(
                    modifier = Modifier
                        .clickable(
                            interactionSource = closeActionInteractionSource,
                            indication = null,
                            onClick = onClose,
                            role = Role.Button,
                        )
                        .size(16.dp),
                    painter = closePainter,
                    contentDescription = "Close tab",
                )
            } else if (closable) {
                Spacer(Modifier.size(16.dp))
            }
        }
    }
}
