package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.browser

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.onDrag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.florian_fuhrmann.musictimedtriggers.gui.alerts.BasicAlert
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.triggerusages.TriggerUsagesDialog
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.InspectorOption
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.browser.BrowserState
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.browser.BrowserTemplate
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.utils.icons.MttIcons
import eu.florian_fuhrmann.musictimedtriggers.utils.color.getContrasting
import org.jetbrains.jewel.foundation.modifier.onHover
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.util.thenIf
import sh.calvin.reorderable.*

@Composable
fun TriggerTemplatesList(project: Project) {
    // State
    val browserState = project.browserState
    val reorderableLazyListState = rememberReorderableLazyListState(browserState.templatesLazyListState) { from, to ->
        project.browserState.getSelectedTriggerTemplateGroup()?.moveTemplate(from.index, to.index)
    }
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
                // context menu for the hovered template
                listOfNotNull(
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
                        browserState.removeSelectedTemplates()
                    },
                    ContextMenuItem("Copy") {
                        browserState.copy()
                    }
                )
            } else {
                // unselect all templates
                browserState.unselectAllTemplates()
                // context menu for not hovered template
                listOf(
                    ContextMenuItem("Paste") {
                        browserState.paste()
                    }
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
                            browserState.removeSelectedTemplates(it.isShiftPressed && it.isAltPressed)
                            return@onKeyEvent true
                        }
                        return@onKeyEvent false
                    },
            state = browserState.templatesLazyListState,
            contentPadding = PaddingValues(3.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            items(browserState.templates, key = { it }) { item ->
                ReorderableItem(reorderableLazyListState, key = item, animateItemModifier = Modifier) { isDragging ->
                    val interactionSource = remember { MutableInteractionSource() }
                    TriggerTemplateItem(this, browserState, item, isDragging, interactionSource, project.browserState.selectedTemplates.indexOf(item))
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
    selectedIndex: Int,
) {
    val selected by derivedStateOf { browserState.selectedTemplates.contains(browserTemplate) }
    val hovered = browserState.hoveredTemplate.value == browserTemplate
    val backgroundColor = browserTemplate.composeColor.value
    val textColor = backgroundColor.getContrasting(Color.White, Color.Black)
    val borderColor = backgroundColor.getContrasting(Color.White, Color.Gray)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(Color.Transparent)
                .padding(0.5.dp)
                .thenIf(selected) {
                    border(
                        width = 3.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(5.dp)
                    )
                }
                .thenIf(hovered && !selected) {
                    border(
                        width = 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(6.dp)
                    )
                }
                .padding(1.dp)
                .background(
                    color = backgroundColor,
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
                    },
                    onDoubleClick = {
                        // select the template
                        browserState.selectTemplate(browserTemplate, false)
                        // open the template inspector
                        MainUiState.inspectorOption = InspectorOption.TriggerTemplate
                    }
                ).onClick(
                    keyboardModifiers = { isShiftPressed },
                    matcher = PointerMatcher.mouse(PointerButton.Primary),
                    onClick = {
                        browserState.selectTemplate(browserTemplate, true)
                    }
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
                ).padding(3.5.dp).trackActivation(),
    ) {
        // Selection Number
        if(selected) {
            Column(Modifier.fillMaxHeight().padding(start = 2.dp, end = 5.dp), verticalArrangement = Arrangement.Center) {
                Row {
                    Box(Modifier.size(16.dp).background(textColor, CircleShape), contentAlignment = Alignment.Center) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "${selectedIndex + 1}",
                            color = backgroundColor,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Visible,
                            letterSpacing = 0.1.sp,
                        )
                    }
                }
            }
        }
        // Icon
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
        // Name
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
        // Drag Handle
        Column(Modifier.padding(start = 5.dp)) {
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var handleHovered by remember { mutableStateOf(false) }
                Icon(
                    key = MttIcons.contentViewList,
                    null,
                    modifier = with(scope) { Modifier.draggableHandle(interactionSource = interactionSource) }
                        .alpha(if (handleHovered) 1f else 0.5f)
                        .onHover { handleHovered = it }
                        .size(16.dp),
                    tint = textColor
                )
            }
        }
    }
}

private fun searchUsagesOfSelectedTemplates(browserState: BrowserState) {
    // get current project
    val project = ProjectManager.currentProject ?: throw IllegalStateException("No project currently open")
    // collect selected triggers
    val selectedTemplates = browserState.selectedTemplates.map { it.getTriggerTemplate() }
    // search for usages of the selected triggers
    val usages = project.triggersManager.searchUsagesOfTriggerTemplates(project, selectedTemplates)
    // open usages dialog
    if (usages.isEmpty()) {
        BasicAlert(
            type = BasicAlert.Type.Info,
            title = "No Usages Found",
            buttons = { OKButton() }
        ) {
            Text("No usages of the selected trigger templates were found.")
        }.show()
    } else {
        DialogManager.openDialog(TriggerUsagesDialog(TriggerUsagesDialog.Type.ViewTemplateUsages, usages))
    }
}
