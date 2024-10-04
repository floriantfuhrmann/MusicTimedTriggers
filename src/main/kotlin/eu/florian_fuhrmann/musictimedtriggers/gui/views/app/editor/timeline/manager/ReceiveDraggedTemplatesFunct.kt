package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.manager

import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.getPointerPointOnPanel
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.redrawTimeline
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.RenderUtils
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineBackgroundRenderer
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineBackgroundRenderer.durationToWidth
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineBackgroundRenderer.timeToX
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineSequenceRenderer
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedTrigger
import eu.florian_fuhrmann.musictimedtriggers.triggers.utils.intensity.Keyframes
import java.awt.Color
import java.awt.Graphics2D
import java.awt.event.MouseEvent
import java.awt.event.MouseListener

/*
this object should handle the functionality of receiving templates dragged from the templates browser
 */
object ReceiveDraggedTemplatesFunct {

    var insertPossible = true

    val mouseListener: MouseListener = object : MouseListener {
        override fun mouseClicked(e: MouseEvent?) {}
        override fun mousePressed(e: MouseEvent?) {}
        override fun mouseReleased(e: MouseEvent?) {}
        override fun mouseEntered(e: MouseEvent?) {
            ProjectManager.currentProject?.browserState?.handleTimelineEnter()
        }
        override fun mouseExited(e: MouseEvent?) {
            ProjectManager.currentProject?.browserState?.handleTimelineExit()
        }
    }

