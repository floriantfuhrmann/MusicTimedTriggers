package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.funct

import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Alert
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.redrawTimeline
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineBackgroundRenderer
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineBackgroundRenderer.durationToWidth
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineBackgroundRenderer.xToTime
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineRenderer
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineSequenceRenderer
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.updateCursor
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedIntensityTrigger
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedTrigger
import eu.florian_fuhrmann.musictimedtriggers.triggers.sequence.TriggerSequenceLine
import eu.florian_fuhrmann.musictimedtriggers.triggers.utils.intensity.Keyframes
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.player.currentAudioPlayer
import java.awt.Color
import java.awt.Graphics2D
import java.awt.event.*
import javax.swing.SwingUtilities
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object MoveTriggersFunct { // 'Funct' should be renamed to 'Handler' imo

    // Selection Variables:
    // bool to mark weither we are currently selecting triggers
    var selecting = false

    // corners of selection (not determined weither x1 or x2 is smaller, same for y)
    private var selectionX1 = 0
    private var selectionX2 = 0
    private var selectionY1 = 0
    private var selectionY2 = 0

    // triggers currently in the selection box
    private var triggersInSelectionBox: Set<AbstractPlacedTrigger> = emptySet()

    // triggers otherwise selected
    private val alreadySelectedTriggers: MutableSet<AbstractPlacedTrigger> = mutableSetOf()

    // Movement Variables:
    // bool to mark weither we are currently moving some triggers
    var moving = false

    // which part of the selected triggers we are currently moving
    var movingPart = TriggerPart.Middle

    // line on which the move captain currently is
    private var currentMoveLineIndex = 0

    // offsets the place triggers should have from the pointer when moving (see move implementation)
    private var moveOffsets: Map<AbstractPlacedTrigger, Double> = emptyMap()

    // Hover Variables:
    // which part of any trigger is currently hovered or null if no trigger is hovered
    var hoveredTriggerPart: TriggerPart? = null
    var hoveredTrigger: AbstractPlacedTrigger? = null
    var hoveredLineIndex: Int? = null
    var hoveredKeyframe: Keyframes.Keyframe? = null

    val mouseListener: MouseListener =
        object : MouseListener {
            override fun mouseClicked(e: MouseEvent?) {}

            override fun mousePressed(e: MouseEvent?) {
                if (e == null || !SwingUtilities.isLeftMouseButton(e)) return
                // only proceed if no audio is playing
                if (currentAudioPlayer.value?.playing?.value != false) return
                // get trigger clicked on and on which part was clicked
                val triggerAtResult = getTriggerAt(e.x, e.y)
                if (triggerAtResult == null) {
                    // user clicked outside any trigger
                    // ensure clicked bellow seconds grid header
                    if(e.y > TimelineRenderer.currentSecondsGridHeight) {
                        // user didn't click a trigger or seconds grid, so start selection box
                        startSelection(e)
                    }
                } else {
                    // user clicked on a trigger
                    val trigger = triggerAtResult.trigger
                    // check if also clicked on a keyframe
                    if (triggerAtResult.keyframe != null) {
                        // is so start moving keyframe
                        require(trigger is AbstractPlacedIntensityTrigger) { "Only intensity triggers can have keyframes" }
                        startMovingKeyframe(e, triggerAtResult.keyframe, trigger, triggerAtResult.lineIndex)
                    } else {
                        // make sure that the clicked trigger is selected
                        if (!isSelected(trigger)) {
                            selectTrigger(trigger, e.isShiftDown)
                        }
                        // start trigger moving
                        startMoving(e, triggerAtResult.part, triggerAtResult.lineIndex, trigger)
                    }
                }
            }

            override fun mouseReleased(e: MouseEvent?) {
                if (e == null || !SwingUtilities.isLeftMouseButton(e)) return
                if (selecting) {
                    endSelection(e)
                } else if (moving) {
                    endMove(e)
                } else if (isMovingKeyframe) {
                    endKeyframeMove(e)
                }
            }

            override fun mouseEntered(e: MouseEvent?) {}

            override fun mouseExited(e: MouseEvent?) {}
        }

    val mouseMotionListener: MouseMotionListener =
        object : MouseMotionListener {
            override fun mouseDragged(e: MouseEvent?) {
                if (e == null) return
                if (selecting) {
                    updateSelection(e)
                } else if (moving) {
                    updateMove(e)
                } else if (isMovingKeyframe) {
                    updateKeyframeMove(e)
                }
            }

            override fun mouseMoved(e: MouseEvent?) {
                if (e == null) return
                if (selecting) {
                    updateSelection(e)
                } else if (moving) {
                    updateMove(e)
                } else if (isMovingKeyframe) {
                    updateKeyframeMove(e)
                } else if (currentAudioPlayer.value?.playing?.value == false) {
                    updateTriggerHover(e)
                }
            }
        }

    private const val OUTSIDE_EDGE_CLICK_WIDTH = 5
    private const val INSIDE_EDGE_CLICK_WIDTH = 8

    data class TriggerAtResult(
        val trigger: AbstractPlacedTrigger,
        val lineIndex: Int,
        val part: TriggerPart,
        val keyframe: Keyframes.Keyframe? = null,
    )
    enum class TriggerPart { Start, Middle, End }

    /**
     * Finds trigger, lineIndex, trigger part (and keyframe) at [x], [y]
     * position. Intended for finding hovered/clicked trigger at pointer
     * position.
     */
    fun getTriggerAt(
        x: Int,
        y: Int,
    ): TriggerAtResult? {
        // first check whether any keyframes of the currently hovered trigger are at the pointer position. if so that
        // trigger inherits "being under pointer" even when pointer position is technically on another line.
        val currentlyHoveredTrigger = hoveredTrigger
        val currentlyHoveredPart = hoveredTriggerPart
        val currentlyHoveredLineIndex = hoveredLineIndex
        if (currentlyHoveredTrigger != null && currentlyHoveredTrigger is AbstractPlacedIntensityTrigger && currentlyHoveredPart != null && currentlyHoveredLineIndex != null) {
            // get trigger at pointer position
            val keyframeAt = getKeyframeAt(x, y, currentlyHoveredTrigger, currentlyHoveredLineIndex)
            // because a keyframe of the currently hovered trigger is at pointer position, the old info is returned
            if (keyframeAt != null) {
                return TriggerAtResult(
                    trigger = currentlyHoveredTrigger,
                    lineIndex = currentlyHoveredLineIndex,
                    part = currentlyHoveredPart,
                    keyframe = keyframeAt
                )
            }
        }
        // get clicked line and time
        val lineIndex = TimelineSequenceRenderer.getSequenceLineIndexAt(y) ?: return null
        val sequence = ProjectManager.currentProject?.currentSong?.sequence ?: return null
        val line = sequence.lines[lineIndex]
        val time = xToTime(x)
        // get trigger closest to that point
        val trigger = line.getTriggerClosestTo(time) ?: return null
        val clickedOnTrigger = trigger.isActiveAt(time)
        val edgeWidth =
            if (clickedOnTrigger) {
                INSIDE_EDGE_CLICK_WIDTH
            } else {
                OUTSIDE_EDGE_CLICK_WIDTH
            }
        val distanceToStart = durationToWidth(abs(trigger.startTime - time))
        val distanceToEnd = durationToWidth(abs(trigger.endTime - time))
        val triggerThirdWidth = durationToWidth(trigger.duration) / 3.0
        // find on which part of the trigger the user clicked
        val triggerPartAt = if (distanceToStart < edgeWidth && (!clickedOnTrigger || distanceToStart < triggerThirdWidth)) {
            // clicked on start
            TriggerPart.Start
        } else if (distanceToEnd < edgeWidth && (!clickedOnTrigger || distanceToEnd < triggerThirdWidth)) {
            // clicked on end
            TriggerPart.End
        } else if (clickedOnTrigger) {
            // click on the trigger
            TriggerPart.Middle
        } else {
            // clicked outside of trigger
            return null
        }
        // also find keyframe when trigger is intensity trigger
        return if (trigger is AbstractPlacedIntensityTrigger) {
            TriggerAtResult(trigger, lineIndex, triggerPartAt, getKeyframeAt(x, y, trigger, lineIndex))
        } else {
            TriggerAtResult(trigger, lineIndex, triggerPartAt)
        }
    }

    /**
     * Find's the keyframe at [x], [y] pointer position. This method expects the
     * hovered trigger at this position and the triggers line index is already
     * known.
     *
     * @param x pointer x
     * @param y pointer y
     * @param intensityTrigger hovered trigger, which has the keyframe candidates
     * @param triggersLineIndex line index of the line on which [intensityTrigger] is placed
     * @return the keyframe at [x], [y] or null, when no keyframe is at [x], [y]
     */
    private fun getKeyframeAt(x: Int, y: Int, intensityTrigger: AbstractPlacedIntensityTrigger, triggersLineIndex: Int): Keyframes.Keyframe? {
        // calculate trigger x and width
        val triggerX1 = TimelineBackgroundRenderer.timeToX(intensityTrigger.startTime)
        val triggerX2 = TimelineBackgroundRenderer.timeToX(intensityTrigger.endTime)
        val triggerWidth = triggerX2 - triggerX1
        // lookup line y and height
        val lineY = TimelineSequenceRenderer.getSequenceLineFromY(triggersLineIndex)
        val lineHeight = TimelineSequenceRenderer.getSequenceLineHeight(triggersLineIndex)
        // find keyframe, which contains x, y point
        return intensityTrigger.keyframes().keyframesList.find {
            val keyframeShape = TimelineSequenceRenderer.getKeyframeShape(triggerX1, lineY, triggerWidth, lineHeight, it)
            keyframeShape.contains(x, y)
        }
    }

    fun deleteSelectedTriggers() {
        // ensure there are any triggers selected
        if (alreadySelectedTriggers.isEmpty()) return
        // get sequence
        val sequence = ProjectManager.currentProject?.currentSong?.sequence ?: return
        if (alreadySelectedTriggers.size == 1) {
            // delete the one selected trigger
            val first = alreadySelectedTriggers.firstOrNull()!!
            sequence.findLineOf(first)!!.removeTrigger(first)
            //redraw timeline so change becomes visible
            redrawTimeline()
        } else {
            // Confirm Deletion first
            DialogManager.alert(
                Alert(
                    title = "Delete ${alreadySelectedTriggers.size} placed Triggers?",
                    text = "Are you sure you want to delete ${alreadySelectedTriggers.size} placed Triggers?",
                    onDismiss = {},
                    dismissText = "Cancel",
                    onConfirm = {
                        alreadySelectedTriggers.forEach {
                            sequence.findLineOf(it)!!.removeTrigger(it)
                        }
                        //redraw timeline so change becomes visible
                        redrawTimeline()
                    },
                ),
            )
        }
    }

    fun editSelectedTrigger() {
        if (alreadySelectedTriggers.size == 1) {
            alreadySelectedTriggers.first().openEditDialog()
        }
    }

    // Trigger Hovering

    fun updateTriggerHover(
        e: MouseEvent,
        updateCursorIfRequired: Boolean = true,
    ) {
        // find trigger, which would be clicked at current mouse position
        val triggerAtResult = getTriggerAt(e.x, e.y)
        // init vars storing whether to update cursor/rerender
        var updateCursor = false
        var redraw = false
        // update hovered line
        if(triggerAtResult?.lineIndex != hoveredLineIndex) {
            hoveredLineIndex = triggerAtResult?.lineIndex
        }
        // updated hovered trigger
        if(triggerAtResult?.trigger != hoveredTrigger) {
            // update value
            hoveredTrigger = triggerAtResult?.trigger
            // when hovered trigger changes a redraw is needed
            redraw = true
        }
        // update which part of trigger is hovered and cursor
        if (triggerAtResult?.part != hoveredTriggerPart) {
            // update value
            hoveredTriggerPart = triggerAtResult?.part
            // update cursor (because depending on hovered part another cursor icon should be displayed)
            updateCursor = true
        }
        // update hovered keyframe
        if(triggerAtResult?.keyframe != hoveredKeyframe) {
            // update value
            hoveredKeyframe = triggerAtResult?.keyframe
            // redraw needed to render keyframe as hovered and also update cursor so correct cursor is shown for hovered keyframe
            redraw = true
            updateCursor = true
        }
        // redraw timeline and update cursor (if needed)
        if(redraw) {
            redrawTimeline()
        }
        if(updateCursor && updateCursorIfRequired) {
            updateCursor()
        }
    }

    fun isTriggerHovered(trigger: AbstractPlacedTrigger) = trigger == hoveredTrigger
    fun isKeyframeHovered(keyframe: Keyframes.Keyframe) = keyframe == hoveredKeyframe

    private fun resetHovered() {
        hoveredTriggerPart = null
        hoveredTrigger = null
        hoveredLineIndex = null
        hoveredKeyframe = null
    }

    // Trigger Selection Box

    fun startSelection(e: MouseEvent) {
        // only start selection when audio player is not playing
        if (currentAudioPlayer.value?.playing?.value != false) return
        // set to no trigger hovered
        resetHovered()
        // only keep others when shift is pressed
        if (!e.isShiftDown) {
            alreadySelectedTriggers.clear()
        }
        // start selection
        selecting = true
        selectionX1 = e.x
        selectionX2 = e.y
        // and update for the first time
        updateSelection(e)
    }

    fun updateSelection(e: MouseEvent) {
        selectionY1 = e.x
        selectionY2 = e.y
        updateTriggersInSelectionBox()
        // redraw timeline to show selection
        redrawTimeline()
    }

    fun endSelection(e: MouseEvent) {
        updateSelection(e)
        selecting = false
        if (e.isShiftDown && alreadySelectedTriggers.containsAll(triggersInSelectionBox)) {
            // if shift is pressed and all triggers in box are already selected, then remove instead
            alreadySelectedTriggers.removeAll(triggersInSelectionBox)
        } else {
            // add to other selected and clear selection box
            alreadySelectedTriggers.addAll(triggersInSelectionBox)
        }
        triggersInSelectionBox = emptySet()
        // redraw timeline to show selection
        redrawTimeline()
        // also update trigger hovered because pointer could have stopped on a trigger
        updateTriggerHover(e, false)
        updateCursor() // we update here ourselves so the cursor is always updated
    }

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
        triggersInSelectionBox =
            sequence.lines
                .slice(fromLineIndex..toLineIndex)
                .flatMap {
                    it.getTriggersInPeriod(fromTime, toTime)
                }.toSet()
    }

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

    // General Trigger Selecting

    fun selectTrigger(
        trigger: AbstractPlacedTrigger,
        keepOthers: Boolean = false,
    ) {
        // update selected triggers
        if (!keepOthers) {
            alreadySelectedTriggers.clear()
        }
        alreadySelectedTriggers.add(trigger)
        // redraw timeline to show selection
        redrawTimeline()
    }

    /**
     * @return weither the [trigger] is visually selected in some way
     */
    fun isVisuallySelected(trigger: AbstractPlacedTrigger): Boolean =
        triggersInSelectionBox.contains(trigger) || alreadySelectedTriggers.contains(trigger)

    /**
     * @return weither the [trigger] is selected (a trigger is only considered to be finally selected after selecting
     * with selection box has ended)
     */
    fun isSelected(trigger: AbstractPlacedTrigger) = alreadySelectedTriggers.contains(trigger)

    // Trigger Movement

    /**
     * Starts the moving process. Triggers will not be affected yet, but when the pointer is moved. Also, when the
     * pointer will be first moved the caption will snap to the pointer position. If this behaviour is not wanted, then
     * null should be passed as [captain]
     */
    fun startMoving(
        e: MouseEvent,
        part: TriggerPart,
        captainLineIndex: Int,
        captain: AbstractPlacedTrigger?,
    ) {
        // set moving vars
        moving = true
        movingPart = part
        currentMoveLineIndex = captainLineIndex
        // calculate pointer time (time at pointer x)
        // when we have a captain then we calculate as if the user clicked perfectly on start/end of trigger
        val pointerTime =
            when (movingPart) {
                TriggerPart.Start -> captain?.startTime
                TriggerPart.End -> captain?.endTime
                else -> null
            } ?: xToTime(e.x)
        // for every selected trigger remember offset from pointer time
        moveOffsets =
            alreadySelectedTriggers.associateWith {
                if (movingPart == TriggerPart.End) {
                    it.endTime - pointerTime
                } else {
                    it.startTime - pointerTime
                }
            }
        // update cursor
        updateCursor()
    }

    fun updateMove(e: MouseEvent) {
        // get sequence
        val sequence = ProjectManager.currentProject?.currentSong?.sequence ?: throw IllegalStateException("No sequence")
        // calculate pointer time and get line the pointer is on
        val pointerTime = xToTime(e.x)
        val pointerLineIndex = TimelineSequenceRenderer.getSequenceLineIndexAt(e.y)
        // init any affected var (so we only need to redraw if anything actually changed)
        var anyAffected = false
        // check if triggers need to be moved between lines
        if (movingPart == TriggerPart.Middle && pointerLineIndex != null && pointerLineIndex != currentMoveLineIndex) {
            // init movePossible with true. will be set to false as soon as a conflict is found
            var movePossible: Boolean
            // map containing the lines the triggers should be moved to
            val targetLines: MutableMap<AbstractPlacedTrigger, TriggerSequenceLine> = mutableMapOf()
            // an adjusted pointer line index (the adjusted pointer line index will be moved towards to current line
            // index in every iteration to try to find an adjusted pointer line index where moving to is possible)
            var adjustedPointerLineIndex = pointerLineIndex
            do {
                // init move possible as true
                movePossible = true
                // clear target lines again
                targetLines.clear()
                // calculate how many lines the triggers need to be moved down (negativ delta means move up)
                val deltaLineIndex = adjustedPointerLineIndex - currentMoveLineIndex
                // check if triggers can be moved between lines like this and fill map with target lines for all triggers
                for (trigger in alreadySelectedTriggers) {
                    // get the trigger's current line index and calculate target line index
                    val currentLineIndex =
                        sequence.findLineIndexOf(trigger)
                            ?: throw IllegalStateException("Updating move for a trigger without a line in sequence")
                    val targetLineIndex = currentLineIndex + deltaLineIndex
                    // get the target line
                    val targetLine = sequence.lines.getOrNull(targetLineIndex)
                    // make sure that line exists
                    if (targetLine == null) {
                        movePossible = false
                        break
                    }
                    // check if there is space on the target line
                    if (targetLine.isPeriodFree(trigger.startTime, trigger.endTime, alreadySelectedTriggers)) {
                        // if so remember target line in map
                        targetLines[trigger] = targetLine
                    } else {
                        // if not than the move is not possible
                        movePossible = false
                        break
                    }
                }
                // if move is not possible try adjusting the pointer line index to move nearer
                if (!movePossible) {
                    if (adjustedPointerLineIndex > currentMoveLineIndex) {
                        adjustedPointerLineIndex--
                    } else {
                        adjustedPointerLineIndex++
                    }
                }
            } while (!movePossible && adjustedPointerLineIndex != currentMoveLineIndex)
            // perform the move if it's possible and set currentMoveLineIndex
            if (movePossible) {
                // update currentMoveLineIndex
                currentMoveLineIndex = adjustedPointerLineIndex
                // first remove all selected triggers
                alreadySelectedTriggers.forEach {
                    sequence.findLineOf(it)!!.removeTrigger(it)
                }
                // then add on their target lines
                alreadySelectedTriggers.forEach {
                    targetLines[it]!!.addTrigger(it)
                }
                // also now triggers have for sure been affected
                anyAffected = true
            }
        }
        // do the moving (within line)
        when (movingPart) {
            TriggerPart.Start -> {
                // try moving start time of every selected trigger to the pointer time + the triggers offset
                alreadySelectedTriggers.forEach {
                    // calculated the triggers desired start time
                    val desiredStartTime =
                        pointerTime + (
                            moveOffsets[it]
                                ?: throw IllegalStateException("Updating move for a trigger without an offset in map")
                        )
                    // check in what direction we are moving
                    if (desiredStartTime < it.startTime) {
                        // extending start time to the left
                        // get the triggers line
                        val line =
                            sequence.findLineOf(it)
                                ?: throw IllegalStateException("Updating move for a trigger without a line in sequence")
                        // calculate by how much we should extend
                        val extendTime =
                            (it.startTime - desiredStartTime)
                                .coerceAtMost(line.getFreeDurationUntil(it.startTime, true))
                        // update triggers start time
                        if (extendTime > 0) {
                            it.startTime -= extendTime
                            it.duration += extendTime
                            anyAffected = true
                        }
                    } else if (desiredStartTime > it.startTime) {
                        // reducing start time to the right
                        // calculate by how much we should reduce
                        val reduceTime =
                            (desiredStartTime - it.startTime)
                                .coerceAtMost(it.duration - AbstractPlacedTrigger.MINIMUM_TRIGGER_DURATION)
                        // update triggers start time
                        if (reduceTime > 0) {
                            it.startTime += reduceTime
                            it.duration -= reduceTime
                            anyAffected = true
                        }
                    }
                }
            }
            TriggerPart.Middle -> {
                // try moving all selected trigger's start time to the pointer time + their offset when moving started
                alreadySelectedTriggers.forEach {
                    // calculated the triggers desired start time
                    val desiredStartTime =
                        pointerTime + (
                            moveOffsets[it]
                                ?: throw IllegalStateException("Updating move for a trigger without an offset in map")
                        )
                    // check in what direction we are moving
                    if (desiredStartTime < it.startTime) {
                        // moving start time to left
                        // get the triggers line
                        val line =
                            sequence.findLineOf(it)
                                ?: throw IllegalStateException("Updating move for a trigger without a line in sequence")
                        // calculate by how much we should move left
                        val moveLeftTime =
                            (it.startTime - desiredStartTime)
                                // limit to space on the left
                                .coerceAtMost(line.getFreeDurationUntil(it.startTime, true))
                        // update the triggers start time
                        if (moveLeftTime > 0) {
                            it.startTime -= moveLeftTime
                            anyAffected = true
                        }
                    } else if (desiredStartTime > it.startTime) {
                        // moving start time to the right
                        // get the triggers line
                        val line =
                            sequence.findLineOf(it)
                                ?: throw IllegalStateException("Updating move for a trigger without a line in sequence")
                        // calculate by how much we should move right
                        val moveRightTime =
                            (desiredStartTime - it.startTime)
                                // limit to space on the right
                                .coerceAtMost(line.getFreeDurationFrom(it.endTime, true))
                                // prevent moving start past sequence end
                                .coerceAtMost(sequence.duration - it.startTime)
                        // update the triggers start time
                        if (moveRightTime > 0) {
                            it.startTime += moveRightTime
                            anyAffected = true
                        }
                    }
                }
            }
            TriggerPart.End -> {
                // try moving all selected trigger's end time to pointer time + their offset
                alreadySelectedTriggers.forEach {
                    // calculate the triggers desired end time
                    val desiredEndTime =
                        pointerTime + (
                            moveOffsets[it]
                                ?: throw IllegalStateException("Updating move for a trigger without an offset in map")
                        )
                    // check in what direction we are moving
                    if (desiredEndTime > it.endTime) {
                        // extending end time to the right
                        // get the triggers line
                        val line =
                            sequence.findLineOf(it)
                                ?: throw IllegalStateException("Updating move for a trigger without a line in sequence")
                        // calculate by how much we should move end time to the right
                        val extendTime =
                            (desiredEndTime - it.endTime)
                                .coerceAtMost(line.getFreeDurationFrom(it.endTime))
                        // update the triggers end time
                        if (extendTime > 0) {
                            it.duration += extendTime
                            anyAffected = true
                        }
                    } else if (desiredEndTime < it.endTime) {
                        // reducing end time to the left
                        // calculate by how much we should reduce the end time to the left
                        val reduceTime =
                            (it.endTime - desiredEndTime)
                                // trigger should not become shorter than min duration
                                .coerceAtMost(it.duration - AbstractPlacedTrigger.MINIMUM_TRIGGER_DURATION)
                        // update triggers end time
                        if (reduceTime > 0) {
                            it.duration -= reduceTime
                            anyAffected = true
                        }
                    }
                }
            }
        }
        // redraw if any triggers changed, so these changes are visible
        if (anyAffected) {
            redrawTimeline()
        }
    }

    fun endMove(e: MouseEvent) {
        // update a last time
        updateMove(e)
        // end moving
        moving = false
        // also update trigger hovered because move could have stopped on a trigger
        updateTriggerHover(e, false)
        // update cursor
        updateCursor()
    }

    // Keyframe Movement

    private const val KEYFRAME_MIN_DISTANCE_SECONDS = 0.05 // 50ms
    val isMovingKeyframe: Boolean
        get() = currentlyMovedKeyframe != null
    private var currentlyMovedKeyframe: Keyframes.Keyframe? = null
    private var currentlyMovedKeyframeParent: AbstractPlacedIntensityTrigger? = null
    private var currentlyMovedKeyframeMoveMinTime = 0.0
    private var currentlyMovedKeyframeMoveMaxTime = 0.0
    private var currentlyMovedKeyframeLineYOffset = 0
    private var currentlyMovedKeyframeLineHeight = 0

    fun startMovingKeyframe(
        event: MouseEvent,
        keyframe: Keyframes.Keyframe,
        parentTrigger: AbstractPlacedIntensityTrigger,
        parentTriggerLineIndex: Int
    ) {
        // set currently moved keyframe and parent
        currentlyMovedKeyframe = keyframe
        currentlyMovedKeyframeParent = parentTrigger
        // lookup line y and height
        currentlyMovedKeyframeLineYOffset = TimelineSequenceRenderer.getSequenceLineFromY(parentTriggerLineIndex)
        currentlyMovedKeyframeLineHeight = TimelineSequenceRenderer.getSequenceLineHeight(parentTriggerLineIndex)
        // calculate min and max time this trigger may be moved to
        when(val keyframeIndex = parentTrigger.keyframes().keyframesList.indexOf(keyframe)) {
            0 -> {
                currentlyMovedKeyframeMoveMinTime = parentTrigger.startTime
                currentlyMovedKeyframeMoveMaxTime = parentTrigger.startTime
            }
            parentTrigger.keyframes().keyframesList.size - 1 -> {
                currentlyMovedKeyframeMoveMinTime = parentTrigger.endTime
                currentlyMovedKeyframeMoveMaxTime = parentTrigger.endTime
            }
            else -> {
                // get previous and next keyframe
                val previousKeyframe = parentTrigger.keyframes().keyframesList[keyframeIndex - 1]
                val nextKeyframe = parentTrigger.keyframes().keyframesList[keyframeIndex + 1]
                // calculate min and max time this keyframe may be moved to
                currentlyMovedKeyframeMoveMinTime = previousKeyframe.absoluteSecondPosition(parentTrigger) + KEYFRAME_MIN_DISTANCE_SECONDS
                currentlyMovedKeyframeMoveMaxTime = nextKeyframe.absoluteSecondPosition(parentTrigger) - KEYFRAME_MIN_DISTANCE_SECONDS
                // set keyframe min and max time to current keyframe time position if min is not smaller than max
                if(currentlyMovedKeyframeMoveMinTime >= currentlyMovedKeyframeMoveMaxTime) {
                    currentlyMovedKeyframeMoveMinTime = keyframe.absoluteSecondPosition(parentTrigger)
                    currentlyMovedKeyframeMoveMaxTime = currentlyMovedKeyframeMoveMinTime
                }
            }
        }
        // update cursor
        updateCursor()
        // do first update
        updateKeyframeMove(event)
    }

    private fun updateKeyframeMove(e: MouseEvent) {
        val keyframe = currentlyMovedKeyframe ?: return
        val keyframeParent = currentlyMovedKeyframeParent ?: return
        // get cursor time
        val cursorTime = xToTime(e.x).coerceIn(currentlyMovedKeyframeMoveMinTime, currentlyMovedKeyframeMoveMaxTime)
        // calculate new position and set it
        keyframe.position = Keyframes.Keyframe.fromAbsoluteSecondPositionToProportion(cursorTime, keyframeParent)
        // calculate new keyframe value and set it
        keyframe.value = (1.0 - (e.y - currentlyMovedKeyframeLineYOffset).toDouble() / currentlyMovedKeyframeLineHeight).coerceIn(0.0, 1.0)
        // redraw timeline to show keyframe move
        redrawTimeline()
    }

    fun endKeyframeMove(e: MouseEvent) {
        updateKeyframeMove(e)
        // end moving keyframe
        currentlyMovedKeyframe = null
        currentlyMovedKeyframeParent = null
        // update cursor
        updateCursor()
    }

}
