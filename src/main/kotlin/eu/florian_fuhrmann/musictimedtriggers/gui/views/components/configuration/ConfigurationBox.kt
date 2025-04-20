package eu.florian_fuhrmann.musictimedtriggers.gui.views.components.configuration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.ConfigurationContext
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.EmptyConfigurationContext

@Composable
fun ConfigurationBox(
    configuration: Configuration,
    context: ConfigurationContext = EmptyConfigurationContext(),
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        //State
        val entries by remember { mutableStateOf(configuration.createOrGetEntries(context)) }
        //Ui
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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