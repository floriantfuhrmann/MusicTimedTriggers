package eu.florian_fuhrmann.musictimedtriggers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.configureSwingGlobalsForCompose
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.IntUiThemes
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.App
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.manager.EditTriggersManager
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.manager.TimelineFocusManager
import eu.florian_fuhrmann.musictimedtriggers.gui.views.titlebar.TitleBarView
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.intui.standalone.theme.darkThemeDefinition
import org.jetbrains.jewel.intui.standalone.theme.lightThemeDefinition
import org.jetbrains.jewel.intui.window.decoratedWindow
import org.jetbrains.jewel.intui.window.styling.dark
import org.jetbrains.jewel.intui.window.styling.light
import org.jetbrains.jewel.intui.window.styling.lightWithLightHeader
import org.jetbrains.jewel.ui.ComponentStyling
import org.jetbrains.jewel.window.DecoratedWindow
import org.jetbrains.jewel.window.styling.TitleBarStyle
import java.awt.Dimension

var windowState = WindowState()

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Init Stuff
    println("Hello World")
    System.setProperty("compose.swing.render.on.graphics", "true")
    configureSwingGlobalsForCompose(
        useAutoDpiOnLinux = false,
    )

    // Show UI
    application {
        val themeDefinition =
            if (MainUiState.theme.isDark()) {
                JewelTheme.darkThemeDefinition()
            } else {
                JewelTheme.lightThemeDefinition()
            }

        IntUiTheme(
            theme = themeDefinition,
            styling =
                ComponentStyling.decoratedWindow(
                    titleBarStyle =
                        when (MainUiState.theme) {
                            IntUiThemes.Light -> TitleBarStyle.lightWithLightHeader()
                            IntUiThemes.Dark -> TitleBarStyle.dark()
                            IntUiThemes.System ->
                                if (MainUiState.theme.isDark()) {
                                    TitleBarStyle.dark()
                                } else {
                                    TitleBarStyle.light()
                                }
                        },
                ),
            swingCompatMode = true,
        ) {
            val scaleFactor = 1f
            DecoratedWindow(
                state = windowState,
                onCloseRequest = { exitApplication() },
                title = "ToDo: Title",
                onKeyEvent = {
                    // because Compose can't seem to pass KeyEvents to Swing Component Timeline Keyboard Presses need to be handled by Window
                    if (!TimelineFocusManager.timelineFocused) return@DecoratedWindow false
                    if (it.type != KeyEventType.KeyUp) return@DecoratedWindow false
                    if (it.key == Key.Backspace || it.key == Key.Delete) {
                        EditTriggersManager.editSelectedTrigger()
                        return@DecoratedWindow true
                    } else if (it.key == Key.Enter) {
                        EditTriggersManager.editSelectedTrigger()
                        return@DecoratedWindow true
                    }
                    return@DecoratedWindow false
                },
                // icon = icon,
            ) {
                this.window.minimumSize = Dimension(500, 350)
                TitleBarView()
                Box(
                    modifier = Modifier.fillMaxSize(scaleFactor),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize(fraction = (1 / scaleFactor))
                                .scale(scaleFactor),
                    ) {
                        App()
                    }
                }
            }
        }
    }
}
