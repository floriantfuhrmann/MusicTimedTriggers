package eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector.edittemplate

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.configuration.ConfigurationBox
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.inspector.ScrollableInspectorContentsContainer
import eu.florian_fuhrmann.musictimedtriggers.project.Project
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.ChangeListenerContext
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.ConfigurationContext
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.Configurable
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import java.lang.reflect.Field

@Composable
fun EditTemplateInspector(project: Project) {
    // selected browser template
    val selectedBrowserTemplate by derivedStateOf(referentialEqualityPolicy()) {
        project.browserState.selectedTemplates.let { if (it.size == 1) it.first() else null }
    }
    ScrollableInspectorContentsContainer("Edit Trigger Template") {
        Box(Modifier.padding(horizontal = 10.dp).fillMaxSize()) {
            // get template as a local variable, so smart casting works
            val template = selectedBrowserTemplate
            // configuration box or fallback text
            if(template != null) {
                // configuration box for the template's configuration
                key(template.uuid) {
                    // configuration box
                    ConfigurationBox(
                        modifier = Modifier.fillMaxSize(),
                        configuration = template.getTriggerTemplate().configuration,
                        context = object : ConfigurationContext(), ChangeListenerContext {
                            override fun onChange(field: Field, configurable: Configurable) {
                                // update the template in the ui
                                project.browserState.updateTriggerTemplate(template.getTriggerTemplate())
                                // save the group to file (should be debounced in the future)
                                template.getTriggerTemplate().group.saveToFile(project.projectDirectory)
                            }
                        }
                    )
                }
            } else {
                Text(
                    text = "Select a single Trigger Template to edit.",
                    modifier = Modifier.padding(horizontal = 10.dp).align(Alignment.TopCenter),
                    color = JewelTheme.globalColors.text.disabled
                )
            }
        }
    }
}