    fun drawDragIndicator(
        g: Graphics2D,
        width: Int, //total width of the content drawn
        height: Int //total height of the content drawn
    ) {
        //only draw when dragging on timeline
        if(ProjectManager.currentProject?.browserState?.draggingOnTimeline?.value != true) return
        //get pointer point
        val p = getPointerPointOnPanel()
        //get sequence
        val sequence = ProjectManager.currentProject?.currentSong?.sequence ?: return
        //get time at pointer point
        val time = TimelineBackgroundRenderer.xToTime(p.x)
        //init insertPossible with time check
        insertPossible = time >= 0 && time < sequence.duration
        //if insert possible try drawing ghost triggers
        if(insertPossible) {
            //get index of sequence line at pointer point
            val hoveredLineIndex = TimelineSequenceRenderer.getSequenceLineIndexAt(p.y)
            //only continue if there is a line at pointer
            if(hoveredLineIndex != null) {
                //try drawing a ghost for every template dragged
                ProjectManager.currentProject?.browserState?.selectedTemplates?.forEachIndexed { index, template ->
                    //get sequence line
                    val line = sequence.lines.getOrNull(hoveredLineIndex + index)
                    //only continue if we have a line
                    if(line != null) {
                        //adjust time so it is at the end of the trigger already at time (only if ghost will still be visible)
                        val triggerAtTime = line.getTriggerAt(time)
                        val adjustedLineTime = if(triggerAtTime != null) {
                            val xOfTriggerAt = timeToX(triggerAtTime.endTime)
                            //end time of the previous trigger can only be the adjusted time if the ghost trigger will still be clearly visible
                            if(xOfTriggerAt < width - durationToWidth(AbstractPlacedTrigger.MINIMUM_TRIGGER_DURATION)) {
                                //also the time still has to be in sequence period
                                if(triggerAtTime.endTime < sequence.duration) {
                                    triggerAtTime.endTime
                                } else {
                                    time
                                }
                            } else {
                                time
                            }
                        } else {
                            time
                        }
                        //get longest duration the new trigger could have at time in this line
                        var ghostTriggerDuration = line.getFreeDurationFrom(adjustedLineTime, true).coerceAtMost(AbstractPlacedTrigger.DEFAULT_TRIGGER_DURATION)
                        var ghostPossible = true
                        //make sure the trigger can be at least min MINIMUM_TRIGGER_DURATION seconds long
                        if(ghostTriggerDuration <= AbstractPlacedTrigger.MINIMUM_TRIGGER_DURATION) {
                            //if not than make it MINIMUM_TRIGGER_DURATION seconds long, so the conflict is visible and mark insert as not possible
                            insertPossible = false
                            ghostPossible = false
                            ghostTriggerDuration = AbstractPlacedTrigger.MINIMUM_TRIGGER_DURATION
                        }
                        //render ghost trigger
                        val lineFromY = TimelineSequenceRenderer.getSequenceLineFromY(hoveredLineIndex + index)
                        val lineHeight = TimelineSequenceRenderer.getSequenceLineHeight(hoveredLineIndex + index)
                        val ghostWidth = durationToWidth(ghostTriggerDuration)
                        TimelineSequenceRenderer.drawTrigger(
                            g,
                            timeToX(adjustedLineTime),
                            lineFromY,
                            ghostWidth, // TimelineBackgroundRenderer.timeToX(time + ghostTriggerDuration) - p.x
                            lineHeight,
                            template.getTriggerTemplate().configuration.color,
                            template.name.value,
                            style = if(ghostPossible) {
                                TimelineSequenceRenderer.TriggerStateStyle.GhostValid
                            } else {
                                TimelineSequenceRenderer.TriggerStateStyle.GhostInvalid
                            },
                            keyframes = if(template.getTriggerTemplate().getType().isIntensity) {
                                Keyframes.DUMMY_KEYFRAMES
                            } else {
                                null
                            }
                        )
                        //render conflict indicator
                        if(adjustedLineTime + ghostTriggerDuration >= sequence.duration) {
                            val sequenceEndX = timeToX(sequence.duration)
                            val conflictWidth = p.x + ghostWidth - sequenceEndX
                            g.color = Color(255, 0, 0, 128)
                            g.fillRect(sequenceEndX, lineFromY, conflictWidth, lineHeight)
                            g.color = Color.red
                            RenderUtils.drawStringVerticallyCentered(g, sequenceEndX + conflictWidth + 5, lineFromY, lineHeight, "Trigger ends after Sequence end!")
                        }
                    } else {
                        //otherwise insert is no longer possible
                        insertPossible = false
                    }
                }
            } else {
                //otherwise insert is no longer possible
                insertPossible = false
            }
        }
        //draw vertical line with color representing if insert is possible
        g.color = if(insertPossible) { Color.green } else { Color.red }
        //g.fillRect(p.x - 5, p.y - 5, 10, 10)
        g.fillRect(p.x - 1, 0, 2, height)
    }

    /**
     * Receives the dragged templates by placing them in the songs Sequence
     */
    fun receive() {
        //only proceed when insert was possible
        if(!insertPossible) {
            //redraw timeline then abort
            redrawTimeline()
            return
        }
        //get pointer point
        val p = getPointerPointOnPanel()
        //get sequence
        val sequence = ProjectManager.currentProject?.currentSong?.sequence ?: return
        //get time at pointer point
        val time = TimelineBackgroundRenderer.xToTime(p.x)
        //make sure time is inbounds
        if(time < 0 || time >= sequence.duration) return
        //get index of sequence line at pointer point
        var lineIndex = TimelineSequenceRenderer.getSequenceLineIndexAt(p.y) ?: return
        ProjectManager.currentProject?.browserState?.selectedTemplates?.forEach {
            //get line
            val line = sequence.lines[lineIndex]
            //adjust time if there is a conflict at time itself
            val triggerAt = line.getTriggerAt(time)
            val adjustedTime = triggerAt?.endTime ?: time
            val duration = line.getFreeDurationFrom(adjustedTime, true)
                .coerceAtMost(AbstractPlacedTrigger.DEFAULT_TRIGGER_DURATION)
            line.addTrigger(it.getTriggerTemplate().createPlaced(adjustedTime, duration))
            lineIndex++
        }
        //also redraw so drag indicator disappears
        redrawTimeline()
    }

}