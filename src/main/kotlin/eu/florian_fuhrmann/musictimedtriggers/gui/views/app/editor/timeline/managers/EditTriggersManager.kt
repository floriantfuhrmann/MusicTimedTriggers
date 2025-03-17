package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.managers

import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.alerts.Alert
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.redrawTimeline
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.sequence.TriggerSequenceLine

object EditTriggersManager {

    fun deleteSelectedTriggers() {
        // ensure there are any triggers selected
        if (TriggerSelectionManager.selectedTriggers.isEmpty()) return
        // get sequence
        val sequence = ProjectManager.currentProject?.currentSong?.sequence ?: return
        if (TriggerSelectionManager.selectedTriggers.size == 1) {
            // delete the one selected trigger
            val firstSelectedTrigger = TriggerSelectionManager.selectedTriggers.first()
            val line = sequence.findLineOf(firstSelectedTrigger)
            require(line != null) {"Failed to find the selected triggers line"}
            line.removeTrigger(firstSelectedTrigger)
            //make sure the trigger is no longer selected
            TriggerSelectionManager.deselectAllTriggers(false)
            //redraw timeline so change becomes visible
            redrawTimeline()
            //also save the affected line
            line.saveToFile()
        } else {
            // Confirm Deletion first
            DialogManager.alert(
                Alert(
                    title = "Delete ${TriggerSelectionManager.selectedTriggers.size} placed Triggers?",
                    text = "Are you sure you want to delete ${TriggerSelectionManager.selectedTriggers.size} placed Triggers?",
                    onDismiss = {},
                    dismissText = "Cancel",
                    onConfirm = {
                        //init set containing affected lines
                        val affectedLines = mutableSetOf<TriggerSequenceLine>()
                        //remove selected triggers
                        TriggerSelectionManager.selectedTriggers.forEach {
                            //find line, remove trigger and add line to affected lines
                            val line = sequence.findLineOf(it)!!
                            line.removeTrigger(it)
                            affectedLines.add(line)
                        }
                        //make sure the triggers are no longer selected
                        TriggerSelectionManager.deselectAllTriggers(false)
                        //redraw timeline so change becomes visible
                        redrawTimeline()
                        //also save the affected lines
                        affectedLines.forEach { it.saveToFile() }
                    }
                )
            )
        }
    }

    fun editSelectedTrigger() {
        if (TriggerSelectionManager.selectedTriggers.size == 1) {
            TriggerSelectionManager.selectedTriggers.first().openEditDialog()
        }
    }

}