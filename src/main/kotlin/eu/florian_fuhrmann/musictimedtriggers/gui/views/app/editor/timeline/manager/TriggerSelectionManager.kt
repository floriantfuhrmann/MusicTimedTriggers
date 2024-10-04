package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.manager

import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.manager.MoveTriggersManager.getTriggerAt
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.manager.MoveTriggersManager.updateTriggerHover
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.redrawTimeline
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineBackgroundRenderer.xToTime
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineRenderer
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineSequenceRenderer
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.updateCursor
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedTrigger
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.player.currentAudioPlayer
import java.awt.Color
import java.awt.Graphics2D
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import javax.swing.SwingUtilities
import kotlin.math.max
import kotlin.math.min

/**
 * Manages the selection of triggers in the timeline.
 */
object TriggerSelectionManager {

    // Variables

    /**
     * bool to mark weither we are currently selecting triggers
     */
    var selecting = false
        private set

    // corners of selection (not determined weither x1 or x2 is smaller, same for y)
    private var selectionX1 = 0
    private var selectionX2 = 0
    private var selectionY1 = 0
    private var selectionY2 = 0

    /**
     * triggers currently in the selection box (they don't count as selected
     * everywhere)
     */
    private var selectionBoxTriggers: Set<AbstractPlacedTrigger> = emptySet()

    /**
     * triggers that are currently selected (does not always include triggers
     * in selection box)
     */
    val selectedTriggers: MutableSet<AbstractPlacedTrigger> = mutableSetOf()

    // General Selection Logic

    fun selectTrigger(
        trigger: AbstractPlacedTrigger,
        keepOthers: Boolean = false,
    ) {
        // update selected triggers
        if (!keepOthers) {
            selectedTriggers.clear()
        }
        selectedTriggers.add(trigger)
        // redraw timeline to show selection
        redrawTimeline()
    }

    /** @return weither the [trigger] is visually selected in some way */
    fun isVisuallySelected(trigger: AbstractPlacedTrigger) =
        selectionBoxTriggers.contains(trigger) || selectedTriggers.contains(trigger)

    /**
     * @return weither the [trigger] is selected (a trigger is only considered
     *    to be finally selected after selecting with selection box has ended)
     */
    fun isSelected(trigger: AbstractPlacedTrigger) = selectedTriggers.contains(trigger)

    // Selection Box Logic

    /**
     * begins selection of triggers using a selection box
     */
    fun beginSelection(e: MouseEvent) {
        // only start selection when audio player is not playing
        if (currentAudioPlayer.value?.playing?.value != false) return
        // set to no trigger hovered
        MoveTriggersManager.resetHovered()
        // only keep others when shift is pressed
        if (!e.isShiftDown) {
            selectedTriggers.clear()
        }
        // start selection
        selecting = true
        selectionX1 = e.x
        selectionX2 = e.y
        // and update for the first time
        updateSelection(e)
    }

    /**
     * updates the selection box to the current mouse position
     */
    private fun updateSelection(e: MouseEvent) {
        selectionY1 = e.x
        selectionY2 = e.y
        updateTriggersInSelectionBox()
        // redraw timeline to show selection
        redrawTimeline()
    }

    fun endSelection(e: MouseEvent) {
        updateSelection(e)
        selecting = false
        if (e.isShiftDown && selectedTriggers.containsAll(selectionBoxTriggers)) {
            // if shift is pressed and all triggers in box are already selected, then remove instead
            selectedTriggers.removeAll(selectionBoxTriggers)
        } else {
            // add to selected and clear selection box
            selectedTriggers.addAll(selectionBoxTriggers)
        }
        selectionBoxTriggers = emptySet()
        // redraw timeline to show selection
        redrawTimeline()
        // also update trigger hovered because pointer could have stopped on a trigger
        updateTriggerHover(e, false)
        updateCursor() // we update here ourselves so the cursor is always updated
    }

    /**
     * updates which triggers are in the selection box
     */
    private fun updateTriggersInSelectionBox() {
        // get sequence
        val sequence = ProjectManager.currentProject?.currentSong?.sequence ?: throw IllegalStateException("No sequence")
        // calculate period of selection
        val fromTime = xToTime(min(selectionX1, selectionY1))
        val toTime = xToTime(max(selectionX1, selectionY1))
        val fromLineIndex = TimelineSequenceRenderer.getSequenceLineIndexAt(min(selectionX2, selectionY2))
        val toLineIndex = TimelineSequenceRenderer.getSequenceLineIndexAt(max(selectionX2, selectionY2))
        if (fromLineIndex == null || toLineIndex == null) return
        // update triggers in box
        selectionBoxTriggers =
            sequence.lines
                .slice(fromLineIndex..toLineIndex)
                .flatMap {
                    it.getTriggersInPeriod(fromTime, toTime)
                }.toSet()
    }

    // Selection Listeners

    val mouseListener: MouseListener =
        object : MouseListener {
            override fun mouseClicked(e: MouseEvent?) {}

            override fun mousePressed(e: MouseEvent?) {
                // ignore null event
                if (e == null) return
                // only proceed if left mouse button was pressed
                if (!SwingUtilities.isLeftMouseButton(e)) return
                // only proceed if no audio is playing
                if (currentAudioPlayer.value?.playing?.value != false) return
                // only proceed if no trigger is hovered
                if (getTriggerAt(e.x, e.y) != null) return
                // only proceed if user clicked bellow seconds grid header
                if (e.y <= TimelineRenderer.currentSecondsGridHeight) return
                // user didn't click a trigger or seconds grid, so start selection box
                beginSelection(e)
            }

            override fun mouseReleased(e: MouseEvent?) {
                // ignore null event
                if (e == null) return
                // only proceed if left mouse button was released
                if (!SwingUtilities.isLeftMouseButton(e)) return
                // end selection (if currently selecting)
                if (selecting) {
                    endSelection(e)
                }
            }

            override fun mouseEntered(e: MouseEvent?) {}
            override fun mouseExited(e: MouseEvent?) {}
        }

    val mouseMotionListener: MouseMotionListener =
        object : MouseMotionListener {
            override fun mouseDragged(e: MouseEvent?) {
                // ignore null event
                if (e == null) return
                // update selection (if currently selecting)
                if (selecting) {
                    updateSelection(e)
                }
            }

            override fun mouseMoved(e: MouseEvent?) {
                // ignore null event
                if (e == null) return
                // update selection (if currently selecting)
                if (selecting) {
                    updateSelection(e)
                }
            }
        }

    // Selection Box Visuals

    fun drawSelectionBox(g: Graphics2D) {
        // only draw when selecting
        if (!selecting) return
        // calculate corners
        val minX = min(selectionX1, selectionY1)
        val maxX = max(selectionX1, selectionY1)
        val minY = min(selectionX2, selectionY2)
        val maxY = max(selectionX2, selectionY2)
        // draw selection box
        g.color = Color(255, 255, 255, 64)
        g.fillRect(minX, minY, maxX - minX, maxY - minY)
        g.color = Color.white
        g.drawRect(minX, minY, maxX - minX, maxY - minY)
    }

}
