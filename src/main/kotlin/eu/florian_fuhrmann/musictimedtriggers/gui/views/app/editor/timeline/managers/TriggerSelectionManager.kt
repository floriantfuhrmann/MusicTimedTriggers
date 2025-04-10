package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.managers

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.managers.MoveTriggersManager.getTriggerAt
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.managers.MoveTriggersManager.updateTriggerHover
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.redrawTimeline
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineBackgroundRenderer.xToTime
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineRenderer
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineSequenceRenderer
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.updateCursor
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedIntensityTrigger
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedTrigger
import eu.florian_fuhrmann.musictimedtriggers.triggers.sequence.TriggerSequence
import eu.florian_fuhrmann.musictimedtriggers.triggers.utils.intensity.Keyframes
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.player.currentAudioPlayer
import java.awt.Color
import java.awt.Graphics2D
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import javax.swing.SwingUtilities
import kotlin.math.max
import kotlin.math.min

/** Manages the selection of triggers and keyframes in the timeline. */
object TriggerSelectionManager {

    // Variables

    /** bool to mark whether we are currently selecting triggers */
    var selecting = false
        private set

    // corners of selection (not determined whether x1 or x2 is smaller, same for y)
    private var selectionX1 = 0
    private var selectionY1 = 0
    private var selectionX2 = 0
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

    /**
     * the currently selected trigger in a state object, so it can be observed
     * from compose. null if not exactly one trigger is selected
     */
    var singleSelectedTriggerState: MutableState<AbstractPlacedTrigger?> = mutableStateOf(null)

