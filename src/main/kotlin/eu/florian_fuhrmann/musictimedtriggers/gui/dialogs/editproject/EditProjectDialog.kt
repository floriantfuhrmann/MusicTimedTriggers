package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.editproject

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Dialog
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.components.CloseDialogButton
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.components.DialogFrame
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.FilePathField
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.utils.color.GenericColor
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Text
import java.io.File

class EditProjectDialog(val create: Boolean, val project: Project? = null) : Dialog() {

    @Composable
    override fun Content() {
        //State
        //Project Directory
        var projectDirectory: File by remember {
            mutableStateOf(project?.projectDirectory ?: getFallbackFile())
        }
        val existingEmptyDirectorySelected: Boolean by derivedStateOf {
            (projectDirectory.exists() && projectDirectory.isDirectory && projectDirectory.listFiles()
                ?.isEmpty() ?: true)
        }
        //Color
        var selectedColor: HsvColor by remember { mutableStateOf(project?.projectSettings?.projectColor?.toHsvColor() ?: HsvColor.DEFAULT) }
        //UI
        DialogFrame(
            rightButtons = {
                //Close Button
                CloseDialogButton(text = "Cancel", modifier = Modifier.padding(end = 5.dp))
                //Apply/Create Button
                DefaultButton(
                    onClick = {
                        DialogManager.closeDialog()
                        if(create) {
                            //create project
                            ProjectManager.createProject(projectDirectory, GenericColor.fromHsvColor(selectedColor))
                        } else if(project != null) {
                            //update project
                            TODO("Update and save project")
                        } else {
                            error("EditProjectDialog is not creating, but project is null")
                        }
                    },
                    modifier = Modifier.trackActivation(),
                    enabled = !create || existingEmptyDirectorySelected
                ) {
                    when(create) {
                        true -> Text("Create")
                        else -> Text("Apply")
                    }
                }
            }
        ) {
            //Heading
            Row(modifier = Modifier.trackActivation()) {
                if(create) {
                    Text("Please select an empty directory to create a new Project in")
                } else {
                    Text("Project Directory:")
                }
            }
            //Project Directory Selector
            Row(modifier = Modifier.padding(top = 2.dp)) {
                FilePathField(
                    file = projectDirectory,
                    placeholder = "Project Directory",
                    fileDialogTitle = "Choose Project Directory",
                    directoryMode = true,
                    enabled = create,
                    onChange = {
                        projectDirectory = it
                    }
                )
            }
            if(create) {
                Row(modifier = Modifier.padding(top = 5.dp)) {
                    if(existingEmptyDirectorySelected) {
                        Text(
                            buildAnnotatedString {
                                withStyle(style = SpanStyle(color = Color.Gray)) {
                                    append("A new Project named ")
                                }
                                withStyle(style = SpanStyle(color = MainUiState.theme.highlightGray())) {
                                    append((projectDirectory.name) ?: "<Invalid>")
                                }
                                withStyle(style = SpanStyle(color = Color.Gray)) {
                                    append(" will be created in ")
                                }
                                withStyle(style = SpanStyle(color = MainUiState.theme.highlightGray())) {
                                    append(projectDirectory.absolutePath)
                                }
                            }
                        )
                    } else {
                        Text(color = Color.Gray, text = "Directory needs to exist and be empty!")
                    }
                }
            }
            //Color Picker Heading
            Row(modifier = Modifier.padding(top = 8.dp)) {
                Text("Project Color:")
            }
            //Color Picker
            Row(modifier = Modifier.padding(top = 1.dp).trackActivation()) {
                ClassicColorPicker(
                    modifier = Modifier.height(100.dp),
                    color = selectedColor,
                    showAlphaBar = false,
                    onColorChanged = {
                        selectedColor = it
                    }
                )
            }
        }
    }

    override fun title(): String {
        return when(create) {
            true -> "Create Project"
            else -> "Edit Project"
        }
    }

    private fun getFallbackFile(): File {
        //find desktop or user home
        val userHomeDir = File(System.getProperty("user.home"))
        val desktopDir = File(userHomeDir, "Desktop")
        return if(desktopDir.exists() && desktopDir.isDirectory) {
            File(desktopDir, "Example/")
        } else if(userHomeDir.exists() && userHomeDir.isDirectory) {
            File(userHomeDir, "Example/")
        } else {
            File("Example")
        }
    }
}