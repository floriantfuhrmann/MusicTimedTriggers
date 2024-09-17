package eu.florian_fuhrmann.musictimedtriggers.triggers.utils.intensity

import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedTrigger

class Keyframes(
    val keyframesList: MutableList<Keyframe>
) {

    companion object {
        fun create() = Keyframes(
            keyframesList = mutableListOf(
                Keyframe(0.0, 0.0),
                Keyframe(1.0, 1.0),
            )
        )
    }

    /**
     * @param position the proportional progress on the trigger at which this
     *    keyframe is located (so 0 is at the beginning of the trigger and 1 is
     *    at the end)
     * @param value proportional value
     */
    data class Keyframe(var position: Double, var value: Double) {
        fun relativeSecondPosition(placedTrigger: AbstractPlacedTrigger) = relativeSecondPosition(position, placedTrigger)
        fun absoluteSecondPosition(placedTrigger: AbstractPlacedTrigger) = absoluteSecondPosition(position, placedTrigger)

        companion object {
            fun relativeSecondPosition(proportionInput: Double, triggerContext: AbstractPlacedTrigger) =
                proportionInput * triggerContext.duration

            fun absoluteSecondPosition(proportionInput: Double, triggerContext: AbstractPlacedTrigger) =
                triggerContext.startTime + proportionInput * triggerContext.duration

            fun fromRelativeSecondPositionToProportion(input: Double, triggerContext: AbstractPlacedTrigger) =
                input / triggerContext.duration

            fun fromAbsoluteSecondPositionToProportion(input: Double, triggerContext: AbstractPlacedTrigger) =
                (input - triggerContext.startTime) / triggerContext.duration
        }
    }

}
