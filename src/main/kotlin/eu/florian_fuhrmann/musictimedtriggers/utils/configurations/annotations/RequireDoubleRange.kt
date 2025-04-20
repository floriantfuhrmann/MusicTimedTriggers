package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations

/**
 * Annotation to require a configurable double value to be in a specific
 * range.
 */
@Target(AnnotationTarget.FIELD)
annotation class RequireDoubleRange(val min: Double = -Double.MAX_VALUE, val max: Double = Double.MAX_VALUE)