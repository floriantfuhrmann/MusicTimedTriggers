package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        var selectedOption by remember { mutableStateOf(0) }
        SelectableIconActionButton(
            key = AllIconsKeys.Stub,
            contentDescription = null,
            onClick = { selectedOption = if(selectedOption == 0) -1 else 0 },
            extraHint = Size(20),
            selected = selectedOption == 0,
            focusable = false,
            modifier = Modifier.trackActivation()
        )
        SelectableIconActionButton(
            key = AllIconsKeys.Stub,
            contentDescription = null,
            onClick = { selectedOption = if(selectedOption == 1) -1 else 1 },
            extraHint = Size(20),
            selected = selectedOption == 1,
            focusable = false,
            modifier = Modifier.trackActivation()
        )
        SelectableIconActionButton(
            key = AllIconsKeys.Stub,
            contentDescription = null,
            onClick = { selectedOption = if(selectedOption == 2) -1 else 2 },
            extraHint = Size(20),
            selected = selectedOption == 2,
            focusable = false,
            modifier = Modifier.trackActivation(),
        )
    }
}