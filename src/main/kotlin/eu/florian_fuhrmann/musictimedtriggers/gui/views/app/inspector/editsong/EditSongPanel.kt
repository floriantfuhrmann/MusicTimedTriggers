package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector.editsong

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.OpenableGroupHeader
import eu.florian_fuhrmann.musictimedtriggers.song.Song
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.getAudioFormat
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.spectrogram.SpectrogramParameters
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.theme.colorPalette
import javax.sound.sampled.AudioFormat

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
    VerticallyScrollableContainer {
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
            //get audio format
            val audioFormat: AudioFormat? = if (song != null) {
                getAudioFormat(song.audioFile)
            } else {
                // todo: get audio format of selected file during creation
                null
            }
            if(state.audioEncodingOpened) {
                Row {
                    Column(Modifier.padding(start = 24.dp)) {
                        listOf(
                            "Encoding" to (audioFormat?.encoding ?: "Unknown").toString(),
                            "Sample Rate" to audioFormat?.sampleRate.let { if(it != null) "${it/1000.0} kHz" else "Unknown" },
                            "Sample Size" to audioFormat?.sampleSizeInBits.let { if(it != null) "$it Bit" else "Unknown" },
                            "Channels" to (audioFormat?.channels ?: "Unknown").toString(),
                            "Frame Size" to audioFormat?.frameSize.let { if(it != null) "$it Byte" else "Unknown" },
                            "Frame Rate" to audioFormat?.frameRate.let { if(it != null) "${it/1000.0} kHz" else "Unknown" },
                            "Endianness" to when(audioFormat?.isBigEndian) {
                                true -> "Big Endian"
                                false -> "Little Endian"
                                else -> "Unknown"
                            },
                        ).forEach {
                            Row {
                                Text(buildAnnotatedString {
                                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = JewelTheme.colorPalette.gray(7))) {
                                        append("${it.first}: ")
                                    }
                                    append(it.second)
                                })
                            }
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
                    SpectrogramConfigurationPane(song?.spectrogramParams?.copy() ?: SpectrogramParameters(), audioFormat?.sampleRate)
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