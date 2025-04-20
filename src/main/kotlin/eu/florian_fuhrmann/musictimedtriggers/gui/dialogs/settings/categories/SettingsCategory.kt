package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.settings.categories

import androidx.compose.runtime.Composable
import eu.florian_fuhrmann.musictimedtriggers.project.Project

abstract class SettingsCategory(val name: String) {

    @Composable
    abstract fun Content(project: Project)

}