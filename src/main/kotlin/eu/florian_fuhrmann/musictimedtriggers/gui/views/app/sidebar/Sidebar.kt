package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.sidebar

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import eu.florian_fuhrmann.musictimedtriggers.project.Project

@Composable
fun Sidebar(project: Project) {
    Column {
        // Toolbar
        SidebarToolbar()
        // Song List
        Row {
            SongList(project)
        }
    }
}
