package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineBackgroundRenderer
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.SimpleIconButton
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.song.Song
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.player.currentAudioPlayer
import eu.florian_fuhrmann.musictimedtriggers.utils.icons.MttIcons
import org.jetbrains.jewel.foundation.theme.JewelTheme

@Composable
fun EditorToolbar(song: Song?) {
    Row(
        modifier = Modifier
            .background(JewelTheme.globalColors.panelBackground)
            .fillMaxWidth()
            .padding(3.dp)
            .height(26.dp)
    ) {
        // Play / Pause Button
        if (song != null) {
            if(currentAudioPlayer.value?.playing?.value == true) {
                SimpleIconButton(
                    iconKey = MttIcons.pause,
                    onClick = {
                        ProjectManager.currentProject?.currentSong?.pause()
                    }
                )
            } else if(currentAudioPlayer.value?.playing?.value == false) {
                SimpleIconButton(
                    iconKey = MttIcons.play,
                    onClick = {
                        ProjectManager.currentProject?.currentSong?.play()
                    }
                )
            }
        }
        // Spacer in the middle
        Spacer(modifier = Modifier.weight(1f))
        // Zoom In / Out Buttons
        if (song != null) {
            SimpleIconButton(
                iconKey = MttIcons.zoomIn,
                onClick = { TimelineBackgroundRenderer.zoomIn() }
            )
            SimpleIconButton(
                iconKey = MttIcons.zoomOut,
                onClick = { TimelineBackgroundRenderer.zoomOut() },
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}