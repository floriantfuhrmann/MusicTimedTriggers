package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer

import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.manager.MoveTriggersManager
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.manager.ReceiveDraggedTemplatesManger
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.manager.TriggerSelectionManager
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.player.currentAudioPlayer
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints

object TimelineRenderer {
    var currentSecondsGridHeight = 0

    fun render(g: Graphics, width: Int, height: Int) {
        //get Graphics2D and enable antialiasing
        val g2 = (g as Graphics2D)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        //fill background black
        g.color = Color.black
        g.fillRect(0, 0, width, height)
        //don't draw content when not ready
        if(ProjectManager.currentProject?.currentSong?.spectrogram == null || currentAudioPlayer.value == null) {
            g.color = Color.white
            g.drawString("Preparing Spectrogram and AudioPlayer ...", 20, 10)
            return
        }
        //draw background
        //calculate height for seconds grid
        currentSecondsGridHeight = TimelineGridRenderer.calculateSecondsGridHeight(g)
        //draw spectrogram background
        TimelineBackgroundRenderer.render(
            g,
            ProjectManager.currentProject!!.currentSong!!.spectrogram!!,
            currentAudioPlayer.value!!.secondPosition,
            currentAudioPlayer.value!!.secondDuration,
            0,
            currentSecondsGridHeight,
            width,
            height - currentSecondsGridHeight
        )
        //draw second grid
        TimelineGridRenderer.drawSecondGrid(
            g,
            currentSecondsGridHeight,
            false,
            width,
            height
        )
        //draw triggers
        val sequence = ProjectManager.currentProject?.currentSong?.sequence
        if(sequence != null) {
            TimelineSequenceRenderer.drawSequence(
                g2,
                0,
                currentSecondsGridHeight,
                width,
                height - currentSecondsGridHeight,
                sequence
            )
        } else {
            g.color = Color.red
            g.drawString("No Trigger Sequence", 20, 10)
        }
        ReceiveDraggedTemplatesManger.drawDragIndicator(g, width, height)
        //draw selection
        TriggerSelectionManager.drawSelectionBox(g)
        //draw play head
        TimelineGridRenderer.drawPlayHead(
            g,
            currentAudioPlayer.value!!.secondPosition,
            currentSecondsGridHeight,
            width,
            height
        )
    }
}