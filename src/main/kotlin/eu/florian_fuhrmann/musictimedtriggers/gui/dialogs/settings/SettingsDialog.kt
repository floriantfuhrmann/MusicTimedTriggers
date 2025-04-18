package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.Dialog
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.settings.categories.SettingsCategory
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.settings.categories.AppearanceCategory
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.settings.categories.AudioFilesCategory
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import org.jetbrains.jewel.foundation.modifier.onHover
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.VerticalScrollbar
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.theme.colorPalette
import org.jetbrains.jewel.ui.theme.iconButtonStyle
import org.jetbrains.jewel.ui.theme.simpleListItemStyle

class SettingsSection(val name: String, val categories: List<SettingsCategory>)

val sections = listOf(
    SettingsSection("Project Settings", listOf(
        AppearanceCategory(),
        AudioFilesCategory()
    ))
)
var selectedSection: SettingsSection? by mutableStateOf(sections.first())
var selectedCategory: SettingsCategory? by mutableStateOf(sections.first().categories.first())

class SettingsDialog(val project: Project) : Dialog("Settings", 750.dp, 525.dp) {

    @Composable
    override fun Content() {
        Column(Modifier.fillMaxSize().background(JewelTheme.globalColors.panelBackground)) {
            Row(Modifier.weight(1f)) {
                // Categories Sidebar
                Column {
                    Box {
                        val scrollState = rememberScrollState()
                        Column(Modifier.width(150.dp).verticalScroll(scrollState).padding(vertical = 6.dp)) {
                            sections.forEach { section ->
                                // spacer above header if not first section
                                if (section != sections.first()) {
                                    Spacer(Modifier.height(20.dp))
                                }
                                // section header and categories
                                SectionHeader(section)
                                section.categories.forEach { category ->
                                    CategoryItem(section, category, selectedCategory == category)
                                }
                            }
                        }
                        VerticalScrollbar(scrollState, Modifier.fillMaxHeight().align(Alignment.CenterEnd))
                    }
                }
                // Main Content
                Divider(Orientation.Vertical, Modifier.fillMaxHeight())
                Column(Modifier.weight(1f)) {
                    // header row for the settings main content
                    selectedSection?.let { section ->
                        selectedCategory?.let { category ->
                            HeaderRow(section, category)
                        }
                    }
                    // settings content in a scrollable column
                    Row(Modifier.weight(1f)) {
                        Column {
                            Box {
                                val scrollState = rememberScrollState()
                                Column(Modifier.fillMaxWidth().verticalScroll(scrollState)) {
                                    Box(Modifier.padding(horizontal = 20.dp)) {
                                        selectedCategory?.Content(project)
                                    }
                                }
                                VerticalScrollbar(scrollState, Modifier.fillMaxHeight().align(Alignment.CenterEnd))
                            }
                        }
                    }
                }
            }
            // Buttons Row
            Divider(Orientation.Horizontal, Modifier.fillMaxWidth())
            Row(Modifier.padding(horizontal = 20.dp, vertical = 15.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Spacer(Modifier.weight(1f))
                DefaultButton(onClick = { DialogManager.closeDialog(this@SettingsDialog) }) {
                    Text("OK")
                }
            }
        }
    }

    @Composable
    fun SectionHeader(section: SettingsSection) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
            Text(text = section.name, fontWeight = FontWeight.SemiBold, color = JewelTheme.colorPalette.gray(7))
        }
    }

    @Composable
    fun CategoryItem(section: SettingsSection, category: SettingsCategory, selected: Boolean = false) {
        var hovered by remember { mutableStateOf(false) }
        Row(Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth()
            .background(
                color = when {
                    selected -> MainUiState.theme.secondarySelectedBackgroundColor
                    hovered -> JewelTheme.iconButtonStyle.colors.backgroundHovered
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(JewelTheme.simpleListItemStyle.metrics.selectionBackgroundCornerSize)
            )
            .clickable(indication = null, interactionSource = null) {
                selectedSection = section
                selectedCategory = category
            }.onHover { hovered = it }
        ) {
            Text(category.name, Modifier.padding(start = 16.dp).padding(4.dp))
        }
    }

    @Composable
    fun ColumnScope.HeaderRow(section: SettingsSection, category: SettingsCategory) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 8.dp).height(IntrinsicSize.Min)) {
            Column(Modifier.padding(vertical = 6.dp)) {
                Text(section.name, fontWeight = FontWeight.SemiBold)
            }
            Column(Modifier.fillMaxHeight().padding(horizontal = 6.dp), verticalArrangement = Arrangement.Center) {
                Icon(
                    key = AllIconsKeys.General.ChevronRight,
                    contentDescription = null,
                    tint = JewelTheme.colorPalette.gray(7)
                )
            }
            Column(Modifier.padding(vertical = 6.dp)) {
                Text(category.name, fontWeight = FontWeight.SemiBold)
            }
        }
    }

}