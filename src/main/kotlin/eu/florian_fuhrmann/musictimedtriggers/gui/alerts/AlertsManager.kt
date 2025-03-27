package eu.florian_fuhrmann.musictimedtriggers.gui.alerts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.window.DialogWindowScope
import androidx.compose.ui.window.FrameWindowScope

object AlertsManager {

    private var alerts: MutableList<BasicAlert> = mutableStateListOf()

    fun showAlert(alert: BasicAlert) {
        alerts.add(alert)
    }

    fun closeAlert(alert: BasicAlert) {
        alerts.remove(alert)
    }

    @Composable
    fun AlertsContainer(frameWindowScope: FrameWindowScope) {
        alerts.forEach { alert ->
            WindowedAlertContainer(frameWindowScope) {
                alert.Content()
            }
        }
    }

    @Composable
    fun AlertsContainer(dialogWindowScope: DialogWindowScope) {
        alerts.forEach { alert ->
            WindowedAlertContainer(dialogWindowScope) {
                alert.Content()
            }
        }
    }

}