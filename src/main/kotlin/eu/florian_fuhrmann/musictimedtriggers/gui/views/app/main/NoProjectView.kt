package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.editproject.EditProjectDialog
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.openproject.OpenProjectDialog
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Text
import java.awt.datatransfer.DataFlavor
import java.io.File


@Composable
fun NoProjectView() {
    Column(
        modifier = Modifier.fillMaxSize().background(JewelTheme.globalColors.panelBackground).padding(20.dp)
    ) {
        Row {
            Text("Please open or create a project to continue", fontSize = 1.em)
        }
        Row(Modifier.padding(top = 5.dp)) {
            DefaultButton(
                onClick = {
                    DialogManager.openDialog(OpenProjectDialog())
                }
            ) {
                Text("Open Project")
            }
            DefaultButton(
                modifier = Modifier.padding(start = 5.dp),
                onClick = {
                    DialogManager.openDialog(EditProjectDialog(create = true))
                }
            ) {
                Text("Create Project")
            }
        }
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OpenProjectDragBox()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun OpenProjectDragBox() {
    var showTargetBorder by remember { mutableStateOf(false) }
    val dragAndDropTarget = remember {
        object: DragAndDropTarget {

            override fun onEntered(event: DragAndDropEvent) { showTargetBorder = true }
            override fun onExited(event: DragAndDropEvent) { showTargetBorder = false }
            override fun onEnded(event: DragAndDropEvent) { showTargetBorder = false }

            private fun getFile(event: DragAndDropEvent): File? {
                if (event.awtTransferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    val fileList = event.awtTransferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
                    if (fileList.size == 1 && fileList.first() is File) {
                        val file = fileList.first() as File
                        if (file.isDirectory) {
                            return file
                        }
                    }
                }
                return null
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                val file = getFile(event)
                if(file == null) {
                    return false
                } else {
                    ProjectManager.openProject(file)
                    return true
                }
            }
        }
    }

    // Box
    Box(
        modifier = Modifier
            .background(JewelTheme.globalColors.panelBackground)
            .border(
                width = 5.dp,
                color = if (showTargetBorder) JewelTheme.globalColors.text.normal else JewelTheme.globalColors.borders.disabled,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 20.dp)
            .fillMaxSize(0.7f)
            .dragAndDropTarget(
                shouldStartDragAndDrop = { true },
                target = dragAndDropTarget
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Or Drag Project Directory here",
            fontSize = 1.5.em,
            fontWeight = FontWeight.Bold,
            color = if (showTargetBorder) JewelTheme.globalColors.text.normal else JewelTheme.globalColors.text.disabled,
            lineHeight = 1.0.em
        )
    }
}
