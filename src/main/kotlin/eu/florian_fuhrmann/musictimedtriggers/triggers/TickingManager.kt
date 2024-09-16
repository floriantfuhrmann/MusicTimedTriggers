package eu.florian_fuhrmann.musictimedtriggers.triggers

import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.redrawTimeline
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.player.currentAudioPlayer
import java.util.*
import kotlin.concurrent.fixedRateTimer

object TickingManager {

    // Functions for Ticking

    private var timer: Timer? = null

    fun startTicking() {
        //make sure the old ticker is not running
        stopTicking()
        //start ticking
        timer = fixedRateTimer(
            name = "MainTicker",
            daemon = false,
            initialDelay = 0,
            period = 50
        ) {
            //Tick Triggers
            tick()
            //Update UI
            redrawTimeline()
        }
    }

    private fun tick() {
        //Triggers will be updated here
        ProjectManager.currentProject?.currentSong?.sequence?.tick(currentAudioPlayer.value?.secondPosition ?: 0.0)
    }

    fun stopTicking() {
        timer?.cancel()
        timer = null
    }

}