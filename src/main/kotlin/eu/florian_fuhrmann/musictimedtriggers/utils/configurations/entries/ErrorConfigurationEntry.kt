package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.entries

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.Configurable
import org.jetbrains.jewel.ui.component.Text
import java.lang.reflect.Field

class ErrorConfigurationEntry(
    configuration: Configuration,
    field: Field,
    configurable: Configurable
) : AbstractConfigurationEntry<Any>(
    configuration,
    field,
    configurable
) {
    @Composable
    override fun Content() {
        Row {
            Text(color = Color.Red, text = "Can't create Entry for ${configurable.displayName}")
        }
    }
}