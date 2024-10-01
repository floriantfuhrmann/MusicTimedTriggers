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

    /**
     * Returns the intensity at the given time position correcting out of
     * bounds times.
     *
     * @param timePosition time position in seconds
     * @return intensity at the given time position
     */
    fun intensity(timePosition: Double): Double {
        // calculate proportional position
        val proportionalPosition = (inBoundsTime(timePosition) - startTime) / duration
        // return intensity at proportional position
        return keyframes().intensityAt(proportionalPosition)
    }

}