package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.convertaudio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.alerts.BasicAlert
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Dialog
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.components.CloseDialogButton
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.*
import java.io.IOException

const val LITTLE_ENDIAN_FILE_EXTENSION = "wav"
const val BIG_ENDIAN_FILE_EXTENSION = "aiff"
val SAMPLE_SIZES = listOf(8, 16, 24, 32) // in bits (8bit should be treated as big endian)
val SAMPLE_RATES = listOf(11025, 22050, 44100) // in Hz
val SAMPLE_SIZE_ITEMS = SAMPLE_SIZES.map { "$it bit" }
val SAMPLE_RATE_ITEMS = SAMPLE_RATES.map { "${it/1000.0} kHz" }

class ConvertAudioDialog : Dialog("Convert Audio") {

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content() {
        // States
        var selectedSampleSizeIndex by remember { mutableIntStateOf(0) }
        var selectedSampleRateIndex by remember { mutableIntStateOf(0) }
        var bigEndian by remember { mutableStateOf(false) }
        // Contents
        Column(Modifier.fillMaxSize().background(JewelTheme.globalColors.panelBackground)) {
            // Header Message
            Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp).padding(top = 10.dp)) {
                Text("Convert audio file to PCM signed encoding using system ffmpeg installation.")
                Spacer(Modifier.height(8.dp))
            }
            // Sample Size, Sample Rate and Endianness Input
            FlowRow(Modifier.fillMaxWidth().padding(horizontal = 10.dp).height(IntrinsicSize.Min)) {
                // Sample Size and Sample Rate Labels
                Column(Modifier.fillMaxRowHeight()) {
                    Row(Modifier.weight(1f)) {
                        Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                            Text("Sample Size:")
                        }
                    }
                    Row(Modifier.weight(1f)) {
                        Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                            Text("Sample Rate:")
                        }
                    }
                }
                // Sample Size and Sample Rate Input
                Column(Modifier.fillMaxRowHeight().padding(end = 10.dp), verticalArrangement = Arrangement.Center) {
                    SelectionRow(
                        items = SAMPLE_SIZE_ITEMS,
                        onSelectedIndexChange = { selectedSampleSizeIndex = it }
                    )
                    SelectionRow(
                        items = SAMPLE_RATE_ITEMS,
                        onSelectedIndexChange = { selectedSampleRateIndex = it }
                    )
                }
                // Endianness Radio Buttons
                Column(Modifier.height(IntrinsicSize.Min)) {
                    Row(Modifier.padding(vertical = 6.dp).width(IntrinsicSize.Max)) {
                        Column {
                            RadioButtonRow("little-endian", !bigEndian, { bigEndian = false })
                        }
                        Column(Modifier.padding(start = 10.dp)) {
                            RadioButtonRow("big-endian", bigEndian, { bigEndian = true })
                        }
                    }
                }
            }
            // Destination Message
            Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp).padding(top = 10.dp)) {
                Text("The converted file will be saved in the Audio directory of the project directory.", color = JewelTheme.globalColors.text.disabled)
            }
            // Spacer to fill the space
            Spacer(Modifier.weight(1f))
            // Row with buttons
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Link("Check ffmpeg version", {
                    checkFFmpegVersion()
                })
                Spacer(Modifier.weight(1f))
                CloseDialogButton(text = "Close", dialog = this@ConvertAudioDialog)
                Spacer(Modifier.width(12.dp))
                DefaultButton(onClick = {
                    println("Todo: Convert audio file") // todo
                }) {
                    Text("Convert")
                }
            }
        }
    }

}

@Composable
fun SelectionRow(items: List<String>, onSelectedIndexChange: (Int) -> Unit) {
    Row {
        ListComboBox(
            items = items,
            isEditable = false,
            modifier = Modifier.width(150.dp).padding(vertical = 6.dp, horizontal = 6.dp),
            maxPopupHeight = 150.dp,
            onSelectedItemChange = { text -> onSelectedIndexChange(items.indexOf(text)) },
            listItemContent = { item, isSelected, _, isHovered, _ ->
                SimpleListItem(
                    text = item,
                    state = ListItemState(isSelected = isSelected, isHovered = isHovered, previewSelection = isHovered)
                )
            }
        )
    }
}

@OptIn(DelicateCoroutinesApi::class)
private fun checkFFmpegVersion() {
    val command = listOf("ffmpeg", "-version")
    val commandResult = execCommand(command)
    var ffmpegVersion: String? = null
    if(commandResult != null && commandResult.exitCode == 0 && commandResult.output.isNotEmpty()) {
        val matchResult = Regex("ffmpeg version ([^ ]+)").find(commandResult.output)
        if(matchResult != null) {
            val (version) = matchResult.destructured
            ffmpegVersion = version
        }
    }
    BasicAlert(
        type = if(ffmpegVersion != null) BasicAlert.Type.Info else BasicAlert.Type.Error,
        title = if(ffmpegVersion != null) "FFmpeg version $ffmpegVersion found" else "Failed to find ffmpeg version",
        buttons = {
            OKButton()
        }
    ) {
        if (commandResult != null) {
            Link("Show Full Command Output", {
                // close alert
                close()
                // open dialog with a delay of 10ms to allow the alert to fully close
                GlobalScope.launch {
                    delay(10)
                    // open dialog with command output (but keep open other dialogs)
                    DialogManager.openDialog(CommandResultDialog(command, commandResult), false)
                }
            })
        } else {
            Text("Error executing command")
        }
    }.show()
}

data class CommandResult(
    val output: String,
    val exitCode: Int
)
private fun execCommand(command: List<String>): CommandResult? {
    try {
        val proc = ProcessBuilder(command)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        val exitCode = proc.waitFor()
        return CommandResult(proc.inputStream.bufferedReader().readText(), exitCode)
    } catch(e: IOException) {
        e.printStackTrace()
        return null
    }
}