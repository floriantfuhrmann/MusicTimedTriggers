package eu.florian_fuhrmann.musictimedtriggers.gui.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.onClick
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeDialog
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.darkrockstudios.libraries.mpfilepicker.DirectoryPicker
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import eu.florian_fuhrmann.musictimedtriggers.utils.icons.MttIcons
import org.jetbrains.jewel.foundation.modifier.onHover
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Outline
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import java.awt.Cursor
import java.io.File

@Composable
fun FilePathField(
    state: FilePathFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    outline: Outline = Outline.None,
    placeholder: @Composable (() -> Unit)? = null
) {
    // State for file picker
    var showFilePicker by remember { mutableStateOf(false) }
    var popupHidden by remember { mutableStateOf(false) }
    LaunchedEffect(state.textFieldState.text) {
        popupHidden = false
    }
    // Error Popup
    if(!state.isValid && !popupHidden) {
        // show error message
        PopupContainer(
            onDismissRequest = { popupHidden = true },
            horizontalAlignment = Alignment.Start,
            popupProperties = PopupProperties(focusable = false)
        ) {
            Box(modifier = Modifier.background(JewelTheme.globalColors.outlines.error).padding(5.dp)) {
                Text(state.invalidMessage ?: "", color = JewelTheme.globalColors.text.error)
            }
        }
    }
    // Text Field
    TextField(
        state = state.textFieldState,
        modifier = modifier.onHover {
            popupHidden = false
        },
        enabled = enabled,
        readOnly = readOnly,
        outline = if(state.isValid) outline else Outline.Error,
        placeholder = placeholder,
        trailingIcon = {
            IconButton(onClick = {
                showFilePicker = true
            }) {
                Icon(AllIconsKeys.General.OpenDisk, null)
            }
        }
    )
    // File Picker
    if(showFilePicker) {
        if(state.directoryMode) {
            DirectoryPicker(
                show = true,
                initialDirectory = findInitialDirectory(state.file),
                title = state.fileDialogTitle
            ) { path ->
                //hide file picker again
                showFilePicker = false
                //update value
                if(path != null) {
                    state.file = File(path)
                }
            }
        } else {
            FilePicker(
                show = true,
                fileExtensions = state.allowedExtensions,
                title = state.fileDialogTitle,
                initialDirectory = findInitialDirectory(state.file)
            ) { platformFile ->
                //hide file picker again
                showFilePicker = false
                //update value
                if(platformFile != null) {
                    state.file = File(platformFile.path)
                }
            }
        }
    }
}

class FilePathFieldState(
    private val baseFile: File = File(""),
    initialValue: File = File(""),
    val allowedExtensions: List<String> = emptyList(),
    private val mustExist: Boolean = true,
    val directoryMode: Boolean = false,
    val fileDialogTitle: String = "Choose File Path"
) {
    val textFieldState = TextFieldState(
        if(initialValue.canonicalPath.startsWith(baseFile.canonicalPath)) {
            initialValue.relativeToOrSelf(baseFile).path
        } else if(initialValue.isAbsolute) {
            initialValue.canonicalPath
        } else {
            initialValue.relativeToOrSelf(baseFile).path
        }
    )
    var file: File
        get() = textFieldState.text.toString().let {
            val file = File(it)
            if (file.isAbsolute) {
                file
            } else {
                File(baseFile, it)
            }
        }
        set(value) {
            if(value.canonicalPath.startsWith(baseFile.canonicalPath)) {
                textFieldState.setTextAndPlaceCursorAtEnd(value.relativeToOrSelf(baseFile).path)
            } else {
                textFieldState.setTextAndPlaceCursorAtEnd(value.path)
            }
        }
    val isValid: Boolean
        get() = when {
            mustExist && !directoryMode -> file.exists() && file.isFile && (allowedExtensions.isEmpty() || file.extension in allowedExtensions)
            mustExist && directoryMode -> file.exists() && file.isDirectory
            else -> true
        }
    val invalidMessage: String?
        get() = when {
            !file.exists() && !directoryMode -> "File does not exist ($file)"
            !file.exists() && directoryMode -> "Directory does not exist"
            !file.isFile && !directoryMode -> "File is not a file"
            !file.isDirectory && directoryMode -> "File is not a directory"
            allowedExtensions.isNotEmpty() && file.extension !in allowedExtensions -> "File extension is not allowed (${allowedExtensions.joinToString(", ")})"
            else -> null
        }
}

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
                        key = MttIcons.openFolderOutline,
                        "Browse",
                        modifier = Modifier.trackActivation(),
                        tint = MainUiState.theme.iconColor()
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
