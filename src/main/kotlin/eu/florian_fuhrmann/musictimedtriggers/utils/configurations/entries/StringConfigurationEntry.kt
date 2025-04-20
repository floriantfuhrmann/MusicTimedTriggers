package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.entries

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.InvalidInputPopup
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.ConfigurationContext
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
    context: ConfigurationContext,
    customCheckers: List<RequireCustom>,
    visibleWhen: VisibleWhen?,
    private val intRange: RequireIntRange?,
    private val placeholderText: PlaceholderText?
) : AbstractConfigurationEntry<String>(
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
        // State
        var fieldFocused by remember { mutableStateOf(false) }
        var invalidInputMessage: String? by remember { mutableStateOf(null) }
        val textFieldState = rememberTextFieldState(field.get(configuration) as String)
        // Ui
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            // label
            Column {
                Tooltip(
                    tooltip = { Text(configurable.description) },
                    enabled = configurable.description.isNotEmpty()
                ) {
                    Text("${configurable.displayName}:")
                }
            }
            // text field (verticalArrangement = Arrangement.Center)
            Column {
                // checker message
                invalidInputMessage?.let {
                    if(fieldFocused) {
                        InvalidInputPopup(it)
                    }
                }
                // text field
                TextField(
                    state = textFieldState,
                    outline = if(invalidInputMessage == null) Outline.None else Outline.Error,
                    modifier = Modifier.trackActivation().fillMaxWidth().onFocusChanged { fieldFocused = it.isFocused },
                    placeholder = { placeholderText?.text?.let { Text(it) } }
                )
            }
            // Export State back to field
            LaunchedEffect(textFieldState) {
                snapshotFlow { textFieldState.text }.collect { value ->
                    // check length
                    if(intRange != null && (value.length < intRange.min || value.length > intRange.max)) {
                        // set message
                        invalidInputMessage = "Has to be between ${intRange.min} and ${intRange.max} characters"
                        return@collect
                    }
                    // do custom check
                    val checkResult = checkCustom(value.toString())
                    if(!checkResult.valid) {
                        // set message
                        invalidInputMessage = checkResult.message
                        return@collect
                    }
                    // unset message, set field and call change callback
                    invalidInputMessage = null
                    field.set(configuration, value)
                    handleValueChanged()
                }
            }
        }
    }
}

object NotBlankChecker : CustomChecker<String>() {
    override fun check(value: String): CheckResult {
        return CheckResult(value.isNotBlank(), "Can not be blank")
    }
}