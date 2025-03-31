package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector.editsong

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.song.Song
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.Text

@Composable
fun EditSongInspector(song: Song?) {
    Column(Modifier
        .background(JewelTheme.globalColors.borders.normal)
        .padding(start = 1.dp)
        .background(JewelTheme.globalColors.panelBackground)
    ) {
        // Scroll State for the content
        val scrollState = rememberScrollState()
        // Title
        Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp).height(25.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Edit Song", fontWeight = FontWeight.SemiBold)
        }
        // Divider
        Divider(
            orientation = Orientation.Horizontal,
            modifier = Modifier.height(1.dp).fillMaxWidth(),
            color = if (scrollState.canScrollBackward) JewelTheme.globalColors.borders.normal else Color.Transparent
        )
        // Content
        Row {
            Box(Modifier.fillMaxSize()) {
                if(song != null) {
                    // Edit Song Panel
                    EditSongPanel(song, scrollState)
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
    }
}