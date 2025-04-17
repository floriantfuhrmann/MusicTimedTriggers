package eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.settings.categories

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.Theme
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.ColorField
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.ColorFieldState
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.jewel.ui.component.ListComboBox
import org.jetbrains.jewel.ui.component.ListItemState
import org.jetbrains.jewel.ui.component.SimpleListItem
import org.jetbrains.jewel.ui.component.Text

class AppearanceCategory : SettingsCategory("Appearance") {

    @Composable
    override fun Content(project: Project) {
        // States
        var anyChanges by remember { mutableStateOf(false) }
        val colorFieldState = remember { ColorFieldState(project.projectSettings.projectColor) }
        // Export state changes to the project settings
        LaunchedEffect(Unit) {
            snapshotFlow { colorFieldState.value }.collectLatest {
                if(project.projectSettings.projectColor == it) return@collectLatest
                project.projectSettings.projectColor = it
                anyChanges = true
            }
        }
        // DisposableEffect to save project settings when the dialog is closed
        DisposableEffect(Unit) {
            onDispose {
                // save project settings if any changes were made
                if(anyChanges) {
                    project.projectSettings.save(project)
                }
            }
        }
        // Ui
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Labels
            Column(Modifier.fillMaxHeight()) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Text("Project Color:")
                }
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Text("Theme:")
                }
            }
            // Inputs
            Column(Modifier.padding(start = 6.dp)) {
                Row(Modifier.padding(vertical = 3.dp)) {
                    ColorField(colorFieldState)
                }
                Row(Modifier.padding(vertical = 3.dp)) {
                    ListComboBox(
                        items = Theme.entries.map { it.name },
                        isEditable = false,
                        onSelectedItemChange = { itemName ->
                            // get the selected theme by name
                            val selected = Theme.entries.find { it.name == itemName }
                            check(selected != null) { "Could not find selected theme: $itemName." }
                            // set the selected theme in the project settings and mark changes
                            project.projectSettings.selectedTheme = selected
                            anyChanges = true
                        },
                        listItemContent = { item, isSelected, _, isHovered, _ ->
                            SimpleListItem(
                                text = item,
                                state = ListItemState(isSelected = isSelected, isHovered = isHovered, previewSelection = isHovered)
                            )
                        }
                    )
                }
            }
        }
    }

}