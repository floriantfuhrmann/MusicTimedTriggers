package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.zIndex
import eu.florian_fuhrmann.musictimedtriggers.gui.alerts.AlertsManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.alerts.AbstractAlert
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.alerts.CustomAlert
import java.awt.Dimension

object DialogManager {
    private var openedDialog: Dialog? by mutableStateOf(null)
    val dialogOpened by derivedStateOf { openedDialog != null }
    private var alwaysOnTop: Boolean by mutableStateOf(true)
    private var alerts: MutableList<AbstractAlert> = mutableStateListOf()
    val anyAlerts = derivedStateOf { alerts.isNotEmpty() } //tracks whether any alerts are currently visible

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

    fun alert(alert: AbstractAlert) {
        alerts.add(alert)
    }
    fun closeAlert() {
        alerts.removeFirst()
    }

    @Composable
    fun DialogContainer() {
        // shadow the openedDialog variable to prevent it from changing while the dialog is being displayed
        val openedDialog = openedDialog
        if(openedDialog != null) {
            if(openedDialog.windowed) {
                // DialogWindow is used to create a windowed dialog
                DialogWindow(
                    onCloseRequest = { closeDialog() },
                    alwaysOnTop = alwaysOnTop && alerts.isEmpty(),
                    title = openedDialog.title()
                ) {
                    this.window.minimumSize = Dimension(350, 350)
                    // Dialog Content
                    openedDialog.Content()
                    // (deprecated) Alerts
                    if(alerts.isNotEmpty()) {
                        key(alerts.first()) {
                            alerts.first().Content(alerts.size)
                        }
                    }
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
        if(openedDialog == null || !openedDialog.windowed) {
            if(alerts.isNotEmpty()) {
                key(alerts.first()) {
                    alerts.first().Content(alerts.size)
                }
                LaunchedEffect(alerts.first()) {
                    if (alerts.first() is CustomAlert) {
                        (alerts.first() as CustomAlert).focusRequester?.requestFocus()
                    }
                }
            }
        }
    }

}
