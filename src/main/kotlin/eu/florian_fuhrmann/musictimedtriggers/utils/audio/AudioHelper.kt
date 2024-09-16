package eu.florian_fuhrmann.musictimedtriggers.utils.audio

import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.UnsupportedAudioFileException

val AVAILABLE_CODECS: List<Codec> = listOf(
    Codec("pcm_s8", "PCM signed 8-bit", false),
    Codec("pcm_s16be", "PCM signed 16-bit big-endian", true),
    Codec("pcm_s16le", "PCM signed 16-bit little-endian", false),
    Codec("pcm_s24be", "PCM signed 24-bit big-endian", true),
    Codec("pcm_s24le", "PCM signed 24-bit little-endian", false),
    Codec("pcm_s32be", "PCM signed 32-bit big-endian", true),
    Codec("pcm_s32le", "PCM signed 32-bit little-endian", false),
    Codec("pcm_s64be", "PCM signed 64-bit big-endian", true),
    Codec("pcm_s64le", "PCM signed 64-bit little-endian", false)
)
val AVAILABLE_SAMPLE_RATES: List<Int> = listOf(11025, 22050, 44100)

fun getAudioFormat(audioFile: File): AudioFormat? {
    try {
        val audioInputStream = AudioSystem.getAudioInputStream(audioFile)
        val audioFormat = audioInputStream.format
        return audioFormat
    } catch (exception: UnsupportedAudioFileException) {
        return null
    }
}

fun isPcmEncoding(audioFile: File): Boolean {
    val audioFormat = getAudioFormat(audioFile)
    return audioFormat != null && audioFormat.encoding == AudioFormat.Encoding.PCM_SIGNED
}

fun getDurationOrNull(audioFile: File?): Double? {
    if(audioFile == null || !audioFile.exists()) {
        return null
    }
    try {
        val audioIn = AudioSystem.getAudioInputStream(audioFile)
        return (audioIn.frameLength / audioIn.format.frameRate).toDouble()
    } catch (exception: UnsupportedAudioFileException) {
        return null
    }
}
