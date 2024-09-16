package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.openproject

import androidx.compose.runtime.Composable
import com.darkrockstudios.libraries.mpfilepicker.DirectoryPicker
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Dialog
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import java.io.File

class OpenProjectDialog : Dialog(windowed = false) {
    @Composable
    override fun Content() {
        DirectoryPicker(
            show = true,
            title = "Select Project Directory"
        ) { path ->
            //close dialog again
            DialogManager.closeDialog()
            //open project
            if(path == null) return@DirectoryPicker
            ProjectManager.openProject(File(path))
        }
    }
}