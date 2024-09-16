package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineBackgroundRenderer
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.SimpleIconButton
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.player.currentAudioPlayer
import org.jetbrains.jewel.foundation.theme.JewelTheme

@Composable
fun EditorToolbar() {
    Row(
        modifier = Modifier
            .background(JewelTheme.globalColors.paneBackground)
            .fillMaxWidth()
            .padding(3.dp)
            .height(26.dp)
    ) {
        if(currentAudioPlayer.value?.playing?.value == true) {
            SimpleIconButton(
                iconName = "pause-icon",
                onClick = {
                    ProjectManager.currentProject?.currentSong?.pause()
                }
            )
        } else if(currentAudioPlayer.value?.playing?.value == false) {
            SimpleIconButton(
                iconName = "play-icon",
                onClick = {
                    ProjectManager.currentProject?.currentSong?.play()
                }
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        SimpleIconButton(
            iconName = "zoom-in-icon",
            onClick = { TimelineBackgroundRenderer.zoomIn() }
        )
        SimpleIconButton(
            iconName = "zoom-out-icon",
            onClick = { TimelineBackgroundRenderer.zoomOut() },
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}