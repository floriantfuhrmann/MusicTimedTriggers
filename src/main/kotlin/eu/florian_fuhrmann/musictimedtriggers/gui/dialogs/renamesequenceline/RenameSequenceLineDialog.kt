package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.renamesequenceline

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Dialog
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.components.CloseDialogButton
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.components.DialogFrame
import eu.florian_fuhrmann.musictimedtriggers.triggers.sequence.TriggerSequenceLine
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.ui.Outline
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField

class RenameSequenceLineDialog(private val line: TriggerSequenceLine) : Dialog("Rename ${line.name}") {

    @Composable
    override fun Content() {
        //State
        val initialName = line.name
        var nameTextFieldValue by remember {
            mutableStateOf(TextFieldValue(initialName, TextRange(initialName.length, initialName.length)))
        }
        val validName by derivedStateOf { nameTextFieldValue.text.isNotBlank() }
        //Done Callback
        val onDone = {
            if(validName) {
                //close dialog and update name
                DialogManager.closeDialog()
                line.updateName(nameTextFieldValue.text)
            }
        }
        //UI
        DialogFrame(
            rightButtons = {
                //Close Button
                CloseDialogButton(text = "Cancel", modifier = Modifier.padding(end = 5.dp))
                //Done Button
                DefaultButton(
                    enabled = validName,
                    onClick = onDone,
                    modifier = Modifier.trackActivation()
                ) {
                    Text("Done")
                }
            }
        ) {
            //Heading
            Row {
                Text("Sequence Line Name:")
            }
            //Text Field
            Row {
                //because this dialog only contains one field it should have focus when launching
                val focusRequester = remember { FocusRequester() }
                TextField(
                    outline = if(validName) { Outline.None } else { Outline.Error },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    value = nameTextFieldValue,
                    onValueChange = {
                        nameTextFieldValue = it
                    },
                    //so Enter also triggers onDone
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onDone() })
                )
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            }
        }
    }
}