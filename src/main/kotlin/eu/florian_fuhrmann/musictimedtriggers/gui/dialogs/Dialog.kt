package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

abstract class Dialog(
    private val title: String = "Dialog",
    val defaultWidth: Dp = 500.dp,
    val defaultHeight: Dp = 350.dp,
    val windowed: Boolean = true,
    val onClose: (() -> Unit)? = null //called by the DialogManager when closing this dialog
) {
    @Composable
    abstract fun Content()

    open fun title(): String {
        return title
    }
}