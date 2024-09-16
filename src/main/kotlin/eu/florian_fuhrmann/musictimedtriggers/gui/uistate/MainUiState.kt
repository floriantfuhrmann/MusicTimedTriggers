package eu.florian_fuhrmann.musictimedtriggers.gui.uistate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import org.jetbrains.skiko.SystemTheme
import org.jetbrains.skiko.currentSystemTheme

object MainUiState {

    var theme: IntUiThemes by mutableStateOf(IntUiThemes.Dark)

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

    companion object {
        fun fromSystemTheme(systemTheme: SystemTheme) =
            if (systemTheme == SystemTheme.LIGHT) Light else Dark
    }
}