    /** keyframes currently in the selection box */
    private var selectionBoxKeyframes: Map<Keyframes.Keyframe, AbstractPlacedIntensityTrigger> = emptyMap()
    /** keyframes that are currently fully selected mapped to their parents */
    var selectedKeyframes: HashMap<Keyframes.Keyframe, AbstractPlacedIntensityTrigger> = HashMap()
    
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
        // update single selected trigger state
        updateSingleSelectedTriggerState()
        // redraw timeline to show selection
        redrawTimeline()
    }

    fun selectKeyframe(keyframe: Keyframes.Keyframe, parent: AbstractPlacedIntensityTrigger, keepOthers: Boolean = false) {
        // update selected keyframes
        if (!keepOthers) {
            selectedKeyframes.clear()
        }
        selectedKeyframes[keyframe] = parent
        // redraw timeline to show selection
        redrawTimeline()
    }

    fun deselectTrigger(trigger: AbstractPlacedTrigger, redraw: Boolean = true) {
        val removed = selectedTriggers.remove(trigger)
        if (removed && redraw) {
            redrawTimeline()
        }
        // update single selected trigger state
        updateSingleSelectedTriggerState()
    }

    fun deselectAllTriggersAndKeyframes(redraw: Boolean = true) {
        selectedTriggers.clear()
        selectedKeyframes.clear()
        if (redraw) {
            redrawTimeline()
        }
        // update single selected trigger state
        updateSingleSelectedTriggerState()
    }

    private fun updateSingleSelectedTriggerState() {
        // update single selected trigger state
        if (selectedTriggers.size == 1) {
            singleSelectedTriggerState.value = selectedTriggers.first()
        } else {
            singleSelectedTriggerState.value = null
        }
    }

    /** @return whether the [trigger] is visually selected in some way */
    fun isVisuallySelected(trigger: AbstractPlacedTrigger) =
        selectionBoxTriggers.contains(trigger) || selectedTriggers.contains(trigger)

    /**
     * @return whether the [trigger] is selected (a trigger is only considered
     *    to be finally selected after selecting with selection box has ended)
     */
    fun isSelected(trigger: AbstractPlacedTrigger) = selectedTriggers.contains(trigger)

    /**
     * @return whether the [keyframe] is visually selected (so either in the
     *    selection box or fully selected)
     */
    fun isVisuallySelected(keyframe: Keyframes.Keyframe) =
        selectionBoxKeyframes.contains(keyframe) || selectedKeyframes.contains(keyframe)

    /** @return whether the [keyframe] is fully selected */
    fun isSelected(keyframe: Keyframes.Keyframe) = selectedKeyframes.contains(keyframe)

    // Selection Box Logic

    /**
     * begins selection of triggers using a selection box
     */
    fun beginSelection(e: MouseEvent) {
        // set to no trigger hovered
        MoveTriggersManager.resetHovered()
        // only keep others when shift is pressed
        if (!e.isShiftDown) {
            selectedTriggers.clear()
            selectedKeyframes.clear()
            // update single selected trigger state
            updateSingleSelectedTriggerState()
        }
        // start selection
        selecting = true
        selectionX1 = e.x
        selectionY1 = e.y
        // and update for the first time
        updateSelection(e)
    }

    /**
     * updates the selection box to the current mouse position
     */
    private fun updateSelection(e: MouseEvent, redraw: Boolean = true) {
        // get sequence
        val sequence = ProjectManager.currentProject?.currentSong?.sequence ?: throw IllegalStateException("No sequence")
        // update selection box corner
        selectionX2 = e.x
        selectionY2 = e.y
        // calculate time period of selection
        val fromTime = xToTime(min(selectionX1, selectionX2))
        val toTime = xToTime(max(selectionX1, selectionX2))
        // calculate line index range
        // get line indices of first and last line in selection box
        val fromLineIndex = TimelineSequenceRenderer.getSequenceLineIndexAt(min(selectionY1, selectionY2)) ?: 0
        val toLineIndex = TimelineSequenceRenderer.getSequenceLineIndexAt(max(selectionY1, selectionY2)) ?: sequence.lines.lastIndex
        val lineIndexRange = fromLineIndex..toLineIndex
        // update triggers and keyframes in selection box
        updateTriggersInSelectionBox(fromTime, toTime, lineIndexRange, sequence)
        updateKeyframesInSelectionBox(fromTime, toTime, lineIndexRange, sequence)
        // redraw timeline to show selection
        if(redraw) {
            redrawTimeline()
        }
    }

    fun endSelection(e: MouseEvent) {
        // update selection one last time
        updateSelection(e, false)
        selecting = false
        // update selected triggers and keyframes
        selectedTriggers.addAll(selectionBoxTriggers)
        selectedKeyframes.putAll(selectionBoxKeyframes)
        // reset selection boxes
        selectionBoxTriggers = emptySet()
        selectionBoxKeyframes = emptyMap()
        // update single selected trigger state
        updateSingleSelectedTriggerState()
        // redraw timeline to show selection
        redrawTimeline()
        // also update trigger hovered because pointer could have stopped on a trigger
        updateTriggerHover(e, false)
        updateCursor() // we update here ourselves so the cursor is always updated
    }

    /** updates which triggers are in the selection box */
    private fun updateTriggersInSelectionBox(fromTime: Double, toTime: Double, lineIndexRange: IntRange, sequence: TriggerSequence) {
        // update triggers in box
        selectionBoxTriggers =
            sequence.lines
                .slice(lineIndexRange)
                .flatMap {
                    it.getTriggersInPeriod(fromTime, toTime)
                }.toSet()
    }

    /**
     * Updates which keyframes of the triggers in selection box
     * are themselves in the selection box. (Must be called after
     * [updateTriggersInSelectionBox] because it uses the triggers in the
     * selection box.)
     */
    private fun updateKeyframesInSelectionBox(fromTime: Double, toTime: Double, lineIndexRange: IntRange, sequence: TriggerSequence) {
        // get max and min y
        val minY = min(selectionY1, selectionY2)
        val maxY = max(selectionY1, selectionY2)
        // calculate value threshold for first line (values need to be below this threshold to be in selection box)
        val firstLineHeight = TimelineSequenceRenderer.getSequenceLineHeight(lineIndexRange.first) ?: throw IllegalStateException()
        val firstLineTopY = TimelineSequenceRenderer.getSequenceLineTopY(lineIndexRange.first) ?: throw IllegalStateException()
        val firstLineThreshold = 1.0 - ((minY - firstLineTopY) / firstLineHeight.toDouble())
        // calculate value threshold for last line (values need to be above this threshold to be in selection box)
        val lastLineHeight = TimelineSequenceRenderer.getSequenceLineHeight(lineIndexRange.last) ?: throw IllegalStateException()
        val lastLineTopY = TimelineSequenceRenderer.getSequenceLineTopY(lineIndexRange.last) ?: throw IllegalStateException()
        val lastLineThreshold = 1.0 - ((maxY - lastLineTopY) / lastLineHeight.toDouble())
        selectionBoxKeyframes = selectionBoxTriggers.filterIsInstance<AbstractPlacedIntensityTrigger>()
            .flatMap { trigger: AbstractPlacedIntensityTrigger ->
                // find line index of trigger
                val lineIndex = sequence.findLineIndexOf(trigger, lineIndexRange)
                // check y for lines, which are not fully contained in selection box
                if(lineIndex == lineIndexRange.first) {
                    // check if also the last line (so selection box only includes one line)
                    if(lineIndex == lineIndexRange.last) {
                        // so we have to check both directions
                        trigger.keyframes().getKeyframesInTimePeriod(fromTime, toTime, trigger).filter { keyframe ->
                            keyframe.value in lastLineThreshold..firstLineThreshold
                        }.map { k -> k to trigger }
                    } else {
                        // so we just have to check the keyframes values are not above threshold
                        trigger.keyframes().getKeyframesInTimePeriod(fromTime, toTime, trigger).filter { keyframe ->
                            keyframe.value <= firstLineThreshold
                        }.map { k -> k to trigger }
                    }
                } else if(lineIndex == lineIndexRange.last) {
                    // so we just have to check the keyframes values are not below threshold
                    trigger.keyframes().getKeyframesInTimePeriod(fromTime, toTime, trigger).filter { keyframe ->
                        keyframe.value >= lastLineThreshold
                    }.map { k -> k to trigger }
                } else {
                    // otherwise we can just return all keyframes in time period
                    trigger.keyframes().getKeyframesInTimePeriod(fromTime, toTime, trigger).map { k -> k to trigger }
                }
            }.toMap()
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
                // only proceed if user clicked in timeline area
                if (e.y <= TimelineRenderer.timelineCoreX + TimelineRenderer.secondsGridHeight
                    || e.y > TimelineRenderer.timelineCoreX + TimelineRenderer.timelineCoreHeight
                    || e.x < TimelineRenderer.timelineCoreX
                    || e.x > TimelineRenderer.timelineCoreX + TimelineRenderer.timelineCoreWidth) {
                    return
                }
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
        val minX = min(selectionX1, selectionX2)
        val maxX = max(selectionX1, selectionX2)
        val minY = min(selectionY1, selectionY2)
        val maxY = max(selectionY1, selectionY2)
        // draw selection box
        g.color = Color(255, 255, 255, 64)
        g.fillRect(minX, minY, maxX - minX, maxY - minY)
        g.color = Color.white
        g.drawRect(minX, minY, maxX - minX, maxY - minY)
    }

}
