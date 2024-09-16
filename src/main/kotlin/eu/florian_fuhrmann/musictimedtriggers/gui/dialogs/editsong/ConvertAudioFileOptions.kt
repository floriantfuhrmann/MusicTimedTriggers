package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.editsong

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.AVAILABLE_CODECS
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.AVAILABLE_SAMPLE_RATES
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.Codec
import eu.florian_fuhrmann.musictimedtriggers.utils.file.findAvailableTargetFile
import eu.florian_fuhrmann.musictimedtriggers.utils.file.getFileNameWithoutExtensions
import eu.florian_fuhrmann.musictimedtriggers.utils.file.getStringWithoutSpecialChars
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.RadioButtonRow
import org.jetbrains.jewel.ui.component.Text
import java.io.File
import java.io.IOException
import kotlin.concurrent.thread

@Composable
fun ConvertAudioFileOptions(
    project: Project,
    audioFile: File,
    onConvert: (File) -> Unit
) {
    //init states for selected output format
    var selectedCodec: Codec by remember { mutableStateOf(AVAILABLE_CODECS[2]) }
    var selectedSampleRate: Int by remember { mutableStateOf(AVAILABLE_SAMPLE_RATES[1]) }
    //find target file
    val targetConvertedFile: File by remember {
        derivedStateOf {
            findAvailableTargetFile(
                directory = project.getAudioDirectory(),
                fileName = getStringWithoutSpecialChars(getFileNameWithoutExtensions(audioFile.name))
                        + "_converted_${selectedCodec.name}_${selectedSampleRate}Hz"
                        + if (selectedCodec.bigEndian) { ".aiff" } else { ".wav" }
            )
        }
    }
    //show message
    Row(modifier = Modifier.padding(top = 3.dp).trackActivation()) {
        Text(
            color = Color.Gray,
            text = "Audio File has to be in PCM signed Encoding. If you have ffmpeg you can use that to convert it."
        )
    }
    //Output Codec Heading
    Row(modifier = Modifier.padding(top = 5.dp).trackActivation()) {
        Text("Output Codec:")
    }
    //Output Codec Radio
    AVAILABLE_CODECS.forEach {
        Row(modifier = Modifier.trackActivation()) {
            RadioButtonRow(
                text = "${it.name} (${it.description})",
                selected = (selectedCodec == it),
                onClick = { selectedCodec = it },
                modifier = Modifier.trackActivation()
            )
        }
    }
    //Output Sample Rate Heading
    Row(modifier = Modifier.padding(top = 2.dp).trackActivation()) {
        Text("Output SampleRate:")
    }
    //Output Codec Radio
    AVAILABLE_SAMPLE_RATES.forEach {
        Row(modifier = Modifier.trackActivation()) {
            RadioButtonRow(
                text = "${it/1000f} kHz",
                selected = (selectedSampleRate == it),
                onClick = { selectedSampleRate = it },
                modifier = Modifier.trackActivation()
            )
        }
    }
    //show message
    Row(modifier = Modifier.padding(top = 2.dp).trackActivation()) {
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Gray)) {
                    append("The converted file will be saved at ")
                }
                withStyle(style = SpanStyle(color = MainUiState.theme.highlightGray())) {
                    append(targetConvertedFile.absolutePath ?: "<No Target File>")
                }
            }
        )
    }
    //Convert
    var currentOutput by remember { mutableStateOf("") }
    var convertButtonEnabled by remember { mutableStateOf(true) }
    var convertFailed by remember { mutableStateOf(false) }
    Row(modifier = Modifier.padding(top = 3.dp).trackActivation()) {
        //Convert Button
        OutlinedButton(
            onClick = {
                //disable convert button
                convertButtonEnabled = false
                //convert
                convert(audioFile, selectedCodec, selectedSampleRate, targetConvertedFile, updateOutput = {
                    currentOutput = it
                }, onExit = {
                    //enable convert button again
                    convertButtonEnabled = true
                    //check if it was successful
                    if(it == 0) {
                        //if so call callback
                        onConvert(targetConvertedFile)
                    } else {
                        //else delete the output file
                        if(targetConvertedFile.exists()) {
                            targetConvertedFile.delete()
                        }
                        convertFailed = true
                    }
                })
            },
            modifier = Modifier
                .padding(end = 5.dp)
                .trackActivation(),
            enabled = convertButtonEnabled
        ) {
            Text("Convert")
        }
        //Failed Message
        if(convertFailed) {
            Box(modifier = Modifier.align(alignment = Alignment.CenterVertically).trackActivation()) {
                Text(color = Color.Red, text = "Convert failed.")
            }
        }
    }
    if(currentOutput.isNotBlank()) {
        Row(modifier = Modifier.padding(top = 3.dp, bottom = 5.dp).trackActivation()) {
            //Console Output
            Box(modifier = Modifier.fillMaxWidth().background(Color.Black).padding(5.dp).trackActivation()) {
                Text(color = Color.Gray, text = currentOutput)
            }
        }
    }
}

fun convert(audioFile: File, codec: Codec, sampleRate: Int, outputFile: File, updateOutput: (String) -> Unit, onExit: (Int) -> Unit) {
    updateOutput("Starting thread...")
    thread(start = true) {
        try {
            //init process
            val command = listOf(
                "ffmpeg",
                "-i",
                audioFile.canonicalPath,
                "-acodec",
                codec.name,
                "-ar",
                "$sampleRate",
                outputFile.canonicalPath
            )
            var currentOutput = command.joinToString(separator = " ") + "\n"
            updateOutput(currentOutput)
            val proc: Process = ProcessBuilder(command).start()
            //read input
            //read first byte
            var currentByte = proc.inputStream.read()
            while (currentByte != -1) {
                //convert to char
                val c = Char(currentByte)
                currentOutput += c
                updateOutput(currentOutput)
                //read next byte
                currentByte = proc.inputStream.read()
            }
            //read error
            //read first byte
            currentByte = proc.errorStream.read()
            while (currentByte != -1) {
                //convert to char
                val c = Char(currentByte)
                currentOutput += c
                updateOutput(currentOutput)
                //read next byte
                currentByte = proc.errorStream.read()
            }
            //wait for process
            val exitCode = proc.waitFor()
            currentOutput += "\nExit Code: $exitCode"
            updateOutput(currentOutput)
            onExit(exitCode)
        } catch (exception: IOException) {
            //call onExit with max value
            onExit(Int.MAX_VALUE)
            //update Output
            updateOutput("IOException occurred: "+exception.message.orEmpty())
        }
    }
}
