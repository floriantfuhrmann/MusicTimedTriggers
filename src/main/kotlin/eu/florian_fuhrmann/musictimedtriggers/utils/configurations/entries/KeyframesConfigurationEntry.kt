package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.entries

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.florian_fuhrmann.musictimedtriggers.gui.uistate.MainUiState
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.Collapsible
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.DoubleField
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.SimpleIconButton
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedTrigger
import eu.florian_fuhrmann.musictimedtriggers.triggers.utils.intensity.Keyframes
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.ConfigurationContext
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.*
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Dropdown
import org.jetbrains.jewel.ui.component.Text
import java.lang.reflect.Field

class KeyframesConfigurationEntry(
    configuration: Configuration,
    field: Field,
    configurable: Configurable,
    visibleWhen: VisibleWhen?,
    private val context: ConfigurationContext
) : AbstractConfigurationEntry<String>(
    configuration,
    field,
    configurable,
    visibleWhen = visibleWhen
) {
    @Composable
    override fun Content() {
        if(context !is AbstractPlacedTrigger.PlacedTriggerConfigurationContext) {
            Row {
                Text("Configuration Context has to be PlacedTriggerConfigurationContext to configure ${configurable.displayName}", color = MainUiState.theme.errorTextColor())
            }
            return
        } else {
            Spacer(modifier = Modifier.height(10.dp))
            val extended = remember { mutableStateOf(true) }
            Collapsible(
                extended = extended,
                header = {
                    // Heading
                    Text(configurable.displayName)
                },
                contentModifier = Modifier.padding(0.dp)
            ) {
                Column {
                    // Header Row for Keyframe List
                    var positionDisplayFormat by mutableStateOf(PositionDisplayFormat.Proportion)
                    Row(
                        modifier = Modifier.padding(start = 5.dp, top = 5.dp, end = 5.dp, bottom = 0.dp),
                    ) {
                        // Position Display Format Selector
                        Column(
                            modifier = Modifier.fillMaxWidth().height(25.dp).weight(1f)
                        ) {
                            Dropdown(
                                modifier = Modifier.fillMaxHeight().fillMaxWidth(),
                                menuContent = {
                                    PositionDisplayFormat.entries.forEach {
                                        selectableItem(selected = positionDisplayFormat == it, onClick = { positionDisplayFormat = it }) {
                                            Text(it.displayName)
                                        }
                                    }
                                }) {
                                Text(positionDisplayFormat.displayName)
                            }
                        }
                        Spacer(Modifier.width(3.dp))
                        // Value Heading
                        Column(
                            modifier = Modifier.fillMaxWidth().height(25.dp).weight(1f)
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            Row {
                                Text(
                                    modifier = Modifier.padding(start = 5.dp),
                                    text = "Value"
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Spacer(Modifier.width(3.dp))
                        // Empty column as spacing
                        Column(
                            modifier = Modifier.width(24.dp).height(25.dp)
                        ) {}
                    }
                    // Keyframe List
                    val keyframes = field.get(configuration) as Keyframes
                    keyframes.keyframesList.forEach {
                        // Separator Line
                        Row(
                            modifier = Modifier.padding(top = 5.dp).fillMaxWidth().height(1.dp)
                                .background(JewelTheme.globalColors.borders.normal)
                        ) {}
                        // Keyframe Item
                        Row(
                            modifier = Modifier.padding(start = 5.dp, top = 5.dp, end = 5.dp)
                        ) {
                            // Position
                            Column(
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            ) {
                                DoubleField(it.position)
                            }
                            Spacer(Modifier.width(3.dp))
                            // Value
                            Column(
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            ) {
                                DoubleField(it.value)
                            }
                            Spacer(Modifier.width(3.dp))
                            // Actions Button
                            Column(
                                modifier = Modifier.height(36.dp).width(24.dp)
                            ) {
                                Spacer(modifier = Modifier.weight(1f))
                                Row {
                                    SimpleIconButton(
                                        forceHoverHandCursor = true,
                                        iconName = "more-options-icon",
                                        onClick = {
                                            //Todo: Dropdown with Actions: Insert Above, Insert Bellow, Remove
                                            println("TODO")
                                        },
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }
        }
    }
}

enum class PositionDisplayFormat(val displayName: String) {
    Proportion("Proportion"),
    Relative("Relative Seconds"),
    Absolute("Absolute Seconds")
}