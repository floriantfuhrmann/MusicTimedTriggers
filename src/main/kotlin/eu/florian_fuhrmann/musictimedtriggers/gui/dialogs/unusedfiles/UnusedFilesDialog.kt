package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.unusedfiles

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Alert
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Dialog
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.components.CloseDialogButton
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.components.DialogFrame
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import java.io.File

class UnusedFilesDialog(private val unusedFiles: List<File>) : Dialog(windowed = true) {
    @Composable
    override fun Content() {
        //State
        //create state for unusedFiles list
        val unusedFilesState by remember { mutableStateOf(unusedFiles.toMutableList()) }
        //UI
        DialogFrame(
            rightButtons = {
                CloseDialogButton()
            }
        ) {
            //Heading
            Row(modifier = Modifier.trackActivation()) {
                Text("The following files are unused:")
            }
            //List Unused files
            unusedFilesState.forEach {
                Row(modifier = Modifier.padding(top = 5.dp)) {
                    Column(
                        modifier = Modifier.border(
                            width = 1.dp,
                            color = JewelTheme.globalColors.borders.normal,
                            shape = RoundedCornerShape(5.dp)
                        ).padding(5.dp).fillMaxWidth()
                    ) {
                        Row {
                            Text("File: ${it.name}")
                        }
                        Row(modifier = Modifier.padding(top = 3.dp)) {
                            OutlinedButton(
                                onClick = {
                                    //open confirm alter
                                    DialogManager.alert(Alert(
                                        title = "Confirm delete ${it.name}",
                                        text = "Are you sure you want to delete the file ${it.canonicalPath}?",
                                        dismissText = "Cancel",
                                        onDismiss = {},
                                        onConfirm = {
                                            //delete file
                                            it.delete()
                                            //remove from list
                                            unusedFilesState.remove(it)
                                        }
                                    ))
                                },
                                modifier = Modifier.trackActivation()
                            ) {
                                Text(color = MainUiState.theme.errorTextColor(), text = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}