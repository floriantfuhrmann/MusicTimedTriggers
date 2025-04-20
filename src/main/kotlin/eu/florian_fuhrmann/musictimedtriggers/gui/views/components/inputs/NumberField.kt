package eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Outline
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.component.styling.TextFieldStyle
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.theme.textFieldStyle

@Composable
fun NumberField(
    state: NumberFieldState<*>,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = JewelTheme.defaultTextStyle,
    style: TextFieldStyle = JewelTheme.textFieldStyle,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    plusMinusButtons: Boolean = false,
    plusMinusButtonsStep: Number = 1,
    outline: Outline = Outline.None,
    forceOverrideOutline: Boolean = false,
    placeholder: @Composable (() -> Unit)? = null,
    undecorated: Boolean = false,
    customTrailingIcon: @Composable (() -> Unit)? = null
) {
    var textFieldFocused by remember { mutableStateOf(false) }
    if(state.isInValidRange == false && textFieldFocused) {
        // show error message
        InvalidInputPopup("must be in range ${state.validRange.start} - ${state.validRange.endInclusive}")
    }
    TextField(
        state = state.textFieldState,
        modifier = modifier.onFocusChanged {
            textFieldFocused = it.isFocused
        },
        textStyle = textStyle,
        style = style,
        enabled = enabled,
        readOnly = readOnly,
        outline = if(forceOverrideOutline || state.isValid) outline else Outline.Error,
        placeholder = placeholder,
        undecorated = undecorated,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = if(state.isDecimal) KeyboardType.Decimal else KeyboardType.Number,
        ),
        inputTransformation = {
            // get text
            val text = this.asCharSequence()
            // only allow digits, minus and dots (for decimal numbers) and colons (for time numbers)
            if(text.any { c -> !c.isDigit() && c != '-' && (c != '.' || !state.isDecimal) && (c != ':' || state !is TimeNumberFieldState) }) revertAllChanges()
            // only allow 1 dot for decimal numbers
            if(state.isDecimal && text.count { c -> c == '.' } > 1) revertAllChanges()
            // only allow minus as first char (check if any char after the first char is minus)
            if(text.drop(1).any { c -> c == '-' }) revertAllChanges()
            // special conditions for time numbers (some are commented out, because they would be too strict and a temporary invalid state should be ok, when changing the value)
            if(state is TimeNumberFieldState) {
                // only allow 1 colon
                if(text.count { c -> c == ':' } > 1) revertAllChanges()
//                // only allow dot after 2 digits if a colon is present
//                if(text.contains(':') && text.contains('.') && text.indexOf('.') - 2 <= text.indexOf(':')) revertAllChanges()
//                // only allow dot if there is a digit before it
//                if(text.contains('.') && text.indexOf('.').let { it == 0 || !text[it-1].isDigit() }) revertAllChanges()
                // do not allow number after colon to have more than 2 digits
                if(text.contains(':')) {
                    val colonIndex = text.indexOf(':')
                    val seconds = text.drop(colonIndex + 1).takeWhile { it.isDigit() }
                    if(seconds.length > 2) revertAllChanges()
                }
                // do not allow number after dot to have more than 3 digits
                if(text.contains('.')) {
                    val dotIndex = text.indexOf('.')
                    val milliseconds = text.drop(dotIndex + 1).takeWhile { it.isDigit() }
                    if(milliseconds.length > 3) revertAllChanges()
                }
//                // only allow 0..5 for first digit after colon
//                if(text.contains(':') && text.last() != ':' && text[text.indexOf(':') + 1].toString().toIntOrNull() !in 0..5) revertAllChanges()
            }
        },
        trailingIcon = {
            // invoke custom trailing icon if present (will replace the plus and minus buttons)
            if(customTrailingIcon != null) {
                customTrailingIcon.invoke()
            } else if(plusMinusButtons) {
                Row {
                    // Minus Button
                    Column {
                        IconButton(
                            onClick = {
                                val newValue = state.value.let {
                                    when (it) {
                                        is Double -> it - plusMinusButtonsStep.toDouble()
                                        is Int -> it - plusMinusButtonsStep.toInt()
                                        else -> return@IconButton
                                    }
                                }
                                state.textFieldState.setTextAndPlaceCursorAtEnd(newValue.toString())
                            }
                        ) {
                            Icon(AllIconsKeys.General.Remove, null)
                        }
                    }
                    // Plus Button
                    Column {
                        IconButton(
                            onClick = {
                                val newValue = state.value.let {
                                    when (it) {
                                        is Double -> it + plusMinusButtonsStep.toDouble()
                                        is Int -> it + plusMinusButtonsStep.toInt()
                                        else -> return@IconButton
                                    }
                                }
                                state.textFieldState.setTextAndPlaceCursorAtEnd(newValue.toString())
                            }
                        ) {
                            Icon(AllIconsKeys.General.Add, null)
                        }
                    }
                }
            }
        }
    )
}

