package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.SelectableIconActionButton
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.painter.hints.Size

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
            key = AllIconsKeys.Stub,
            contentDescription = null,
            onClick = { MainUiState.inspectorOption = if(MainUiState.inspectorOption == 0) -1 else 0 },
            extraHint = Size(20),
            selected = MainUiState.inspectorOption == 0,
            focusable = false,
            modifier = Modifier.trackActivation()
        )
        SelectableIconActionButton(
            key = AllIconsKeys.Stub,
            contentDescription = null,
            onClick = { MainUiState.inspectorOption = if(MainUiState.inspectorOption == 1) -1 else 1 },
            extraHint = Size(20),
            selected = MainUiState.inspectorOption == 1,
            focusable = false,
            modifier = Modifier.trackActivation()
        )
        SelectableIconActionButton(
            key = AllIconsKeys.Stub,
            contentDescription = null,
            onClick = { MainUiState.inspectorOption = if(MainUiState.inspectorOption == 2) -1 else 2 },
            extraHint = Size(20),
            selected = MainUiState.inspectorOption == 2,
            focusable = false,
            modifier = Modifier.trackActivation(),
        )
    }
}