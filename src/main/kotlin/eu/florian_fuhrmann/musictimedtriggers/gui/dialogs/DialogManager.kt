package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs

import androidx.compose.runtime.*
import androidx.compose.ui.window.DialogWindow
import java.awt.Dimension

object DialogManager {
    private var openedDialog: Dialog? by mutableStateOf(null)
    private var alwaysOnTop: Boolean by mutableStateOf(true)
    private var alerts: List<Alert> by mutableStateOf(emptyList())
    val anyAlerts = derivedStateOf { alerts.isNotEmpty() } //tracks weither any alerts are currently visible

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

    fun alert(alert: Alert) {
        alerts = (alerts + listOf(alert))
    }
    fun closeAlert() {
        alerts = alerts.slice(1..<alerts.size)
    }

    @Composable
    fun DialogContainer() {
        if(openedDialog != null) {
            if(openedDialog!!.windowed) {
                DialogWindow(
                    onCloseRequest = { closeDialog() },
                    alwaysOnTop = alwaysOnTop && alerts.isEmpty(),
                    title = openedDialog?.title().orEmpty()
                ) {
                    this.window.minimumSize = Dimension(350, 350)
                    openedDialog!!.Content()
                    if(alerts.isNotEmpty()) {
                        key(alerts.first()) {
                            alerts.first().Content()
                        }
                    }
                }
            } else {
                openedDialog!!.Content()
            }
        }
        if((openedDialog == null || openedDialog?.windowed == false)) {
            if(alerts.isNotEmpty()) {
                key(alerts.first()) {
                    alerts.first().Content(alerts.size)
                }
            }
        }
    }

}
