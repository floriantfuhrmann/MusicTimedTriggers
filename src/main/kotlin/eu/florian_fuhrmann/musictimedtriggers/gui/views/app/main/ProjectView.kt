package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.main

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
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.browser.TriggerBrowser
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.SongEditor
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.managers.TimelineFocusManager
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.sidebar.Sidebar
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import org.jetbrains.compose.splitpane.*
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.foundation.theme.JewelTheme

@OptIn(ExperimentalSplitPaneApi::class, ExperimentalComposeUiApi::class)
@Composable
fun OpenedProjectView(project: Project) {
    // Splitter States
    val sidebarSplitterState = rememberSplitPaneState()
    val verticalSplitterState = rememberSplitPaneState()
    // Main Window Content
    // Container Box (mainly for tracking clicks anywhere on the main window so the timeline can lose focus)
    Box(
        modifier = Modifier
            .background(JewelTheme.globalColors.borders.normal)
            .onPointerEvent(eventType = PointerEventType.Press) { _ ->
                // when a click is detected by Compose anywhere on the App it should lead to the Timeline losing focus
                TimelineFocusManager.timelineLostFocus()
            }.trackActivation()
    )
    {
        if(MainUiState.sidebarExpanded) {
            HorizontalSplitPane(splitPaneState = sidebarSplitterState) {
                first(130.dp) {
                    SidebarContainer(project)
                }
                second(170.dp) {
                    EditorWithBrowserContainer(project, verticalSplitterState)
                }
            }
        } else {
            EditorWithBrowserContainer(project, verticalSplitterState)
        }
    }
}

@Composable
fun SidebarContainer(project: Project) {
    // Sidebar Container Box with 1.dp border on the right
    Box(
        Modifier
            .background(JewelTheme.globalColors.borders.normal)
            .padding(end = 1.dp)
            .background(JewelTheme.globalColors.panelBackground)
    ) {
        Sidebar(project)
    }
}

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
fun EditorWithBrowserContainer(project: Project, verticalSplitterState: SplitPaneState) {
    VerticalSplitPane(
        splitPaneState = verticalSplitterState,
    ) {
        first(150.dp) {
            SongEditor(project)
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