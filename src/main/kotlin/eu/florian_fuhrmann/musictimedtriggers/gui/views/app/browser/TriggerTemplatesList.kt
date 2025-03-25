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
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.alerts.AlertCreator
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.browser.BrowserState
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.browser.BrowserTemplate
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.managers.TriggerSelectionManager
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.redrawTimeline
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.sequence.TriggerSequenceLine
import eu.florian_fuhrmann.musictimedtriggers.utils.icons.MttIcons
import eu.florian_fuhrmann.musictimedtriggers.utils.color.getContrasting
import org.jetbrains.jewel.foundation.modifier.onHover
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Text
import sh.calvin.reorderable.*
import java.awt.Cursor

@Composable
fun TriggerTemplatesList() {
    // State
    val browserState = ProjectManager.currentProject!!.browserState
    val reorderableLazyListState = rememberReorderableLazyListState(browserState.templatesLazyListState) { from, to ->
        ProjectManager.currentProject?.browserState?.getSelectedTriggerTemplateGroup()?.moveTemplate(from.index, to.index)
    }
    browserState.currentCoroutineScope = rememberCoroutineScope() // need to allow auto-scrolling to templates
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
                    ContextMenuItem("Search Usages") {
                        searchUsagesOfSelectedTemplates(browserState)
                    },
                    ContextMenuItem(
                        "Delete" +
                            if (browserState.selectedTemplates.size != 1) {
                                " ${browserState.selectedTemplates.size} Templates"
                            } else {
                                ""
                            },
                    ) {
                        removeSelectedTemplates(browserState)
                    },
                    ContextMenuItem("Copy") {
                        browserState.copy()
                    }
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
                    .background(JewelTheme.globalColors.borders.normal)
                    .fillMaxSize()
                    .clickable(indication = null, interactionSource = null) {
                        // only triggers when clicked outside a list item
                        browserState.unselectAllTemplates()
                    }.onKeyEvent {
                        // Delete should only work when focus is on the list, so the key event lister is here
                        if (it.type != KeyEventType.KeyUp) return@onKeyEvent false
                        if (it.key == Key.Backspace || it.key == Key.Delete) {
                            // remove all selected triggers
                            removeSelectedTemplates(browserState, it.isShiftPressed && it.isAltPressed)
                            return@onKeyEvent true
                        }
                        return@onKeyEvent false
                    },
            state = browserState.templatesLazyListState,
            contentPadding = PaddingValues(3.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            items(browserState.templates, key = { it }) { item ->
                ReorderableItem(reorderableLazyListState, key = item, animateItemModifier = Modifier) { isDragging ->
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
    scope: ReorderableCollectionItemScope,
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
                }.onClick (
                    matcher = PointerMatcher.mouse(PointerButton.Primary),
                    onClick = {
                        browserState.selectTemplate(browserTemplate, false)
                    }
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
                    browserTemplate.type.iconKey,
                    null,
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
                    key = MttIcons.pencil,
                    null,
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
                        .clickable(indication = null, interactionSource = null) {
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
                    key = MttIcons.contentViewList,
                    null,
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

private fun removeSelectedTemplates(browserState: BrowserState, skipConfirmation: Boolean = false) {
    // ensure that at least one template is selected
    if (browserState.selectedTemplates.isEmpty()) {
        return
    }
    // get current project
    val project = ProjectManager.currentProject ?: throw IllegalStateException("No project currently open")
    // collect triggers to remove
    val selectedTemplates = browserState.selectedTemplates.map { it.getTriggerTemplate() }
    // search for usages of the selected templates
    val usages = project.triggersManager.searchUsagesOfTriggerTemplates(project, selectedTemplates)
    // create onConfirm function
    val onConfirm: () -> Unit = {
        // remove placed triggers in usages and collect set of affected lines
        val affectedLines = mutableSetOf<TriggerSequenceLine>()
        usages.forEach {
            TriggerSelectionManager.deselectTrigger(it.placedTrigger, false)
            it.line.removeTrigger(it.placedTrigger)
            affectedLines.add(it.line)
        }
        // redraw timeline because some placed triggers currently visible might have been removed
        redrawTimeline()
        // save the affected lines
        affectedLines.forEach { it.saveToFile() }
        // remove the templates
        project.triggersManager.removeTriggerTemplates(selectedTemplates) // also saves the affected groups
    }
    // show confirmation dialog if needed
    if (skipConfirmation) {
        onConfirm.invoke()
    } else {
        DialogManager.alert(AlertCreator.createUsagesAlert(true, selectedTemplates, usages, onConfirm))
    }
}

private fun searchUsagesOfSelectedTemplates(browserState: BrowserState) {
    // get current project
    val project = ProjectManager.currentProject ?: throw IllegalStateException("No project currently open")
    // collect selected triggers
    val selectedTemplates = browserState.selectedTemplates.map { it.getTriggerTemplate() }
    // search for usages of the selected triggers
    val usages = project.triggersManager.searchUsagesOfTriggerTemplates(project, selectedTemplates)
    // open usages alert
    DialogManager.alert(AlertCreator.createUsagesAlert(false, selectedTemplates, usages))
}
