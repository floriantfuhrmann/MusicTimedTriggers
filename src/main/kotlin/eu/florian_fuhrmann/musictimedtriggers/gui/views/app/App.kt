package eu.florian_fuhrmann.musictimedtriggers.gui.views.app

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.browser.TriggerBrowser
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.SongEditor
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.manager.TimelineFocusManager
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.overlay.Overlay
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.sidebar.Sidebar
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.VerticalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.foundation.theme.JewelTheme

@OptIn(ExperimentalSplitPaneApi::class, ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    // Overlay
    Overlay()

    val sidebarSplitterState = rememberSplitPaneState()
    val verticalSplitterState = rememberSplitPaneState()

    // Main Window Content
    HorizontalSplitPane(
        splitPaneState = sidebarSplitterState,
        modifier =
            Modifier
                .onPointerEvent(eventType = PointerEventType.Press) { _ ->
                    // when a click is detected by Compose anywhere on the App it should lead to the Timeline losing focus
                    TimelineFocusManager.timelineLostFocus()
                }.trackActivation(),
    ) {
        first(100.dp) {
            Box(
                Modifier
                    .trackActivation()
                    .background(JewelTheme.globalColors.borders.normal)
                    .padding(start = 0.dp, top = 0.dp, end = 1.dp, bottom = 0.dp)
                    .fillMaxSize(),
            ) {
                Box(
                    Modifier
                        .trackActivation()
                        .background(JewelTheme.globalColors.paneBackground)
                        .padding(0.dp),
                ) {
                    Sidebar()
                }
            }
        }
        second(100.dp) {
            VerticalSplitPane(
                splitPaneState = verticalSplitterState,
            ) {
                first(150.dp) {
                    SongEditor()
                }
                second(100.dp) {
                    Box(
                        Modifier
                            .trackActivation()
                            .background(JewelTheme.globalColors.borders.normal)
                            .padding(start = 0.dp, top = 1.dp, end = 0.dp, bottom = 0.dp)
                            .fillMaxSize(),
                    ) {
                        TriggerBrowser()
                    }
                }
            }
        }
    }
    // Dialog Container
    DialogManager.DialogContainer()
}
