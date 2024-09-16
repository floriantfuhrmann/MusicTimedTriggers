package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun TriggerBrowser() {
    Column(
        Modifier.trackActivation()
            .background(JewelTheme.globalColors.paneBackground)
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
        if(ProjectManager.currentProject != null) {
            Row {
                BrowserTabsBar()
            }
            Row {
                TriggerTemplatesList()
            }
        } else {
            Box(modifier = Modifier.padding(5.dp)) {
                Text(
                    color = Color.Gray,
                    text = "The Trigger Browser will be here, when you open a Project"
                )
            }
        }
    }
}
