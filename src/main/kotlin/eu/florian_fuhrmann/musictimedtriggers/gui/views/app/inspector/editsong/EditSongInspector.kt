package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector.editsong

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector.InspectorContentsContainer
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import eu.florian_fuhrmann.musictimedtriggers.song.Song
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun EditSongInspector(project: Project, song: Song?) {
    // Scroll State for the content
    val scrollState = rememberScrollState()
    // Inspector Contents Container (not scrollable itself, so edit song panel can put a banner on top and handle scroll stuff itself)
    InspectorContentsContainer("Edit Song", scrollState) {
        if(song != null) {
            // Edit Song Panel
            key(song) {
                EditSongPanel(project, song, scrollState)
            }
        } else {
            // No song selected text
            Text(
                text = "Select a song to edit.",
                modifier = Modifier.padding(horizontal = 10.dp).align(Alignment.TopCenter),
                color = JewelTheme.globalColors.text.disabled
            )
        }
    }
}