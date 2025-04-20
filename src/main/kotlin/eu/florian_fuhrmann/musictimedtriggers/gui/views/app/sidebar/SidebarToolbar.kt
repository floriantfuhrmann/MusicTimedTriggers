package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.sidebar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.alerts.BasicAlert
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.addsong.AddSongDialog
import eu.florian_fuhrmann.musictimedtriggers.gui.styles.fixedCursorTooltipStyle
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.song.Song
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip
import org.jetbrains.jewel.ui.icons.AllIconsKeys

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SidebarToolbar() {
    // ensure there is a project opened
    val project = ProjectManager.currentProject ?: return

    // UI
    Row(
        modifier = Modifier
            .background(JewelTheme.globalColors.panelBackground)
            .fillMaxWidth()
            .padding(horizontal = 5.dp, vertical = 5.dp)
            .height(26.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Songs", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Tooltip(tooltip = { Text("Add Song") }, style = fixedCursorTooltipStyle) {
            IconButton(
                onClick = { DialogManager.openDialog(AddSongDialog(project = project)) },
                focusable = false
            ) {
                Icon(AllIconsKeys.General.Add, null)
            }
        }
        Tooltip(tooltip = { Text("Delete Song") }, style = fixedCursorTooltipStyle) {
            IconButton(
                onClick = { openDeleteSongDialog(project, project.currentSong) },
                focusable = false
            ) {
                Icon(AllIconsKeys.General.Remove, null)
            }
        }
    }
}

fun openDeleteSongDialog(project: Project, song: Song?) {
    //show confirmation alert
    BasicAlert(
        type = BasicAlert.Type.Warning,
        title = "Confirm Deletion",
        buttons = {
            CancelButton()
            CancelButtonFocused()
            OKButton(onClick = {
                // close alert
                close()
                // delete song
                if (song != null) {
                    project.deleteSong(song)
                }
            }, label = "Confirm")
        }
    ) {
        Text("Are you sure you want to delete Song ${song?.name}?")
    }.show()
}