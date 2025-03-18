package eu.florian_fuhrmann.musictimedtriggers.triggers

import eu.florian_fuhrmann.musictimedtriggers.utils.icons.MttIcons
import org.jetbrains.jewel.ui.icon.PathIconKey

enum class TriggerType(val displayName: String, iconResource: String, val isIntensity: Boolean = false) {

    TEST_ON_OFF("Test On/Off", "icons/triggers/OnOffTrigger_24x24_unset.png"),
    TEST_GREETING("Test Greeting", "icons/triggers/GreetingTrigger_24x24.png"),
    PRINT_INTENSITY("Test Intensity", "icons/triggers/PrintIntensityTrigger_480x480.png", true);

    val iconKey = PathIconKey(iconResource, MttIcons::class.java)

}