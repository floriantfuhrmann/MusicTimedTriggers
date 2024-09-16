package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.edittemplategroup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Alert
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Dialog
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.components.CloseDialogButton
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.components.DialogFrame
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.groups.TriggerTemplateGroup
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.ui.Outline
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField

class EditTemplateGroupDialog(val create: Boolean, private val triggerTemplateGroup: TriggerTemplateGroup?) : Dialog() {

    override fun title(): String {
        return if(create) {
            "Create Group"
        } else {
            "Edit Group ${triggerTemplateGroup?.name}"
        }
    }

    @Composable
    override fun Content() {
        //State
        val initialName = triggerTemplateGroup?.name ?: ""
        var nameTextFieldValue by remember {
            mutableStateOf(
                TextFieldValue(
                    triggerTemplateGroup?.name ?: "",
                    TextRange(initialName.length, initialName.length)
                )
            )
        }
        val validName by derivedStateOf { nameTextFieldValue.text.isNotBlank() }
        //Done Callback
        val onDone = {
            if(validName) {
                //close dialog
                DialogManager.closeDialog()
                if (create) {
                    //create new group
                    ProjectManager.currentProject?.triggersManager?.createNewTriggerTemplateGroup(nameTextFieldValue.text)
                } else {
                    //edit group
                    require(triggerTemplateGroup != null) { "Should be editing, but templateGroup is null" }
                    ProjectManager.currentProject?.triggersManager?.updateTriggerTemplateGroup(
                        triggerTemplateGroup,
                        nameTextFieldValue.text
                    )
                }
            }
        }
        //UI
        DialogFrame(
            leftButtons = {
                //Delete Button
                if(triggerTemplateGroup != null) {
                    OutlinedButton(
                        onClick = {
                            //show confirmation alert
                            DialogManager.alert(
                                Alert(
                                    title = "Confirm Delete",
                                    text = "Are you sure you want to delete the Group ${triggerTemplateGroup.name}? This will also delete ${triggerTemplateGroup.templates.size} Templates.",
                                    dismissText = "Cancel",
                                    onDismiss = {},
                                    onConfirm = {
                                        //close dialog
                                        DialogManager.closeDialog()
                                        //delete group
                                        ProjectManager.currentProject?.triggersManager?.deleteTriggerTemplateGroup(triggerTemplateGroup)
                                    }
                                )
                            )
                        },
                        modifier = Modifier.padding(end = 5.dp).trackActivation()
                    ) {
                        Text(color = MainUiState.theme.errorTextColor(), text = "Delete")
                    }
                }
            },
            rightButtons = {
                //Close Button
                CloseDialogButton(text = "Cancel", modifier = Modifier.padding(end = 5.dp))
                //Apply/Create Button
                DefaultButton(
                    enabled = validName,
                    onClick = onDone,
                    modifier = Modifier.trackActivation()
                ) {
                    when(create) {
                        true -> Text("Create")
                        else -> Text("Done")
                    }
                }
            }
        ) {
            //Heading
            Row {
                Text("Group Name:")
            }
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
            if(triggerTemplateGroup != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    Text(
                        color = Color.Gray,
                        text = "Uuid: ${triggerTemplateGroup.uuid}"
                    )
                }
                Row {
                    Text(
                        color = Color.Gray,
                        text = "Templates: ${triggerTemplateGroup.templates.size}"
                    )
                }
            }
        }
    }


}