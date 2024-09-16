package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs

import androidx.compose.runtime.Composable

abstract class Dialog(
    private val title: String = "Dialog",
    val windowed: Boolean = true,
    val onClose: (() -> Unit)? = null //called by the DialogManager when closing this dialog
) {
    @Composable
    abstract fun Content()

    open fun title(): String {
        return title
    }
}