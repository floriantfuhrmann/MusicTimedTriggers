package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.EditorTimeline
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.toolbar.EditorToolbar
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun SongEditor() {
    Column {
        if(ProjectManager.currentProject?.currentSong != null) {
            //Toolbar
            EditorToolbar()
            //Timeline
            Row {
                EditorTimeline()
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(JewelTheme.globalColors.paneBackground)
                    .padding(5.dp)
            ) {
                Text(
                    color = Color.Gray,
                    text = "When you open a Song the Editor will be displayed here"
                )
            }
        }
    }
}