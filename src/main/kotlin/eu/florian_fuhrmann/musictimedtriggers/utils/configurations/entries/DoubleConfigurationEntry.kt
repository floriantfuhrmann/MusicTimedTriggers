package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.entries

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.DoubleField
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.*
import org.jetbrains.jewel.ui.Outline
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip
import java.lang.reflect.Field

class DoubleConfigurationEntry(
    configuration: Configuration,
    field: Field,
    configurable: Configurable,
    customCheckers: List<RequireCustom>,
    visibleWhen: VisibleWhen?,
    private val doubleRange: RequireDoubleRange?,
    private val buttonsParams: PlusMinusButtons?
) : AbstractConfigurationEntry<Double>(
    configuration,
    field,
    configurable,
    customCheckers,
    visibleWhen
) {
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        var checkerMessage: String? by remember { mutableStateOf(null) }
        Row(
            modifier = Modifier.padding(top = 5.dp)
        ) {
            Tooltip(tooltip = {
                Text(configurable.description)
            }) {
                Text("${configurable.displayName}:")
            }
        }
        Row(
            modifier = Modifier.padding(top = 2.dp)
        ) {
            DoubleField(
                initialValue = field.getDouble(configuration),
                minValue = doubleRange?.min ?: -Double.MAX_VALUE,
                maxValue = doubleRange?.max ?: Double.MAX_VALUE,
                buttons = buttonsParams != null,
                buttonStep = buttonsParams?.step ?: 1.0,
                outline = if(checkerMessage == null) { Outline.None } else { Outline.Error },
                onChange = {
                    val checkResult = checkCustom(it)
                    if(checkResult.valid) {
                        //set checker message
                        checkerMessage = null
                        //set field and call change callback
                        field.setDouble(configuration, it)
                        handleValueChanged()
                    } else {
                        checkerMessage = checkResult.message
                    }
                },
                onChangeInvalid = {
                    checkerMessage = null
                }
            )
        }
        if(checkerMessage != null) {
            Row {
                Text(color = Color.Red, text = checkerMessage!!)
            }
        }
    }
}

