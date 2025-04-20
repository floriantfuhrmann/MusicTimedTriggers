package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.settings.categories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.alerts.BasicAlert
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.OpenableGroupHeader
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Link
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import java.io.File
import java.io.IOException
import java.nio.file.Files

class AudioFilesCategory : SettingsCategory("Audio Files") {

    @Composable
    override fun Content(project: Project) {
        // States
        val usedFiles = remember { mutableStateListOf<File>() }
        val unusedFiles = remember { mutableStateListOf<File>() }
        // can when launched
        LaunchedEffect(Unit) {
            // scan audio files and add them to the lists
            scanAudioFiles(project).let { result ->
                usedFiles.addAll(result.usedFiles)
                unusedFiles.addAll(result.unusedFiles)
            }
        }
        // Ui
        Column {
            FilesListRow(FilesListType.Used, usedFiles)
            FilesListRow(FilesListType.Unused, unusedFiles)
            Spacer(Modifier.height(20.dp))
        }
    }

    private enum class FilesListType(val title: String) {
        Used("Used"),
        Unused("Unused");

        var opened by mutableStateOf(true)
    }

    @Composable
    private fun ColumnScope.FilesListRow(type: FilesListType, files: SnapshotStateList<File>) {
        // header
        OpenableGroupHeader(
            open = type.opened, onOpenedChange = { type.opened = it },
            text = type.title, modifier = Modifier.padding(vertical = 6.dp)
        )
        // files
        AnimatedVisibility(
            visible = type.opened
        ) {
            Column {
                FilesRows(type, files)
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun ColumnScope.FilesRows(type: FilesListType, files: SnapshotStateList<File>) {
        files.forEach { file ->
            Row(Modifier.padding(start = 16.dp).height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // File icon
                Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                    Icon(AllIconsKeys.FileTypes.Any_type, null)
                }
                // File name and optionally links
                Column {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Column {
                            Text(file.name, Modifier.padding(vertical = 4.dp))
                        }
                        if(type == FilesListType.Unused) {
                            Column(Modifier.fillMaxRowHeight(), verticalArrangement = Arrangement.Center) {
                                Link(text = "Delete", onClick = {
                                    //delete the file
                                    var deleted = false
                                    var exception: IOException? = null
                                    try {
                                        Files.delete(file.toPath())
                                        deleted = true
                                    } catch (ioException: IOException) {
                                        ioException.printStackTrace()
                                        exception = ioException
                                    }
                                    // remove the file from the list if it was deleted successfully
                                    if(deleted) {
                                        files.remove(file)
                                    } else {
                                        // otherwise show error
                                        BasicAlert(
                                            type = BasicAlert.Type.Error,
                                            title = "Failed to delete file",
                                            buttons = { OKButton() }
                                        ) {
                                            if(exception != null) {
                                                Text(exception.toString())
                                            }
                                        }.show()
                                    }
                                })
                            }
                        }
                    }
                }
            }
        }
    }

    data class ScanResult(val usedFiles: List<File>, val unusedFiles: List<File>)
    fun scanAudioFiles(project: Project): ScanResult {
        // lists for used and unused files
        val usedFiles = mutableListOf<File>()
        val unusedFiles = mutableListOf<File>()
        // scan all audio files in the project's audio directory
        project.getAudioDirectory().listFiles()?.forEach { file ->
            // check if any song uses the file and put it into the correct list
            val used = project.songs.any { it.audioFile == file }
            if(used) usedFiles.add(file) else unusedFiles.add(file)
        }
        // return the result
        return ScanResult(usedFiles, unusedFiles)
    }

}