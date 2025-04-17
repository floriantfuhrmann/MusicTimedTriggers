package eu.florian_fuhrmann.musictimedtriggers.gui.uistate

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import org.jetbrains.skiko.SystemTheme
import org.jetbrains.skiko.currentSystemTheme

object MainUiState {

    // Sidebar expanded state (this could be moved to project settings or some general project ui state)
    var sidebarExpanded: Boolean by mutableStateOf(true)

    // Browser expanded state (this could also be moved to project settings or some general project ui state)
    var browserExpanded: Boolean by mutableStateOf(true)

    // Currently selected inspector panel (this could also be moved to project settings or some general project ui state)
    var inspectorOption: InspectorOption by mutableStateOf(InspectorOption.None)

    // Currently selected theme or system theme if no project is opened (derived from project settings)
    // should be moved to some global settings in the future
    val theme: Theme by derivedStateOf { ProjectManager.currentProject?.projectSettings?.selectedTheme ?: Theme.System }

    fun toggleSidebar() {
        sidebarExpanded = !sidebarExpanded
    }

    fun toggleBrowser() {
        browserExpanded = !browserExpanded
    }

}

enum class InspectorOption {
    None,
    Song,
    PlacedTrigger,
    TriggerTemplate
}

enum class Theme(val displayName: String) {
    Light("Light"),
    Dark("Dark"),
    System("System");

    fun isDark() =
        (if (this == System) fromSystemTheme(currentSystemTheme) else this) == Dark

    fun highlightGray(): Color = if (isDark()) {
        Color.LightGray
    } else {
        Color.DarkGray
    }
    fun errorTextColor(): Color = if (isDark()) {
        Color(224, 0, 0)
    } else {
        Color.Red
    }
    fun iconColor(): Color = if (isDark()) {
        Color(255, 255, 255)
    } else {
        Color(85, 85, 85)
    }

    companion object {
        fun fromSystemTheme(systemTheme: SystemTheme) =
            if (systemTheme == SystemTheme.LIGHT) Light else Dark
    }
}
