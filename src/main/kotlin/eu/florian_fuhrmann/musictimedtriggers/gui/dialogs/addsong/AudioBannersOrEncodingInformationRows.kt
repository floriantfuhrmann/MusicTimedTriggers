package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.addsong

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.convertaudio.ConvertAudioDialog
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.convertaudio.convertWithoutUI
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector.editsong.AudioEncodingInformationRow
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.FilePathFieldState
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.InterimInlineBanner
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.InterimInlineBannerType
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.OpenableGroupHeader
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.getAudioFormatOrNull
import eu.florian_fuhrmann.musictimedtriggers.utils.file.findAvailableTargetFile
import org.jetbrains.jewel.ui.component.Link
import java.io.File
import java.nio.file.Files
import javax.sound.sampled.AudioFormat

@Composable
fun AudioBannersOrEncodingInformationRows(project: Project, filePathFieldState: FilePathFieldState) {
    // State
    val audioFormat: AudioFormat? =
        if (filePathFieldState.isValid) {
            getAudioFormatOrNull(filePathFieldState.file)
        } else {
            null
        }
    val encodingIsValid = audioFormat != null && audioFormat.encoding == AudioFormat.Encoding.PCM_SIGNED
    val fileLocationIsValid = project.isFileInsideAudioDirectory(filePathFieldState.file, false)
    val conversionResult = remember { ConversionResultState() }
    // Move/Copy Banner
    if (encodingIsValid && !fileLocationIsValid) {
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
    if (!encodingIsValid && filePathFieldState.isValid) {
        Row {
            InterimInlineBanner(
                InterimInlineBannerType.Error,
                Modifier.fillMaxWidth(),
                "Audio file has to be in PCM Signed format.",
            ) {
                val onConverted: (File, String, Int) -> Unit = { targetFile, codecName, sampleRateInHz ->
                    filePathFieldState.file = targetFile
                    conversionResult.converted = true
                    conversionResult.codec = codecName
                    conversionResult.sampleRateInHz = sampleRateInHz
                }
                // Link to convert audio without UI
                Link("Convert", {
                    convertWithoutUI(
                        project = project,
                        file = filePathFieldState.file,
                        onConverted = onConverted
                    )
                })
                // Link to open convert audio dialog
                Link("Options\u2026", {
                    // open convert audio dialog
                    DialogManager.openDialog(
                        ConvertAudioDialog(
                            project = project,
                            file = filePathFieldState.file,
                            onConverted = onConverted
                        ), false
                    )
                })
            }
        }
    }
    // Successful Conversion Banner
    if (conversionResult.converted) {
        Row(Modifier.padding(bottom = 6.dp)) {
            InterimInlineBanner(
                InterimInlineBannerType.Success,
                Modifier.fillMaxWidth(),
                "Audio file was converted to ${conversionResult.codec} ${conversionResult.sampleRateInHz}Hz.",
            ) {
                Link(
                    onClick = { conversionResult.converted = false },
                    text = "Ok"
                )
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
}

private class ConversionResultState {
    var converted by mutableStateOf(false)
    var codec by mutableStateOf("")
    var sampleRateInHz by mutableStateOf(0)
}