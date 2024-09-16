package eu.florian_fuhrmann.musictimedtriggers.gui.views.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeDialog
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.darkrockstudios.libraries.mpfilepicker.DirectoryPicker
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import eu.florian_fuhrmann.musictimedtriggers.utils.IconsDummy
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.ui.Outline
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import java.awt.Cursor
import java.io.File

@Composable
fun FilePathField(
    file: File = File(""),
    placeholder: String = "File Path",
    fileDialogTitle: String = "Choose File Path",
    allowedExtensions: List<String> = emptyList(),
    directoryMode: Boolean = false,
    enabled: Boolean = true,
    onChange: (File) -> Unit = {},
    outline: Outline = Outline.None
): (File) -> Unit {
    var value by remember { mutableStateOf(file.absolutePath) }
    var showFilePicker by remember { mutableStateOf(false) }
    val initialDirectory by remember { derivedStateOf { findInitialDirectory(File(value)) } }

    TextField(
        value = value,
        onValueChange = {
            value = it
            onChange(File(value))
        },
        placeholder = {
            Text(placeholder)
        },
        outline = outline,
        trailingIcon = {
            IconButton(
                onClick = {
                    showFilePicker = true
                },
                modifier = Modifier.size(30.dp)
                    .pointerHoverIcon(icon = PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                    .trackActivation(),
                enabled = enabled
            ) {
                Box(modifier = Modifier.padding(5.dp).trackActivation()) {
                    Icon(
                        when (MainUiState.theme.isDark()) {
                            true -> "icons/open-folder-outline-icon_dark.svg"
                            else -> "icons/open-folder-outline-icon.svg"
                        },
                        "Browse",
                        IconsDummy::class.java,
                        modifier = Modifier.trackActivation()
                    )
                }
            }
        },
        enabled = enabled,
        modifier = Modifier.trackActivation().fillMaxWidth()
    )
    if(showFilePicker) {
        if(directoryMode) {
            DirectoryPicker(
                show = true,
                initialDirectory = initialDirectory,
                title = fileDialogTitle
            ) { path ->
                //hide file picker again
                showFilePicker = false
                //update value
                if(path != null) {
                    value = path
                    onChange(File(value))
                }
            }
        } else {
            FilePicker(
                show = true,
                fileExtensions = allowedExtensions,
                title = fileDialogTitle,
                initialDirectory = initialDirectory
            ) { platformFile ->
                //hide file picker again
                showFilePicker = false
                //update value
                if(platformFile != null) {
                    value = platformFile.path
                    onChange(File(value))
                }
            }
        }
    }
    //return callback to update the value
    return { newFile: File ->
        value = newFile.absolutePath
    }
}

// adapted from: https://www.reddit.com/r/Kotlin/comments/n16u8z/comment/gwceshv/
private fun openFileDialog(
    window: ComposeWindow? = null, //should be ComposeWindow or ComposeDialog
    dialog: ComposeDialog? = null,
    title: String,
    allowAllExtension: Boolean = true,
    allowedExtensions: List<String> = emptyList(),
    allowMultiSelection: Boolean = true,
    initialDirectory: String? = null
): Set<File> {
    //allow the compose dialog not to be on top (so FileDialog can be on top)
    DialogManager.allowNotOnTop()
    //create a java.awt FileDialog
    val awtFileDialog = if(window != null) {
        java.awt.FileDialog(window, title, java.awt.FileDialog.LOAD)
    } else {
        java.awt.FileDialog(dialog, title, java.awt.FileDialog.LOAD)
    }
    //get result
    val result = awtFileDialog.apply {
        isMultipleMode = allowMultiSelection

        if (!allowAllExtension) {
            // windows
            file = allowedExtensions.joinToString(";") { "*$it" } // e.g. '*.jpg'

            // linux
            setFilenameFilter { _, name ->
                allowedExtensions.any {
                    name.endsWith(it)
                }
            }
        }

        if(initialDirectory != null) {
            directory = initialDirectory
        }

        isVisible = true
    }.files.toSet()
    //before returning require on top again
    DialogManager.requireOnTop()
    //return result
    return result
}

private fun findInitialDirectory(file: File): String {
    //find a parent directory which exists
    var f: File? = file
    var counter = 0 //count how often the while loops
    while (f != null) {
        //make sure the loop does not end in an infinite loop
        if(counter++ > 100) {
            println("findInitialDirectory: Aborting while loop, to prevent infinite loop!")
            break
        }
        //check if f exits and is dir
        if(f.exists() && f.isDirectory) {
            return f.absolutePath
        } else {
            //find parent
            val newAbsolutPath: String = f.absolutePath.substring(0, f.absolutePath.lastIndexOf(File.separator))
            if(newAbsolutPath.length == 1 || newAbsolutPath == File.separator || newAbsolutPath.isBlank()) {
                break
            }
            f = File(newAbsolutPath)
        }
    }
    //as fallback return user home
    return System.getProperty("user.home")
}
