package eu.florian_fuhrmann.musictimedtriggers.triggers

enum class TriggerType(val displayName: String, val iconResource: String, val isIntensity: Boolean = false) {

    TEST_ON_OFF("Test On/Off", "icons/triggers/OnOffTrigger_24x24_unset.png"),
    TEST_GREETING("Test Greeting", "icons/triggers/GreetingTrigger_24x24.png"),
    PRINT_INTENSITY("Test Intensity", "icons/triggers/PrintIntensityTrigger_480x480.png", true)

}