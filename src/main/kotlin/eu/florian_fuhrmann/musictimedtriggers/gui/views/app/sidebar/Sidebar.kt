package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun Sidebar() {
    if (ProjectManager.currentProject != null) {
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
                    SongList()
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .padding(5.dp)
                .fillMaxSize()
        ) {
            Text(
                color = Color.Gray,
                text = "Sidebar will show up here, when you open a Project",
            )
        }
    }
}
