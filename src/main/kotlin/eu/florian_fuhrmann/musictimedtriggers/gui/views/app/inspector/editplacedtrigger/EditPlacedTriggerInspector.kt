package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector.editplacedtrigger

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.configuration.ConfigurationBox
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.managers.TriggerSelectionManager
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector.ScrollableInspectorContentsContainer
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Link
import org.jetbrains.jewel.ui.component.Text

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditPlacedTriggerInspector(project: Project) {
    ScrollableInspectorContentsContainer("Edit Placed Trigger") {
        Box(Modifier.padding(horizontal = 10.dp).fillMaxSize()) {
            // get trigger as a local variable, so smart casting works
            val trigger = TriggerSelectionManager.singleSelectedTriggerState.value
            if(trigger == null) {
                Text(
                    text = "Select a single Placed Trigger to edit.",
                    modifier = Modifier.padding(horizontal = 10.dp).align(Alignment.TopCenter),
                    color = JewelTheme.globalColors.text.disabled
                )
            } else {
                Column {
                    // link to the trigger template
                    FlowRow {
                        Text("Instance of ")
                        Link(trigger.triggerTemplate.name(), overflow = TextOverflow.Ellipsis, onClick = {
                            println("Todo: Select template in browser and open template inspector")
                        })
                    }
                    Spacer(Modifier.height(8.dp))
                    // position settings
                    Row {
                        Text("Todo: Start Time, End Time and Duration")
                    }
                    // triggers configuration (with significant padding, so it is clearly separated from the other settings)
                    Spacer(Modifier.height(20.dp))
                    key(trigger) {
                        if(trigger.configuration != null) {
                            Row {
                                ConfigurationBox(trigger.configuration, trigger.getConfigurationContext())
                            }
                        }
                    }
                }
            }
        }
    }
}