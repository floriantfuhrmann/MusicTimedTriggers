package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations

/**
 * Can be applied to Int or String. Requires the configurable int value or
 * string length to be in given range.
 */
@Target(AnnotationTarget.FIELD)
annotation class RequireIntRange(val min: Int = Int.MIN_VALUE, val max: Int = Int.MAX_VALUE)