package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.replaceaudio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Dialog
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector.editsong.AudioEncodingInformationRow
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.FilePathField
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.FilePathFieldState
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.OpenableGroupHeader
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import eu.florian_fuhrmann.musictimedtriggers.song.Song
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.getAudioFormat
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import javax.sound.sampled.AudioFormat

class ReplaceAudioDialog(val project: Project, val song: Song) : Dialog("Replace Audio") {
    @Composable
    override fun Content() {
        Column(Modifier.background(JewelTheme.globalColors.panelBackground).padding(10.dp)) {
            // File Path Field State
            val filePathFieldState = remember { FilePathFieldState(
                baseFile = project.projectDirectory,
                initialValue = song.audioFile,
                allowedExtensions = listOf("wav", "aiff", "mp3", "ogg", "m4a", "mp4", "flac", "webm", "opus"),
                mustExist = true,
                directoryMode = false
            ) }
            val audioFormat: AudioFormat? = filePathFieldState.file.let { if(it.exists()) getAudioFormat(it) else null }
            // File Path Input
            Row(Modifier.height(IntrinsicSize.Min).padding(bottom = 12.dp)) {
                Column(verticalArrangement = Arrangement.Center) {
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Text("Audio file:")
                    }
                }
                Column(Modifier.padding(start = 12.dp)) {
                    FilePathField(filePathFieldState, modifier = Modifier.fillMaxWidth())
                }
            }
            // Convert Banner
            Row {
                Text("Todo: Optional Convert Banner (Error Banner)")
            }
            // Audio Encoding Information
            if(audioFormat != null) {
                var opened by remember { mutableStateOf(false) }
                OpenableGroupHeader(
                    open = opened,
                    onOpenedChange = { opened = it },
                    text = "Audio Encoding"
                )
                if(opened) {
                    AudioEncodingInformationRow(audioFormat, Modifier.padding(start = 24.dp, top = 6.dp))
                }
            }
            Spacer(Modifier.weight(1f))
            Row {
                Text("Todo: Optional triggers past new duration banner (Error Banner)")
            }
            Row {
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = { DialogManager.closeDialog() }) {
                    Text("Cancel")
                }
                DefaultButton(modifier = Modifier.padding(start = 12.dp), onClick = { println("TODO") }) {
                    Text("Replace")
                }
            }
        }
    }

}