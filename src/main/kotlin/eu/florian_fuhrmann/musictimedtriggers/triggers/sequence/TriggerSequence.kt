package eu.florian_fuhrmann.musictimedtriggers.triggers.sequence

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.redrawTimeline
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedTrigger
import eu.florian_fuhrmann.musictimedtriggers.utils.gson.GSON_PRETTY
import java.io.File
import java.util.*

/**
 * A Sequence consists of multiple SequenceLines which contain triggers.
 */
class TriggerSequence(
    val uuid: UUID,
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
        //save sequence info
        saveSequenceInfoToFile()
    }

    fun newLine(insertIndex: Int) {
        //create new line
        val newLine = TriggerSequenceLine.createTriggerSequenceLine(this, "New Line")
        //insert new line
        lines.add(insertIndex, newLine)
        //redraw timeline
        redrawTimeline()
        //save sequence info and newly created line
        newLine.saveToFile()
        saveSequenceInfoToFile()
    }

    private var lastLineRemovedCounter = 0
    fun removeLine(lineIndex: Int) {
        //remove from lines list
        lines.removeAt(lineIndex)
        //ensure there is always at least 1 line
        var replacementLine: TriggerSequenceLine? = null
        if(lines.isEmpty()) {
            //create a replacement line
            replacementLine = if(lastLineRemovedCounter++ >= 10) {
                lastLineRemovedCounter = 0
                TriggerSequenceLine.createTriggerSequenceLine(this, "Please stop deleting the last Line")
            } else {
                TriggerSequenceLine.createTriggerSequenceLine(this, "Very last Line")
            }
            //add replacement line to lines
            lines.add(replacementLine)
        }
        //redraw timeline
        redrawTimeline()
        //save updated sequence info
        saveSequenceInfoToFile()
        replacementLine?.saveToFile()
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

    // Saving and Loading

    /**
     * Get the directory for this specific trigger sequence in the trigger
     * sequences directory.
     */
    fun getSequenceDirectory() = getSequenceDirectory(project.projectDirectory, uuid)

    /**
     * Create the directory for this trigger sequence inside the general
     * trigger sequences directory. Function intended to be used during
     * sequence creation.
     */
    private fun createSequenceDirectory() = getSequenceDirectory(project.projectDirectory, uuid).mkdir()

    /**
     * Save the sequence info and all its lines to files.
     *
     * @param createDirectory Whether also to create the sequence directory
     *    first. (default: false)
     */
    fun saveAll(createDirectory: Boolean = false) {
        //first create the sequence directory
        if(createDirectory) {
            createSequenceDirectory()
        }
        //save sequence info
        saveSequenceInfoToFile()
        //save all lines
        lines.forEach { it.saveToFile() }
    }

    /**
     * Save the sequence info to a json file inside the sequence directory.
     */
    private fun saveSequenceInfoToFile() {
        //create json
        val json = JsonObject()
        //add duration
        json.addProperty("duration", duration)
        //add list of sequence line uuids
        val linesJsonArray = JsonArray()
        lines.forEach { linesJsonArray.add(it.uuid.toString()) }
        json.add("lines", linesJsonArray)
        //save to file
        val file = getSequenceInfoFile(project.projectDirectory, uuid)
        file.writeText(text = GSON_PRETTY.toJson(json), charset = Charsets.UTF_8)
    }

    companion object {
        /**
         * Create a new trigger sequence with default lines.
         * @warning Does not save the sequence to file.
         */
        fun createSequence(project: Project, duration: Double): TriggerSequence {
            //create instance
            val sequence = TriggerSequence(UUID.randomUUID(), project, duration, mutableListOf())
            //add initial lines
            for (i in 1..4) {
                sequence.lines.add(TriggerSequenceLine.createTriggerSequenceLine(sequence, "$i. Line"))
            }
            //return
            return sequence
        }

        /**
         * The name of the directory containing the directories for trigger
         * sequences. (Which in turn contain the sequence.json file and sequence
         * line files)
         */
        private const val TRIGGER_SEQUENCES_DIRECTORY_NAME = "TriggerSequences"
        /**
         * The name of the file containing the sequence info (like name, etc.) for a trigger sequence.
         */
        private const val TRIGGER_SEQUENCE_INFO_FILE_NAME = "sequence_info.json"

        /**
         * Get the directory for a specific trigger sequence in the trigger
         * sequences directory.
         */
        fun getSequenceDirectory(projectDirectory: File, sequenceUuid: UUID) =
            File(projectDirectory, TRIGGER_SEQUENCES_DIRECTORY_NAME + File.separator + sequenceUuid)

        /**
         * Get the file for the sequence info of a trigger sequence in the trigger
         * sequences directory.
         */
        private fun getSequenceInfoFile(projectDirectory: File, sequenceUuid: UUID) =
            File(getSequenceDirectory(projectDirectory, sequenceUuid), TRIGGER_SEQUENCE_INFO_FILE_NAME)

        /**
         * Create the trigger sequences directory inside the project directory.
         * Function intended to be used during project creation.
         */
        fun createTriggerSequencesDirectory(projectDirectory: File) {
            File(projectDirectory, TRIGGER_SEQUENCES_DIRECTORY_NAME).mkdir()
        }

        fun loadFromFiles(project: Project, sequenceUuid: UUID): TriggerSequence {
            val sequenceDirectory = getSequenceDirectory(project.projectDirectory, sequenceUuid)
            require(sequenceDirectory.exists()) { "Sequence directory does not exist." }
            val sequenceInfoFile = getSequenceInfoFile(project.projectDirectory, sequenceUuid)
            require(sequenceInfoFile.exists()) { "Sequence info file does not exist." }
            //load json from file
            val sequenceInfoJson = JsonParser.parseString(sequenceInfoFile.readText(Charsets.UTF_8)).asJsonObject
            //get line uuids from json
            val lineUuids = sequenceInfoJson.get("lines").asJsonArray.map { UUID.fromString(it.asString) }
            //create instance
            val duration: Double = sequenceInfoJson.get("duration").asDouble
            val sequence = TriggerSequence(sequenceUuid, project, duration, mutableListOf())
            //load lines
            val lines = lineUuids.map { TriggerSequenceLine.loadFromFile(sequence, it) }.toMutableList()
            //add lines to sequence
            sequence.lines.addAll(lines)
            //return
            return sequence
        }
    }

}