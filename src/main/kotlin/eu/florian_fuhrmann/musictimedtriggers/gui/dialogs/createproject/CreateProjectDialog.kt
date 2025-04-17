package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.createproject

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.godaddy.android.colorpicker.HsvColor
import eu.florian_fuhrmann.musictimedtriggers.gui.alerts.BasicAlert
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Dialog
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.components.CloseDialogButton
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.OpenableGroupHeader
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.ColorField
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.ColorFieldState
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.FilePathField
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.FilePathFieldState
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.InvalidInputPopup
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.utils.color.GenericColor
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.Outline
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.component.VerticallyScrollableContainer
import org.jetbrains.jewel.ui.theme.colorPalette
import java.io.File

class CreateProjectDialog : Dialog("Create Project") {

    @Composable
    override fun Content() {
        Column(Modifier.fillMaxSize().background(JewelTheme.globalColors.panelBackground)) {
            // Name and containing directory States
            val nameFieldState = rememberTextFieldState()
            val nameValid by derivedStateOf {
                nameFieldState.text.isNotBlank()
                        && nameFieldState.text.matches(Regex("[a-zA-Z0-9_\\- ]+"))
                        && nameFieldState.text.toString().trim() == nameFieldState.text.toString()
                        && nameFieldState.text.length <= 200
            }
            val locationFieldState = remember { FilePathFieldState(
                initialValue = File(System.getProperty("user.home")+File.separator+"MTT-Projects"),
                directoryMode = true,
                mustExist = false
            ) }
            // the target directory should be a new directory inside the location directory
            val targetDirectory by derivedStateOf {
                File(locationFieldState.file, nameFieldState.text.toString())
            }
            val targetDirectoryValid by derivedStateOf { targetDirectory.exists().not() }
            val colorState = remember {
                ColorFieldState(
                    GenericColor.fromHsvColor(HsvColor(
                        hue = (Math.random() * 360).toFloat(),
                        saturation = (0.5 + Math.random() * 0.5).toFloat(),
                        value = (0.5 + Math.random() * 0.5).toFloat(),
                        alpha = 1.0f
                    ))
                )
            }
            // Main Content
            Row(Modifier.fillMaxWidth().weight(1f)) {
                VerticallyScrollableContainer(Modifier.fillMaxSize()) {
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        // Name and containing directory Input Ui
                        Row(Modifier.height(IntrinsicSize.Min).fillMaxHeight()) {
                            // Labels
                            Column(Modifier.fillMaxHeight()) {
                                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Text("Name:")
                                }
                                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Text("Location:")
                                }
                            }
                            // Input fields
                            Column(Modifier.padding(start = 6.dp)) {
                                Row(Modifier.padding(vertical = 6.dp)) {
                                    var nameFieldFocused by remember { mutableStateOf(false) }
                                    TextField(
                                        state = nameFieldState,
                                        modifier = Modifier
                                            .width(200.dp)
                                            .onFocusChanged {
                                                nameFieldFocused = it.hasFocus
                                            }.trackActivation(),
                                        outline = if(nameValid) Outline.None else Outline.Error,
                                    )
                                    if(!nameValid && nameFieldFocused) {
                                        InvalidInputPopup(when {
                                            nameFieldState.text.isBlank() -> "Name must not be blank"
                                            nameFieldState.text.length > 200 -> "Name must not be longer than 200 characters"
                                            else -> "Name must only contain letters, numbers, dashes, underscores and spaces, but must not start or end with spaces"
                                        })
                                    }
                                }
                                Row(Modifier.padding(vertical = 6.dp)) {
                                    FilePathField(
                                        state = locationFieldState,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                        // Info text row
                        if(nameValid) {
                            Row {
                                // Label text again, but transparent to hack the alignment
                                Column {
                                    Text("Location:", color = Color.Transparent)
                                }
                                // Info text
                                Column(Modifier.padding(start = 6.dp)) {
                                    if (targetDirectoryValid) {
                                        Text(
                                            "Project will be created in ${targetDirectory.canonicalPath}",
                                            color = JewelTheme.globalColors.text.disabled
                                        )
                                    } else {
                                        Text(
                                            "Project would be created in ${targetDirectory.canonicalPath}, but that already exists!",
                                            color = JewelTheme.globalColors.text.error
                                        )
                                    }
                                }
                            }
                        }
                        // Advanced section
                        var advancedExpanded by remember { mutableStateOf(false) }
                        OpenableGroupHeader(
                            open = advancedExpanded, onOpenedChange = { advancedExpanded = it },
                            text = "Advanced", modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                        )
                        if(advancedExpanded) {
                            Row(Modifier.padding(start = 24.dp).height(IntrinsicSize.Min)) {
                                Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                                    Text("Color:")
                                }
                                Column(Modifier.padding(start = 6.dp)) {
                                    ColorField(state = colorState)
                                }
                            }
                        }
                    }
                }
            }
            // Buttons
            Divider(Orientation.Horizontal, Modifier.fillMaxWidth())
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.weight(1f))
                CloseDialogButton("Cancel")
                DefaultButton(
                    enabled = nameValid && targetDirectoryValid,
                    onClick = {
                        createLocationDirectoryAndProject(targetDirectory, colorState.value)
                    }
                ) {
                    Text("Create")
                }
            }
        }
    }

    private fun createLocationDirectoryAndProject(projectDirectory: File, color: GenericColor) {
        // check whether the parent directory exists
        if(!projectDirectory.parentFile.exists()) {
            // if not, ask the user if he wants to create it
            BasicAlert(
                type = BasicAlert.Type.Question,
                title = "Create containing directory?",
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row {
                            Text(buildAnnotatedString {
                                append("The containing directory ")
                                withStyle((SpanStyle(color = JewelTheme.colorPalette.gray(7)))) {
                                    append(projectDirectory.parentFile.canonicalPath)
                                }
                                append(" does not exist. Would you like to create it?")
                            })
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Spacer(Modifier.weight(1f))
                            CancelButton()
                            OKButton(label = "Create", onClick = {
                                // close alert
                                close()
                                // create the parent directory
                                projectDirectory.parentFile.mkdirs()
                                // create the project
                                createProject(projectDirectory, color)
                            })
                        }
                    }
                },
            ).show()
        } else {
            // if it does continue with creation directly
            createProject(projectDirectory, color)
        }
    }

    private fun createProject(projectDirectory: File, color: GenericColor) {
        // create the project directory
        projectDirectory.mkdir()
        // close the dialog
        DialogManager.closeDialog(this)
        // create the project
        ProjectManager.createProject(projectDirectory, color)
    }

}