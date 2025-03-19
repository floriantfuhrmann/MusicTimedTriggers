package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.EditorTimeline
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.toolbar.EditorToolbar
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun SongEditor(project: Project) {
    Column {
        //Toolbar
        EditorToolbar(project.currentSong)
        //Timeline
        Row(Modifier.padding(top = 1.dp)) { // 1.dp padding to separate the toolbar from the timeline with a thin top border
            if (project.currentSong != null) {
                EditorTimeline()
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(JewelTheme.globalColors.panelBackground)
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
}