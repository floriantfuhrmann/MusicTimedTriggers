package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.sidebar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.editsong.EditSongDialog
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.song.Song
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Link
import org.jetbrains.jewel.ui.component.SelectableIconButton
import org.jetbrains.jewel.ui.component.Text
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableItemScope
import sh.calvin.reorderable.rememberReorderableLazyColumnState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongList() {
    // ensure there is a project opened
    val project = ProjectManager.currentProject ?: return

    val lazyListState = rememberLazyListState()
    val reorderableLazyColumnState =
        rememberReorderableLazyColumnState(lazyListState) { from, to ->
            // Update the list
            ProjectManager.currentProject?.moveSong(from.index, to.index)
        }

    if (project.songs.isNotEmpty()) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .background(JewelTheme.globalColors.paneBackground)
                .padding(5.dp)
                .fillMaxSize(),
        ) {
            items(ProjectManager.currentProject!!.songs, key = { it }) {
                ReorderableItem(reorderableLazyColumnState, key = it) { _ ->
                    // Item content
                    DragHandle(
                        this,
                        it,
                        onClick = {
                            // open clicked song
                            ProjectManager.currentProject?.openSong(it)
                        },
                        onDoubleClick = {
                            // open song edit dialog
                            DialogManager.openDialog(
                                EditSongDialog(project = ProjectManager.currentProject!!, add = false, song = it),
                            )
                        },
                    )
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .background(JewelTheme.globalColors.paneBackground)
                .padding(5.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Row {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    text = "You don't have any Songs yet.",
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            Row {
                Spacer(modifier = Modifier.weight(1f))
                Column {
                    Link("Add Song", {
                        DialogManager.openDialog(EditSongDialog(project = project, add = true))
                    })
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DragHandle(
    scope: ReorderableItemScope,
    item: Song,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
) {
    SelectableIconButton(
        selected = ProjectManager.currentProject?.currentSong == item,
        modifier =
            with(scope) { Modifier.draggableHandle() }
                .trackActivation()
                .fillMaxWidth()
                .onPointerEvent(PointerEventType.Press) {
                    if (it.buttons.isPrimaryPressed && it.awtEventOrNull?.clickCount == 2) {
                        onDoubleClick()
                    }
                },
        onClick = onClick,
    ) {
        Text(
            item.name,
            modifier =
                Modifier
                    .trackActivation()
                    .padding(horizontal = 5.dp, vertical = 2.dp)
                    .align(alignment = Alignment.CenterStart),
        )
    }
}
