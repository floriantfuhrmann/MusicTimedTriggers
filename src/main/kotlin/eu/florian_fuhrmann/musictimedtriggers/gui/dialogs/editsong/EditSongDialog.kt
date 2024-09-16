package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.editsong

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Alert
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Dialog
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.components.CloseDialogButton
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.components.DialogFrame
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.configuration.ConfigurationCollapsible
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.FilePathField
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import eu.florian_fuhrmann.musictimedtriggers.song.Song
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.getAudioFormat
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.isPcmEncoding
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.spectrogram.SpectrogramParameters
import eu.florian_fuhrmann.musictimedtriggers.utils.file.findAvailableTargetFile
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.ui.Outline
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import java.io.File
import java.nio.file.Files
import javax.sound.sampled.AudioFormat

class EditSongDialog(val project: Project, val add: Boolean, val song: Song? = null) : Dialog() {

    @Composable
    override fun Content() {
        //ensure we have a song when not adding
        require(add || song != null)

        //Init State
        //Name
        var songName: String by remember { mutableStateOf(song?.name ?: "") }
        val validSongName: Boolean by derivedStateOf { songName.isNotBlank() }
        //Audio File
        var selectedAudioFile: File by remember {
            mutableStateOf(song?.audioFile ?: project.getAudioDirectory())
        }
        val existingFileSelected: Boolean by derivedStateOf { selectedAudioFile.exists() && selectedAudioFile.isFile }
        val fileHasCorrectFormat: Boolean by remember {
            derivedStateOf {
                selectedAudioFile.exists() && selectedAudioFile.isFile && isPcmEncoding(selectedAudioFile)
            }
        }
        val fileInsideProjectDirectory: Boolean by remember {
            derivedStateOf {
                selectedAudioFile.exists() && selectedAudioFile.isFile
                        && project.isFileInsideProjectDirectory(selectedAudioFile)
            }
        }
        val copyTargetFile: File? by remember {
            derivedStateOf {
                if (!existingFileSelected || fileInsideProjectDirectory) {
                    null
                } else {
                    findAvailableTargetFile(project.getAudioDirectory(), selectedAudioFile.name)
                }
            }
        }
        //Spectrogram Configuration
        val spectrogramConfiguration = song?.spectrogramParams?.copy() ?: SpectrogramParameters()
        //UI
        DialogFrame(leftButtons = {
            //Delete Button
            if (!add) {
                OutlinedButton(
                    onClick = {
                        openDeleteSongDialog(project, song)
                    },
                    modifier = Modifier
                        .padding(end = 5.dp)
                        .trackActivation()
                ) {
                    Text(color = MainUiState.theme.errorTextColor(), text = "Delete")
                }
            }
        }, rightButtons = {
            //Close Button
            CloseDialogButton(text = "Cancel", modifier = Modifier.padding(end = 5.dp))
            //Done Button
            DefaultButton(
                onClick = {
                    DialogManager.closeDialog()
                    if (add) {
                        //add song
                        Song.createSong(project, songName, selectedAudioFile)
                    } else if (song != null) {
                        //update song
                        song.edit(songName, selectedAudioFile, spectrogramConfiguration)
                    } else {
                        error("EditSongDialog is not creating, but song is null")
                    }
                },
                modifier = Modifier.trackActivation(),
                enabled = existingFileSelected && validSongName
            ) {
                when (add) {
                    true -> Text("Add Song")
                    else -> Text("Done")
                }
            }
        }
        ) {
            //Name Heading
            Row {
                Text("Name:")
            }
            //Name Input
            Row(modifier = Modifier.padding(top = 2.dp).trackActivation()) {
                TextField(
                    value = songName,
                    onValueChange = { songName = it },
                    outline = when (validSongName) {
                        true -> Outline.None
                        false -> Outline.Error
                    },
                    modifier = Modifier.trackActivation().fillMaxWidth()
                )
            }
            //Name Message
            if (!validSongName) {
                Row(modifier = Modifier.padding(top = 3.dp).trackActivation()) {
                    Text(color = MainUiState.theme.errorTextColor(), text = "Song Name can not be blank")
                }
            }
            //Audio File Heading
            Row(modifier = Modifier.padding(top = 5.dp).trackActivation()) {
                Text("Audio File:")
            }
            //Audio File Selector
            var showConvertSuccessMessage by remember { mutableStateOf(false) }
            var updateSelectedFile: (File) -> Unit = {}
            Row(modifier = Modifier.padding(top = 2.dp).trackActivation()) {
                updateSelectedFile = FilePathField(
                    file = selectedAudioFile,
                    placeholder = "Audio File",
                    fileDialogTitle = "Choose Audio File",
                    allowedExtensions = listOf("wav", "mp3"),
                    onChange = { selectedAudioFile = it },
                    outline = when (existingFileSelected) {
                        true -> if (fileInsideProjectDirectory && fileHasCorrectFormat) {
                            Outline.None
                        } else {
                            Outline.Warning
                        }

                        false -> Outline.Error
                    }
                )
            }
            //File Message
            Row(modifier = Modifier.padding(top = 3.dp).trackActivation()) {
                if (!existingFileSelected) {
                    Text(color = MainUiState.theme.errorTextColor(), text = "File needs to exist")
                } else if (fileHasCorrectFormat && !fileInsideProjectDirectory) {
                    Text(
                        buildAnnotatedString {
                            withStyle(style = SpanStyle(color = Color.Gray)) {
                                append("It's recommended to store the audio file in the project directory. Should the file be copied or moved to ")
                            }
                            withStyle(style = SpanStyle(color = MainUiState.theme.highlightGray())) {
                                append(copyTargetFile?.absolutePath ?: "<No Target File>")
                            }
                        }
                    )
                } else if (showConvertSuccessMessage) {
                    Text(color = MainUiState.theme.successTextColor(), text = "File has been converted successfully.")
                }
            }
            //Convert Options
            if (existingFileSelected && !fileHasCorrectFormat) {
                ConvertAudioFileOptions(
                    project = project,
                    audioFile = selectedAudioFile,
                    onConvert = {
                        //update selected file
                        selectedAudioFile = it
                        updateSelectedFile(selectedAudioFile)
                        //show message
                        showConvertSuccessMessage = true
                    }
                )
            }
            //Copy and Move Buttons (shown when an existing file is selected, but it's not inside the project directory)
            if (existingFileSelected && fileHasCorrectFormat && !fileInsideProjectDirectory) {
                Row(modifier = Modifier.padding(top = 2.dp, bottom = 5.dp)) {
                    //Copy File Button
                    OutlinedButton(
                        onClick = {
                            //ensure we have a valid target
                            if (copyTargetFile != null) {
                                //copy file
                                Files.copy(selectedAudioFile.toPath(), copyTargetFile!!.toPath())
                                //update selected file
                                selectedAudioFile = copyTargetFile!!
                                updateSelectedFile(selectedAudioFile)
                            } else {
                                error("Can't copy file because target is null!")
                            }
                        },
                        modifier = Modifier.trackActivation()
                    ) {
                        Text("Copy File")
                    }
                    //Move File Button
                    OutlinedButton(
                        onClick = {
                            //ensure we have a valid target
                            if (copyTargetFile != null) {
                                //move file
                                Files.move(selectedAudioFile.toPath(), copyTargetFile!!.toPath())
                                //update selected file
                                selectedAudioFile = copyTargetFile!!
                                updateSelectedFile(selectedAudioFile)
                            } else {
                                error("Can't move file because target is null!")
                            }
                        },
                        modifier = Modifier.padding(start = 5.dp).trackActivation()
                    ) {
                        Text("Move File")
                    }
                }
            }
            //AudioFormat Info
            if (existingFileSelected && fileHasCorrectFormat) {
                //get current audio format
                val inputAudioFormat: AudioFormat? by remember { derivedStateOf { getAudioFormat(selectedAudioFile) } }
                //Lines
                Row {
                    Text("Encoding: " + (inputAudioFormat?.encoding ?: "<Unknown>"))
                }
                Row {
                    Text("Sample Rate: " + (inputAudioFormat?.sampleRate ?: "<Unknown>"))
                }
                Row {
                    Text("Sample Size (in Bits): " + (inputAudioFormat?.sampleSizeInBits ?: "<Unknown>"))
                }
                Row {
                    Text("Channels: " + (inputAudioFormat?.channels ?: "<Unknown>"))
                }
                Row {
                    Text("Frame Size: " + (inputAudioFormat?.frameSize ?: "<Unknown>"))
                }
                Row {
                    Text("Frame Rate: " + (inputAudioFormat?.frameRate ?: "<Unknown>"))
                }
                Row {
                    Text(
                        when (inputAudioFormat?.isBigEndian) {
                            true -> "Big Endian"
                            false -> "Little Endian"
                            else -> "<Unknown>"
                        }
                    )
                }
            }
            //Spectrogram Configuration
            Row(modifier = Modifier.padding(top = 5.dp)) {
                ConfigurationCollapsible(spectrogramConfiguration, "Spectrogram Parameters:")
            }
        }
    }

    override fun title(): String {
        return when(add) {
            true -> "Add Song"
            else -> "Edit Song"
        }
    }
}

fun openDeleteSongDialog(project: Project, song: Song?) {
    //show confirmation alert
    DialogManager.alert(
        Alert(
            title = "Confirm Delete",
            text = "Are you sure you want to delete Song ${song?.name}?",
            dismissText = "Cancel",
            onDismiss = {},
            onConfirm = {
                //close dialog
                DialogManager.closeDialog()
                //delete song
                if (song != null) {
                    project.deleteSong(song)
                }
            }
        )
    )
}