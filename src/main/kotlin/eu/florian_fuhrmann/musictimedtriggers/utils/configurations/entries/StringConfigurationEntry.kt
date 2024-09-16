package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.entries

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.*
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.CheckResult
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.ui.Outline
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.component.Tooltip
import java.lang.reflect.Field

class StringConfigurationEntry(
    configuration: Configuration,
    field: Field,
    configurable: Configurable,
    customCheckers: List<RequireCustom>,
    visibleWhen: VisibleWhen?,
    private val intRange: RequireIntRange?,
) : AbstractConfigurationEntry<String>(
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
        var textValue by remember { mutableStateOf(field.get(configuration) as String) }
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
            TextField(
                value = textValue,
                onValueChange = { newValue ->
                    //update ui value
                    textValue = newValue
                    //check length
                    if(intRange == null || (intRange.min <= newValue.length && intRange.max >= newValue.length)) {
                        //custom check
                        val checkResult = checkCustom(newValue)
                        if(checkResult.valid) {
                            //set checker message
                            checkerMessage = null
                            //set field and call change callback
                            field.set(configuration, newValue)
                            handleValueChanged()
                        } else {
                            checkerMessage = checkResult.message
                        }
                    } else {
                        checkerMessage = "Has to be between ${intRange.min} and ${intRange.max} characters"
                    }
                },
                outline = if(checkerMessage == null) { Outline.None } else { Outline.Error },
                modifier = Modifier.trackActivation().fillMaxWidth()
            )
        }
        if(checkerMessage != null) {
            Row {
                Text(color = Color.Red, text = checkerMessage!!)
            }
        }
    }
}

object NotBlankChecker : CustomChecker<String>() {
    override fun check(value: String): CheckResult {
        return CheckResult(value.isNotBlank(), "Can not be blank")
    }
}