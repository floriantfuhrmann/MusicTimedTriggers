package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.browser

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.DropdownMenu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.edittemplategroup.EditTemplateGroupDialog
import eu.florian_fuhrmann.musictimedtriggers.gui.styles.outlinedButtonStyleWithNoPadding
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.browser.BrowserGroup
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.SimpleIcon
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.SingleTab
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.TriggerType
import eu.florian_fuhrmann.musictimedtriggers.triggers.templates.AbstractTriggerTemplate
import eu.florian_fuhrmann.musictimedtriggers.utils.IconsDummy
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
    val scrollState = rememberScrollState()

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f).height(JewelTheme.defaultTabStyle.metrics.tabHeight)) {
            if(scrollState.canScrollForward || scrollState.canScrollBackward) {
                TabStripHorizontalScrollbar(
                    modifier = Modifier.fillMaxWidth(),
                    adapter = rememberScrollbarAdapter(scrollState)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                ReorderableRow(
                    list = openedGroups,
                    onSettle = { fromIndex, toIndex ->
                        ProjectManager.currentProject?.browserState?.moveGroup(fromIndex, toIndex)
                    }
                ) { _, item, _ ->
                    // Item content
                    scrollState.viewportSize
                    key(item.uuid) {
                        DragHandle(
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
                        iconResource = when(MainUiState.theme.isDark()) {
                            true -> "icons/pencil-outline-icon_dark.svg"
                            else -> "icons/pencil-outline-icon.svg"
                        }
                    ) {
                        Text("Edit Group")
                    }
                    selectableItem(
                        selected = false,
                        onClick = {
                            DialogManager.openDialog(EditTemplateGroupDialog(true, null))
                        },
                        iconResource = when(MainUiState.theme.isDark()) {
                            true -> "icons/plus-line-icon_dark.svg"
                            else -> "icons/plus-line-icon.svg"
                        }
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
                    SimpleIcon("plus-line-icon")
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                },
                modifier = Modifier
                    .background(color = JewelTheme.globalColors.paneBackground)
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
                                            triggerType.iconResource,
                                            null,
                                            IconsDummy::class.java,
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
private fun DragHandle(
    scope: ReorderableScope,
    browserGroup: BrowserGroup,
    selected: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    SingleTab(
        modifier = with(scope) { Modifier.draggableHandle() },
        editorStyle = true,
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