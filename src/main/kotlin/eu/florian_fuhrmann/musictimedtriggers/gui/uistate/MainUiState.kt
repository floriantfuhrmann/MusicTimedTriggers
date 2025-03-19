package eu.florian_fuhrmann.musictimedtriggers.gui.uistate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import org.jetbrains.skiko.SystemTheme
import org.jetbrains.skiko.currentSystemTheme

object MainUiState {

    // Sidebar expanded state (this could be moved to project settings or some general project ui state)
    var sidebarExpanded: Boolean by mutableStateOf(true)

    // Currently selected theme (this should probably be moved to project or global settings sometime)
    var theme: IntUiThemes by mutableStateOf(IntUiThemes.Dark)

    fun toggleSidebar() {
        sidebarExpanded = !sidebarExpanded
    }

}

enum class IntUiThemes {
    Light, Dark, System;

    fun isDark() =
        (if (this == System) fromSystemTheme(currentSystemTheme) else this) == Dark

    fun primaryColor(): Color = Color(0, 100, 255)
    fun secondaryColor(): Color = if (isDark()) {
        Color(40, 45, 52)
    } else {
        Color(242, 242, 242)
    }
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
    fun successTextColor(): Color = if (isDark()) {
        Color(0, 255, 0)
    } else {
        Color(0, 192, 0)
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
