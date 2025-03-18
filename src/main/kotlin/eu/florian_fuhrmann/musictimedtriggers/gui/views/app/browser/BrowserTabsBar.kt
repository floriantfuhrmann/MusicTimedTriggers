package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.browser

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.DropdownMenu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.edittemplategroup.EditTemplateGroupDialog
import eu.florian_fuhrmann.musictimedtriggers.gui.styles.outlinedButtonStyleWithNoPadding
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.browser.BrowserGroup
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.SingleTab
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.TriggerType
import eu.florian_fuhrmann.musictimedtriggers.triggers.templates.AbstractTriggerTemplate
import eu.florian_fuhrmann.musictimedtriggers.utils.icons.MttIcons
import org.jetbrains.jewel.foundation.modifier.onHover
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.theme.defaultTabStyle
import org.jetbrains.jewel.ui.theme.dropdownStyle
import sh.calvin.reorderable.*

@Composable
fun BrowserTabsBar() {
    val openedGroups by remember {
        derivedStateOf {
            ProjectManager.currentProject?.browserState?.openedGroups?.value ?: emptyList()
        }
    }
    val closedGroups: List<BrowserGroup> by remember {
        derivedStateOf {
            ProjectManager.currentProject?.browserState?.allGroups?.value?.filter {
                ProjectManager.currentProject?.browserState?.openedGroups?.value?.contains(it) == false
            } ?: emptyList()
        }
    }
    val tabsScrollState = rememberScrollState()
    var tabsHovered by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        //Column with tabs
        Column(modifier = Modifier.weight(1f).height(JewelTheme.defaultTabStyle.metrics.tabHeight)) {
            // This is similar to TabStrip from org.jetbrains.jewel.ui.component, but modified to be reorderable
            Box(
                Modifier.focusable(true, remember { MutableInteractionSource() }).onHover {
                    tabsHovered = it
                }
            ) {
                // Scrollbar for Tabs
                androidx.compose.animation.AnimatedVisibility(
                    modifier = Modifier.zIndex(0.5f),
                    visible = tabsHovered,
                    enter = fadeIn(tween(durationMillis = 125, delayMillis = 0, easing = LinearEasing)),
                    exit = fadeOut(tween(durationMillis = 125, delayMillis = 700, easing = LinearEasing)),
                ) {
                    HorizontalScrollbar(tabsScrollState, style = JewelTheme.defaultTabStyle.scrollbarStyle, modifier = Modifier.fillMaxWidth())
                }
                // Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(tabsScrollState)
                        .scrollable(
                            state = tabsScrollState,
                            orientation = Orientation.Vertical,
                            reverseDirection =
                                ScrollableDefaults.reverseDirection(
                                    LocalLayoutDirection.current,
                                    Orientation.Vertical,
                                    false
                                )
                        )
                        .selectableGroup()
                ) {
                    ReorderableRow(
                        list = openedGroups,
                        onSettle = { fromIndex, toIndex ->
                            ProjectManager.currentProject?.browserState?.moveGroup(fromIndex, toIndex)
                        }
                    ) { _, item, _ ->
                        // Item content
                        key(item.uuid) {
                            TabDragHandle(
                                this, item, item == ProjectManager.currentProject?.browserState?.selectedGroup?.value,
                                onClick = {
                                    ProjectManager.currentProject?.browserState?.selectGroup(item)
                                },
                                onClose = {
                                    ProjectManager.currentProject?.browserState?.closeGroup(item)
                                }
                            )
                        }
                    }
                }
            }
        }
        Column(modifier = Modifier.padding(end = 5.dp)) {
            Dropdown(
                menuContent = {
                    selectableItem(
                        selected = false,
                        onClick = {
                            val selectedUuid = ProjectManager.currentProject?.browserState?.selectedGroup?.value?.uuid
                            if(selectedUuid != null) {
                                val templateGroup = ProjectManager.currentProject?.triggersManager?.getTemplateGroup(selectedUuid)
                                if(templateGroup != null) {
                                    DialogManager.openDialog(EditTemplateGroupDialog(false, templateGroup))
                                }
                            }
                        },
                        iconKey = MttIcons.pencilOutline,
                    ) {
                        Text("Edit Group")
                    }
                    selectableItem(
                        selected = false,
                        onClick = {
                            DialogManager.openDialog(EditTemplateGroupDialog(true, null))
                        },
                        iconKey = MttIcons.plusLine,
                    ) {
                        Text("Create Group")
                    }
                    submenu(
                        enabled = closedGroups.isNotEmpty(),
                        submenu = {
                            closedGroups.forEach {
                                selectableItem(
                                    selected = false,
                                    onClick = {
                                        ProjectManager.currentProject?.browserState?.openGroup(it)
                                    }
                                ) {
                                    Text(it.name.value)
                                }
                            }
                        }
                    ) {
                        Text("Open Group")
                    }
                }
            ) {
                Text("More")
            }
        }
        Column(modifier = Modifier.padding(end = 5.dp)) {
            var expanded by remember { mutableStateOf(false) }
            OutlinedButton(
                modifier = Modifier
                    .height(JewelTheme.dropdownStyle.metrics.minSize.height)
                    .width(JewelTheme.dropdownStyle.metrics.minSize.height),
                onClick = {
                    expanded = !expanded
                },
                style = outlinedButtonStyleWithNoPadding
            ) {
                Box(modifier = Modifier.padding(5.dp)) {
                    Icon(MttIcons.plusLine, null)
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                },
                modifier = Modifier
                    .background(color = JewelTheme.globalColors.panelBackground)
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 5.dp, end = 5.dp)
                ) {
                    TriggerType.entries.forEach { triggerType ->
                        Row {
                            SelectableIconButton(
                                selected = false,
                                onClick = {
                                    expanded = false
                                    openNewEditDialog(triggerType)
                                },
                                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start,
                                    modifier = Modifier.fillMaxHeight().padding(5.dp)
                                ) {
                                    //Trigger Template Icon
                                    Column {
                                        Icon(
                                            key = triggerType.iconKey,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    //Trigger Template Name
                                    Column(
                                        modifier = Modifier.padding(start = 5.dp)
                                    ) {
                                        Text(triggerType.displayName)
                                    }
                                    //Spacer
                                    Column(modifier = Modifier.weight(1f)) {  }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabDragHandle(
    scope: ReorderableScope,
    browserGroup: BrowserGroup,
    selected: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    SingleTab(
        modifier = with(scope) { Modifier.draggableHandle() },
        editorStyle = false,
        selected = selected,
        closable = true,
        onClose = onClose,
        onClick = onClick
    ) {
        Text(browserGroup.name.value)
    }
}

fun openNewEditDialog(triggerType: TriggerType) {
    //get currently selected group
    val selectGroupUuid = ProjectManager.currentProject?.browserState?.selectedGroup?.value?.uuid ?: return
    val triggerTemplateGroup = ProjectManager.currentProject?.triggersManager?.getTemplateGroup(selectGroupUuid) ?: return
    //create a new template (but not yet added to the group)
    val triggerTemplate = AbstractTriggerTemplate.create(triggerType, triggerTemplateGroup)
    //open edit dialog (when done editing the edit dialog should add the template to the group)
    triggerTemplate.openEditDialog(true)
}