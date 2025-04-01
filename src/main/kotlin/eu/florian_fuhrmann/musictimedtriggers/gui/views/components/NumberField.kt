package eu.florian_fuhrmann.musictimedtriggers.gui.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Outline
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@Composable
fun NumberField(
    state: NumberFieldState<*>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    plusMinusButtons: Boolean = false,
    outline: Outline = Outline.None,
    placeholder: @Composable (() -> Unit)? = null,
    undecorated: Boolean = false
) {
    if(state.isInValidRange == false) {
        // show error message
        PopupContainer(
            onDismissRequest = {},
            horizontalAlignment = Alignment.Start,
            popupProperties = PopupProperties(focusable = false)
        ) {
            Box(modifier = Modifier.background(JewelTheme.globalColors.outlines.error).padding(5.dp)) {
                Text("must be in range ${state.validRange.start} - ${state.validRange.endInclusive}", color = JewelTheme.globalColors.text.error)
            }
        }
    }
    TextField(
        state = state.textFieldState,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        outline = if(state.isValid) outline else Outline.Error,
        placeholder = placeholder,
        undecorated = undecorated,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = if(state.isDecimal) KeyboardType.Decimal else KeyboardType.Number,
        ),
        inputTransformation = {
            // get text
            val text = this.asCharSequence()
            // only allow digits, minus and dots for decimal numbers
            if(text.any { c -> !c.isDigit() && c != '-' && (c != '.' || !state.isDecimal) }) revertAllChanges()
            // only allow 1 dot for decimal numbers
            if(state.isDecimal && text.count { c -> c == '.' } > 1) revertAllChanges()
            // only allow minus as first char (check if any char after the first char is minus)
            if(text.drop(1).any { c -> c == '-' }) revertAllChanges()
        },
        trailingIcon = {
            // only show plus and minus buttons if enabled
            if(!plusMinusButtons) return@TextField
            Row {
                // Minus Button
                Column {
                    IconButton(
                        onClick = {
                            val newValue = state.value.let {
                                when (it) {
                                    is Double -> it - 1.0
                                    is Int -> it - 1
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
                                    is Double -> it + 1.0
                                    is Int -> it + 1
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
    )
}

sealed class NumberFieldState<T>(initialValue: Number) where T : Number, T : Comparable<T> {
    abstract val isDecimal: Boolean
    val textFieldState = TextFieldState(initialValue.toString())
    abstract val validRange: ClosedRange<T>
    abstract val value: T?
    val isValid: Boolean
        get() = value.let { it != null && it in validRange }
    val isInValidRange: Boolean?
        get() = value?.let { it in validRange }
}

class IntNumberFieldState(
    initialValue: Int,
    override val validRange: ClosedRange<Int> = Int.MIN_VALUE..Int.MAX_VALUE
) : NumberFieldState<Int>(initialValue) {
    override val isDecimal = false
    /**
     * The current value of the field. This is an [Int] or null if the field is
     * empty or invalid.
     */
    override val value by derivedStateOf { textFieldState.text.toString().toIntOrNull() }
}

class DoubleNumberFieldState(
    initialValue: Double,
    override val validRange: ClosedRange<Double> = Double.NEGATIVE_INFINITY..Double.POSITIVE_INFINITY
) : NumberFieldState<Double>(initialValue) {
    override val isDecimal = true
    /**
     * The current value of the field. This is a [Double] or null if the field
     * is empty or invalid.
     */
    override val value by derivedStateOf { textFieldState.text.toString().toDoubleOrNull() }
}
