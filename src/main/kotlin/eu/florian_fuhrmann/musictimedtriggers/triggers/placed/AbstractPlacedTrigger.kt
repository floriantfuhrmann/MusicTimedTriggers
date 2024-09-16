package eu.florian_fuhrmann.musictimedtriggers.triggers.placed

import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.DialogManager
import eu.florian_fuhrmann.musictimedtriggers.gui.dialogs.configuration.ConfigurationDialog
import eu.florian_fuhrmann.musictimedtriggers.triggers.templates.AbstractTriggerTemplate
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.Configuration
import eu.florian_fuhrmann.musictimedtriggers.utils.configurations.ConfigurationContext

/**
 * The Base for all placed Triggers. Every object represents a Trigger that is actually placed in a Sequence (Song).
 */
abstract class AbstractPlacedTrigger(
    val triggerTemplate: AbstractTriggerTemplate,
    var startTime: Double,
    var duration: Double,
    val configuration: Configuration? = null
) {

    val endTime: Double
        get() = startTime + duration

    fun isActiveAt(timePosition: Double) = startTime <= timePosition && endTime > timePosition

    /**
     * Makes sure the time is in bounds for this trigger (not lower than start time and not higher than end time)
     */
    fun inBoundsTime(time: Double): Double = time.coerceAtLeast(startTime).coerceAtMost(endTime)

    /**
     * Name for this placed Trigger (displayed on the placed trigger in the Timeline)
     * By Default this is the name of the Trigger Template
     */
    open fun name() = triggerTemplate.name()

    /**
     * Will be called when the trigger should turn on (either because a placed version turns on or manually)
     *
     * @param timePosition The current time position of the sequence this trigger is played in. This value it expected
     * to be equal to startTime or slightly higher (but other values should also be tolerated). Use [inBoundsTime] to
     * get a time which is ensured to be in bounds.
     */
    open fun on(timePosition: Double) {}

    /**
     * Will be called when the trigger should turn off (either because a placed version turns off or manually)
     *
     * @param timePosition The current time position of the sequence this trigger is played in. This value it expected
     * to be slightly higher than endTime (but other values should also be tolerated). Use [inBoundsTime] to get a time
     * which is ensured to be in bounds.
     */
    open fun off(timePosition: Double) {}

    /**
     * Called when the trigger should update (so is already on, but the time position changes)
     *
     * @param timePosition The current time position of the sequence this trigger is played in. This value is expected
     * to be between start and end time. (Normally it should not be called with a time position equal to start time,
     * because then on() is expected to be called instead. It should also be very uncommon to be called with a time
     * position equal to endTime, but both cases should also be tolerated.) Use [inBoundsTime] to get a time which is
     * ensured to be in bounds.
     */
    open fun update(timePosition: Double) {}

    /**
     * Called when the User wants to edit this Placed Trigger (normally through the Timeline)
     * By Default a ConfigurationDialog will be opened with the configuration
     */
    fun openEditDialog() {
        if(configuration != null) {
            DialogManager.openDialog(
                ConfigurationDialog(
                    configuration = configuration,
                    context = PlacedTriggerConfigurationContext(this),
                    heading = "Configuring Placed Trigger ${name()}",
                    showCancelButton = false
                )
            )
        }
    }

    class PlacedTriggerConfigurationContext(val placedTrigger: AbstractPlacedTrigger) : ConfigurationContext()

    companion object {
        const val DEFAULT_TRIGGER_DURATION = 1.0
        const val MINIMUM_TRIGGER_DURATION = 0.1
    }

}