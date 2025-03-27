package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import androidx.compose.ui.zIndex
import eu.florian_fuhrmann.musictimedtriggers.gui.alerts.AlertsManager
import java.awt.Dimension

object DialogManager {
    private var openedDialog: Dialog? by mutableStateOf(null)
    val dialogOpened by derivedStateOf { openedDialog != null }
    private var alwaysOnTop: Boolean by mutableStateOf(true)

    fun openDialog(dialog: Dialog) {
        alwaysOnTop = true
        openedDialog = dialog
    }
    fun closeDialog() {
        openedDialog?.onClose?.let { it() }
        openedDialog = null
    }
    fun allowNotOnTop() {
        alwaysOnTop = false
    }
    fun requireOnTop() {
        alwaysOnTop = true
    }

    @Composable
    fun DialogContainer(frameWindowScope: FrameWindowScope) {
        // shadow the openedDialog variable to prevent it from changing while the dialog is being displayed
        val openedDialog = openedDialog
        if(openedDialog != null) {
            if(openedDialog.windowed) {
                // DialogWindow is used to create a windowed dialog
                DialogWindow(
                    state = rememberDialogState(
                        getCenteredAbsolutePosition(frameWindowScope, 500.dp, 350.dp),
                        500.dp, 350.dp
                    ),
                    onCloseRequest = { closeDialog() },
                    alwaysOnTop = alwaysOnTop,
                    title = openedDialog.title()
                ) {
                    this.window.minimumSize = Dimension(350, 350)
                    // Dialog Content
                    openedDialog.Content()
                    // (new) Alerts
                    AlertsManager.AlertsContainer(this@DialogWindow)
                }
                // put a box behind the dialog to prevent the user from interacting with the main window
                Box(modifier = Modifier.zIndex(2f).fillMaxSize().background(Color.Black.copy(alpha = 0.5f))) {
                    // empty box
                }
            } else {
                // non-windowed dialogs are displayed inline
                openedDialog.Content()
            }
        }
    }

    @Composable
    fun getCenteredAbsolutePosition(frameWindowScope: FrameWindowScope, width: Dp, height: Dp): WindowPosition.Absolute {
        return with(frameWindowScope) {
            WindowPosition.Absolute(
                x = Dp(window.locationOnScreen.x + window.size.width / 2.0f) - width / 2.0f,
                y = Dp(window.locationOnScreen.y + window.size.height / 2.0f) - height / 2.0f
            )
        }
    }

}
