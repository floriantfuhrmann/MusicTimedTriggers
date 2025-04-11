package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.managers

import eu.florian_fuhrmann.musictimedtriggers.gui.alerts.BasicAlert
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.renamesequenceline.RenameSequenceLineDialog
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.redrawTimeline
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineSequenceRenderer.getSequenceLineAt
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedIntensityTrigger
import org.jetbrains.jewel.ui.component.Text
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.SwingUtilities

object RightClickMenuManager {

    val mouseListener: MouseListener = object : MouseListener {
        override fun mouseClicked(e: MouseEvent?) {
            // return if event is null
            if (e == null) return
            // return if not right mouse button
            if (!SwingUtilities.isRightMouseButton(e)) return
            //get current sequence
            val sequence = ProjectManager.currentProject?.currentSong?.sequence ?: return
            //get SequenceLine at clicked pointer position or return if none
            val line = getSequenceLineAt(e.y) ?: return
            //init menu
            val menu = JPopupMenu()
            //get clicked trigger / keyframe
            val triggerAtResult = MoveTriggersManager.getTriggerAt(e.x, e.y)
            val clickedTrigger = triggerAtResult?.trigger
            //check whether keyframe was clicked
            if(clickedTrigger != null && clickedTrigger is AbstractPlacedIntensityTrigger && triggerAtResult.keyframe != null) {
                //get clicked keyframe and index
                val clickedKeyframe = triggerAtResult.keyframe
                val clickedKeyframeIndex = clickedTrigger.keyframes().findIndex(clickedKeyframe)
                //Insert Left Option
                menu.add(JMenuItem("Insert Left").apply {
                    //only enable if keyframe can be inserted at index
                    isEnabled = clickedTrigger.keyframes().canInsertAt(clickedKeyframeIndex, clickedTrigger.duration)
                    addActionListener {
                        // insert new keyframe and redraw timeline, so the new keyframe is shown
                        clickedTrigger.keyframes().insertNewAtIndex(clickedKeyframeIndex)
                        redrawTimeline()
                    }
                })
                //Insert Right Option
                menu.add(JMenuItem("Insert Right").apply {
                    //only enable if keyframe can be inserted at index
                    isEnabled = clickedTrigger.keyframes().canInsertAt(clickedKeyframeIndex + 1, clickedTrigger.duration)
                    addActionListener {
                        // insert new keyframe and redraw timeline, so the new keyframe is shown
                        clickedTrigger.keyframes().insertNewAtIndex(clickedKeyframeIndex + 1)
                        redrawTimeline()
                    }
                })
                //Delete Option
                menu.add(JMenuItem("Delete").apply {
                    //only enable when not first or last index
                    isEnabled = clickedTrigger.keyframes().canRemoveAt(clickedKeyframeIndex)
                    addActionListener {
                        //delete keyframe and redraw timeline
                        clickedTrigger.keyframes().removeAtIndex(clickedKeyframeIndex)
                        redrawTimeline()
                    }
                })
            } else if(clickedTrigger != null) {
                //make sure the clicked trigger is selected
                if(!TriggerSelectionManager.isSelected(clickedTrigger)) {
                    TriggerSelectionManager.selectTrigger(clickedTrigger, e.isShiftDown)
                }
                //Delete Option
                menu.add(JMenuItem("Delete").apply {
                    addActionListener {
                        EditTriggersManager.deleteSelectedTriggers()
                    }
                })
            } else {
                //Show menu for sequence line
                menu.add(JMenuItem(if(line.name.isEmpty()) "Give Name" else "Rename").apply {
                    addActionListener {
                        DialogManager.openDialog(RenameSequenceLineDialog(line))
                    }
                })
                //get index of clicked line
                val lineIndex = sequence.lines.indexOf(line)
                menu.add(JMenuItem("Move Up").apply {
                    addActionListener {
                        sequence.moveLine(lineIndex, lineIndex - 1)
                    }
                    setEnabled(lineIndex > 0)
                })
                menu.add(JMenuItem("Move Down").apply {
                    addActionListener {
                        sequence.moveLine(lineIndex, lineIndex + 1)
                    }
                    setEnabled(lineIndex < sequence.lines.lastIndex)
                })
                menu.add(JMenuItem("Insert Above").apply {
                    addActionListener {
                        sequence.newLine(lineIndex)
                    }
                })
                menu.add(JMenuItem("Insert Bellow").apply {
                    addActionListener {
                        sequence.newLine(lineIndex + 1)
                    }
                })
                menu.add(JMenuItem("Delete").apply {
                    addActionListener {
                        BasicAlert(
                            type = BasicAlert.Type.Warning,
                            title = "Delete ${line.name}?",
                            buttons = {
                                CancelButton()
                                CancelButtonFocused()
                                OKButton(onClick = {
                                    close()
                                    sequence.removeLine(lineIndex)
                                }, label = "Confirm")
                            }
                        ) {
                            Text("Are you sure you want to delete Sequence Line ${line.name} containing ${line.getTriggersCount()} placed Triggers?")
                        }.show()
                    }
                })
            }
            //show menu
            menu.show(e.component, e.x, e.y)
        }
        override fun mousePressed(e: MouseEvent?) {}
        override fun mouseReleased(e: MouseEvent?) {}
        override fun mouseEntered(e: MouseEvent?) {}
        override fun mouseExited(e: MouseEvent?) {}
    }

}