package eu.florian_fuhrmann.musictimedtriggers.utils.audio.player

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import kotlin.math.roundToInt

var currentAudioPlayer: MutableState<AudioPlayer?> = mutableStateOf(null)

fun openAudioPlayer(file: File) {
    currentAudioPlayer.value?.onClose()
    currentAudioPlayer.value = AudioPlayer(file)
}

fun closeAudioPlayer() {
    currentAudioPlayer.value?.onClose()
    currentAudioPlayer.value = null
}

class AudioPlayer(var file: File) {

    var playing = mutableStateOf(false)
    private val clip = AudioSystem.getClip()
    private val audioInputStream: AudioInputStream = AudioSystem.getAudioInputStream(file)
    private val format: AudioFormat = audioInputStream.format

    init {
        clip.open(audioInputStream)
    }

    fun start() {
        clip.start()
        playing.value = true
    }

    fun stop() {
        clip.stop()
        playing.value = false
    }

    val secondDuration: Double
        get() = clip.frameLength / format.frameRate.toDouble()
    var secondPosition: Double
        get() = clip.longFramePosition / format.frameRate.toDouble()
        set(value) {
            clip.framePosition = (value * format.frameRate).roundToInt()
            if(playing.value && !clip.isRunning) {
                clip.start()
            }
        }

    fun onClose() {
        clip.close()
    }
}
