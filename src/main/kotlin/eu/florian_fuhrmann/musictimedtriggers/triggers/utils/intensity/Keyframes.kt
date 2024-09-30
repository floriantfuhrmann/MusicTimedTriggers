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
