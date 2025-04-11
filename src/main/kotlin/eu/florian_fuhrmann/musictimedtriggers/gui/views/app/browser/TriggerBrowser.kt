package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.foundation.theme.JewelTheme

@Composable
fun TriggerBrowser(project: Project) {
    Column(
        Modifier.trackActivation()
            .background(JewelTheme.globalColors.panelBackground)
            .fillMaxSize().onKeyEvent {
                //Copy and Paste should work even if focus is not in the list itself, but on other parts of the browser
                if(it.type != KeyEventType.KeyUp) return@onKeyEvent false
                if(it.key == Key.C && (it.isCtrlPressed || it.isMetaPressed)) {
                    ProjectManager.currentProject?.browserState?.copy()
                    return@onKeyEvent true
                } else if(it.key == Key.V && (it.isCtrlPressed || it.isMetaPressed)) {
                    ProjectManager.currentProject?.browserState?.paste()
                    return@onKeyEvent true
                }
                return@onKeyEvent false
            }
    ) {
        Row {
            BrowserTabsBar(project)
        }
        Row {
            TriggerTemplatesList()
        }
    }
}
