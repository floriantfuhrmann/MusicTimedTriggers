package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.configuration

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.Collapsible
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import org.jetbrains.jewel.ui.component.Text

@Composable
fun ConfigurationCollapsible(configuration: Configuration, title: String) {
    Collapsible(
        header = {
            Text(title)
        },
        contentModifier = Modifier.padding(top = 5.dp, start = 10.dp, end = 10.dp, bottom = 10.dp)
    ) {
        ConfigurationBox(configuration)
    }
}
