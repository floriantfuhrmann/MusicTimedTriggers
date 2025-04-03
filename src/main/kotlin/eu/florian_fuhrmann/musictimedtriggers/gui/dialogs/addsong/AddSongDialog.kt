package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.addsong

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Dialog
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.components.CloseDialogButton
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector.editsong.EditSongPanel
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector.editsong.SpectrogramConfigurationState
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector.editsong.createAudioFilePathFieldState
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import eu.florian_fuhrmann.musictimedtriggers.song.Song
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.getAudioFormatOrNull
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.spectrogram.SpectrogramParameters
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.Text
import javax.sound.sampled.AudioFormat

class AddSongDialog(val project: Project) : Dialog("Add Song") {
    @Composable
    override fun Content() {
        Column(Modifier.fillMaxSize().background(JewelTheme.globalColors.panelBackground)) {
            // State
            val scrollState = rememberScrollState()
            val nameFieldState = rememberTextFieldState("")
            val audioFilePathFieldState = remember { createAudioFilePathFieldState(project) }
            val spectrogramConfigurationState = remember { SpectrogramConfigurationState(SpectrogramParameters()) }
            val audioEncodingAndLocationValid = audioFilePathFieldState.isValid &&
                    getAudioFormatOrNull(audioFilePathFieldState.file)?.encoding == AudioFormat.Encoding.PCM_SIGNED
                    && project.isFileInsideAudioDirectory(audioFilePathFieldState.file, false)
            // Edit Song Panel
            Row(Modifier.weight(1f).fillMaxWidth()) {
                EditSongPanel(
                    project = project,
                    song = null,
                    scrollState = scrollState,
                    extraTopPadding = 10.dp,
                    nameFieldState = nameFieldState,
                    audioFilePathFieldState = audioFilePathFieldState,
                    spectrogramConfigurationState = spectrogramConfigurationState,
                )
            }
            // Divider
            Divider(
                orientation = Orientation.Horizontal,
                modifier = Modifier.height(1.dp).fillMaxWidth(),
                color = if (scrollState.canScrollForward) JewelTheme.globalColors.borders.normal else Color.Transparent
            )
            // Buttons
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 14.dp, top = 14.dp)) {
                Spacer(Modifier.weight(1f))
                CloseDialogButton("Cancel")
                Spacer(Modifier.width(12.dp))
                DefaultButton(
                    enabled = audioEncodingAndLocationValid && nameFieldState.text.isNotBlank(),
                    onClick = {
                        // close dialog
                        DialogManager.closeAllDialogs()
                        // create song
                        Song.createSong(
                            project = project,
                            name = nameFieldState.text.toString(),
                            audioFile = audioFilePathFieldState.file,
                            spectrogramParams = spectrogramConfigurationState.spectrogramParameters,
                            openAfterCreation = true
                        )
                    }
                ) {
                    Text("Add")
                }
            }
        }

    }
}