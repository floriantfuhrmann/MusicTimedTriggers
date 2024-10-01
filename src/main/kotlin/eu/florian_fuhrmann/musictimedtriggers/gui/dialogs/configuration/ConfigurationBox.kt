package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.configuration

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.ConfigurationContext
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.EmptyConfigurationContext

@Composable
fun ConfigurationBox(
    configuration: Configuration,
    context: ConfigurationContext = EmptyConfigurationContext()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        //State
        val entries by remember { mutableStateOf(configuration.createOrGetEntries(context)) }
        //Ui
        Column {
            entries.forEach {
                key(it.configurable) {
                    if(it.visible.value) {
                        it.Content()
                    }
                }
            }
        }
    }
}