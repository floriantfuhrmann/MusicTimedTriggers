package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.funct

import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Alert
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.renamesequenceline.RenameSequenceLineDialog
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineSequenceRenderer.getSequenceLineAt
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.SwingUtilities

object RightClickMenuFunct {

    val mouseListener: MouseListener = object : MouseListener {
        override fun mouseClicked(e: MouseEvent?) {
            if(e == null) return
            if (SwingUtilities.isRightMouseButton(e)) {
                //get sequence
                val sequence = ProjectManager.currentProject?.currentSong?.sequence ?: return
                //get SequenceLine at clicked pointer position
                val line = getSequenceLineAt(e.y)
                if(line != null) {
                    //init menu
                    val menu = JPopupMenu()
                    //get clicked trigger
                    val clickedTrigger = MoveTriggersFunct.getClickedTriggerAt(e.x, e.y)
                    if(clickedTrigger != null) {
                        //make sure the clicked trigger is selected
                        if(!MoveTriggersFunct.isSelected(clickedTrigger.trigger)) {
                            MoveTriggersFunct.selectTrigger(clickedTrigger.trigger, e.isShiftDown)
                        }
                        //Edit Option
                        menu.add(JMenuItem("Edit").apply {
                            addActionListener {
                                //open edit dialog for the clicked trigger
                                clickedTrigger.trigger.openEditDialog()
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
        }
        override fun mousePressed(e: MouseEvent?) {}
        override fun mouseReleased(e: MouseEvent?) {}
        override fun mouseEntered(e: MouseEvent?) {}
        override fun mouseExited(e: MouseEvent?) {}
    }

}