package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.entries

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.*
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.ConfigurationContext
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.*
import org.jetbrains.jewel.ui.Outline
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip
import java.lang.reflect.Field

abstract class NumberConfigurationEntry<T>(
    configuration: Configuration,
    field: Field,
    configurable: Configurable,
    context: ConfigurationContext,
    customCheckers: List<RequireCustom>,
    visibleWhen: VisibleWhen?,
    private val buttonsParams: PlusMinusButtons?,
    private val numberFieldState: NumberFieldState<T>
) : AbstractConfigurationEntry<T>(
    configuration,
    field,
    configurable,
    context,
    customCheckers,
    visibleWhen
) where T : Number, T : Comparable<T> {
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        // State
        var checkerMessage: String? by remember { mutableStateOf(null) }
        // Ui
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Label (with tooltip)
            Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                Tooltip(
                    tooltip = { Text(configurable.description) },
                    enabled = configurable.displayName.isNotEmpty()
                ) {
                    Text("${configurable.displayName}:")
                }
            }
            // Input field
            Column {
                // Checker message (only show if number field itself is valid)
                checkerMessage?.let {
                    if(numberFieldState.isValid) {
                        InvalidInputPopup(it)
                    }
                }
                // Input field
                NumberField(
                    state = numberFieldState,
                    modifier = Modifier.fillMaxWidth(),
                    plusMinusButtons = buttonsParams != null,
                    plusMinusButtonsStep = buttonsParams?.step ?: 1,
                    outline = if(checkerMessage != null) Outline.Error else Outline.None
                )
            }
            // Export State back to field
            LaunchedEffect(numberFieldState) {
                snapshotFlow { numberFieldState.value }.collect { value ->
                    if(!numberFieldState.isValid) {
                        // if number field is invalid, do not set value, but remove checker message
                        checkerMessage = null
                        return@collect
                    }
                    // do custom check
                    val checkResult = when(value) {
                        is Int -> checkCustom(value)
                        is Double -> checkCustom(value)
                        else -> error("Unsupported type")
                    }
                    // check if value is valid
                    if(checkResult.valid) {
                        // set checker message
                        checkerMessage = null
                        // set field and call change callback
                        when(value) {
                            is Int -> field.setInt(configuration, value)
                            is Double -> field.setDouble(configuration, value)
                        }
                        handleValueChanged()
                    } else {
                        // set checker message
                        checkerMessage = checkResult.message
                    }
                }
            }
        }
    }
}

class IntegerConfigurationEntry(
    configuration: Configuration,
    field: Field,
    configurable: Configurable,
    context: ConfigurationContext,
    customCheckers: List<RequireCustom>,
    visibleWhen: VisibleWhen?,
    intRange: RequireIntRange?,
    buttonsParams: PlusMinusButtons?
) : NumberConfigurationEntry<Int>(
    configuration,
    field,
    configurable,
    context,
    customCheckers,
    visibleWhen,
    buttonsParams,
    IntNumberFieldState(
        field.getInt(configuration),
        (intRange?.min ?: Int.MIN_VALUE)..(intRange?.max ?: Int.MAX_VALUE)
    )
)

class DoubleConfigurationEntry(
    configuration: Configuration,
    field: Field,
    configurable: Configurable,
    context: ConfigurationContext,
    customCheckers: List<RequireCustom>,
    visibleWhen: VisibleWhen?,
    doubleRange: RequireDoubleRange?,
    buttonsParams: PlusMinusButtons?
) : NumberConfigurationEntry<Double>(
    configuration,
    field,
    configurable,
    context,
    customCheckers,
    visibleWhen,
    buttonsParams,
    DoubleNumberFieldState(
        field.getDouble(configuration),
        (doubleRange?.min ?: Double.NEGATIVE_INFINITY)..(doubleRange?.max ?: Double.POSITIVE_INFINITY)
    )
)