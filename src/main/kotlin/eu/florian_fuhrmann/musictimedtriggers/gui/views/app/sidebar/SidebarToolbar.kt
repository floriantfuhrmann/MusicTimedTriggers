package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.addsong.AddSongDialog
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.editsong.openDeleteSongDialog
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys

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
        IconButton(
            onClick = { DialogManager.openDialog(AddSongDialog(project = project)) },
            focusable = false
        ) {
            Icon(AllIconsKeys.General.Add, null)
        }
        IconButton(
            onClick = { openDeleteSongDialog(project, project.currentSong) },
            focusable = false
        ) {
            Icon(AllIconsKeys.General.Remove, null)
        }
    }
}