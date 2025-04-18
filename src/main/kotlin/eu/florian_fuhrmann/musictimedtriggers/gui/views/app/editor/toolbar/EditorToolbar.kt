package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.toolbar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.styles.fixedCursorTooltipStyle
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineBackgroundRenderer
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.renderer.TimelineSequenceRenderer
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.song.Song
import eu.florian_fuhrmann.musictimedtriggers.utils.audio.player.currentAudioPlayer
import eu.florian_fuhrmann.musictimedtriggers.utils.icons.MttIcons
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.PopupContainer
import org.jetbrains.jewel.ui.component.Slider
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import kotlin.math.roundToInt

var lineHeightPopupOpened by mutableStateOf(false)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditorToolbar(song: Song?) {
    Row(
        modifier = Modifier
            .background(JewelTheme.globalColors.borders.normal)
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 5.dp)
            .height(26.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Start Spacer
        Spacer(Modifier.width(5.dp))
        // Play / Pause Button
        if (song != null) {
            if(currentAudioPlayer.value?.playing?.value == true) {
                Tooltip(tooltip = { Text("Pause") }, style = fixedCursorTooltipStyle) {
                    IconButton(
                        onClick = { ProjectManager.currentProject?.currentSong?.pause() },
                        focusable = false
                    ) {
                        Icon(AllIconsKeys.Actions.Pause, null)
                    }
                }
            } else if(currentAudioPlayer.value?.playing?.value == false) {
                Tooltip(tooltip = { Text("Play") }, style = fixedCursorTooltipStyle) {
                    IconButton(
                        onClick = { ProjectManager.currentProject?.currentSong?.play() },
                        focusable = false
                    ) {
//                    Icon(AllIconsKeys.Actions.Execute, null)
                        Icon(AllIconsKeys.Toolwindows.ToolWindowRun, null)
                    }
                }
            }
        }
        // Spacer in the middle
        Spacer(modifier = Modifier.weight(1f))
        // Right Tool Buttons
        if (song != null) {
            // Minimum Line Height Setting
            Tooltip(
                tooltip = { Text("Minimum Line Height") },
                style = fixedCursorTooltipStyle
            ) {
                IconButton(
                    onClick = {
                        lineHeightPopupOpened = true
                    },
                    focusable = false
                ) {
                    Icon(MttIcons.lineHeight, null)
                }
            }
            // Zoom In / Out Buttons
            Tooltip(
                tooltip = { Text("Zoom In") },
                style = fixedCursorTooltipStyle
            ) {
                IconButton(
                    onClick = { TimelineBackgroundRenderer.Zooming.zoomIn() },
                    focusable = false
                ) {
                    Icon(AllIconsKeys.General.ZoomIn, null)
                }
            }
            Tooltip(
                tooltip = { Text("Zoom Out") },
                style = fixedCursorTooltipStyle
            ) {
                IconButton(
                    onClick = { TimelineBackgroundRenderer.Zooming.zoomOut() },
                    focusable = false
                ) {
                    Icon(AllIconsKeys.General.ZoomOut, null)
                }
            }
        }
        // End Spacer
        Spacer(Modifier.width(5.dp))
        // Minimum Line Height Setting Popup
        if(lineHeightPopupOpened) {
            var minimumLineHeight by mutableStateOf(TimelineSequenceRenderer.minimumLineHeight.toFloat())
            PopupContainer(
                onDismissRequest = { lineHeightPopupOpened = false },
                horizontalAlignment = Alignment.End,
            ) {
                Column(Modifier.padding(12.dp).width(400.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Heading
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Column {
                            Text("Minimum Line Height", fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text("(${(minimumLineHeight/5).roundToInt() * 5}px)", color = JewelTheme.globalColors.text.info)
                        }
                    }
                    // Slider
                    Row {
                        Slider(
                            value = minimumLineHeight,
                            onValueChange = {
                                minimumLineHeight = it
                                TimelineSequenceRenderer.minimumLineHeight = minimumLineHeight.toInt()
                            },
                            steps = 13,
                            valueRange = 10f..80f
                        )
                    }
                }
            }
        }
    }
}