package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.settings.categories

import androidx.compose.runtime.Composable
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import org.jetbrains.jewel.ui.component.Text

class AppearanceCategory : SettingsCategory("Appearance") {

    @Composable
    override fun Content(project: Project) {
        Text("Todo")
    }

}