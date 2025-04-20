package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.replaceaudio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Dialog
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.addsong.AudioBannersOrEncodingInformationRows
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.triggerusages.TriggerUsagesDialog
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.*
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.FilePathField
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.FilePathFieldState
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import eu.florian_fuhrmann.musictimedtriggers.song.Song
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.getAudioFormatOrNull
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.getDurationOrNull
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Link
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
            val encodingAndLocationValid = filePathFieldState.isValid &&
                    getAudioFormatOrNull(filePathFieldState.file)?.encoding == AudioFormat.Encoding.PCM_SIGNED
                    && project.isFileInsideAudioDirectory(filePathFieldState.file, false)
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
            // Banners or Encoding Information
            AudioBannersOrEncodingInformationRows(project, filePathFieldState)
            // Spacer
            Spacer(Modifier.weight(1f))
            // Placed Triggers after new audio end
            val duration = getDurationOrNull(filePathFieldState.file)
            val usagesAfterEnd = if(duration != null) song.searchUsagesAfterTime(duration) else null
            if(!usagesAfterEnd.isNullOrEmpty() && encodingAndLocationValid) {
                Row(Modifier.padding(vertical = 10.dp)) {
                    InterimInlineBanner(
                        InterimInlineBannerType.Error,
                        Modifier.fillMaxWidth(),
                        usagesAfterEnd.size.let {
                            if (it == 1) "One Placed Trigger starts after the new audio end. It needs to be removed before replacing the audio."
                            else "$it Placed Triggers start after the new audio end. These need to be removed before replacing the audio."
                        }
                    ) {
                        Link("Manage triggers\u2026", {
                            DialogManager.openDialog(TriggerUsagesDialog(TriggerUsagesDialog.Type.ShortenSequenceByReplacing, usagesAfterEnd, onConfirm = {
                                // dialogs will be closed automatically
                                // remove all protruding triggers
                                require(duration != null)
                                song.sequence.lines.forEach {
                                    if(it.removeTriggersStartingAtOrAfter(duration)) {
                                        it.saveToFile()
                                    }
                                }
                                // replace the audio file
                                song.replaceAudioFile(filePathFieldState.file)
                            }), closeOthers = false)
                        })
                    }
                }
            }
            Row(Modifier.padding(top = 15.dp)) {
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = { DialogManager.closeAllDialogs() }) {
                    Text("Cancel")
                }
                DefaultButton(
                    modifier = Modifier.padding(start = 12.dp),
                    enabled = usagesAfterEnd != null && usagesAfterEnd.isEmpty() && encodingAndLocationValid,
                    onClick = {
                        // close all dialogs
                        DialogManager.closeAllDialogs()
                        // and replace the audio file
                        song.replaceAudioFile(filePathFieldState.file)
                    }) {
                    Text("Replace")
                }
            }
        }
    }

}