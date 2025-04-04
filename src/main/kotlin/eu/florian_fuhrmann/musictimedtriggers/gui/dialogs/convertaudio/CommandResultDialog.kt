package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.convertaudio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Dialog
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.TextArea

class CommandResultDialog(private val command: List<String>, private val result: CommandResult) : Dialog("Command Output") {
    @Composable
    override fun Content() {
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxHeight().background(JewelTheme.globalColors.panelBackground)) {
                TextArea(
                    state = rememberTextFieldState(
                        "$ ${command.joinToString(" ")}\n" +
                                "${result.output}\n" +
                                "Exit Code: ${result.exitCode}"
                    ),
                    modifier = Modifier.fillMaxSize(),
                    readOnly = true,
                    undecorated = true
                )
            }
        }
    }
}