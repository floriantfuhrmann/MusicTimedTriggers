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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import eu.florian_fuhrmann.musictimedtriggers.gui.alerts.BasicAlert
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.browser.BrowserGroup
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.SingleTab
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.triggers.TriggerType
import eu.florian_fuhrmann.musictimedtriggers.triggers.templates.AbstractTriggerTemplate
import org.jetbrains.jewel.foundation.modifier.onHover
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Outline
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.theme.defaultTabStyle
import sh.calvin.reorderable.ReorderableRow
import sh.calvin.reorderable.ReorderableScope

@Composable
fun BrowserTabsBar(project: Project) {
    val closedGroups: List<BrowserGroup> by remember {
        derivedStateOf {
            project.browserState.allGroups.value.filter {
                !project.browserState.openedGroups.value.contains(it)
            }
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        // Opened Groups Tabs
        OpenGroupsTabs(project, project.browserState.tabsScrollState, project.browserState.openedGroups.value)
        // Tools
        Row(Modifier.padding(top = 5.dp, bottom = 5.dp, end = 5.dp).height(26.dp)) {
            // More Options
            MoreOptionsDropdown(project, closedGroups)
            // Divider
            Divider(org.jetbrains.jewel.ui.Orientation.Vertical, Modifier.fillMaxHeight().padding(horizontal = 4.dp))
            // Remove Templates Button
            RemoveTemplatesButton()
            // Add Templates Dropdown
            AddTemplateButton()
            // Divider
            Divider(org.jetbrains.jewel.ui.Orientation.Vertical, Modifier.fillMaxHeight().padding(horizontal = 4.dp))
            // Collapse Browser Button
            ToggleBrowserButton()
        }
    }
}

@Composable
fun CollapsedBrowserBar() {
    Row(
        modifier = Modifier
            .background(JewelTheme.globalColors.borders.normal)
            .padding(top = 1.dp)
            .background(JewelTheme.globalColors.panelBackground)
            .padding(horizontal = 5.dp)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Spacer to align the buttons to the right
        Spacer(Modifier.weight(1f))
        // Expand Browser Button
        ToggleBrowserButton()
    }
}

@Composable
fun RowScope.OpenGroupsTabs(project: Project, tabsScrollState: ScrollState, openedGroups: List<BrowserGroup>) {
    var tabsHovered by remember { mutableStateOf(false) }
    //Column with tabs
    Column(Modifier.weight(1f).height(JewelTheme.defaultTabStyle.metrics.tabHeight)) {
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
                        project.browserState.moveGroup(fromIndex, toIndex)
                    }
                ) { index, item, _ ->
                    // Item content
                    key(item.uuid) {
                        BrowserTab(
                            scope = this,
                            browserGroup = item,
                            selected = item == project.browserState.selectedGroup.value,
                            hasTabsToTheLeft = item != openedGroups.firstOrNull() && openedGroups.size > 1,
                            hasTabsToTheRight = item != openedGroups.lastOrNull() && openedGroups.size > 1,
                            onClick = { project.browserState.selectGroup(item) },
                            onClose = { project.browserState.closeGroup(item) },
                            onCloseOthers = { project.browserState.closeMultipleGroups(openedGroups.filter { it != item }) },
                            onCloseAll = { project.browserState.closeMultipleGroups(openedGroups) },
                            onCloseLeft = { project.browserState.closeMultipleGroups(openedGroups.subList(0, index)) },
                            onCloseRight = { project.browserState.closeMultipleGroups(openedGroups.subList(index + 1, openedGroups.size)) },
                            onRename = { newName ->
                                // get the trigger template group
                                val triggerTemplateGroup = project.triggersManager.getTemplateGroup(item.uuid)
                                check(triggerTemplateGroup != null) { "Could not find template group for ${item.uuid}" }
                                // update the name
                                project.triggersManager.updateTriggerTemplateGroup(triggerTemplateGroup, newName)
                            },
                            onDelete = {
                                // get the trigger template group
                                val triggerTemplateGroup = project.triggersManager.getTemplateGroup(item.uuid)
                                check(triggerTemplateGroup != null) { "Could not find template group for ${item.uuid}" }
                                // require the group to be empty
                                if(triggerTemplateGroup.templates.isNotEmpty()) {
                                    BasicAlert(
                                        type = BasicAlert.Type.Error,
                                        title = "Group not empty",
                                        buttons = { OKButton() }
                                    ) {
                                        Text("Please remove all templates from the group before deleting it.")
                                    }.show()
                                } else {
                                    //delete the group
                                    project.triggersManager.deleteTriggerTemplateGroup(triggerTemplateGroup)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MoreOptionsDropdown(project: Project, closedGroups: List<BrowserGroup>) {
    // States
    var menuExpanded by remember { mutableStateOf(false) }
    var showCreatePopup by remember { mutableStateOf(false) }
    // Icon Button
    IconButton(onClick = { menuExpanded = true }, focusable = false) {
        Icon(AllIconsKeys.Actions.More, null)
    }
    // Popups
    if(menuExpanded) {
        // Menu with closed groups and create group option
        PopupMenu(
            onDismissRequest = {
                menuExpanded = false
                true
            }, content = {
                // Open Group
                closedGroups.forEach {
                    selectableItem(
                        selected = false,
                        onClick = { project.browserState.openGroup(it, scrollToBack = true) }
                    ) {
                        Text(it.name.value)
                    }
                }
                if(closedGroups.isNotEmpty()) {
                    separator()
                }
                // Create New Group
                selectableItem(
                    selected = false,
                    onClick = {
                        showCreatePopup = true
                    },
                    iconKey = AllIconsKeys.General.Add,
                ) {
                    Text("Create Group")
                }
            },
            horizontalAlignment = Alignment.Start
        )
    } else if (showCreatePopup) {
        // Create Group Popup
        CreateRenameGroupPopup(
            creating = true,
            onClose = { showCreatePopup = false },
            onDone = { newName ->
                // create group
                project.triggersManager.createNewTriggerTemplateGroup(newName)
            }
        )
    }
}

@Composable
fun AddTemplateButton() {
    var expanded by remember { mutableStateOf(false) }
    IconButton(
        onClick = { expanded = !expanded },
        focusable = false
    ) {
        Icon(AllIconsKeys.General.Add, null)
        Icon(AllIconsKeys.General.Dropdown, null)
    }
    if(expanded) {
        PopupContainer(
            onDismissRequest = {
                expanded = false
            },
            horizontalAlignment = Alignment.Start
        ) {
            Column(Modifier.width(150.dp)) {
                TriggerType.entries.forEach { triggerType ->
                    Row {
                        SelectableIconButton(
                            selected = false,
                            onClick = {
                                expanded = false
                                addNewTemplate(triggerType)
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

@Composable
fun RemoveTemplatesButton() {
    IconButton(
        onClick = { ProjectManager.currentProject?.browserState?.removeSelectedTemplates() },
        focusable = false
    ) {
        Icon(AllIconsKeys.General.Remove, null)
    }
}

@Composable
fun ToggleBrowserButton() {
    IconButton(
        onClick = { MainUiState.toggleBrowser() },
        modifier = Modifier.fillMaxHeight().trackActivation(),
        focusable = false
    ) {
        Icon(AllIconsKeys.General.PreviewHorizontally, null)
    }
}

@Composable
private fun BrowserTab(
    scope: ReorderableScope,
    browserGroup: BrowserGroup,
    selected: Boolean,
    hasTabsToTheLeft: Boolean,
    hasTabsToTheRight: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit,
    onCloseOthers: () -> Unit,
    onCloseAll: () -> Unit,
    onCloseLeft: () -> Unit,
    onCloseRight: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit
) {
    // Popup for renaming the group
    var showRenamePopup by remember { mutableStateOf(false) }
    if (showRenamePopup) {
        CreateRenameGroupPopup(
            creating = false,
            initialName = browserGroup.name.value,
            onClose = { showRenamePopup = false },
            onDone = onRename
        )
    }
    // Context Menu Area for the tab
    ContextMenuArea(
        items = {
            listOfNotNull(
                ContextMenuItem("Close") { onClose.invoke() },
                if(hasTabsToTheLeft || hasTabsToTheRight) ContextMenuItem("Close Other Tabs") { onCloseOthers.invoke() } else null,
                ContextMenuItem("Close All Tabs") { onCloseAll.invoke() },
                if(hasTabsToTheLeft) ContextMenuItem("Close Tabs to the Left") { onCloseLeft.invoke() } else null,
                if(hasTabsToTheRight) ContextMenuItem("Close Tabs to the Right") { onCloseRight.invoke() } else null,
                ContextMenuDivider,
                ContextMenuItem("Rename") { showRenamePopup = true },
                ContextMenuItem("Delete") { onDelete.invoke() }
            )
        }
    ) {
        // Tab
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
}

@Composable
fun CreateRenameGroupPopup(
    creating: Boolean,
    initialName: String = "",
    onClose: () -> Unit,
    onDone: (String) -> Unit
) {
    val textFieldState = rememberTextFieldState(initialName)
    PopupContainer(
        onDismissRequest = { onClose() },
        horizontalAlignment = if(creating) Alignment.CenterHorizontally else Alignment.Start,
        popupProperties = PopupProperties(focusable = true)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row {
                Text(if(creating) "Create Group" else "Rename Group", fontWeight = FontWeight.SemiBold)
            }
            Row(Modifier.padding(top = 12.dp)) {
                val focusRequester = remember { FocusRequester() }
                TextField(
                    modifier = Modifier.focusRequester(focusRequester),
                    state = textFieldState,
                    outline = if(textFieldState.text.isBlank()) Outline.Error else Outline.None,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    onKeyboardAction = {
                        // call onDone (if a valid name is given)
                        textFieldState.text.toString().let {
                            if(it.isBlank()) return@TextField
                            onDone(it.trim())
                        }
                        // close popup
                        onClose()
                    }
                )
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            }
        }
    }
}

fun addNewTemplate(triggerType: TriggerType) {
    // get project
    val project = ProjectManager.currentProject ?: return
    // get currently selected group
    val selectGroupUuid = project.browserState.selectedGroup.value?.uuid ?: return
    val triggerTemplateGroup = project.triggersManager.getTemplateGroup(selectGroupUuid) ?: return
    // create a new template and add it to the group
    val triggerTemplate = AbstractTriggerTemplate.create(triggerType, triggerTemplateGroup)
    project.triggersManager.addTriggerTemplate(triggerTemplate, selectNewTemplates = true)
}