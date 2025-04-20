package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.entries

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.*
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.ConfigurationContext
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
    context: ConfigurationContext,
    customCheckers: List<RequireCustom>,
    visibleWhen: VisibleWhen?,
) : AbstractConfigurationEntry<Boolean>(
    configuration,
    field,
    configurable,
    context,
    customCheckers,
    visibleWhen
) {
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        var checked by remember { mutableStateOf(field.getBoolean(configuration)) }
        Tooltip(
            tooltip = { Text(configurable.description) },
            enabled = configurable.description.isNotEmpty()
        ) {
            CheckboxRow(
                text = configurable.displayName,
                checked = checked,
                onCheckedChange = {
                    checked = it
                    //set field and call change callback
                    field.setBoolean(configuration, it)
                    handleValueChanged()
                }
            )
        }
    }
}
