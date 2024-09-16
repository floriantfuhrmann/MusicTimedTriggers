package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.entries

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.*
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.utils.ConfigurationColor
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip
import java.lang.reflect.Field

class ColorConfigurationEntry(
    configuration: Configuration,
    field: Field,
    configurable: Configurable,
    customCheckers: List<RequireCustom>,
    visibleWhen: VisibleWhen?,
    private val showAlphaBar: Boolean,
) : AbstractConfigurationEntry<ConfigurationColor>(
    configuration,
    field,
    configurable,
    customCheckers,
    visibleWhen
) {
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        //init checker message
        var checkerMessage: String? by remember { mutableStateOf(null) }
        //get current color
        var configurationColor by remember { mutableStateOf(ConfigurationColor.fromAnyColor(field.get(configuration))) }
        Row(
            modifier = Modifier.padding(top = 5.dp)
        ) {
            Tooltip(tooltip = {
                Text(configurable.description)
            }) {
                Text("${configurable.displayName}:")
            }
        }
        Row(
            modifier = Modifier.padding(top = 2.dp)
        ) {
            ClassicColorPicker(
                modifier = Modifier.height(100.dp),
                color = configurationColor.toHsvColor(),
                showAlphaBar = showAlphaBar,
                onColorChanged = {
                    //get new configuration color
                    val newConfigurationColor = ConfigurationColor.fromHsvColor(it)
                    //run custom checkers
                    val checkResult = checkCustom(newConfigurationColor)
                    if(checkResult.valid) {
                        //set checker message
                        checkerMessage = null
                        //set field and call change callback
                        configurationColor = ConfigurationColor.fromHsvColor(it)
                        setColorField(field, configuration, newConfigurationColor)
                        handleValueChanged()
                    } else {
                        checkerMessage = checkResult.message
                    }
                }
            )
        }
        if(checkerMessage != null) {
            Row {
                Text(color = Color.Red, text = checkerMessage!!)
            }
        }
    }
}

fun setColorField(field: Field, configuration: Configuration, configurationColor: ConfigurationColor) {
    when(field.type) {
        java.awt.Color::class.java -> {
            field.set(configuration, configurationColor.toAwtColor())
        }
        androidx.compose.ui.graphics.Color::class.java -> {
            field.set(configuration, configurationColor.toComposeColor())
        }
        HsvColor::class.java -> {
            field.set(configuration, configurationColor.toHsvColor())
        }
        ConfigurationColor::class.java -> {
            field.set(configuration, configurationColor)
        }
    }
}
