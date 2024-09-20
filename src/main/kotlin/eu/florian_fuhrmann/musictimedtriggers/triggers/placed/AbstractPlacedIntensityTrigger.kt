package eu.florian_fuhrmann.musictimedtriggers.triggers.placed

import eu.florian_fuhrmann.musictimedtriggers.triggers.templates.AbstractTriggerTemplate
import eu.florian_fuhrmann.musictimedtriggers.triggers.utils.intensity.Keyframes
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration

abstract class AbstractPlacedIntensityTrigger(
    triggerTemplate: AbstractTriggerTemplate,
    startTime: Double,
    duration: Double,
    configuration: Configuration? = null
) : AbstractPlacedTrigger(triggerTemplate, startTime, duration, configuration) {

    abstract fun keyframes(): Keyframes

}