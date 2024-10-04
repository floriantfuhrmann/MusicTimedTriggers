package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.manager

import androidx.compose.ui.focus.FocusManager
import java.awt.event.*

object TimelineFocusManager {
    var focusManager: FocusManager? = null
    var timelineFocused = false
        private set

    val mouseListener: MouseListener =
        object : MouseListener {
            override fun mouseClicked(e: MouseEvent?) {}

            override fun mousePressed(e: MouseEvent?) {
                // clicked on timeline, so it should gain focus
                timelineGainedFocus()
            }

            override fun mouseReleased(e: MouseEvent?) {}

            override fun mouseEntered(e: MouseEvent?) {}

            override fun mouseExited(e: MouseEvent?) {}
        }

    fun timelineGainedFocus() {
        // because Composes' Focus System does not recognize SwiftPanels being focused, we manually clear focus when Timeline should gain focus
        focusManager?.clearFocus()
        timelineFocused = true
    }

    fun timelineLostFocus() {
        timelineFocused = false
    }
}
