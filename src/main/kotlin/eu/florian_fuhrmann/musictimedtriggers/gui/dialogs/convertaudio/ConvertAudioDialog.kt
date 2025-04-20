package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.convertaudio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import eu.florian_fuhrmann.musictimedtriggers.gui.alerts.BasicAlert
import eu.florian_fuhrmann.musictimedtriggers.gui.alerts.BasicAlertScope
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Dialog
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.components.CloseDialogButton
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import eu.florian_fuhrmann.musictimedtriggers.utils.file.findAvailableTargetFile
import eu.florian_fuhrmann.musictimedtriggers.utils.file.getStringWithoutSpecialChars
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.*
import java.io.File

const val LITTLE_ENDIAN_FILE_EXTENSION = "wav"
const val BIG_ENDIAN_FILE_EXTENSION = "aiff"
val SAMPLE_SIZES = listOf(8, 16, 24, 32) // in bits (8bit should be treated as big endian)
val SAMPLE_RATES = listOf(11025, 22050, 44100, 48000, 96000) // in Hz
val SAMPLE_SIZE_ITEMS = SAMPLE_SIZES.map { "$it bit" }
val SAMPLE_RATE_ITEMS = SAMPLE_RATES.map { "${it/1000.0} kHz" }

class ConvertAudioDialog(val project: Project, val file: File, val onConverted: (File, String, Int) -> Unit) : Dialog("Convert Audio") {

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
                var enabled by mutableStateOf(true)
                DefaultButton(
                    enabled = enabled,
                    onClick = {
                        // disable button
                        enabled = false
                        // convert audio
                        convert(
                            SAMPLE_SIZES[selectedSampleSizeIndex],
                            SAMPLE_RATES[selectedSampleRateIndex],
                            bigEndian,
                        )
                    }) {
                    Text("Convert")
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

    private fun convert(sampleSizeInBit: Int, sampleRateInHz: Int, bigEndian: Boolean) {
        convert(project, file, sampleSizeInBit, sampleRateInHz, bigEndian) { targetFile, resultCodec, resultSampleRateInHz ->
            // close dialog
            DialogManager.closeDialog(this@ConvertAudioDialog)
            // call onConverted callback
            onConverted(targetFile, resultCodec, resultSampleRateInHz)
        }
    }
}

fun convertWithoutUI(project: Project, file: File, onConverted: (File, String, Int) -> Unit) {
    val command = listOf(
        "ffprobe",
        "-v", "quiet",
        "-print_format", "json",
        "-show_format",
        "-show_streams",
        file.canonicalPath
    )
    val ffProbeCommandResult = execCommandOrShowErrorAlert(command) ?: return
    // ensure exit code 0
    if(ffProbeCommandResult.exitCode != 0) {
        BasicAlert(BasicAlert.Type.Error, "Error (Exit Code: ${ffProbeCommandResult.exitCode})", { OKButton() }) {
            ShowFullCommandOutputLink(command, ffProbeCommandResult)
        }.show()
    }
    // ensure ffprobe command gave valid json output
    val ffProbeJson: JsonObject = try {
        JsonParser.parseString(ffProbeCommandResult.output).asJsonObject
    } catch (e: JsonParseException) {
        showError("JsonParseException", command, ffProbeCommandResult)
        return
    }
    // find audio stream
    val audioStreamJson = ffProbeJson.get("streams").asJsonArray.find { stream ->
        stream.asJsonObject.get("codec_type").asString == "audio"
    } ?: {
        showError("No Audio stream in json", command, ffProbeCommandResult)
    }
    if(audioStreamJson !is JsonObject) return
    // find format
    val formatJson = ffProbeJson.get("format").asJsonObject
    // extract sample_fmt, bits_per_sample, sample_rate (from audio stream)
    val sampleFormat = if(audioStreamJson.has("sample_fmt")) audioStreamJson.get("sample_fmt").asString else null
    val bitsPerSample = if(audioStreamJson.has("bits_per_sample")) audioStreamJson.get("bits_per_sample").asInt else null
    val sampleRateInHz = if(audioStreamJson.has("sample_rate")) audioStreamJson.get("sample_rate").asInt else null
    // extract bit_rate (from format)
    val bitRate = if(formatJson.has("bit_rate")) formatJson.get("bit_rate").asInt else null
    // require sample rate to be present
    if(sampleRateInHz == null) {
        showError("No sample rate in json", command, ffProbeCommandResult)
        return
    }
    // decide target sample size (in bits)
    var targetSampleSizeInBits: Int? = if(bitsPerSample != null && bitsPerSample > 0) {
        if(bitsPerSample > 16) {
            24
        } else if(bitsPerSample > 8) {
            16
        } else {
            8
        }
    } else if(sampleFormat != null) {
        if(sampleFormat.contains("32") || sampleFormat.contains("64") || sampleFormat.contains("flt") || sampleFormat.contains("dbl")) {
            24
        } else if(sampleFormat.contains("16")) {
            16
        } else if(sampleFormat.contains("8")) {
            8
        } else {
            null
        }
    } else {
        null
    }
    // if target sample size is still unknown, guess from bit rate
    if(targetSampleSizeInBits == null) {
        targetSampleSizeInBits = when(bitRate) {
            null -> {
                showError("No bit rate in json", command, ffProbeCommandResult)
                return
            }
            in 0..96_000 -> 8
            in 96_001..500_000 -> 16
            else -> 24
        }
    }
    // do conversion
    convert(project, file, targetSampleSizeInBits, sampleRateInHz, false) { targetFile, codec, sampleRateInHz ->
        // call onConverted callback
        onConverted(targetFile, codec, sampleRateInHz)
    }
}

private fun showError(title: String, command: List<String>, commandResult: CommandResult) {
    BasicAlert(
        type = BasicAlert.Type.Error,
        title = title,
        buttons = { OKButton() }
    ) {
        ShowFullCommandOutputLink(command, commandResult)
    }.show()
}

private fun convert(project: Project, file: File, sampleSizeInBit: Int, sampleRateInHz: Int, bigEndian: Boolean, onConverted: (File, String, Int) -> Unit) {
    // construct codec name
    var codec = "pcm_s$sampleSizeInBit"
    if(sampleSizeInBit != 8) codec += if(bigEndian) "be" else "le"
    // construct file extension
    val fileExtension = if(bigEndian || sampleSizeInBit == 8) BIG_ENDIAN_FILE_EXTENSION else LITTLE_ENDIAN_FILE_EXTENSION
    // construct output file name
    val outputFileName = "${getStringWithoutSpecialChars(file.nameWithoutExtension)}_converted_${codec}_${sampleRateInHz}Hz.$fileExtension"
    // get target file
    val targetFile = findAvailableTargetFile(project.getAudioDirectory(), outputFileName)
    // construct command
    val command = listOf(
        "ffmpeg",
        "-i", file.canonicalPath,
        "-acodec", codec,
        "-ar", sampleRateInHz.toString(),
        targetFile.canonicalPath
    )
    // execute command
    val commandResult = execCommandOrShowErrorAlert(command) ?: return
    // check whether command was successful
    if(commandResult.exitCode == 0) {
        // check if output file exists
        if(targetFile.exists()) {
            // call onConverted callback
            onConverted(targetFile, codec, sampleRateInHz)
        } else {
            // show error message
            BasicAlert(
                type = BasicAlert.Type.Error,
                title = "Output file does not exist",
                buttons = { OKButton() }
            ) {
                Column {
                    Row {
                        Text("The command was executed successfully, but the output file does not exist.")
                    }
                    Spacer(Modifier.height(8.dp))
                    Row {
                        ShowFullCommandOutputLink(command, commandResult)
                    }
                }
            }.show()
        }
    } else {
        // show error message
        BasicAlert(
            type = BasicAlert.Type.Error,
            title = "Error (Exit Code: ${commandResult.exitCode})",
            buttons = { OKButton() }
        ) {
            ShowFullCommandOutputLink(command, commandResult)
        }.show()
    }
}

private fun checkFFmpegVersion() {
    val command = listOf("ffmpeg", "-version")
    val commandResult = execCommandOrShowErrorAlert(command) ?: return
    var ffmpegVersion: String? = null
    if(commandResult.exitCode == 0 && commandResult.output.isNotEmpty()) {
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
        ShowFullCommandOutputLink(command, commandResult)
    }.show()
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun BasicAlertScope.ShowFullCommandOutputLink(command: List<String>, commandResult: CommandResult) {
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
}

data class CommandResult(
    val output: String,
    val exitCode: Int
)
private fun execCommand(command: List<String>): CommandResult {
    val proc = ProcessBuilder(command)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectErrorStream(true)
        .start()

    val exitCode = proc.waitFor()
    return CommandResult(proc.inputStream.bufferedReader().readText(), exitCode)
}
private fun execCommandOrShowErrorAlert(command: List<String>): CommandResult? {
    return try {
        execCommand(command)
    } catch (e: Exception) {
        // show error message
        BasicAlert(
            type = BasicAlert.Type.Error,
            title = "Error executing command",
            buttons = { OKButton() }
        ) {
            Column {
                Row {
                    Text(command.joinToString(" "), color = JewelTheme.globalColors.text.disabled)
                }
                Spacer(Modifier.height(8.dp))
                Row {
                    Text("${e.message}")
                }
            }
        }.show()
        // return null
        null
    }
}