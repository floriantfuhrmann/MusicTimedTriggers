package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.main

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.overlay.Overlay
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager

@Composable
@Preview
fun App() {
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
    DialogManager.DialogContainer()
}

