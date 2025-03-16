package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.alerts

import androidx.compose.runtime.Composable

abstract class AbstractAlert {

    @Composable
    abstract fun Content(totalAlertAmount: Int)

}