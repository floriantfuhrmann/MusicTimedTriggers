package eu.florian_fuhrmann.musictimedtriggers.triggers.sequence

import androidx.compose.ui.util.fastAny
import com.google.gson.JsonObject
import eu.florian_fuhrmann.musictimedtriggers.gui.views.app.editor.timeline.redrawTimeline
import eu.florian_fuhrmann.musictimedtriggers.triggers.placed.AbstractPlacedTrigger

class TriggerSequenceLine(
    val sequence: TriggerSequence,
    var name: String,
    private val triggers: ArrayList<AbstractPlacedTrigger>
) {

    fun updateName(newName: String) {
        //change name
        name = newName
        //redraw timeline
        redrawTimeline()
        //save project
        sequence.project.save()
    }

    fun getTriggersCount() = triggers.size

    fun getTriggerByIndex(index: Int) = triggers.getOrNull(index)

    /**
     * Returns the placed trigger at time [timePosition] or the first trigger starting after [timePosition]
     *
     * @return Placed Trigger at [timePosition] or the first trigger starting after [timePosition]. If no trigger as at
     * [timePosition] and no trigger starts after, than null is returned.
     */
    fun getTriggerAtOrAfter(timePosition: Double): AbstractPlacedTrigger? {
        //get index of trigger at or inverted insertion point
        val index = indexOfTriggerAt(timePosition)
        //if index is not-negative then there is a trigger at time position
        if(index >= 0) {
            //if so return it
            return triggers[index]
        } else {
            //calculate actual insertion point
            val actualInsertionPoint = -(index + 1)
            //the index of the actual insertion point is the index of the trigger which would be moved to the right by
            // adding at the insertion point, so the first trigger after the time position
            //so return that trigger
            return triggers.getOrNull(actualInsertionPoint)
        }
    }

    /**
     * @see getTriggerAtOrAfter
     * @return index of the trigger at [timePosition] or index of the first trigger after [timePosition] or
     * size of triggers (index a new element at the end of the list would have, so an out-of-bounds index) if no trigger
     * ist at or comes after [timePosition]
     */
    fun getIndexOfTriggerAtOrIndexOfTriggerAfter(timePosition: Double): Int {
        val index = indexOfTriggerAt(timePosition)
        return if(index < 0) {
            -(index + 1)
        } else {
            index
        }
    }

    /**
     * Finds the trigger closest to [timePosition]
     * @return the trigger at [timePosition] or the trigger which is closest to [timePosition]
     */
    fun getTriggerClosestTo(timePosition: Double): AbstractPlacedTrigger? {
        val indexOfAtOrAfter = getIndexOfTriggerAtOrIndexOfTriggerAfter(timePosition)
        val triggerAtOrAfter = triggers.getOrNull(indexOfAtOrAfter)
        val triggerBefore = triggers.getOrNull(indexOfAtOrAfter - 1)
        if(triggerAtOrAfter != null) {
            return if(triggerAtOrAfter.isActiveAt(timePosition)) {
                //there just is a trigger active at timePosition
                triggerAtOrAfter
            } else {
                //this means triggerAtOrAfter ist after
                // -> check if we are closer to start time of trigger after or end time of trigger before
                if(triggerBefore == null || triggerAtOrAfter.startTime - timePosition < timePosition - triggerBefore.endTime) {
                    triggerAtOrAfter
                } else {
                    //otherwise time has to be closer to trigger before (or if all are null there is no trigger on this line)
                    triggerBefore
                }
            }
        } else {
            //otherwise time has to be closest to trigger before (because there just is no trigger at or after)
            return triggerBefore // or if trigger before is null than there just is no trigger on this line
        }
    }

    /**
     * Calculates the length of the time period from [timePosition] until the start of the next trigger or sequence end
     * (unless [allowPastSequenceEnd] is true)
     *
     * @return length of the time period from [timePosition] until the start of the next trigger or end of sequence (if
     * [allowPastSequenceEnd] is false) or 0 if there is already a trigger at [timePosition]. If [allowPastSequenceEnd]
     * is true and there is no trigger at or after [timePosition] then [Double.POSITIVE_INFINITY].
     */
    fun getFreeDurationFrom(
        timePosition: Double,
        allowPastSequenceEnd: Boolean = false
    ): Double {
        val index = indexOfTriggerAt(timePosition)
        if(index < 0) {
            //no trigger at time --> get next trigger (see getTriggerAtOrAfter)
            val nextTrigger = triggers.getOrNull(-(index + 1))
            //find time limit
            val timeLimit = nextTrigger?.startTime ?: if (allowPastSequenceEnd) {
                Double.POSITIVE_INFINITY
            } else {
                sequence.duration
            }
            //return difference of time position and time limit
            return timeLimit - timePosition
        } else {
            return 0.0
        }
    }

    /**
     * Calculates length of duration from sequence start or the trigger before [timePosition] until [timePosition]
     *
     * @return length of duration from trigger with greatest endTime, which is still smaller than [timePosition] or
     * sequence start if there is no such trigger until [timePosition] or 0 if there is already a trigger at
     * [timePosition]. If [allowTriggerStartAtTimePosition] is true, than a trigger at [timePosition], which starts
     * exactly at [timePosition] will be ignored.
     */
    fun getFreeDurationUntil(timePosition: Double, allowTriggerStartAtTimePosition: Boolean = false): Double {
        val index = indexOfTriggerAt(timePosition)
        if(index < 0) {
            //no trigger at time --> get trigger before time position (by calculating insertion index -> that will be
            // the index of trigger after time position -> so we subtract 1 to get trigger before time position)
            // (    some math: (-(index + 1)) - 1 = -index - 2    )
            val previousTrigger = triggers.getOrNull(-index - 2)
            //calculate time limit (limit is before time position) (if there is no trigger before timePosition than 0
            // will be used as limit because triggers can't start before sequence starts)
            val timeLimit = previousTrigger?.endTime ?: 0.0
            //return duration of time period from previous end until time position
            return timePosition - timeLimit
        } else {
            //there is already a trigger at time position
            if(allowTriggerStartAtTimePosition) {
                //check if the trigger at time position starts exactly at time position
                val triggerAt = triggers[index]
                if(triggerAt.startTime == timePosition) {
                    //so trigger start at time position is allowed, and it's the case, than calculate free time like
                    // there was no trigger at time position (basically triggerAt can be ignored)
                    //get previous trigger and calculate time limit
                    val previousTrigger = triggers.getOrNull(index - 1)
                    val timeLimit = previousTrigger?.endTime ?: 0.0
                    //return duration from time limit to time position
                    return timePosition - timeLimit
                } else {
                    //there is already a trigger at time position at it does not start at time position so return 0
                    return 0.0
                }
            } else {
                //if no trigger may start at time position than for sure there is no free duration
                return 0.0
            }
        }
    }

    /**
     * Finds all Triggers which are active at one point in the time period (will also return triggers which are not
     * completely in the time period)
     *
     * @param excludeToTime if true, then a trigger starting exactly on [toTime] will not be considered to be in period
     *
     * @return all triggers active at one point in time period from [fromTime] to [toTime]
     */
    fun getTriggersInPeriod(fromTime: Double, toTime: Double, excludeToTime: Boolean = false): List<AbstractPlacedTrigger> {
        //get index of the first trigger which (could be in time period)
        val firstIndex = getIndexOfTriggerAtOrIndexOfTriggerAfter(fromTime)
        return triggers
            .drop(firstIndex) //drop all triggers before that
            .takeWhile {
                if(excludeToTime) {
                    it.startTime < toTime
                } else {
                    it.startTime <= toTime //take all trigger which at least start before toTime
                }
            }
    }

    /**
     * Checks if there are no triggers in time period from [fromTime] to [toTime], which should not be ignored (so are
     * not in [ignoreSet])
     *
     * @return weither no time of time period is occupied by any trigger
     */
    fun isPeriodFree(fromTime: Double, toTime: Double, ignoreSet: Set<AbstractPlacedTrigger>): Boolean {
        return !getTriggersInPeriod(fromTime, toTime, true).fastAny { !ignoreSet.contains(it) }
    }

    fun addTrigger(trigger: AbstractPlacedTrigger) {
        //get inverted insertion point
        val invertedInsertionPoint = indexOfTriggerAt(trigger.startTime)
        require(invertedInsertionPoint < 0) { "There is already a trigger at time position ${trigger.startTime}" }
        //calculate actual insertion point
        val actualInsertionPoint = -(invertedInsertionPoint + 1)
        //get trigger previously at that index (the trigger that will be the following trigger after trigger)
        val followingTrigger = triggers.getOrNull(actualInsertionPoint)
        //check if trigger fits before following trigger
        require(followingTrigger == null || trigger.endTime <= followingTrigger.startTime) {
            "The new trigger has to end before or at start time of the next trigger"
        }
        //insert the new trigger
        triggers.add(actualInsertionPoint, trigger)
    }

    fun removeTrigger(trigger: AbstractPlacedTrigger) {
        triggers.remove(trigger)
        if(activeTrigger == trigger) {
            activeTrigger = null
        }
    }

    private var activeTrigger: AbstractPlacedTrigger? = null

    fun tick(timePosition: Double) {
        //get trigger which should now be active
        val newActiveTrigger = getTriggerAt(timePosition)
        //check if already active
        if(newActiveTrigger == activeTrigger) {
            //if so the activeTrigger does not have to be changed only updated (if it exists)
            activeTrigger?.update(timePosition)
        } else {
            //if not turn off the old trigger (if ot exists)
            activeTrigger?.off(timePosition)
            //set variable
            activeTrigger = newActiveTrigger
            //turn on the new trigger
            activeTrigger?.on(timePosition)
        }
    }

    fun getTriggerAt(timePosition: Double): AbstractPlacedTrigger? {
        val index = indexOfTriggerAt(timePosition)
        return if(index >= 0) {
            triggers[index]
        } else {
            null
        }
    }

    private var lastActiveTriggerIndex: Int = -1
    private var lastTimePosition: Double = -1.0
    /**
     * Finds the Trigger, which should be active at time [timePosition]. It tries to do this efficiently through the
     * following assumptions.
     * 1. The last active Trigger is probably still the active trigger (so this is checked first)
     * 2. The trigger after the last active trigger is also very likely to be the new active trigger
     * 3. if [timePosition] is between [AbstractPlacedTrigger.endTime] of the last active trigger and the
     * [AbstractPlacedTrigger.startTime] of the next trigger than there can't be a trigger active at [timePosition]
     * 4. As a fallback the trigger is searched with [getTriggerAt]
     * These assumptions only work when [fastFindActiveTrigger] is only used to find the currently active trigger in a
     * sequential playback. Use [getTriggerAt] for arbitrary searches instead.
     */
    private fun fastFindActiveTrigger(timePosition: Double): AbstractPlacedTrigger? {
        //ensure the new timePosition is equal to or greater than the last time postion
        if(timePosition < lastTimePosition) {
            //it appears we are going backwards in time -> assumptions don't work
            //-> reset lastActiveTriggerIndex so fallback is used
            lastActiveTriggerIndex = -1
        }
        //update lastTimePosition
        lastTimePosition = timePosition
        //make sure we have a valid last active trigger index
        if(lastActiveTriggerIndex < 0) {
            //if not we have to fallback
            lastActiveTriggerIndex = indexOfTriggerAt(timePosition) //set new last active trigger index
            //return the trigger by index
            return if(lastActiveTriggerIndex < 0) { null } else { triggers[lastActiveTriggerIndex] }
        }
        //get lastActiveTrigger
        val lastActiveTrigger: AbstractPlacedTrigger = triggers[lastActiveTriggerIndex]
        //check if it is still active
        if(lastActiveTrigger.isActiveAt(timePosition)) {
            //if so we don't need to update the lastActiveTriggerIndex and can just return lastActiveTrigger
            return lastActiveTrigger
        }
        //get the next active trigger
        val nextActiveTrigger: AbstractPlacedTrigger? = triggers.getOrNull(lastActiveTriggerIndex+1)
        //check if we have a next active trigger
        if(nextActiveTrigger != null) {
            //check if the nextActiveTrigger is active
            if(nextActiveTrigger.isActiveAt(timePosition)) {
                //if so update the lastActiveTriggerIndex
                lastActiveTriggerIndex += 1
                //and return the next trigger
                return nextActiveTrigger
            } else {
                //check if timePosition is between last and next trigger
                if(lastActiveTrigger.endTime <= timePosition && nextActiveTrigger.startTime > timePosition) {
                    //if so there is currently no trigger active
                    return null
                } else {
                    //otherwise timePosition has to have skipped the next trigger (this should be uncommon)
                    //fallback in that case
                    lastActiveTriggerIndex = indexOfTriggerAt(timePosition) //set new last active trigger index
                    //return the trigger by index
                    return if(lastActiveTriggerIndex < 0) { null } else { triggers[lastActiveTriggerIndex] }
                }
            }
        } else {
            // if not the lastActiveTrigger has to have been the last trigger in this sequence line. since we would have
            // already returned it if it were to be active timePosition has to be after the last trigger in this
            // sequence line, so there can't be an active trigger at timePosition
            return null
        }
    }

    /**
     * Uses [List.binarySearch] to search for the index of the Trigger at time [timePosition] or the negative
     * insertion index.
     *
     * A Trigger is defined to be at the [timePosition] when the Trigger's [AbstractPlacedTrigger.startTime] is lower or
     * equal to [timePosition] and the Trigger's [AbstractPlacedTrigger.endTime] is greater than [timePosition]. So if
     * Trigger A ends at time 3.0s and Trigger B starts at time 3.0s than at time 3.0s Trigger B will be active. Further
     * if Trigger B has a duration of 2s, so ends at time position 5.0s, it will be active at 4.99s, but will no longer
     * be active at 5.0s
     */
    private fun indexOfTriggerAt(timePosition: Double): Int {
        return triggers.binarySearch {
            if (it.startTime <= timePosition && it.endTime > timePosition) {
                return@binarySearch 0
            } else if (it.startTime < timePosition) {
                return@binarySearch -1
            } else {
                return@binarySearch 1
            }
        }
    }

    companion object {
        fun createTriggerSequenceLine(sequence: TriggerSequence, name: String): TriggerSequenceLine {
            return TriggerSequenceLine(sequence, name, ArrayList())
        }

        fun fromJson(sequence: TriggerSequence, json: JsonObject): TriggerSequenceLine {
            return TriggerSequenceLine(
                sequence,
                json.get("name").asString,
                ArrayList(json.get("triggers").asJsonArray.map {
                    sequence.project.triggersManager.getPlacedTriggerFromJson(it.asJsonObject)
                })
            )
        }
    }

}