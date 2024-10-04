package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.manager

import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Alert
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.renamesequenceline.RenameSequenceLineDialog
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.redrawTimeline
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineSequenceRenderer.getSequenceLineAt
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedIntensityTrigger
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.SwingUtilities

object RightClickMenuManager {

    val mouseListener: MouseListener = object : MouseListener {
        override fun mouseClicked(e: MouseEvent?) {
            // return if event is null
            if (e == null) {
                return
            }
            // return if not right mouse button
            if (!SwingUtilities.isRightMouseButton(e)) {
                return
            }
            //get current sequence
            val sequence = ProjectManager.currentProject?.currentSong?.sequence ?: return
            //get SequenceLine at clicked pointer position
            val line = getSequenceLineAt(e.y)
            if(line != null) {
                //init menu
                val menu = JPopupMenu()
                //get clicked trigger / keyframe
                val triggerAtResult = MoveTriggersFunct.getTriggerAt(e.x, e.y)
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
                    if(!MoveTriggersFunct.isSelected(clickedTrigger)) {
                        MoveTriggersFunct.selectTrigger(clickedTrigger, e.isShiftDown)
                    }
                    //Edit Option
                    menu.add(JMenuItem("Edit").apply {
                        addActionListener {
                            //open edit dialog for the clicked trigger
                            clickedTrigger.openEditDialog()
                        }
                    })
                    //Delete Option
                    menu.add(JMenuItem("Delete").apply {
                        addActionListener {
                            MoveTriggersFunct.deleteSelectedTriggers()
                        }
                    })
                } else {
                    //Show menu for sequence line
                    menu.add(JMenuItem("Rename").apply {
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
                            DialogManager.alert(Alert(
                                title = "Delete ${line.name}?",
                                text = "Are you sure you want to delete Sequence Line ${line.name} containing ${line.getTriggersCount()} placed Triggers?",
                                onDismiss = {},
                                dismissText = "Cancel",
                                onConfirm = {
                                    sequence.removeLine(lineIndex)
                                }
                            ))
                        }
                    })
                }
                //show menu
                menu.show(e.component, e.x, e.y)
            }
        }
        override fun mousePressed(e: MouseEvent?) {}
        override fun mouseReleased(e: MouseEvent?) {}
        override fun mouseEntered(e: MouseEvent?) {}
        override fun mouseExited(e: MouseEvent?) {}
    }

}