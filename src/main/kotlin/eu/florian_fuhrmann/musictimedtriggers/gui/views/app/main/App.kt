package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.main

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import eu.florian_fuhrmann.musictimedtriggers.gui.alerts.AlertsManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.overlay.Overlay
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager

@Composable
@Preview
fun App(frameWindowScope: FrameWindowScope) {
    // Overlay
    Overlay()

    // get project
    val project = ProjectManager.currentProject

    // Main Window Content
    if(project != null) {
        OpenedProjectView(project)
    } else {
        NoProjectView()
    }

    // Dialog Container
    DialogManager.DialogContainer(frameWindowScope)
    // Alerts Container
    if(!DialogManager.anyDialogOpened) {
        AlertsManager.AlertsContainer(frameWindowScope)
    }
}

