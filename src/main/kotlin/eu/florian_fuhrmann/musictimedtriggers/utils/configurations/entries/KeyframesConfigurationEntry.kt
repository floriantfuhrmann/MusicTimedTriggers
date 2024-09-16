package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.entries

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedTrigger
import eu.florian_fuhrmann.musictimedtriggers.triggers.utils.intensity.Keyframes
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.ConfigurationContext
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.*
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip
import java.lang.reflect.Field

class KeyframesConfigurationEntry(
    configuration: Configuration,
    field: Field,
    configurable: Configurable,
    visibleWhen: VisibleWhen?,
    private val context: ConfigurationContext
) : AbstractConfigurationEntry<String>(
    configuration,
    field,
    configurable,
    visibleWhen = visibleWhen
) {
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        if(context !is AbstractPlacedTrigger.PlacedTriggerConfigurationContext) {
            Row {
                Text("Configuration Context has to be PlacedTriggerConfigurationContext to configure ${configurable.displayName}", color = MainUiState.theme.errorTextColor())
            }
            return
        } else {
            // Heading
            Row(
                modifier = Modifier.padding(top = 5.dp)
            ) {
                if (configurable.description.isNotBlank()) {
                    Tooltip(tooltip = {
                        Text(configurable.description)
                    }) {
                        Text("${configurable.displayName}:")
                    }
                } else {
                    Text("${configurable.displayName}:")
                }
            }
            // Header Row for Keyframe List
            // Todo: Selectable for Position: proportional postion, relative second position, absolute second position
            // Keyframe List
            val keyframes = field.get(configuration) as Keyframes
            keyframes.keyframesList.forEach {
                Row(
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    // Todo: Keyframes Row with Position|Value|Options
                    Text("${it.position} - ${it.value}")
                }
            }
        }
    }
}