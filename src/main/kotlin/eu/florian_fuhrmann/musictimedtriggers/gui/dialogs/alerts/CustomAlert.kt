package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.alerts

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.jetbrains.jewel.foundation.theme.JewelTheme

/**
 * CustomAlert allows passing custom Composable for the content. And uses a custom Dialog container.
 */
open class CustomAlert(
    val onDismissRequest: () -> Unit /* = { DialogManager.closeAlert() }*/,
    val content: @Composable (() -> Unit),
    val contentMargin: Dp = 10.dp
) : AbstractAlert() {

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Content(totalAlertAmount: Int) {
        //Overlay Box
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .zIndex(1f)
                .onClick { onDismissRequest() },
            contentAlignment = Alignment.Center
        ) {
            //1. Wrapper Box encapsulating the content in a max 75% by 75% box
            Box(modifier = Modifier.fillMaxWidth(0.75f).fillMaxHeight(0.75f).zIndex(1.1f), contentAlignment = Alignment.Center) {
                //2. Wrapper Box encapsulating the content in an opaque rounded corner box
                Box(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(JewelTheme.globalColors.panelBackground)
                        .clickable(enabled = false) { } // consume clicks to prevent closing the dialog
                ) {
                    //3. Wrapper Box encapsulating the content in a margin box
                    Box(modifier = Modifier.padding(contentMargin)) {
                        content.invoke()
                    }
                }
            }
        }
    }

}
