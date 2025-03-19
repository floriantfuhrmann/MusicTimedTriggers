package eu.florian_fuhrmann.musictimedtriggers.gui.views.titlebar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.editproject.EditProjectDialog
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.openproject.OpenProjectDialog
import eu.florian_fuhrmann.musictimedtriggers.gui.styles.dropdownLikeIconButtonStyle
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.IntUiThemes
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import eu.florian_fuhrmann.musictimedtriggers.project.ProjectManager
import eu.florian_fuhrmann.musictimedtriggers.utils.icons.MttIcons
import eu.florian_fuhrmann.musictimedtriggers.utils.color.mix
import org.jetbrains.jewel.foundation.modifier.trackActivation
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.window.DecoratedWindowScope
import org.jetbrains.jewel.window.TitleBar
import org.jetbrains.jewel.window.newFullscreenControls

var titleBarDropdownOpened: MutableState<Boolean> = mutableStateOf(false)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DecoratedWindowScope.TitleBarView() {
    TitleBar(
        modifier = Modifier.newFullscreenControls().trackActivation(),
        gradientStartColor = (ProjectManager.currentProject?.projectSettings?.projectColor?.toComposeColor()
            ?.mix(JewelTheme.globalColors.panelBackground, 0.55f) ?: Color.Unspecified)
    )
    {
        Row(Modifier.align(Alignment.Start).padding(5.dp).trackActivation()) {
            // Sidebar Toggle
            if (ProjectManager.currentProject != null) {
                IconButton(
                    style = dropdownLikeIconButtonStyle,
                    onClick = {
                        MainUiState.toggleSidebar()
                    },
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .trackActivation()
                ) {
                    Box(Modifier.padding(5.dp)) {
                        Icon(AllIconsKeys.Nodes.DataColumn, null)
                    }
                }
            }
            //Dropdown
            Dropdown(Modifier.height(30.dp).trackActivation(), menuContent = {
                //track if dropdown is opened
                passiveItem {
                    DisposableEffect(this) {
                        titleBarDropdownOpened.value = true
                        onDispose {
                            titleBarDropdownOpened.value = false
                        }
                    }
                }
                //Project Settings Item
                if(ProjectManager.currentProject != null) {
                    selectableItem(
                        selected = false,
                        onClick = {
                            DialogManager.openDialog(EditProjectDialog(create = false, project = ProjectManager.currentProject))
                        },
                        iconKey = MttIcons.settingLine
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.trackActivation()
                        ) {
                            Text("Project Settings")
                        }
                    }
                }
                //Open Project Item
                selectableItem(
                    selected = false,
                    onClick = {
                        DialogManager.openDialog(OpenProjectDialog())
                    },
                    iconKey = MttIcons.openFolderOutline
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.trackActivation()
                    ) {
                        Text("Open Project")
                    }
                }
                //Create Project Item
                selectableItem(
                    selected = false,
                    onClick = {
                        DialogManager.openDialog(EditProjectDialog(create = true))
                    },
                    iconKey = MttIcons.plusLine
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.trackActivation()
                    ) {
                        Text("Create Project")
                    }
                }
                //Advanced Options
                if(ProjectManager.currentProject != null) {
                    submenu(submenu = {
                        if (ProjectManager.currentProject != null) {
                            selectableItem(
                                selected = false,
                                onClick = {
                                    ProjectManager.currentProject?.scanForUnusedAudioFiles()
                                }
                            ) {
                                Text("Scan for unused Audio Files")
                            }
                        }
                    }) {
                        Text("Advanced")
                    }
                }
            }) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.trackActivation()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.trackActivation()
                    ) {
                        Text(ProjectManager.currentProject?.getProjectName() ?: "No Project")
                    }
                }
            }
        }

        Text(modifier = Modifier.scale(1f).trackActivation(), text = title)

        Row(Modifier.align(Alignment.End).trackActivation()) {
            Tooltip({
                when (MainUiState.theme) {
                    IntUiThemes.Light -> Text("Switch to dark theme")
                    IntUiThemes.Dark, IntUiThemes.System -> Text("Switch to light theme")
                }
            }) {
                IconButton({
                    MainUiState.theme = when (MainUiState.theme) {
                        IntUiThemes.Light -> IntUiThemes.Dark
                        IntUiThemes.Dark, IntUiThemes.System -> IntUiThemes.Light
                    }
                }, Modifier.size(40.dp).padding(5.dp).trackActivation()) {
                    Box(modifier = Modifier.padding(5.dp).trackActivation()) {
                        Icon(
                            key = when (MainUiState.theme.isDark()) {
                                true -> MttIcons.daySunny
                                else -> MttIcons.moon
                            },
                            contentDescription = null,
                            tint = MainUiState.theme.iconColor()
                        )
                    }
                }
            }
        }
    }
}
