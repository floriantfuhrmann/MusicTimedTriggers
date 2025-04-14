package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.entries

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import eu.florian_fuhrmann.musictimedtriggers.gui.views.components.inputs.InvalidInputPopup
import eu.florian_fuhrmann.musictimedtriggers.utils.color.GenericColor
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.ConfigurationContext
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations.*
import org.jetbrains.jewel.foundation.Stroke
import org.jetbrains.jewel.foundation.modifier.border
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.PopupContainer
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Tooltip
import org.jetbrains.jewel.ui.focusOutline
import org.jetbrains.jewel.ui.theme.textFieldStyle
import org.jetbrains.jewel.ui.util.thenIf
import java.lang.reflect.Field

class ColorConfigurationEntry(
    configuration: Configuration,
    field: Field,
    configurable: Configurable,
    context: ConfigurationContext,
    customCheckers: List<RequireCustom>,
    visibleWhen: VisibleWhen?,
    private val showAlphaBar: Boolean,
) : AbstractConfigurationEntry<GenericColor>(
    configuration,
    field,
    configurable,
    context,
    customCheckers,
    visibleWhen
) {
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        // State
        var popupVisible by remember { mutableStateOf(false) }
        var fieldFocused by remember { mutableStateOf(false) }
        var checkerMessage: String? by remember { mutableStateOf(null) }
        var configurationColor by remember { mutableStateOf(GenericColor.fromAnyColor(field.get(configuration))) }
        // Ui
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Column {
                Tooltip(
                    tooltip = { Text(configurable.description) },
                    enabled = configurable.description.isNotEmpty()
                ) {
                    Text("${configurable.displayName}:")
                }
            }
            Column {
                val shape = RoundedCornerShape(JewelTheme.textFieldStyle.metrics.cornerSize)
                Box(Modifier
                    .height(28.dp).width(50.dp)
                    .background(when {
                        (checkerMessage != null && fieldFocused) -> JewelTheme.globalColors.outlines.focusedError
                        checkerMessage != null -> JewelTheme.globalColors.outlines.error
                        fieldFocused -> JewelTheme.globalColors.outlines.focused
                        else -> JewelTheme.textFieldStyle.colors.border
                    }, shape)
                    .padding(if(fieldFocused) JewelTheme.textFieldStyle.metrics.borderWidth else JewelTheme.textFieldStyle.metrics.borderWidth)
                    .background(configurationColor.toComposeColor(), shape)
                    .thenIf(fieldFocused) {
                        border(Stroke.Alignment.Outside, JewelTheme.globalMetrics.outlineWidth, JewelTheme.globalColors.outlines.let { if(checkerMessage != null) it.focusedError else it.focused }, shape)
                    }
                    .focusable()
                    .onFocusChanged { fieldFocused = it.isFocused }
                    .clickable {
                        popupVisible = true
                    }
                )
                // Color picker popup
                if(popupVisible) {
                    PopupContainer(
                        onDismissRequest = { popupVisible = false },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(Modifier.padding(10.dp).width(140.dp).height(120.dp)) {
                            ClassicColorPicker(
                                modifier = Modifier.fillMaxSize(),
                                color = configurationColor.toHsvColor(),
                                showAlphaBar = showAlphaBar,
                                onColorChanged = {
                                    //update configuration color
                                    configurationColor = GenericColor.fromHsvColor(it)
                                    //run custom checkers
                                    val checkResult = checkCustom(configurationColor)
                                    if(checkResult.valid) {
                                        //set checker message
                                        checkerMessage = null
                                        //set field and call change callback
                                        setColorField(field, configuration, configurationColor)
                                        handleValueChanged()
                                    } else {
                                        checkerMessage = checkResult.message
                                    }
                                }
                            )
                        }
                    }
                }
                // Error message
                checkerMessage?.let {
                    if(fieldFocused && !popupVisible) {
                        InvalidInputPopup(it)
                    }
                }
            }
        }
    }
}

fun setColorField(field: Field, configuration: Configuration, configurationColor: GenericColor) {
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
        GenericColor::class.java -> {
            field.set(configuration, configurationColor)
        }
    }
}
