package eu.florian_fuhrmann.musictimedtriggers.triggers.utils.intensity

import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedTrigger

class Keyframes(
    val keyframesList: MutableList<Keyframe>
) {
    fun findIndex(keyframe: Keyframe): Int {
        return keyframesList.indexOf(keyframe)
    }

    /**
     * Inserts a new Keyframe at [index] and moves all existing Keyframes with
     * index >= [index] back. The new Keyframe will be placed in the middle of
     * previous and next Keyframe. Thus, **it is not allowed to pass first or
     * last index to this method**.
     *
     * @param index index the new Keyframe should have
     */
    fun insertNewAtIndex(index: Int) {
        //make sure the index is not index of first or last keyframe
        require(index > 0) { "Can not insert at front" }
        require(index < keyframesList.size) { "Can not insert at back" }
        // Create new Keyframe between existing Keyframes
        val newKeyframe = Keyframe(
            (keyframesList[index - 1].position + keyframesList[index].position) / 2,
            (keyframesList[index - 1].value + keyframesList[index].value) / 2
        )
        // Insert new Keyframe
        keyframesList.add(index, newKeyframe)
    }

    private val minKeyframePositionDifferenceSeconds = 0.05 // 50ms

    /**
     * Checks whether a new Keyframe can be inserted at [index] while keeping
     * minimum distance to previous and next Keyframe.
     *
     * @param index index the new Keyframe should have
     * @param triggerDuration duration of the keyframes trigger in seconds
     */
    fun canInsertAt(index: Int, triggerDuration: Double): Boolean {
        // check index is not first or last
        if (index == 0 || index == keyframesList.size) {
            return false
        }
        // check distance to previous and next keyframe
        val minPositionDifference = minKeyframePositionDifferenceSeconds / triggerDuration
        val prevPosition = keyframesList[index - 1].position
        val nextPosition = keyframesList[index].position
        return nextPosition - prevPosition > minPositionDifference * 2
    }

    /**
     * Removes the Keyframe at [index].
     *
     * @param index index of the Keyframe to remove
     */
    fun removeAtIndex(index: Int) {
        //prevent removing first or last keyframe
        require(index > 0) { "Can not remove first keyframe" }
        require(index < keyframesList.size - 1) { "Can not remove last keyframe" }
        //remove keyframe
        keyframesList.removeAt(index)
    }

    /**
     * Checks whether a Keyframe can be removed at [index]. (Only allowed when
     * not first or last Keyframe)
     *
     * @param index index of the Keyframe to remove
     */
    fun canRemoveAt(index: Int): Boolean {
        return index > 0 && index < keyframesList.size - 1
    }

    /**
     * Calculates the intensity at a given proportional position by
     * interpolating between keyframes.
     */
    fun intensityAt(proportionalPosition: Double): Double {
        // require that the proportional position is in the range [0, 1]
        require(proportionalPosition in 0.0..1.0) { "Proportional position must be in range [0, 1]" }
        // search for keyframe using built-in binary search
        val foundIndex = keyframesList.binarySearch { it.position.compareTo(proportionalPosition) }
        // check whether keyframe was found at exact position
        if (foundIndex >= 0) {
            // return value of found keyframe
            return keyframesList[foundIndex].value
        } else {
            // calculate index of keyframes before and after the position
            val beforeIndex = -foundIndex - 2 // equal to: actual insertion index - 1
            val afterIndex = beforeIndex + 1
            // calculate relative position between keyframes
            val relativePositionBetweenKeyframes =
                (proportionalPosition - keyframesList[beforeIndex].position) / (keyframesList[afterIndex].position - keyframesList[beforeIndex].position)
            // interpolate value between keyframes
            return keyframesList[beforeIndex].value + relativePositionBetweenKeyframes * (keyframesList[afterIndex].value - keyframesList[beforeIndex].value)
        }
    }

    companion object {
        /** Dummy Keyframes, which should not be modified. */
        val DUMMY_KEYFRAMES = create()

        /** Creates a new Keyframes object with two default Keyframes at 0 and 1. */
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
