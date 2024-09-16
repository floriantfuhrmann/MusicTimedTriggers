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

    data class Keyframe(val position: Double, val value: Double) {
        fun relativeSecondPosition(placedTrigger: AbstractPlacedTrigger) = position * placedTrigger.duration
        fun absoluteSecondPosition(placedTrigger: AbstractPlacedTrigger) = placedTrigger.startTime + position * placedTrigger.duration
    }

}
