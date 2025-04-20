package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.styles.fixedCursorTooltipStyle
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.InspectorOption
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import eu.florian_fuhrmann.musictimedtriggers.utils.icons.MttIcons
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.SelectableIconButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip
import org.jetbrains.jewel.ui.painter.hints.Size

@Composable
fun InspectorBar() {
    Column(modifier = Modifier
        .fillMaxHeight()
        .width(IntrinsicSize.Min)
        .background(JewelTheme.globalColors.borders.normal)
        .padding(start = 1.dp)
        .background(JewelTheme.globalColors.panelBackground)
        .padding(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        InspectorItemButton(InspectorOption.Song)
        InspectorItemButton(InspectorOption.PlacedTrigger)
        InspectorItemButton(InspectorOption.TriggerTemplate)
    }
}

private fun selectOption(option: InspectorOption) {
    MainUiState.inspectorOption = if(MainUiState.inspectorOption == option) InspectorOption.None else option
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InspectorItemButton(inspectorOption: InspectorOption) {
    Tooltip(
        tooltip = { Text("${inspectorOption.displayName} Inspector") },
        style = fixedCursorTooltipStyle,
        tooltipPlacement = TooltipPlacement.ComponentRect(
            anchor = Alignment.CenterStart,
            alignment = Alignment.CenterStart,
            offset = DpOffset((-5).dp, 0.dp),
        )
    ) {
        SelectableIconButton(
            onClick = { selectOption(inspectorOption) },
            selected = MainUiState.inspectorOption == inspectorOption,
            focusable = false,
            modifier = Modifier.trackActivation()
        ) {
            Icon(
                key = when (inspectorOption) {
                    InspectorOption.Song -> MttIcons.song
                    InspectorOption.PlacedTrigger -> MttIcons.placedTrigger
                    InspectorOption.TriggerTemplate -> MttIcons.triggerTemplate
                    else -> error("Unknown inspector option $inspectorOption")
                },
                contentDescription = "${inspectorOption.displayName} Inspector",
                hint = Size(20),
                modifier = Modifier.padding(5.dp),
            )
        }
    }
}