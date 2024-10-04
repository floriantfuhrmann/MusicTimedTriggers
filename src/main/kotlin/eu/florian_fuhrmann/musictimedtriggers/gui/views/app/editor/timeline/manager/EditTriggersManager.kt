package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.manager

import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Alert
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.redrawTimeline
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager

object EditTriggersManager {

    fun deleteSelectedTriggers() {
        // ensure there are any triggers selected
        if (TriggerSelectionManager.selectedTriggers.isEmpty()) return
        // get sequence
        val sequence = ProjectManager.currentProject?.currentSong?.sequence ?: return
        if (TriggerSelectionManager.selectedTriggers.size == 1) {
            // delete the one selected trigger
            val first = TriggerSelectionManager.selectedTriggers.first()
            sequence.findLineOf(first)!!.removeTrigger(first)
            //redraw timeline so change becomes visible
            redrawTimeline()
        } else {
            // Confirm Deletion first
            DialogManager.alert(
                Alert(
                    title = "Delete ${TriggerSelectionManager.selectedTriggers.size} placed Triggers?",
                    text = "Are you sure you want to delete ${TriggerSelectionManager.selectedTriggers.size} placed Triggers?",
                    onDismiss = {},
                    dismissText = "Cancel",
                    onConfirm = {
                        TriggerSelectionManager.selectedTriggers.forEach {
                            sequence.findLineOf(it)!!.removeTrigger(it)
                        }
                        //redraw timeline so change becomes visible
                        redrawTimeline()
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