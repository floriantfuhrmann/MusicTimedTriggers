package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector.editsong

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.theme.colorPalette
import javax.sound.sampled.AudioFormat

@Composable
fun AudioEncodingInformationRow(audioFormat: AudioFormat?, columnModifier: Modifier = Modifier) {
    Row {
        Column(columnModifier) {
            listOf(
                "Encoding" to (audioFormat?.encoding ?: "Unknown").toString(),
                "Sample Rate" to audioFormat?.sampleRate.let { if(it != null) "${it/1000.0} kHz" else "Unknown" },
                "Sample Size" to audioFormat?.sampleSizeInBits.let { if(it != null) "$it Bit" else "Unknown" },
                "Channels" to (audioFormat?.channels ?: "Unknown").toString(),
                "Frame Size" to audioFormat?.frameSize.let { if(it != null) "$it Byte" else "Unknown" },
                "Frame Rate" to audioFormat?.frameRate.let { if(it != null) "${it/1000.0} kHz" else "Unknown" },
                "Endianness" to when(audioFormat?.isBigEndian) {
                    true -> "Big Endian"
                    false -> "Little Endian"
                    else -> "Unknown"
                },
            ).forEach {
                Row {
                    Text(buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = JewelTheme.colorPalette.gray(7))) {
                            append("${it.first}: ")
                        }
                        append(it.second)
                    })
                }
            }
        }
    }
}