sealed class NumberFieldState<T>(initialValue: Number, initialTextFieldValue: String = initialValue.toString()) where T : Number, T : Comparable<T> {
    abstract val isDecimal: Boolean
    val textFieldState = TextFieldState(initialTextFieldValue)
    abstract var validRange: ClosedRange<T>
    abstract val value: T?
    val isValid: Boolean
        get() = value.let { it != null && it in validRange }
    val isInValidRange: Boolean?
        get() = value?.let { it in validRange }
}

class IntNumberFieldState(
    initialValue: Int,
    override var validRange: ClosedRange<Int> = Int.MIN_VALUE..Int.MAX_VALUE
) : NumberFieldState<Int>(initialValue) {
    override val isDecimal = false
    /**
     * The current value of the field. This is an [Int] or null if the field is
     * empty or can not be parsed.
     * Warning: It can have a value outside the valid range!
     */
    override val value by derivedStateOf { textFieldState.text.toString().toIntOrNull() }
}

class DoubleNumberFieldState(
    initialValue: Double,
    override var validRange: ClosedRange<Double> = Double.NEGATIVE_INFINITY..Double.POSITIVE_INFINITY
) : NumberFieldState<Double>(initialValue) {
    override val isDecimal = true
    /**
     * The current value of the field. This is a [Double] or null if the field
     * is empty or can not be parsed.
     * Warning: It can have a value outside the valid range!
     */
    override val value by derivedStateOf { textFieldState.text.toString().toDoubleOrNull() }
}

/**
 * A [NumberFieldState] that represents a time value in seconds.
 *
 * @param initialValue The initial value of the field in seconds.
 * @param validRange The valid range of the field in seconds. Defaults to
 *    negative infinity to positive infinity.
 */
class TimeNumberFieldState(
    initialValue: Double,
    override var validRange: ClosedRange<Double> = Double.NEGATIVE_INFINITY..Double.POSITIVE_INFINITY
) : NumberFieldState<Double>(initialValue, run {
    val minutes = (initialValue / 60).toInt()
    val seconds = (initialValue % 60).toInt()
    val milliseconds = ((initialValue - initialValue.toInt()) * 1000).toInt()
    String.format("%02d:%02d.%03d", minutes, seconds, milliseconds)
}) {
    override val isDecimal = true
    /**
     * The current value of the field. This is a [Double] or null if the field
     * is empty or can not be parsed.
     * Warning: It can have a value outside the valid range!
     */
    override val value by derivedStateOf {
        val rawText = textFieldState.text.toString()
        // parse the text as a time value using regex
        val regex = Regex("^-?(?:(\\d+):)?(\\d{1,2})(?:\\.(\\d{1,3}))?\$")
        val matchResult = regex.find(rawText)
        if(matchResult != null) {
            val sign = if(rawText.firstOrNull() == '-') -1 else 1
            val minutes = matchResult.groupValues[1].toIntOrNull() ?: 0
            val seconds = matchResult.groupValues[2].toIntOrNull() ?: 0
            val milliseconds = matchResult.groupValues[3].padEnd(3, '0').toIntOrNull() ?: 0
            val minutesGiven = matchResult.groupValues[1].isNotBlank()
            // if minutes are given, require seconds to be exactly 2 digits
            if(minutesGiven && matchResult.groupValues[2].length != 2) {
                return@derivedStateOf null
            }
            // require values to be in a sensible range
            if(minutes < 0 || seconds < 0 || (seconds > 59 && minutesGiven) || milliseconds < 0 || milliseconds > 999) {
                return@derivedStateOf null
            }
            // convert the time value to seconds
            sign * (minutes * 60 + seconds + milliseconds / 1000.0)
        } else {
            null
        }
    }
}
