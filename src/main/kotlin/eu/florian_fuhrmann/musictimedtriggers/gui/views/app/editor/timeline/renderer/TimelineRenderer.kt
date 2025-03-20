package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer

import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.managers.ReceiveDraggedTemplatesManger
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.managers.TriggerSelectionManager
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.player.currentAudioPlayer
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints

object TimelineRenderer {
    var secondsGridHeight = 0

    fun render(g: Graphics2D, x: Int, y: Int, width: Int, height: Int) {
        // enable antialiasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED)
        // fill background black
        g.color = Color.black
        g.fillRect(x, y, width, height)
        // get spectrogram, audio player and sequence
        val spectrogram = ProjectManager.currentProject?.currentSong?.spectrogram
        val audioPlayer = currentAudioPlayer.value
        val sequence = ProjectManager.currentProject?.currentSong?.sequence
        // don't draw content when not ready
        if(spectrogram == null || audioPlayer == null || sequence == null) {
            g.color = Color.red
            g.drawString("Spectrogram, AudioPlayer or Sequence missing ...", x + 5, y + 15)
            return
        }
        // Draw Background
        // calculate height for seconds grid
        secondsGridHeight = TimelineGridRenderer.calculateSecondsGridHeight(g)
        // draw spectrogram background
        TimelineBackgroundRenderer.render(
            g,
            spectrogram,
            audioPlayer.secondPosition,
            audioPlayer.secondDuration,
            x,
            y + secondsGridHeight,
            width,
            height - secondsGridHeight
        )
//        //draw second grid
//        TimelineGridRenderer.drawSecondGrid(
//            g,
//            secondsGridHeight,
//            false,
//            width,
//            height
//        )
//        //draw triggers
//        TimelineSequenceRenderer.drawSequence(
//            g,
//            0,
//            secondsGridHeight,
//            width,
//            height - secondsGridHeight,
//            sequence
//        )
//        ReceiveDraggedTemplatesManger.drawDragIndicator(g, width, height)
//        //draw selection
//        TriggerSelectionManager.drawSelectionBox(g)
//        //draw play head
//        TimelineGridRenderer.drawPlayHead(
//            g,
//            currentAudioPlayer.value!!.secondPosition,
//            secondsGridHeight,
//            width,
//            height
//        )
    }
}