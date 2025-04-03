package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.replaceaudio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Dialog
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.triggerusages.TriggerUsagesDialog
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector.editsong.AudioEncodingInformationRow
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.*
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import eu.florian_fuhrmann.musictimedtriggers.song.Song
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.getAudioFormat
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.getDurationOrNull
import eu.florian_fuhrmann.musictimedtriggers.utils.file.findAvailableTargetFile
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Link
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import java.nio.file.Files
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
            val audioFormat: AudioFormat? =
                if (filePathFieldState.isValid) {
                    filePathFieldState.file.let { if (it.exists()) getAudioFormat(it) else null }
                } else {
                    null
                }
            val encodingIsValid = audioFormat != null && audioFormat.encoding == AudioFormat.Encoding.PCM_SIGNED
            val fileLocationIsValid = project.isFileInsideAudioDirectory(filePathFieldState.file, false)
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
            // Move/Copy Banner
            if(encodingIsValid && !fileLocationIsValid) {
                Row {
                    InterimInlineBanner(
                        InterimInlineBannerType.Error,
                        Modifier.fillMaxWidth(),
                        "File must be inside projects Audio directory.",
                    ) {
                        val targetFile = findAvailableTargetFile(project.getAudioDirectory(), filePathFieldState.file.name)
                        Link("Copy file", {
                            Files.copy(filePathFieldState.file.toPath(), targetFile.toPath())
                            filePathFieldState.file = targetFile
                        })
                        Link("Move file", {
                            Files.move(filePathFieldState.file.toPath(), targetFile.toPath())
                            filePathFieldState.file = targetFile
                        })
                    }
                }
            }
            // Convert Banner
            if(!encodingIsValid && filePathFieldState.isValid) {
                Row {
                    InterimInlineBanner(
                        InterimInlineBannerType.Error,
                        Modifier.fillMaxWidth(),
                        "Audio file has to be in PCM Signed format.",
                    ) {
                        Link("Convert (requires ffmpeg)", {
                            // todo
                            println("TODO: Open ffmpeg converter")
                        })
                    }
                }
            }
            // Audio Encoding Information
            if(encodingIsValid && fileLocationIsValid) {
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
            // Placed Triggers after new audio end
            val duration = getDurationOrNull(filePathFieldState.file)
            val usagesAfterEnd = if(duration != null) song.searchUsagesAfterTime(duration) else null
            if(!usagesAfterEnd.isNullOrEmpty() && fileLocationIsValid) {
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
                    enabled = usagesAfterEnd != null && usagesAfterEnd.isEmpty() && filePathFieldState.isValid && encodingIsValid && fileLocationIsValid,
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