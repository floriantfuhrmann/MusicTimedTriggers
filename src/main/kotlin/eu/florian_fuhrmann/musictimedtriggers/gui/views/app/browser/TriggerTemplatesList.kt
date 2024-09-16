package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.browser

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.onDrag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Alert
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.browser.BrowserState
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.browser.BrowserTemplate
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.SimpleIconButton
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.utils.IconsDummy
import eu.florian_fuhrmann.musictimedtriggers.utils.color.getContrasting
import org.jetbrains.jewel.foundation.modifier.onHover
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Text
import sh.calvin.reorderable.*
import java.awt.Cursor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TriggerTemplatesList() {
    // State
    val browserState = ProjectManager.currentProject!!.browserState!!
    val reorderableLazyColumnState =
        rememberReorderableLazyColumnState(browserState.templatesLazyListState) { from, to ->
            ProjectManager.currentProject
                ?.browserState
                ?.getSelectedTriggerTemplateGroup()
                ?.moveTemplate(from.index, to.index)
        }
    browserState.currentCoroutineScope = rememberCoroutineScope() // need to allow auto-scrolling to triggers
    // UI
    ContextMenuArea(
        items = {
            // get hovered template
            val hoveredTemplate = browserState.hoveredTemplate.value
            if (hoveredTemplate != null) {
                // select the hovered template if not already selected
                if (!browserState.isSelected(hoveredTemplate)) {
                    browserState.selectTemplate(hoveredTemplate, false)
                }
                listOfNotNull(
                    ContextMenuItem("Edit") {
                        // open edit dialog
                        if (browserState.hoveredTemplate.value != null) {
                            hoveredTemplate.getTriggerTemplate().openEditDialog(false)
                        }
                    },
                    ContextMenuItem(
                        "Delete" +
                            if (browserState.selectedTemplates.size != 1) {
                                " ${browserState.selectedTemplates.size} Templates"
                            } else {
                                ""
                            },
                    ) {
                        removeSelectedTemplates(browserState, browserState.selectedTemplates.size != 1)
                    },
                    ContextMenuItem("Copy") {
                        browserState.copy()
                    },
                )
            } else {
                listOf(
                    ContextMenuItem("Paste") {
                        browserState.paste()
                    },
                )
            }
        },
    ) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clickable {
                        // only triggers when clicked outside a list item
                        browserState.unselectAllTemplates()
                    }.onKeyEvent {
                        // Delete should only work when focus is on the list, so the key event lister is here
                        if (it.type != KeyEventType.KeyUp) return@onKeyEvent false
                        if (it.key == Key.Backspace || it.key == Key.Delete) {
                            // remove all selected triggers
                            removeSelectedTemplates(browserState, true)
                            return@onKeyEvent true
                        }
                        return@onKeyEvent false
                    },
            state = browserState.templatesLazyListState,
            contentPadding = PaddingValues(3.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            items(browserState.templates, key = { it }) { item ->
                ReorderableItem(reorderableLazyColumnState, key = item) { isDragging ->
                    val interactionSource = remember { MutableInteractionSource() }
                    TriggerTemplateItem(this, browserState, item, isDragging, interactionSource)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun TriggerTemplateItem(
    scope: ReorderableItemScope,
    browserState: BrowserState,
    browserTemplate: BrowserTemplate,
    isDragging: Boolean,
    interactionSource: MutableInteractionSource,
) {
    val selected by derivedStateOf { browserState.selectedTemplates.contains(browserTemplate) }
    val backgroundColor = browserTemplate.composeColor.value
    val textColor = backgroundColor.getContrasting(Color.White, Color.Black)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(5.dp),
                ).border(
                    width =
                        if (selected) {
                            5
                        } else {
                            0
                        }.dp,
                    color =
                        if (selected) {
                            MainUiState.theme.primaryColor()
                        } else {
                            Color.Black
                        },
                    shape = RoundedCornerShape(5.dp),
                )
                .onPointerEvent(PointerEventType.Enter) {
                    browserState.onTemplateHoverEnter(browserTemplate)
                }.onPointerEvent(PointerEventType.Exit) {
                    browserState.onTemplateHoverExit(browserTemplate)
                }.combinedClickable(
                    onClick = {
                        browserState.selectTemplate(browserTemplate, false)
                    },
                    onDoubleClick = {
                        // open edit dialog
                        browserTemplate.getTriggerTemplate().openEditDialog(false)
                    },
                ).onClick(
                    keyboardModifiers = { isShiftPressed },
                    matcher = PointerMatcher.mouse(PointerButton.Primary),
                    onClick = {
                        browserState.selectTemplate(browserTemplate, true)
                    },
                ).onDrag(
                    onDragStart = {
                        // select the template if not already selected
                        if (!browserState.isSelected(browserTemplate)) {
                            browserState.selectTemplate(browserTemplate, false)
                        }
                        // start dragging
                        browserState.startDragging()
                    },
                    onDrag = {
                        browserState.updateDrag()
                    },
                    onDragEnd = {
                        browserState.stopDragging()
                    },
                    onDragCancel = {
                        browserState.stopDragging()
                    },
                ).padding(5.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(end = 5.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxHeight(),
            ) {
                Icon(
                    browserTemplate.type.iconResource,
                    null,
                    IconsDummy::class.java,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.weight(1f),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxHeight(),
            ) {
                Text(color = textColor, text = browserTemplate.name.value)
            }
        }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxHeight(),
            ) {
                var hovered by remember { mutableStateOf(false) }
                Icon(
                    "icons/pencil-icon.svg",
                    null,
                    IconsDummy::class.java,
                    modifier = Modifier
                        .alpha(
                            if (hovered) {
                                1f
                            } else {
                                0.5f
                            }
                        )
                        .size(16.dp)
                        .pointerHoverIcon(icon = PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)))
                        .onHover {
                            hovered = it
                        }
                        .clickable {
                            // open edit dialog
                            browserTemplate.getTriggerTemplate().openEditDialog(false)
                        },
                    tint = textColor
                )
            }
        }
        Column(
            modifier = Modifier.padding(start = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxHeight(),
            ) {
                var hovered by remember { mutableStateOf(false) }
                Icon(
                    "icons/content-view-list-icon.svg",
                    null,
                    IconsDummy::class.java,
                    modifier =
                        with(scope) { Modifier.draggableHandle(interactionSource = interactionSource) }
                            .alpha(
                                if (hovered) {
                                    1f
                                } else {
                                    0.5f
                                }
                            )
                            .onHover {
                                hovered = it
                            }
                            .size(16.dp),
                    tint = textColor,
                )
            }
        }
    }
}

private fun removeSelectedTemplates(
    browserState: BrowserState,
    showConfirmationAlert: Boolean,
) {
    if (showConfirmationAlert) {
        DialogManager.alert(
            Alert(
                title = "Confirm deletion of ${browserState.selectedTemplates.size} Templates",
                text = "Are you sure you want to delete ${browserState.selectedTemplates.size} Trigger Templates?",
                onDismiss = {},
                dismissText = "Cancel",
                onConfirm = {
                    removeSelectedTemplates(browserState, false)
                },
            ),
        )
    } else {
        ProjectManager.currentProject!!.triggersManager.removeTriggerTemplates(
            browserState.selectedTemplates.map {
                it.getTriggerTemplate()
            },
        )
    }
}

private fun removeSingleTemplate(browserTemplate: BrowserTemplate) {
    ProjectManager.currentProject!!.triggersManager.removeTriggerTemplates(
        listOf(
            browserTemplate.getTriggerTemplate(),
        ),
    )
}
