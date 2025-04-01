package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector.editsong

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.replaceaudio.ReplaceAudioDialog
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.OpenableGroupHeader
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import eu.florian_fuhrmann.musictimedtriggers.song.Song
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.getAudioFormat
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.spectrogram.SpectrogramParameters
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import javax.sound.sampled.AudioFormat

/**
 * Panel for editing/creating a song. Used by Inspector and Dialog.
 */
@Composable
fun EditSongPanel(project: Project, song: Song?, scrollState: ScrollState = rememberScrollState()) {
    // determine whether this is creation or editing context
    val creating = song == null
    // get state
    val editSongPanelState = if (creating) {
        // create a new independent state for creation
        remember { EditSongPanelState() }
    } else {
        // use the shared state for editing
        sharedEditSongInspectorState
    }
    // init state for spectrogram parameters
    var spectrogramConfigurationState by remember { mutableStateOf(SpectrogramConfigurationState(song?.spectrogramParams ?: SpectrogramParameters())) }
    Column {
        // Spectrogram Parameters Changes Banner
        if (spectrogramConfigurationState.anyChanges) {
            Row {
                WarningBanner("Spectrogram Changes", actions = {
                    Link("Apply", onClick = {
                        // get the new parameters
                        val newParams = spectrogramConfigurationState.spectrogramParameters
                        // apply new params to song
                        song?.updateSpectrogramParameters(newParams)
                        // update state since the changes are now applied
                        spectrogramConfigurationState = SpectrogramConfigurationState(newParams)
                    })
                })
            }
        }
        // Inspector Contents
        Row {
            VerticallyScrollableContainer(scrollState = scrollState) {
                Column(Modifier.padding(horizontal = 10.dp)) {
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
                                val updateSongNameIfNeeded = { newName: String ->
                                    if(song?.name != newName) {
                                        // update song name
                                        song?.updateName(newName)
                                    }
                                }
                                TextField(
                                    state = nameState,
                                    modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth().onFocusChanged {
                                        // update song name if focus is lost
                                        if(!it.isFocused) {
                                            updateSongNameIfNeeded(nameState.text.toString())
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    onKeyboardAction = {
                                        // clear focus
                                        focusManager.clearFocus()
                                    }
                                )
                                LaunchedEffect(nameState) {
                                    snapshotFlow { nameState.text }.filter { it.isNotBlank() }.collectLatest {
                                        // short delay
                                        kotlinx.coroutines.delay(300)
                                        // update song name (if needed)
                                        updateSongNameIfNeeded(it.toString())
                                    }
                                }
                            }
                            Row {
                                TextField(
                                    state = rememberTextFieldState(song?.audioFile?.name ?: ""),
                                    modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth(),
                                    readOnly = true,
                                    enabled = false,
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            // open replace audio dialog
                                            require(song != null) { "Song must not be null for swap button" }
                                            DialogManager.openDialog(ReplaceAudioDialog(project, song))
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
                        open = editSongPanelState.audioEncodingOpened,
                        onOpenedChange = { editSongPanelState.audioEncodingOpened = it },
                        text = "Audio Encoding",
                    )
                    //get audio format
                    val audioFormat: AudioFormat? = if (song != null) {
                        getAudioFormat(song.audioFile)
                    } else {
                        // todo: get audio format of selected file during creation
                        null
                    }
                    if(editSongPanelState.audioEncodingOpened) {
                        AudioEncodingInformationRow(audioFormat, Modifier.padding(start = 24.dp))
                    }
                    OpenableGroupHeader(
                        modifier = Modifier.padding(vertical = 6.dp),
                        open = editSongPanelState.spectrogramParametersOpened,
                        onOpenedChange = { editSongPanelState.spectrogramParametersOpened = it },
                        text = "Spectrogram Parameters"
                    )
                    if(editSongPanelState.spectrogramParametersOpened) {
                        Row(Modifier.padding(start = 24.dp)) {
                            key(spectrogramConfigurationState) {
                                SpectrogramConfigurationPane(spectrogramConfigurationState, audioFormat?.sampleRate)
                            }
                        }
                    }
                }
            }
        }
    }
}

private val sharedEditSongInspectorState = EditSongPanelState()

private class EditSongPanelState {
    var audioEncodingOpened: Boolean by mutableStateOf(false)
    var spectrogramParametersOpened: Boolean by mutableStateOf(false)
}