package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.alerts

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.jetbrains.jewel.foundation.theme.JewelTheme

/**
 * CustomAlert allows passing custom Composable for the content. And uses a custom Dialog container.
 */
@Deprecated("Use Dialogs instead")
open class CustomAlert(
    val onDismissRequest: () -> Unit /* = { DialogManager.closeAlert() }*/,
    val content: @Composable (() -> Unit),
    val contentMargin: Dp = 10.dp
) : AbstractAlert() {

    var focusRequester: FocusRequester? = null

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Content(totalAlertAmount: Int) {
        //init focus requester
        focusRequester = remember { FocusRequester() }
        //shadow the focus requester to prevent it from changing while the dialog is being displayed
        val focusRequester = focusRequester
        require(focusRequester != null)
        //Overlay Box
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .zIndex(1f)
                .onClick { onDismissRequest() }
                .focusable()
                .focusRequester(focusRequester)
                .onKeyEvent {
                    if(it.key == Key.Escape) {
                        onDismissRequest()
                        return@onKeyEvent true
                    }
                    return@onKeyEvent false
                },
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
