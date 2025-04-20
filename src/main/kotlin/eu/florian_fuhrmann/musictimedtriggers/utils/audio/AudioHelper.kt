package eu.florian_fuhrmann.musictimedtriggers.utils.audio

import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.UnsupportedAudioFileException

fun getAudioFormat(audioFile: File): AudioFormat? {
    try {
        val audioInputStream = AudioSystem.getAudioInputStream(audioFile)
        val audioFormat = audioInputStream.format
        return audioFormat
    } catch (exception: UnsupportedAudioFileException) {
        return null
    }
}

fun getAudioFormatOrNull(audioFile: File?): AudioFormat? {
    return if(audioFile == null || !audioFile.exists() || !audioFile.isFile) {
        null
    } else {
        getAudioFormat(audioFile)
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
