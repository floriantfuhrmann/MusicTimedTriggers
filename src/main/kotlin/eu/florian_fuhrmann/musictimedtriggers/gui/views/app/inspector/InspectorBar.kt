package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.InspectorOption
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import eu.florian_fuhrmann.musictimedtriggers.utils.icons.MttIcons
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.SelectableIconActionButton
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.painter.hints.Size

private fun selectOption(option: InspectorOption) {
    MainUiState.inspectorOption = if(MainUiState.inspectorOption == option) InspectorOption.None else option
}

@Composable
fun InspectorBar() {
    Column(modifier = Modifier
        .fillMaxHeight()
        .width(IntrinsicSize.Min)
        .background(JewelTheme.globalColors.borders.normal)
        .padding(start = 1.dp)
        .background(JewelTheme.globalColors.panelBackground)
        .padding(5.dp)
    ) {
        SelectableIconActionButton(
            key = MttIcons.song,
            contentDescription = null,
            onClick = { selectOption(InspectorOption.Song) },
            extraHint = Size(20),
            selected = MainUiState.inspectorOption == InspectorOption.Song,
            focusable = false,
            modifier = Modifier.trackActivation()
        )
        SelectableIconActionButton(
            key = AllIconsKeys.Stub,
            contentDescription = null,
            onClick = { selectOption(InspectorOption.PlacedTrigger) },
            extraHint = Size(20),
            selected = MainUiState.inspectorOption == InspectorOption.PlacedTrigger,
            focusable = false,
            modifier = Modifier.padding(top = 5.dp).trackActivation()
        )
        SelectableIconActionButton(
            key = AllIconsKeys.Stub,
            contentDescription = null,
            onClick = { selectOption(InspectorOption.TriggerTemplate) },
            extraHint = Size(20),
            selected = MainUiState.inspectorOption == InspectorOption.TriggerTemplate,
            focusable = false,
            modifier = Modifier.padding(top = 5.dp).trackActivation()
        )
    }
}