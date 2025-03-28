package eu.florian_fuhrmann.musictimedtriggers.gui.views.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.styling.LocalGroupHeaderStyle
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import java.awt.Cursor

/**
 * Similar to the
 * [GroupHeader composable from the Jewel UI library](https://github.com/JetBrains/intellij-community/blob/d9b2a81d6fba66ea773da7ed6cb3bde48343d157/platform/jewel/ui/src/main/kotlin/org/jetbrains/jewel/ui/component/GroupHeader.kt).
 * GroupHeader is not used directly as the slot api is not available in Jewel 0.27.0.
 */
@Composable
fun OpenableGroupHeader(
    open: Boolean,
    onOpenedChange: (Boolean) -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Row(
        modifier = modifier
            .clickable(indication = null, interactionSource = interactionSource) { onOpenedChange(!open) }
            .hoverable(interactionSource)
            .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(16.dp)) {
            if (open) {
                Icon(AllIconsKeys.General.ChevronDown, "Chevron")
            } else {
                Icon(AllIconsKeys.General.ChevronRight, "Chevron")
            }
        }
        Text(text)

        Divider(
            orientation = Orientation.Horizontal,
            modifier = Modifier.weight(1f),
            color = LocalGroupHeaderStyle.current.colors.divider,
            thickness = LocalGroupHeaderStyle.current.metrics.dividerThickness,
            startIndent = LocalGroupHeaderStyle.current.metrics.indent,
        )
    }
}