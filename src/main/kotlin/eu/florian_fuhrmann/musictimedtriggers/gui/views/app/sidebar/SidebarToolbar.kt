package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.editsong.EditSongDialog
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.editsong.openDeleteSongDialog
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.SimpleIconButton
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import org.jetbrains.jewel.foundation.theme.JewelTheme

@Composable
fun SidebarToolbar() {
    // ensure there is a project opened
    val project = ProjectManager.currentProject ?: return

    // UI
    Row(
        modifier = Modifier
            .background(JewelTheme.globalColors.paneBackground)
            .fillMaxWidth()
            .padding(3.dp)
            .height(26.dp)
    ) {
        SimpleIconButton(
            modifier = Modifier.aspectRatio(1f),
            forceHoverHandCursor = true,
            iconName = "plus-icon",
            onClick = {
                DialogManager.openDialog(EditSongDialog(project = project, add = true))
            }
        )
        SimpleIconButton(
            modifier = Modifier.aspectRatio(1f),
            forceHoverHandCursor = true,
            iconName = "minus-icon",
            enabled = project.currentSong != null,
            onClick = {
                openDeleteSongDialog(project, project.currentSong)
            }
        )
        SimpleIconButton(
            modifier = Modifier.aspectRatio(1f),
            forceHoverHandCursor = true,
            iconName = "edit-box-icon",
            enabled = ProjectManager.currentProject?.currentSong != null,
            onClick = {
                if(ProjectManager.currentProject!!.currentSong != null) {
                    //open song edit dialog
                    DialogManager.openDialog(
                        EditSongDialog(
                            project = project,
                            add = false,
                            song = project.currentSong!!
                        )
                    )
                }
            }
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}