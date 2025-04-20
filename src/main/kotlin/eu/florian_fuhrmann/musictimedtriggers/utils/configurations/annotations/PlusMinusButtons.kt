package eu.florian_fuhrmann.musictimedtriggers.utils.configurations.annotations

/**
 * Annotation to enable plus and minus buttons for the input of a
 * configurable double value.
 *
 * @param step The step size for the buttons. (default: 1.0)
 */
@Target(AnnotationTarget.FIELD)
annotation class PlusMinusButtons(val step: Double = 1.0)