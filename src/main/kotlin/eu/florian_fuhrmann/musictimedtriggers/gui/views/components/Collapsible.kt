package eu.florian_fuhrmann.musictimedtriggers.gui.views.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.Stroke
import org.jetbrains.jewel.foundation.modifier.border
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.foundation.theme.JewelTheme
import java.awt.Cursor

@Composable
fun Collapsible(
    extended: MutableState<Boolean> = remember { mutableStateOf(true) },
    header: @Composable () -> Unit,
    contentModifier: Modifier = Modifier.padding(10.dp),
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .border(
                    alignment = Stroke.Alignment.Center,
                    width = 1.dp,
                    color = JewelTheme.globalColors.borders.normal,
                    shape = if(extended.value) { RoundedCornerShape(5.dp, 5.dp, 0.dp, 0.dp) } else { RoundedCornerShape(5.dp, 5.dp, 5.dp, 5.dp) },
                )
                .fillMaxWidth()
                .pointerHoverIcon(icon = PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                .clickable {
                    extended.value = !extended.value
                }
                .trackActivation()
        ) {
            Column(modifier = Modifier
                .padding(start = 10.dp, top = 10.dp, bottom = 10.dp, end = 0.dp)
                .weight(1f)
            ) {
                header()
            }
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(start = 0.dp, top = 10.dp, bottom = 10.dp, end = 10.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(16.dp)) {
                    if (extended.value) {
                        SimpleIcon("chevron-top-icon")
                    } else {
                        SimpleIcon("chevron-down-icon")
                    }
                }
            }
        }
        if(extended.value) {
            Row(
                modifier = Modifier
                    .border(
                        alignment = Stroke.Alignment.Center,
                        width = 1.dp,
                        color = JewelTheme.globalColors.borders.normal,
                        shape = RoundedCornerShape(0.dp, 0.dp, 5.dp, 5.dp),
                    )
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = contentModifier
                ) {
                    content()
                }
            }
        }
    }
}