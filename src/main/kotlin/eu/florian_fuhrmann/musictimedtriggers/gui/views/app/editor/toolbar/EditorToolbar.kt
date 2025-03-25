package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.editsong.EditSongDialog
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineBackgroundRenderer
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.SimpleIconButton
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.song.Song
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.player.currentAudioPlayer
import eu.florian_fuhrmann.musictimedtriggers.utils.icons.MttIcons
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@Composable
fun EditorToolbar(song: Song?) {
    Row(
        modifier = Modifier
            .background(JewelTheme.globalColors.borders.normal)
            .fillMaxWidth()
            .padding(horizontal = 5.dp, vertical = 5.dp)
            .height(26.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play / Pause Button
        if (song != null) {
            if(currentAudioPlayer.value?.playing?.value == true) {
                IconButton(
                    onClick = { ProjectManager.currentProject?.currentSong?.pause() },
                    focusable = false
                ) {
                    Icon(AllIconsKeys.Actions.Pause, null)
                }
            } else if(currentAudioPlayer.value?.playing?.value == false) {
                IconButton(
                    onClick = { ProjectManager.currentProject?.currentSong?.play() },
                    focusable = false
                ) {
//                    Icon(AllIconsKeys.Actions.Execute, null)
                    Icon(AllIconsKeys.Toolwindows.ToolWindowRun, null)
                }
            }
        }
        // Spacer in the middle
        Spacer(modifier = Modifier.weight(1f))
        // Zoom In / Out Buttons
        if (song != null) {
            IconButton(
                onClick = { TimelineBackgroundRenderer.Zooming.zoomIn() },
                focusable = false
            ) {
                Icon(AllIconsKeys.General.ZoomIn, null)
            }
            IconButton(
                onClick = { TimelineBackgroundRenderer.Zooming.zoomOut() },
                focusable = false
            ) {
                Icon(AllIconsKeys.General.ZoomOut, null)
            }
        }
    }
}