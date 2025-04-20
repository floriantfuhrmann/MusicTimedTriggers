package eu.florian_fuhrmann.musictimedtriggers.triggers

import eu.florian_fuhrmann.musictimedtriggers.utils.icons.MttIcons
import org.jetbrains.jewel.ui.icon.PathIconKey

enum class TriggerType(val displayName: String, iconResource: String, val isIntensity: Boolean = false) {

    TEST_ON_OFF("Test On/Off", "icons/triggers/OnOff.svg"),
    TEST_GREETING("Test Greeting", "icons/triggers/Greeting.svg"),
    PRINT_INTENSITY("Test Intensity", "icons/triggers/Intensity.svg", true);

    val iconKey = PathIconKey(iconResource, MttIcons::class.java)

}