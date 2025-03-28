package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector.editsong

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.song.Song
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text

@Composable
fun EditSongInspector(song: Song?) {
    Column(Modifier
        .background(JewelTheme.globalColors.borders.normal)
        .padding(start = 1.dp)
        .background(JewelTheme.globalColors.panelBackground)
    ) {
        // Title
        Row(Modifier.padding(12.dp)) {
            Text(text = "Edit Song", fontWeight = FontWeight.SemiBold)
        }
        // Content
        Row {
            Box(Modifier.fillMaxSize().padding(horizontal = 10.dp)) {
                if(song != null) {
                    // Edit Song Panel
                    EditSongPanel(song)
                } else {
                    // No song selected text
                    Text(
                        text = "Select a song to edit.",
                        modifier = Modifier.align(Alignment.TopCenter),
                        color = JewelTheme.globalColors.text.disabled
                    )
                }
            }
        }
    }
}