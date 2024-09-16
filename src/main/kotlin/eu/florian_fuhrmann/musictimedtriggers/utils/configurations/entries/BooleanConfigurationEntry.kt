package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.entries

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.Configurable
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.RequireCustom
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.VisibleWhen
import org.jetbrains.jewel.ui.component.CheckboxRow
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip
import java.lang.reflect.Field

class BooleanConfigurationEntry(
    configuration: Configuration,
    field: Field,
    configurable: Configurable,
    customCheckers: List<RequireCustom>,
    visibleWhen: VisibleWhen?,
) : AbstractConfigurationEntry<Boolean>(
    configuration,
    field,
    configurable,
    customCheckers,
    visibleWhen
) {
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        var checked by remember { mutableStateOf(field.getBoolean(configuration)) }
        Tooltip(tooltip = {
            Text(configurable.description)
        }) {
            CheckboxRow(
                text = configurable.displayName,
                checked = checked,
                onCheckedChange = {
                    checked = it
                    //set field and call change callback
                    field.setBoolean(configuration, it)
                    handleValueChanged()
                },
                modifier = Modifier.padding(top = 5.dp)
            )
        }
    }
}
