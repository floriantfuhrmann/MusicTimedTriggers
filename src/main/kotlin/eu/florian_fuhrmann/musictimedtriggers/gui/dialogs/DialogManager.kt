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
    private var openedDialogs: MutableList<Dialog> = mutableStateListOf()
    val anyDialogOpened by derivedStateOf { openedDialogs.isNotEmpty() }
    private var alwaysOnTop: Boolean by mutableStateOf(true)

    /**
     * Opens a dialog and closes all other dialogs if [closeOthers] is true.
     * @param dialog The dialog to open.
     * @param closeOthers Whether to close all other dialogs or not.
     */
    fun openDialog(dialog: Dialog, closeOthers: Boolean = true) {
        alwaysOnTop = true
        if (closeOthers) {
            closeAllDialogs()
        }
        openedDialogs.add(dialog)
    }

    /**
     * Closes a dialog if it is opened.
     * @param dialog The dialog to close.
     * @return true if the dialog was closed, false if it was not opened.
     */
    fun closeDialog(dialog: Dialog): Boolean {
        if (openedDialogs.contains(dialog)) {
            openedDialogs.remove(dialog)
            dialog.onClose?.let { it() }
            return true
        } else {
            return false
        }
    }

    /**
     * Closes all opened dialogs.
     */
    fun closeAllDialogs() {
        openedDialogs.forEach {
            it.onClose?.let { it() }
        }
        openedDialogs.clear()
    }

    fun allowNotOnTop() {
        alwaysOnTop = false
    }

    fun requireOnTop() {
        alwaysOnTop = true
    }

    @Composable
    fun DialogContainer(frameWindowScope: FrameWindowScope) {
        openedDialogs.forEach { dialog ->
            if(dialog.windowed) {
                // DialogWindow is used to create a windowed dialog
                DialogWindow(
                    state = rememberDialogState(
                        getCenteredAbsolutePosition(frameWindowScope, 500.dp, 350.dp),
                        500.dp, 350.dp
                    ),
                    onCloseRequest = { closeAllDialogs() },
                    alwaysOnTop = alwaysOnTop,
                    title = dialog.title()
                ) {
                    this.window.minimumSize = Dimension(350, 350)
                    // Dialog Content
                    dialog.Content()
                    // (new) Alerts
                    AlertsManager.AlertsContainer(this@DialogWindow)
                }
                // put a box behind the dialog to prevent the user from interacting with the main window
                Box(modifier = Modifier.zIndex(2f).fillMaxSize().background(Color.Black.copy(alpha = 0.5f))) {
                    // empty box
                }
            } else {
                // non-windowed dialogs are displayed inline
                dialog.Content()
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
