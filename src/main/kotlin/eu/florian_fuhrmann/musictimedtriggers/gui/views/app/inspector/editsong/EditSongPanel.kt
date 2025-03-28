package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector.editsong

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.OpenableGroupHeader
import eu.florian_fuhrmann.musictimedtriggers.song.Song
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.icons.AllIconsKeys

/**
 * Panel for editing/creating a song. Used by Inspector and Dialog.
 */
@Composable
fun EditSongPanel(song: Song?) {
    // determine whether this is creation or editing context
    val creating = song == null
    // get state
    val state = if (creating) {
        // create a new independent state for creation
        remember { EditSongPanelState() }
    } else {
        // use the shared state for editing
        sharedEditSongInspectorState
    }
    Column {
        // Song Name and Audio File Inputs
        Row(Modifier.height(IntrinsicSize.Min)) {
            // Input Labels
            Column(Modifier.padding(end = 6.dp)) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Text("Name:")
                }
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Text("Audio:")
                }
            }
            // Inputs
            Column {
                Row {
                    val nameState = rememberTextFieldState(song?.name ?: "")
                    val focusManager = LocalFocusManager.current
                    TextField(
                        state = nameState,
                        modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        onKeyboardAction = {
                            // todo: rename song
                            println("Todo: Rename to ${nameState.text}")
                            // clear focus
                            focusManager.clearFocus()
                        }
                    )
                }
                Row {
                    TextField(
                        state = rememberTextFieldState(song?.audioFile?.name ?: ""),
                        modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth(),
                        readOnly = true,
                        enabled = false,
                        trailingIcon = {
                            IconButton(onClick = {
                                // todo: open replace audio dialog
                                println("Todo: Swap")
                            }) {
                                Icon(AllIconsKeys.Actions.SwapPanels, null)
                            }
                        }
                    )
                }
            }
        }
        OpenableGroupHeader(
            modifier = Modifier.padding(vertical = 6.dp),
            open = state.audioEncodingOpened,
            onOpenedChange = { state.audioEncodingOpened = it },
            text = "Audio Encoding",
        )
        if(state.audioEncodingOpened) {
            Row {
                Column(Modifier.padding(start = 24.dp)) {
                    Row {
                        Text("Todo: Audio Encoding")
                    }
                }
            }
        }
        OpenableGroupHeader(
            modifier = Modifier.padding(vertical = 6.dp),
            open = state.spectrogramParametersOpened,
            onOpenedChange = { state.spectrogramParametersOpened = it },
            text = "Spectrogram Parameters"
        )
        if(state.spectrogramParametersOpened) {
            Row(Modifier.padding(start = 24.dp)) {
                Text("Todo: Spectrogram Parameters")
            }
        }
    }
}

private val sharedEditSongInspectorState = EditSongPanelState()

private class EditSongPanelState {
    var audioEncodingOpened: Boolean by mutableStateOf(false)
    var spectrogramParametersOpened: Boolean by mutableStateOf(false)
}