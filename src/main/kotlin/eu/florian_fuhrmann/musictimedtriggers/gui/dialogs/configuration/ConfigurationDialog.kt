package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.configuration

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Dialog
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.components.CloseDialogButton
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.components.DialogFrame
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.ConfigurationContext
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.EmptyConfigurationContext
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Text

/**
 * Dialog which has a ConfigurationBox as Content. The configuration passed to the Dialog will be modified even when
 * Cancel is clicked because the values will be set as soon as they change. If Cancel should not save the modifications
 * then parse a copy of the configuration to the ConfigurationDialog and use the onDone() callback to update the
 * original configuration
 */
class ConfigurationDialog(
    val configuration: Configuration,
    val context: ConfigurationContext = EmptyConfigurationContext(),
    private val heading: String,
    private val showCancelButton: Boolean = false,
    val onDone: (Configuration) -> Unit = {},
    onClose: (() -> Unit)? = null
) : Dialog(onClose = onClose) {

    @Composable
    override fun Content() {
        //UI
        DialogFrame(rightButtons = {
            //Close Button
            if(showCancelButton) {
                CloseDialogButton(text = "Cancel", modifier = Modifier.padding(end = 5.dp))
            }
            //Done Button
            DefaultButton(
                onClick = {
                    DialogManager.closeDialog()
                    onDone(configuration)
                },
                modifier = Modifier.trackActivation()
            ) {
                Text("Done")
            }
        }
        ) {
            Row {
                Text("$heading:")
            }
            Row {
                ConfigurationBox(configuration, context)
            }
        }
    }

    override fun title(): String {
        return heading
    }
}