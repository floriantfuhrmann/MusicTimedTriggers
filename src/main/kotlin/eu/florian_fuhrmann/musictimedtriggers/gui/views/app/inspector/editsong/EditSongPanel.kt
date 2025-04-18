package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector.editsong

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.addsong.AudioBannersOrEncodingInformationRows
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.replaceaudio.ReplaceAudioDialog
import eu.florian_fuhrmann.musictimedtriggers.gui.styles.fixedCursorTooltipStyle
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.FilePathField
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.FilePathFieldState
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.OpenableGroupHeader
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import eu.florian_fuhrmann.musictimedtriggers.song.Song
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.getAudioFormatOrNull
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.spectrogram.SpectrogramParameters
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icons.AllIconsKeys

fun createAudioFilePathFieldState(project: Project, song: Song? = null) =
    FilePathFieldState(
        baseFile = project.projectDirectory,
        initialValue = song?.audioFile ?: project.getAudioDirectory(),
        allowedExtensions = listOf("wav", "aiff", "mp3", "ogg", "m4a", "mp4", "flac", "webm", "opus"),
        mustExist = true,
        directoryMode = false
    )

/** Panel for editing/creating a song. Used by Inspector and Dialog. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditSongPanel(
    project: Project,
    song: Song?,
    scrollState: ScrollState = rememberScrollState(),
    extraTopPadding: Dp = 0.dp,
    nameFieldState: TextFieldState = rememberTextFieldState(song?.name ?: ""),
    audioFilePathFieldState: FilePathFieldState = remember { createAudioFilePathFieldState(project, song) },
    spectrogramConfigurationState: SpectrogramConfigurationState = remember {
        SpectrogramConfigurationState(song?.spectrogramParams ?: SpectrogramParameters())
    }
) {
    // determine whether this is creation or editing context
    val creating = song == null
    // get panel state
    val editSongPanelState = if (creating) {
        // create a new independent state for creation
        remember { EditSongPanelState() }
    } else {
        // use the shared state for editing
        sharedEditSongInspectorState
    }
    Column {
        // Spectrogram Parameters Changes Banner
        if (!creating && spectrogramConfigurationState.anyChanges) {
            Row {
                WarningBanner("Spectrogram Changes", actions = {
                    Link("Apply", onClick = {
                        // get the new parameters
                        val newParams = spectrogramConfigurationState.spectrogramParameters
                        // apply new params to song
                        song?.updateSpectrogramParameters(newParams)
                        // update state since the changes are now applied
                        spectrogramConfigurationState.handleChangesApplied()
                    })
                })
            }
        }
        // Inspector Contents
        Row {
            VerticallyScrollableContainer(scrollState = scrollState) {
                Column(Modifier.padding(horizontal = 10.dp).padding(top = extraTopPadding)) {
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
                                val focusManager = LocalFocusManager.current
                                val updateSongNameIfNeeded = { newName: String ->
                                    if(song?.name != newName) {
                                        // update song name
                                        song?.updateName(newName)
                                    }
                                }
                                TextField(
                                    state = nameFieldState,
                                    modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth().onFocusChanged {
                                        // update song name if focus is lost
                                        if(!it.isFocused) {
                                            updateSongNameIfNeeded(nameFieldState.text.toString())
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    onKeyboardAction = {
                                        // clear focus
                                        focusManager.clearFocus()
                                    },
                                    placeholder = { Text("Song Name") },
                                )
                                LaunchedEffect(nameFieldState) {
                                    snapshotFlow { nameFieldState.text }.filter { it.isNotBlank() }.collectLatest {
                                        // short delay
                                        kotlinx.coroutines.delay(300)
                                        // update song name (if needed)
                                        updateSongNameIfNeeded(it.toString())
                                    }
                                }
                            }
                            Row {
                                if(creating) {
                                    // File Input for Audio
                                    Box(Modifier.padding(vertical = 6.dp)) {
                                        FilePathField(audioFilePathFieldState, modifier = Modifier.fillMaxWidth())
                                    }
                                } else {
                                    // Placeholder audio input with a swap button
                                    val audioFileName by derivedStateOf { song.audioFile.name ?: "" }
                                    key(audioFileName) {
                                        TextField(
                                            state = rememberTextFieldState(audioFileName),
                                            modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth(),
                                            readOnly = true,
                                            enabled = false,
                                            trailingIcon = {
                                                Tooltip(tooltip = { Text("Replace Audio") }, style = fixedCursorTooltipStyle) {
                                                    IconButton(onClick = {
                                                        // open replace audio dialog
                                                        DialogManager.openDialog(ReplaceAudioDialog(project, song))
                                                    }) {
                                                        Icon(AllIconsKeys.Actions.SwapPanels, null)
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    // Banners or Encoding Information
                    Spacer(Modifier.height(6.dp))
                    AudioBannersOrEncodingInformationRows(project, audioFilePathFieldState)
                    // Spectrogram Parameters
                    Spacer(Modifier.height(6.dp))
                    OpenableGroupHeader(
                        modifier = Modifier.padding(vertical = 6.dp),
                        open = editSongPanelState.spectrogramParametersOpened,
                        onOpenedChange = { editSongPanelState.spectrogramParametersOpened = it },
                        text = "Spectrogram Parameters"
                    )
                    if(editSongPanelState.spectrogramParametersOpened) {
                        Row(Modifier.padding(start = 24.dp)) {
                            key(spectrogramConfigurationState) {
                                SpectrogramConfigurationPane(spectrogramConfigurationState, getAudioFormatOrNull(audioFilePathFieldState.file)?.sampleRate)
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