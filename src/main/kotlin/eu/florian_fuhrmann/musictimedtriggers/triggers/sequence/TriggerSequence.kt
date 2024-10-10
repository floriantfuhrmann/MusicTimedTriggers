package eu.florian_fuhrmann.musictimedtriggers.triggers.sequence

import com.google.gson.JsonObject
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.redrawTimeline
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedTrigger

/**
 * A Sequence consists of multiple SequenceLines which contain triggers.
 */
class TriggerSequence(
    val project: Project,
    var duration: Double,
    val lines: MutableList<TriggerSequenceLine>
) {

    fun tick(timePosition: Double) {
        //tick all lines
        lines.forEach { line -> line.tick(timePosition) }
    }

    fun moveLine(fromIndex: Int, toIndex: Int) {
        //move in lines list
        lines.add(toIndex, lines.removeAt(fromIndex))
        //redraw timeline
        redrawTimeline()
        //save project
        // TODO
    }

    fun newLine(insertIndex: Int) {
        //insert new line
        lines.add(insertIndex, TriggerSequenceLine.createTriggerSequenceLine(this, "New Line"))
        //redraw timeline
        redrawTimeline()
        //save project
        // TODO
    }

    private var lastLineRemovedCounter = 0
    fun removeLine(lineIndex: Int) {
        //remove from lines list
        lines.removeAt(lineIndex)
        //ensure there is always at least 1 line
        if(lines.isEmpty()) {
            if(lastLineRemovedCounter++ >= 10) {
                lastLineRemovedCounter = 0
                lines.add(TriggerSequenceLine.createTriggerSequenceLine(this, "Please stop deleting the last Line"))
            } else {
                lines.add(TriggerSequenceLine.createTriggerSequenceLine(this, "Very last Line"))
            }
        }
        //redraw timeline
        redrawTimeline()
        //save project
        // TODO
    }

    fun findLineOf(trigger: AbstractPlacedTrigger): TriggerSequenceLine? {
        //checks for every line if the trigger at the searched trigger's start time is the searched trigger
        return lines.find {
            it.getTriggerAt(trigger.startTime) == trigger
        }
    }

    fun findLineIndexOf(trigger: AbstractPlacedTrigger): Int? {
        lines.forEachIndexed { index, line ->
            if(line.getTriggerAt(trigger.startTime) == trigger) return index
        }
        return null
    }

    companion object {
        fun createSequence(project: Project, duration: Double): TriggerSequence {
            //create instance
            val sequence = TriggerSequence(project, duration, mutableListOf())
            //add initial lines
            for (i in 1..4) {
                sequence.lines.add(TriggerSequenceLine.createTriggerSequenceLine(sequence, "$i. Line"))
            }
            //return
            return sequence
        }

        fun fromJson(project: Project, json: JsonObject): TriggerSequence {
            //get duration
            val duration: Double = json.get("duration").asDouble
            //create instance
            val sequence = TriggerSequence(project, duration, mutableListOf())
            //add lines
            json.get("lines").asJsonArray.forEach {
                sequence.lines.add(TriggerSequenceLine.fromJson(sequence, it.asJsonObject))
            }
            //return
            return sequence
        }
    }

}