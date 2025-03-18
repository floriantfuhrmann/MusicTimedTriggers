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
import eu.florian_fuhrmann.musictimedtriggers.utils.icons.MttIcons
import org.jetbrains.jewel.foundation.theme.JewelTheme

@Composable
fun SidebarToolbar() {
    // ensure there is a project opened
    val project = ProjectManager.currentProject ?: return

    // UI
    Row(
        modifier = Modifier
            .background(JewelTheme.globalColors.panelBackground)
            .fillMaxWidth()
            .padding(3.dp)
            .height(26.dp)
    ) {
        SimpleIconButton(
            modifier = Modifier.aspectRatio(1f),
            forceHoverHandCursor = true,
            iconKey =  MttIcons.plus,
            onClick = {
                DialogManager.openDialog(EditSongDialog(project = project, add = true))
            }
        )
        SimpleIconButton(
            modifier = Modifier.aspectRatio(1f),
            forceHoverHandCursor = true,
            iconKey =  MttIcons.minus,
            enabled = project.currentSong != null,
            onClick = {
                openDeleteSongDialog(project, project.currentSong)
            }
        )
        SimpleIconButton(
            modifier = Modifier.aspectRatio(1f),
            forceHoverHandCursor = true,
            iconKey =  MttIcons.editBox,
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