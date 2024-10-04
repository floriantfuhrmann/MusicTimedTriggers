package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.manager.*
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineRenderer
import eu.florian_fuhrmann.musictimedtriggers.gui.views.titlebar.titleBarDropdownOpened
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.player.currentAudioPlayer
import org.jetbrains.jewel.foundation.modifier.trackActivation
import java.awt.Cursor
import java.awt.Graphics
import java.awt.MouseInfo
import java.awt.Point
import javax.swing.JPanel
import javax.swing.SwingUtilities

private var panel: JPanel? = null

/**
 * Updates the Cursor icon by setting the cursor of the JPanel
 */
fun updateCursor() {
    if (DragTimePositionManager.draggingTimePosition) {
        setCursor(Cursor.MOVE_CURSOR)
    } else if (DragTimePositionManager.secondsGridHovered) {
        setCursor(Cursor.HAND_CURSOR)
    } else if (MoveTriggersManager.moving) {
        setCursor(
            when (MoveTriggersManager.movingPart) {
                MoveTriggersManager.TriggerPart.Start -> Cursor.W_RESIZE_CURSOR
                MoveTriggersManager.TriggerPart.Middle -> Cursor.MOVE_CURSOR
                MoveTriggersManager.TriggerPart.End -> Cursor.E_RESIZE_CURSOR
            },
        )
    } else if(MoveTriggersManager.isMovingKeyframe) {
        setCursor(Cursor.MOVE_CURSOR)
    } else if (currentAudioPlayer.value?.playing?.value == false && MoveTriggersManager.hoveredTriggerPart != null) {
        if(MoveTriggersManager.hoveredKeyframe != null) {
            setCursor(Cursor.HAND_CURSOR)
        } else {
            setCursor(
                when (MoveTriggersManager.hoveredTriggerPart) {
                    MoveTriggersManager.TriggerPart.Start -> Cursor.W_RESIZE_CURSOR
                    MoveTriggersManager.TriggerPart.End -> Cursor.E_RESIZE_CURSOR
                    else -> Cursor.HAND_CURSOR
                }
            )
        }
    } else {
        setCursor(Cursor.DEFAULT_CURSOR)
    }
}

private fun setCursor(cursor: Int) {
    panel?.cursor = Cursor.getPredefinedCursor(cursor)
}

/**
 * Returns the Point of the pointer on the JPanel. (0, 0) is at the top left of the JPanel.
 */
fun getPointerPointOnPanel(): Point {
    val p: Point = MouseInfo.getPointerInfo().location
    SwingUtilities.convertPointFromScreen(p, panel as JPanel)
    return p
}

/**
 * Redraws the Timeline
 */
fun redrawTimeline() {
    panel?.repaint()
}

@Composable
fun EditorTimeline() {
    // panel should only be visible when the dropdown is not opened and no alerts are shown (workaround because SwingPanel is drawn above everything else)
    val panelVisible by remember {
        derivedStateOf {
            !titleBarDropdownOpened.value && !DialogManager.anyAlerts.value
        }
    }
    // set focus manager, so it can be used for manual focus handling
    TimelineFocusManager.focusManager = LocalFocusManager.current
    // only show when the panel should be visible
    if (panelVisible) {
        SwingPanel(
            background = Color.Black,
            modifier = Modifier.fillMaxSize(),
            factory = {
                // create panel with overwritten paintComponent() function
                panel =
                    object : JPanel() {
                        override fun paintComponent(g: Graphics?) {
                            // draw start
                            val start = System.currentTimeMillis()
                            if (g != null) {
                                TimelineRenderer.render(g, this.width, this.height)
                            }
                            // draw finished
//                            println("Draw took ${System.currentTimeMillis() - start}ms")
                        }
                    }
                // add listeners
                (panel as JPanel).addMouseListener(DragTimePositionManager.mouseListener)
                (panel as JPanel).addMouseMotionListener(DragTimePositionManager.mouseMotionListener)
                (panel as JPanel).addMouseListener(ReceiveDraggedTemplatesManger.mouseListener)
                (panel as JPanel).addMouseListener(RightClickMenuManager.mouseListener)
                (panel as JPanel).addMouseListener(MoveTriggersManager.mouseListener)
                (panel as JPanel).addMouseMotionListener(MoveTriggersManager.mouseMotionListener)
                (panel as JPanel).addMouseListener(TriggerSelectionManager.mouseListener)
                (panel as JPanel).addMouseMotionListener(TriggerSelectionManager.mouseMotionListener)
                (panel as JPanel).addMouseListener(TimelineFocusManager.mouseListener)
                (panel as JPanel).addMouseWheelListener(DragTimePositionManager.mouseWheelListener)
                // return panel
                panel as JPanel
            }
        )
    } else {
        Box(
            modifier =
                Modifier
                    .background(Color.Black)
                    .fillMaxSize()
                    .trackActivation(),
        ) {}
    }
}
