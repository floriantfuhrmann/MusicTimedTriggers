package eu.florian_fuhrmann.musictimedtriggers.gui.views.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.ui.Outline
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import java.awt.Cursor

@Composable
fun DoubleField(
    initialValue: Double,
    minValue: Double = -Double.MAX_VALUE,
    maxValue: Double = Double.MAX_VALUE,
    buttons: Boolean = true, // weither to show plus and minus buttons
    buttonStep: Double = 1.0,
    plus: (Double) -> Double = { it + buttonStep},
    minus: (Double) -> Double = { it - buttonStep },
    placeholder: String = "",
    enabled: Boolean = true,
    onChange: (Double) -> Unit = {}, //should only be called with valid values (in range values)
    onChangeInvalid: () -> Unit = {}, //will be called when the current input changes but is invalid
    outline: Outline = Outline.None
): (Double) -> Unit {
    var valueString: String by remember { mutableStateOf(initialValue.toString()) }
    var doubleValue: Double by remember { mutableStateOf(initialValue) }
    val inRange: Boolean by remember { derivedStateOf { doubleValue in minValue..maxValue } }
    var validDouble: Boolean by remember { mutableStateOf(true) }

    Column {
        Row {
            TextField(
                value = valueString,
                onValueChange = {
                    //only allow digits, dots and minus
                    if(it.any { c -> !c.isDigit() && c != '.' && c != '-' }) {
                        return@TextField //just don't change the string value
                    }
                    //only allow one dot
                    if(it.count { c -> c == '.' } > 1) {
                        return@TextField //just don't change the string value
                    }
                    //only allow minus as first char (check if any char after the first char is minus)
                    if(it.drop(1).any { c -> c == '-' }) {
                        return@TextField //just don't change the string value
                    }
                    //update string
                    valueString = it
                    //get double value
                    val newDoubleValue = it.toDoubleOrNull()
                    //check if the string could be parsed as a double
                    if(newDoubleValue != null) {
                        validDouble = true
                        //and update value
                        doubleValue = newDoubleValue
                        //call onChange if in range
                        if(inRange) {
                            onChange(doubleValue)
                        } else {
                            //else call on chane invalid
                            onChangeInvalid()
                        }
                    } else {
                        //the string is no valid double
                        validDouble = false
                        //call on change in valid
                        onChangeInvalid()
                    }
                },
                placeholder = {
                    Text(placeholder)
                },
                outline = if(inRange && validDouble) { outline } else Outline.Error,
                trailingIcon = {
                    if(buttons) {
                        Row {
                            SimpleIconButton(
                                enabled = validDouble && doubleValue > minValue,
                                forceHoverHandCursor = true,
                                iconName = "minus-icon",
                                onClick = {
                                    if(validDouble) {
                                        val newValue = minus(doubleValue).coerceAtLeast(minValue).coerceAtMost(maxValue)
                                        if (doubleValue != newValue) {
                                            doubleValue = newValue
                                            valueString = doubleValue.toString()
                                            onChange(doubleValue)
                                        }
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            )
                            SimpleIconButton(
                                enabled = validDouble && doubleValue < maxValue,
                                forceHoverHandCursor = true,
                                iconName = "plus-icon",
                                onClick = {
                                    if(validDouble) {
                                        val newValue = plus(doubleValue).coerceAtLeast(minValue).coerceAtMost(maxValue)
                                        if (doubleValue != newValue) {
                                            doubleValue = newValue
                                            valueString = doubleValue.toString()
                                            onChange(doubleValue)
                                        }
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                enabled = enabled,
                modifier = Modifier.trackActivation().fillMaxWidth()
            )
        }
        if(!validDouble) {
            Row {
                Text(color = Color.Red, text = "Invalid Decimal Number")
            }
        } else if(!inRange) {
            Row {
                Text(color = Color.Red, text = "Number has to be in range $minValue ... $maxValue")
            }
        }
    }
    //Return Function to update the value from the outside
    return {
        doubleValue = it
    }
}
