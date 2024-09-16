package eu.florian_fuhrmann.musictimedtriggers.triggers.placed

import eu.florian_fuhrmann.musictimedtriggers.triggers.templates.TestOnOffTriggerTemplate

class PlacedTestOnOffTrigger(
    triggerTemplate: TestOnOffTriggerTemplate,
    startTime: Double,
    duration: Double
) : AbstractPlacedTrigger(triggerTemplate, startTime, duration) {

    private fun template() = triggerTemplate as TestOnOffTriggerTemplate

    override fun on(timePosition: Double) {
        println("Debug Trigger '${template().name()}' turned on at $timePosition!")
    }

    override fun off(timePosition: Double) {
        println("Debug Trigger '${template().name()}' turned off at $timePosition!")
    }

    companion object {
        fun create(template: TestOnOffTriggerTemplate, startTime: Double, duration: Double): AbstractPlacedTrigger {
            return PlacedTestOnOffTrigger(template, startTime, duration)
        }
    }

}