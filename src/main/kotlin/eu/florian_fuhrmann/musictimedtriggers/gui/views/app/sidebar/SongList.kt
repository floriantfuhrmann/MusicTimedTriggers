package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.addsong.AddSongDialog
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import eu.florian_fuhrmann.musictimedtriggers.song.Song
import org.jetbrains.jewel.foundation.modifier.onHover
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Link
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.theme.iconButtonStyle
import org.jetbrains.jewel.ui.theme.simpleListItemStyle
import org.jetbrains.jewel.ui.util.thenIf
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyColumnState

@Composable
fun SongList(project: Project) {
    val lazyListState = rememberLazyListState()
    val reorderableLazyColumnState =
        rememberReorderableLazyColumnState(lazyListState) { from, to ->
            // Update the list
            project.moveSongInSongList(from.index, to.index)
        }

    if (project.songs.isNotEmpty()) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .background(JewelTheme.globalColors.panelBackground)
                .padding(horizontal = 5.dp)
                .fillMaxSize(),
        ) {
            items(project.songs, key = { it }) {
                ReorderableItem(reorderableLazyColumnState, key = it) { _ ->
                    // Item content
                    SongListItem(project, it,
                        onClick = { project.openSong(it) }
                    )
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .background(JewelTheme.globalColors.panelBackground)
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
                        DialogManager.openDialog(AddSongDialog(project = project))
                    })
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ReorderableCollectionItemScope.SongListItem(
    project: Project,
    song: Song,
    onClick: () -> Unit
) {
    val selected = project.currentSong == song
    var hovered by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.draggableHandle().fillMaxWidth()
            .onHover { hovered = it }
            .clickable(null, null, onClick = onClick).trackActivation()
            .background(
                color = when {
                    selected -> MainUiState.theme.secondarySelectedBackgroundColor
                    hovered -> JewelTheme.iconButtonStyle.colors.backgroundHovered
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(JewelTheme.simpleListItemStyle.metrics.selectionBackgroundCornerSize)
            )
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(song.name)
    }
}
