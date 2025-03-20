package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer

import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.managers.ReceiveDraggedTemplatesManger
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.managers.TriggerSelectionManager
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.player.currentAudioPlayer
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints

object TimelineRenderer {

    // Values needed globally
    var secondsGridHeight = 0
    // Area for the timeline (contains sequence lines with triggers on spectrogram and second grid)
    var timelineX = 0
    var timelineY = 0
    var timelineWidth = 0
    var timelineHeight = 0

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
        // update timeline area values
        timelineX = x
        timelineY = y
        timelineWidth = width
        timelineHeight = height
        // Draw Background
        // calculate height for seconds grid (needed as offset for spectrogram)
        secondsGridHeight = TimelineGridRenderer.calculateSecondsGridHeight(g)
        // draw spectrogram background
        TimelineBackgroundRenderer.render(g, spectrogram, audioPlayer.secondPosition, audioPlayer.secondDuration, x,
            y + secondsGridHeight, width, height - secondsGridHeight)
        // Draw Content
        // draw second grid
        TimelineGridRenderer.drawSecondGrid(g, x, y, width, height, secondsGridHeight, false)
        // draw placed triggers
        TimelineSequenceRenderer.drawSequence(g, x, y + secondsGridHeight, width, height - secondsGridHeight, sequence)
        // draw dragged templates
        ReceiveDraggedTemplatesManger.drawDragIndicator(g, x, y, width, height)
        // draw selection
        TriggerSelectionManager.drawSelectionBox(g)
        // draw play head
        TimelineGridRenderer.drawPlayHead(g, x, y, width, height, audioPlayer.secondPosition, secondsGridHeight)
    }
}