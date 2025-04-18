package eu.florian_fuhrmann.musictimedtriggers.gui.uistate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.theme.colorPalette
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

    fun iconColor(): Color = if (isDark()) {
        Color(255, 255, 255)
    } else {
        Color(85, 85, 85)
    }

    /** Secondary Background color for selected items (for example, in the sidebar) */
    val secondarySelectedBackgroundColor
        @Composable
        get() = if (MainUiState.theme.isDark()) {
            JewelTheme.colorPalette.blue(2)
        } else {
            JewelTheme.colorPalette.blue(11)
        }

    companion object {
        fun fromSystemTheme(systemTheme: SystemTheme) =
            if (systemTheme == SystemTheme.LIGHT) Light else Dark
    }
}
