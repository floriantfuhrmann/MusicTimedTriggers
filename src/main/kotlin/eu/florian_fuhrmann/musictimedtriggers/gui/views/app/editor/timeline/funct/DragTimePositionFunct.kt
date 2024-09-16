package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.funct

import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.redrawTimeline
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineBackgroundRenderer
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineRenderer
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.updateCursor
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.player.currentAudioPlayer
import eu.florian_fuhrmann.musictimedtriggers.utils.os.OsUtils
import java.awt.event.*

/*
this object should handle the functionality of dragging the time position of the current song with the middle mouse button
 */
object DragTimePositionFunct {

    var restartPlaybackAfterDragEnd = false
    var secondsGridHovered = false
    var draggingTimePosition = false
    private var lastX = 0
    private var draggingButton = 0

    //checks is spectrogram and audioplayer are not null (otherwise time position dragging does not make sense)
    private fun isReady(): Boolean {
        return ProjectManager.currentProject?.currentSong?.spectrogram != null && currentAudioPlayer.value != null
    }

    val mouseListener: MouseListener = object : MouseListener {
        override fun mouseClicked(e: MouseEvent?) {}
        override fun mousePressed(e: MouseEvent?) {
            //check if already dragging and e is not null and ready
            if(draggingTimePosition || e == null || !isReady()) return
            if(e.button == 2 || (e.button == 1 && e.y <= TimelineRenderer.currentSecondsGridHeight)) {
                //start dragging
                draggingTimePosition = true
                draggingButton = e.button
                lastX = e.x
                restartPlaybackAfterDragEnd = ProjectManager.currentProject?.currentSong?.isPlaying() == true
                ProjectManager.currentProject?.currentSong?.pause()
                updateCursor()
            }
        }
        override fun mouseReleased(e: MouseEvent?) {
            if(e == null || !isReady()) return
            if(e.button == draggingButton) {
                //end dragging
                draggingTimePosition = false
                if(restartPlaybackAfterDragEnd) {
                    ProjectManager.currentProject?.currentSong?.play()
                }
                updateCursor()
            }
        }
        override fun mouseEntered(e: MouseEvent?) {}
        override fun mouseExited(e: MouseEvent?) {}
    }
    val mouseMotionListener: MouseMotionListener = object : MouseMotionListener {
        override fun mouseDragged(e: MouseEvent?) {
            if(draggingTimePosition && e != null && isReady()) {
                val deltaX = e.x - lastX
                if(deltaX != 0) {
                    lastX = e.x
                    currentAudioPlayer.value?.apply {
                        secondPosition -= deltaX.toDouble() / TimelineBackgroundRenderer.destinationPixelsPerSecond
                    }
                    redrawTimeline()
                }
            }
        }
        override fun mouseMoved(e: MouseEvent?) {
            if(e == null || !isReady()) return
            val newSecondsGridHovered = e.y < TimelineRenderer.currentSecondsGridHeight
            if(newSecondsGridHovered != secondsGridHovered) {
                secondsGridHovered = newSecondsGridHovered
                updateCursor()
            }
        }
    }
    const val WHEEL_ROTATION_MULTIPLIER = 5
    val mouseWheelListener: MouseWheelListener = object : MouseWheelListener {
        override fun mouseWheelMoved(e: MouseWheelEvent?) {
            if(e == null || !isReady()) return
            if(OsUtils.isMacOs && !e.isShiftDown) return // on macOS shift down indicates horizontal
            currentAudioPlayer.value?.apply {
                secondPosition += WHEEL_ROTATION_MULTIPLIER * (e.preciseWheelRotation / TimelineBackgroundRenderer.destinationPixelsPerSecond)
            }
            redrawTimeline()
        }
    }
}