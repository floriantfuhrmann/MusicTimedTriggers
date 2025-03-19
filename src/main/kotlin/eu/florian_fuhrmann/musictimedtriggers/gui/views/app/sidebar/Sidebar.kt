package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import org.jetbrains.jewel.foundation.theme.JewelTheme

@Composable
fun Sidebar(project: Project) {
    Column {
        // Toolbar
        SidebarToolbar()
        // Song List
        Row {
            Box(
                modifier = Modifier
                    .background(JewelTheme.globalColors.borders.normal)
                    .padding(top = 1.dp)
            ) {
                SongList(project)
            }
        }
    }
}
