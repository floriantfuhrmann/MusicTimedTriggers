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
fun IntegerField(
    initialValue: Int,
    minValue: Int = Int.MIN_VALUE,
    maxValue: Int = Int.MAX_VALUE,
    buttons: Boolean = true, // weither to show plus and minus buttons
    buttonStep: Int = 1,
    plus: (Int) -> Int = { it + buttonStep },
    minus: (Int) -> Int = { it -buttonStep },
    placeholder: String = "",
    enabled: Boolean = true,
    onChange: (Int) -> Unit = {}, //should only be called with valid values (in range values)
    onChangeInvalid: () -> Unit = {}, //will be called when the current input changes but is invalid
    outline: Outline = Outline.None
): (Int) -> Unit {
    var valueString: String by remember { mutableStateOf(initialValue.toString()) }
    var intValue by remember { mutableStateOf(initialValue) }
    val inRange: Boolean by remember { derivedStateOf { intValue in minValue..maxValue } }
    var validInt: Boolean by remember { mutableStateOf(true) }

    Column {
        Row {
            TextField(
                value = valueString,
                onValueChange = {
                    //only allow digits and minus
                    if(it.any { c -> !c.isDigit() && c != '-' }) {
                        return@TextField //just don't change the string value
                    }
                    //only allow minus as first char (check if any char after the first char is minus)
                    if(it.drop(1).any { c -> c == '-' }) {
                        return@TextField //just don't change the string value
                    }
                    //update string
                    valueString = it
                    //get new int value
                    val newIntValue = valueString.toIntOrNull()
                    //check if the string could be parsed as a int
                    if(newIntValue != null) {
                        //if so mark as valid
                        validInt = true
                        //and update value
                        intValue = newIntValue
                        //call onChange if in range
                        if(inRange) {
                            onChange(intValue)
                        } else {
                            //else call on chane invalid
                            onChangeInvalid()
                        }
                    } else {
                        //if so mark as not valid
                        validInt = false
                        onChangeInvalid()
                    }
                },
                placeholder = {
                    Text(placeholder)
                },
                outline = outline,
                trailingIcon = {
                    if(buttons) {
                        Row {
                            SimpleIconButton(
                                enabled = validInt && intValue > minValue,
                                forceHoverHandCursor = true,
                                iconName = "minus-icon",
                                onClick = {
                                    if(validInt) {
                                        val newValue = minus(intValue).coerceAtLeast(minValue).coerceAtMost(maxValue)
                                        if (intValue != newValue) {
                                            //update int and string value and call onChange
                                            intValue = newValue
                                            valueString = newValue.toString()
                                            onChange(intValue)
                                        }
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            )
                            SimpleIconButton(
                                enabled = validInt && intValue < maxValue,
                                forceHoverHandCursor = true,
                                iconName = "plus-icon",
                                onClick = {
                                    if(validInt) {
                                        val newValue = plus(intValue).coerceAtLeast(minValue).coerceAtMost(maxValue)
                                        if (intValue != newValue) {
                                            //update int and string value and call onChange
                                            intValue = newValue
                                            valueString = newValue.toString()
                                            onChange(intValue)
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
        if(!validInt) {
            Row {
                Text(color = Color.Red, text = "Invalid Integer Number")
            }
        } else if(!inRange) {
            Row {
                Text(color = Color.Red, text = "Number has to be in range $minValue ... $maxValue")
            }
        }
    }

    //Return Function to update the value from the outside
    return {
        intValue = it
    }
}